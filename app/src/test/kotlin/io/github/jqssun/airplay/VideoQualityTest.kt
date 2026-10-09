package io.github.jqssun.airplay
import org.junit.Assert.*
import org.junit.Test
class VideoQualityTest {
    @Test fun smoothRequests1080At60WhenSupported() {
        assertEquals(VideoProfile(1920,1080,60),VideoQuality.select(VideoQualityMode.SMOOTH) { !it.hevc })
    }
    @Test fun smoothPreserves1080At30When60Unsupported() {
        assertEquals(VideoProfile(1920,1080,30),VideoQuality.select(VideoQualityMode.SMOOTH) { it.fps<=30 })
    }
    @Test fun uhdRequiresHevcAndFallsBackWithoutIt() {
        assertEquals(VideoProfile(3840,2160,30,true),VideoQuality.select(VideoQualityMode.UHD) { true })
        assertEquals(VideoProfile(1920,1080,30),VideoQuality.select(VideoQualityMode.UHD) { !it.hevc })
    }
    @Test fun unsupported1080FallsBackTo720AndUnsupportedEverythingFails() {
        assertEquals(VideoProfile(1280,720,30),VideoQuality.select(VideoQualityMode.UHD) { !it.hevc && it.width<=1280 })
        assertNull(VideoQuality.select(VideoQualityMode.UHD) { false })
    }
    @Test fun defaultsAndCompatibleKeepExistingLimits() {
        assertEquals(VideoProfile(1920,1080,30),VideoQuality.select(VideoQualityMode.AUTOMATIC) { true })
        assertEquals(VideoProfile(1280,720,30),VideoQuality.select(VideoQualityMode.COMPATIBLE) { true })
    }
    @Test fun surfaceKeepsUhdAndPortraitBuffersButRejectsInvalidDimensions() {
        assertEquals(3840 to 2160,VideoQuality.surfaceSize("3840x2160"))
        assertEquals(2160 to 3840,VideoQuality.surfaceSize("2160x3840"))
        listOf("0x2160","-1x720","7680x4320","","NaNx1080").forEach { assertNull(VideoQuality.surfaceSize(it)) }
    }
    @Test fun preferencesRoundTripAndLegacyMigration() {
        VideoQualityMode.entries.forEach { assertEquals(it,VideoQualityMode.stored(it.name,null)) }
        assertEquals(VideoQualityMode.COMPATIBLE,VideoQualityMode.stored(null,"1280x720"))
        assertEquals(VideoQualityMode.AUTOMATIC,VideoQualityMode.stored("unknown",null))
    }
}
