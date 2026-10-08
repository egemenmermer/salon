package io.github.jqssun.airplay
import org.junit.Assert.*
import org.junit.Test
class ReceiverStateMachineTest {
    @Test fun readinessRequiresListenerAndBothAnnouncements() {
        val m = ReceiverStateMachine(); val t = m.begin("Salon TV")
        m.registered(t, "airplay", "Salon TV (2)")
        assertEquals(SalonStage.PREPARING, m.state.stage)
        m.listening(t); assertEquals(SalonStage.PREPARING, m.state.stage)
        m.registered(t, "raop", "id@Salon TV (2)")
        assertEquals(SalonStage.READY, m.state.stage); assertEquals("Salon TV (2)", m.state.tvName)
    }
    @Test fun lateCallbacksCannotReviveStoppedReceiver() {
        val m = ReceiverStateMachine(); val t = m.begin("Salon TV")
        m.stop(true); m.listening(t); m.registered(t,"airplay","Old"); m.event(t,SalonStage.VIDEO)
        assertEquals(SalonStage.NO_NETWORK,m.state.stage)
    }
    @Test fun permissionOrRegistrationFailureNeverLooksReady() {
        val m = ReceiverStateMachine(); val t = m.begin("Salon TV")
        m.listening(t); m.failed(t,"denied",true)
        m.registered(t,"airplay","Salon TV"); m.registered(t,"raop","id@Salon TV")
        assertEquals(SalonStage.PERMISSION,m.state.stage)
    }
    @Test fun sessionEndsBackAtReadyWithPinCleared() {
        val m = ReceiverStateMachine(); val t = m.begin("Salon TV"); m.listening(t)
        m.registered(t,"airplay","Salon TV"); m.registered(t,"raop","id@Salon TV")
        m.event(t,SalonStage.PIN,"1234"); m.event(t,SalonStage.VIDEO); m.ended(t)
        assertEquals(SalonStage.READY,m.state.stage); assertNull(m.state.pin)
    }
    @Test fun repeatsAndRestartDiscardPreviousAnnouncements() {
        val m = ReceiverStateMachine(); val t = m.begin("First"); m.listening(t)
        m.stop(); m.stop(); val n=m.begin("Second")
        m.registered(t,"airplay","First"); m.registered(n,"raop","id@Second"); m.listening(n)
        assertEquals(SalonStage.PREPARING,m.state.stage); assertEquals("Second",m.state.tvName)
    }
    @Test fun namesRespectUtf8AndRejectControlCharacters() {
        assertTrue(SalonSettings.validName("Oturma odası 📺")); assertFalse(SalonSettings.validName("   "))
        assertFalse(SalonSettings.validName("TV\n")); assertFalse(SalonSettings.validName("ü".repeat(26)))
        assertTrue(SalonSettings.validName("ü".repeat(25))); assertFalse(SalonSettings.validName("\uD800"))
    }
    @Test fun controlledPinTimeoutClearsCodeAndDoesNotOpenSession() {
        val m=ReceiverStateMachine(); val t=m.begin("Salon TV");m.listening(t)
        m.event(t,SalonStage.PIN,"9876")
        assertTrue(m.timedOut(t));assertEquals(SalonStage.VIDEO_ERROR,m.state.stage);assertNull(m.state.pin)
        assertFalse(m.timedOut(t))
    }
    @Test fun timeoutCannotAffectACompletedOrRestartedSession() {
        val m=ReceiverStateMachine();val t=m.begin("Salon TV");m.listening(t)
        m.event(t,SalonStage.CONNECTING);m.event(t,SalonStage.VIDEO)
        assertFalse(m.timedOut(t));assertEquals(SalonStage.VIDEO,m.state.stage)
        m.stop(true);val next=m.begin("Yeni ad");m.listening(next);m.event(next,SalonStage.PIN,"9876")
        assertFalse(m.timedOut(t));assertEquals(SalonStage.PIN,m.state.stage)
    }
    @Test fun listenerWithoutNetworkCannotStartPlaybackOrPairing() {
        val m=ReceiverStateMachine();val t=m.begin("Salon TV")
        m.event(t,SalonStage.PIN,"9876");m.event(t,SalonStage.VIDEO)
        assertEquals(SalonStage.PREPARING,m.state.stage);assertNull(m.state.pin)
        m.stop(true);assertEquals(SalonStage.NO_NETWORK,m.state.stage)
    }

}
