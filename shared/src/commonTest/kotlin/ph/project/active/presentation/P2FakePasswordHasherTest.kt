package ph.project.active.presentation

import kotlin.test.Test

class P2FakePasswordHasherTest {
    @Test
    fun P2_verifyAcceptsThePasswordThatWasHashedAndRejectsAnother() {
        Phase3SharedChecks.fakePasswordHasherRoundTrip()
    }
}
