package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import ph.project.active.ui.testing.FakeShakeSource
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.assertShown
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.runFlow1UiTest
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F1ShakeReplayTest {
    @Test
    fun F1_AC18_shakesBeforeOpeningRegistrationAreIgnored() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val shakes = FakeShakeSource()
            mountNav(world, shakeSource = shakes)
            // Three shakes. An even count toggles back to student, so the replay is invisible.
            shakes.emitShake()
            repeat(2) { shakes.emitShake() }
            waitForIdle()
            tag("guest_home_register_button").performClick()
            waitForIdle()
            tag("registration_title").assertTextContains(Flow1Copy.createAccount)
            assertShown(Flow1Copy.overlineStudent)
            assertShown(Flow1Copy.subtitleStudent)
            tag("registration_employee_id_field").assertExists()
            shakes.emitShake()
            waitForIdle()
            tag("registration_title").assertTextContains(Flow1Copy.coachTitle)
            assertShown(Flow1Copy.overlineCoach)
            assertShown(Flow1Copy.subtitleCoach)
            tag("registration_employee_id_field").assertDoesNotExist()
            tag("registration_submit_button").assertTextContains(Flow1Copy.createCoach)
        }
    }
}
