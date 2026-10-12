package io.github.jqssun.airplay

/** Pure event model: TCP probes alone never imply pairing or successful playback. */
enum class SalonStage { PREPARING, NO_NETWORK, READY, CONNECTING, PIN, VIDEO, AUDIO, START_ERROR, VIDEO_ERROR, PERMISSION }
data class SalonState(val stage: SalonStage = SalonStage.PREPARING, val tvName: String = "Android TV", val pin: String? = null, val detail: String = "")

class ReceiverStateMachine {
    @Volatile var generation = 0L; private set
    var state = SalonState(); private set
    private var listener = false
    private val registrations = mutableSetOf<String>()
    fun begin(name: String): Long {
        generation++; listener = false; registrations.clear()
        state = SalonState(tvName = name)
        return generation
    }
    fun stop(noNetwork: Boolean = false) {
        generation++; listener = false; registrations.clear()
        state = state.copy(stage = if (noNetwork) SalonStage.NO_NETWORK else SalonStage.PREPARING, pin = null, detail = "")
    }
    fun listening(token: Long) { if (token == generation) { listener = true; ready() } }
    fun registered(token: Long, kind: String, name: String) {
        if (token != generation) return
        registrations.add(kind)
        if (kind == "airplay") state = state.copy(tvName = name)
        ready()
    }
    private fun ready() {
        if (listener && registrations.containsAll(listOf("airplay", "raop")) && state.stage == SalonStage.PREPARING)
            state = state.copy(stage = SalonStage.READY)
    }
    fun event(token: Long, stage: SalonStage, pin: String? = null, detail: String = "") {
        if (token != generation || !listener) return
        state = state.copy(stage = stage, pin = pin, detail = detail)
    }
    fun failed(token: Long, detail: String, permission: Boolean = false) {
        if (token != generation) return
        state = state.copy(stage = if (permission) SalonStage.PERMISSION else SalonStage.START_ERROR, pin = null, detail = detail)
    }
    fun timedOut(token: Long): Boolean {
        if (token != generation || !listener || state.stage !in listOf(SalonStage.PIN, SalonStage.CONNECTING)) return false
        state = state.copy(stage = SalonStage.VIDEO_ERROR, pin = null, detail = "Connection timed out")
        return true
    }
    fun ended(token: Long) {
        if (token != generation) return
        state = state.copy(stage = SalonStage.PREPARING, pin = null, detail = "")
        ready()
    }
}

object SalonSettings {
    // DNS-SD service labels have a 63-byte UTF-8 limit; RAOP adds a 13-byte prefix.
    fun validName(value: String): Boolean = value.trim().isNotEmpty() &&
        value.trim().toByteArray(Charsets.UTF_8).size <= 50 &&
        value.none { it.isISOControl() || it == '\u0000' } &&
        value.withIndex().all { (i, c) ->
            !c.isSurrogate() || (c.isHighSurrogate() && i + 1 < value.length && value[i + 1].isLowSurrogate()) ||
                (c.isLowSurrogate() && i > 0 && value[i - 1].isHighSurrogate())
        }
}
