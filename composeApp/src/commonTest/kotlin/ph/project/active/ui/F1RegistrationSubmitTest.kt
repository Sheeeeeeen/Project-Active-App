package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.GatedAuthRepository
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.showBound
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1RegistrationSubmitTest {
    @Test
    fun F1_AC14_createsOneStudentAccount() = runComposeUiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            typeInto("registration_name_field", "  Ella Navarro  ")
            typeInto("registration_email_field", "  ella.navarro@acme.test  ")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "  EMP-20417  ")
            clickTag("registration_submit_button")
            val account = world.session.account.value
            assertEquals(Role.STUDENT, account?.role)
            assertEquals("Ella Navarro", account?.name)
            assertEquals("ella.navarro@acme.test", account?.email)
            assertEquals("EMP-20417", account?.employeeId)
            assertEquals(1, world.users.all().size)
        }
    }

    @Test
    fun F1_AC15_invalidEmailShowsIconAndNoAccount() = runComposeUiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            fillValidStudent(email = "ella.navarro@acme")
            clickTag("registration_submit_button")
            tag("registration_email_error").assertTextContains(Flow1Copy.invalidEmail)
            assertErrorRow("registration_email")
            tag("registration_email_helper").assertDoesNotExist()
            tag("registration_email_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            assertNull(world.session.account.value)
            assertEquals(0, world.users.all().size)
        }
    }

    @Test
    fun F1_AC15_takenEmployeeIdShowsIconAndNoAccount() = runComposeUiTest {
        Flow1Harness().use { world ->
            world.seedStudent(employeeId = "EMP-20611")
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            fillValidStudent(email = "gia.torres@acme.test", employeeId = "EMP-20611")
            clickTag("registration_submit_button")
            tag("registration_employee_id_error").assertTextContains(Flow1Copy.employeeTaken)
            assertErrorRow("registration_employee_id")
            tag("registration_employee_id_helper").assertDoesNotExist()
            tag("registration_employee_id_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
            assertNull(world.session.account.value)
            assertEquals(1, world.users.all().size)
        }
    }

    @Test
    fun F1_AC26_gatedSubmitShowsProgressDisablesTheFormAndIgnoresASecondTap() = runComposeUiTest {
        Flow1Harness().use { world ->
            val gated = GatedAuthRepository(world.auth)
            val viewModel = world.registration(RegistrationOrigin.FromClassList, gated)
            showBound(viewModel)
            fillValidStudent()
            clickTag("registration_submit_button")
            tag("registration_submit_progress").assertExists()
            tag("registration_submit_button").assertTextContains(Flow1Copy.creating)
            listOf(
                "registration_name_field",
                "registration_email_field",
                "registration_password_field",
                "registration_employee_id_field",
                "registration_signin_email_field",
                "registration_signin_password_field",
                "registration_password_toggle",
                "registration_signin_password_toggle",
                "registration_signin_button",
            ).forEach { name -> tag(name).assertIsNotEnabled() }
            val submit = tag("registration_submit_button").fetchSemanticsNode()
            val enabled = SemanticsProperties.Disabled !in submit.config
            if (enabled) tag("registration_submit_button").performClick()
            viewModel.onSubmit()
            assertEquals(1, gated.registerStudentCalls)
            gated.release()
            waitForIdle()
            tag("registration_submit_progress").assertDoesNotExist()
        }
    }

    @Test
    fun F1_AC26_gatedSignInShowsProgressAndDisablesSubmit() = runComposeUiTest {
        Flow1Harness().use { world ->
            world.seedStudent()
            val gated = GatedAuthRepository(world.auth)
            val viewModel = world.registration(RegistrationOrigin.FromClassList, gated)
            showBound(viewModel)
            typeInto("registration_signin_email_field", "ella.navarro@acme.test")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            tag("registration_signin_progress").assertExists()
            tag("registration_signin_button").assertTextContains(Flow1Copy.signIn)
            tag("registration_submit_button").assertIsNotEnabled()
            assertEquals(1, gated.signInCalls)
            gated.release()
            waitForIdle()
            tag("registration_signin_progress").assertDoesNotExist()
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.fillValidStudent(
        email: String = "ella.navarro@acme.test",
        employeeId: String = "EMP-20417",
    ) {
        typeInto("registration_name_field", "Ella Navarro")
        typeInto("registration_email_field", email)
        typeInto("registration_password_field", "password1")
        typeInto("registration_employee_id_field", employeeId)
    }

    private fun androidx.compose.ui.test.ComposeUiTest.assertErrorRow(prefix: String) {
        val error = tag("${prefix}_error").fetchSemanticsNode()
        val icon = tag("${prefix}_error_icon").fetchSemanticsNode()
        assertTrue(error.parent?.id == icon.parent?.id, "$prefix error icon is not a sibling of the error text")
    }
}
