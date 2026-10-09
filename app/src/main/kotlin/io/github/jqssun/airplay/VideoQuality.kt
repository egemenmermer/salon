package io.github.jqssun.airplay

enum class VideoQualityMode { AUTOMATIC, SMOOTH, UHD, COMPATIBLE;
    companion object {
        fun stored(value: String?, oldResolution: String?): VideoQualityMode =
            entries.firstOrNull { it.name == value } ?: if (oldResolution == "1280x720") COMPATIBLE else AUTOMATIC
    }
}
data class VideoProfile(val width: Int, val height: Int, val fps: Int, val hevc: Boolean = false)
object VideoQuality {
    const val PREF = "salon_quality"
    fun surfaceSize(resolution: String): Pair<Int,Int>? {
        val parts = resolution.split('x')
        if(parts.size != 2) return null
        val w = parts[0].toIntOrNull() ?: return null
        val h = parts[1].toIntOrNull() ?: return null
        return if(w in 1..3840 && h in 1..3840) w to h else null
    }
    fun select(mode: VideoQualityMode, supports: (VideoProfile) -> Boolean): VideoProfile? {
        val safe = listOf(VideoProfile(1920,1080,30),VideoProfile(1280,720,30),VideoProfile(960,540,30),VideoProfile(640,360,30))
        val candidates = when(mode) {
            VideoQualityMode.AUTOMATIC -> safe
            VideoQualityMode.SMOOTH -> listOf(VideoProfile(1920,1080,60)) + safe
            VideoQualityMode.UHD -> listOf(VideoProfile(3840,2160,30,true)) + safe
            VideoQualityMode.COMPATIBLE -> safe.drop(1)
        }
        return candidates.firstOrNull(supports)
    }
}
