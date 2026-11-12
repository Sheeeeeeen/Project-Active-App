package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.ui.testing.FakeShakeSource
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.assertShown
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.openRegistration
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F1RequestingClassTest {
    @Test
    fun F1_AC23_requestToJoinShowsTheClassCardNoteAndBackLabel() = runComposeUiTest {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            openRegistration(nav, trxClass.id)
            tag("registration_requesting_card").assertTextContains(Flow1Copy.requestingKicker)
            tag("registration_requesting_card").assertTextContains(Flow1Copy.className)
            tag("registration_requesting_card").assertTextContains(Flow1Copy.requestingSubtitle)
            assertShown(Flow1Copy.subtitleRequest)
            tag("registration_submit_button").assertTextContains(Flow1Copy.createAndSend)
            tag("registration_signin_note").assertTextContains(Flow1Copy.signInNote)
            tag("registration_back").assertTextContains(Flow1Copy.backRequest)
        }
    }

    @Test
    fun F1_AC24_shakeOnRequestToJoinChangesNothing() = runComposeUiTest {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val shakes = FakeShakeSource()
            val factory = world.factory()
            val nav = mountNav(world, shakeSource = shakes, factory = factory)
            openRegistration(nav, trxClass.id)
            shakes.emitShake()
            waitForIdle()
            tag("registration_title").assertTextContains(Flow1Copy.createAccount)
            tag("registration_employee_id_field").assertExists()
            val viewModel = factory(RegistrationOrigin.FromRequestToJoin(trxClass.id))
            assertEquals(Role.STUDENT, viewModel.state.value.mode)
            assertEquals(true, viewModel.state.value.showEmployeeId)
        }
    }

    @Test
    fun F1_AC25_unknownClassHidesTheCardButKeepsTheRequestCopy() = runComposeUiTest {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            openRegistration(nav, "missing-class")
            tag("registration_requesting_card").assertDoesNotExist()
            tag("registration_signin_note").assertDoesNotExist()
            assertShown(Flow1Copy.subtitleRequest)
            tag("registration_submit_button").assertTextContains(Flow1Copy.createAndSend)
        }
    }
}
