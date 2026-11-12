package ph.project.active.ui

import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.ui.testing.FakeShakeSource
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.assertShown
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.showRoute
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto
import ph.project.active.ui.theme.TrxThemeMode
import ph.project.active.ui.theme.TrxThemeModeKey

@OptIn(ExperimentalTestApi::class)
class F1CoachRegistrationTest {
    @Test
    fun F1_AC18_oneShakeShowsCoachRegistrationAndTheNextRestoresStudent() = runComposeUiTest {
        Flow1Harness().use { world ->
            val shakes = FakeShakeSource()
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showRoute(viewModel, shakes)
            shakes.emitShake()
            waitForIdle()
            tag("registration_title").assertTextContains(Flow1Copy.coachTitle)
            assertShown(Flow1Copy.overlineCoach)
            assertShown(Flow1Copy.subtitleCoach)
            tag("registration_employee_id_field").assertDoesNotExist()
            tag("registration_submit_button").assertTextContains(Flow1Copy.createCoach)
            tag("registration_coach_band").assertExists()
            onNode(SemanticsMatcher.expectValue(TrxThemeModeKey, TrxThemeMode.Dark)).assertExists()
            shakes.emitShake()
            waitForIdle()
            tag("registration_title").assertTextContains(Flow1Copy.createAccount)
            tag("registration_employee_id_field").assertExists()
            onNode(SemanticsMatcher.expectValue(TrxThemeModeKey, TrxThemeMode.Light)).assertExists()
            assertEquals(Role.STUDENT, viewModel.state.value.mode)
        }
    }

    @Test
    fun F1_AC19_shakeKeepsTypedNameEmailPasswordAndRestoresEmployeeId() = runComposeUiTest {
        Flow1Harness().use { world ->
            val shakes = FakeShakeSource()
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showRoute(viewModel, shakes)
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            shakes.emitShake()
            waitForIdle()
            tag("registration_name_field").assertTextContains("Ella Navarro")
            tag("registration_email_field").assertTextContains("ella.navarro@acme.test")
            tag("registration_password_field").assertTextContains("password1")
            shakes.emitShake()
            waitForIdle()
            tag("registration_name_field").assertTextContains("Ella Navarro")
            tag("registration_email_field").assertTextContains("ella.navarro@acme.test")
            tag("registration_password_field").assertTextContains("password1")
            tag("registration_employee_id_field").assertTextContains("EMP-20417")
        }
    }

    @Test
    fun F1_AC20_neitherModeShowsARoleControlOrTheWordShake() = runComposeUiTest {
        Flow1Harness().use { world ->
            val shakes = FakeShakeSource()
            showRoute(world.registration(RegistrationOrigin.FromClassList), shakes)
            assertNoRoleControl()
            shakes.emitShake()
            waitForIdle()
            assertNoRoleControl()
        }
    }

    @Test
    fun F1_AC21_createCoachAccountShowsCoachHome() = runComposeUiTest {
        Flow1Harness().use { world ->
            val shakes = FakeShakeSource()
            mountNav(world, shakeSource = shakes)
            tag("guest_home_register_button").performClick()
            shakes.emitShake()
            waitForIdle()
            typeInto("registration_name_field", "Mara Santos")
            typeInto("registration_email_field", "mara.santos@acme.test")
            typeInto("registration_password_field", "password1")
            clickTag("registration_submit_button")
            tag("coach_home").assertExists()
            val account = world.session.account.value
            assertEquals(Role.COACH, account?.role)
            assertNull(account?.employeeId)
            assertEquals("Mara Santos", account?.name)
            assertEquals(1, world.users.all().size)
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.assertNoRoleControl() {
        val roles = listOf(
            SemanticsRole.Switch,
            SemanticsRole.Checkbox,
            SemanticsRole.RadioButton,
            SemanticsRole.Tab,
            SemanticsRole.DropdownList,
        )
        for (role in roles) {
            val nodes = onAllNodes(
                SemanticsMatcher.expectValue(SemanticsProperties.Role, role),
                useUnmergedTree = true,
            ).fetchSemanticsNodes()
            assertTrue(nodes.isEmpty(), "found a ${role} control")
        }
        val shake = onAllNodesWithText("shake", substring = true, ignoreCase = true).fetchSemanticsNodes()
        assertTrue(shake.isEmpty(), "screen contains the word shake")
    }
}
