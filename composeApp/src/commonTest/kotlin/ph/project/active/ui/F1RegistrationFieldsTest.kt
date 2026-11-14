package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import ph.project.active.ui.testing.runFlow1UiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.replaceText
import ph.project.active.ui.testing.showBound
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1RegistrationFieldsTest {
    @Test
    fun F1_AC11_typingUpdatesEachViewModelFieldAndTheFieldShowsIt() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            typeInto("registration_signin_email_field", "mara.santos@acme.test")
            typeInto("registration_signin_password_field", "coach-secret")
            waitForIdle()
            assertEquals("Ella Navarro", viewModel.state.value.name)
            assertEquals("ella.navarro@acme.test", viewModel.state.value.email)
            assertEquals("password1", viewModel.state.value.password)
            assertEquals("EMP-20417", viewModel.state.value.employeeId)
            assertEquals("mara.santos@acme.test", viewModel.state.value.signInEmail)
            assertEquals("coach-secret", viewModel.state.value.signInPassword)
            tag("registration_name_field").assertTextContains("Ella Navarro")
            tag("registration_email_field").assertTextContains("ella.navarro@acme.test")
            tag("registration_employee_id_field").assertTextContains("EMP-20417")
            tag("registration_signin_email_field").assertTextContains("mara.santos@acme.test")
        }
    }

    @Test
    fun F1_AC12_passwordTogglesRevealAndHideWithoutChangingViewModelState() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            typeInto("registration_password_field", "password1")
            typeInto("registration_signin_password_field", "coach-secret")
            waitForIdle()
            val before = viewModel.state.value
            tag("registration_password_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            tag("registration_signin_password_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            clickTag("registration_password_toggle")
            tag("registration_password_field").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
            tag("registration_password_field").assertTextContains("password1")
            clickTag("registration_password_toggle")
            tag("registration_password_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            clickTag("registration_signin_password_toggle")
            tag("registration_signin_password_field").assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Password))
            tag("registration_signin_password_field").assertTextContains("coach-secret")
            clickTag("registration_signin_password_toggle")
            tag("registration_signin_password_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Password))
            assertEquals(before, viewModel.state.value)
        }
    }

    @Test
    fun F1_AC13_submitFollowsCanSubmitAndSignInStaysEnabled() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            tag("registration_submit_button").assertIsNotEnabled()
            tag("registration_signin_button").assertIsEnabled()
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            waitForIdle()
            tag("registration_submit_button").assertIsEnabled()
            tag("registration_signin_button").assertIsEnabled()
            assertEquals(false, viewModel.state.value.isSubmitting)
        }
    }

    @Test
    fun F1_AC17_editingAFieldClearsOnlyThatFieldsError() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme")
            typeInto("registration_password_field", "short")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            tag("registration_email_error").assertExists()
            tag("registration_password_error").assertExists()
            replaceText("registration_email_field", "ella.navarro@acme.test")
            waitForIdle()
            tag("registration_email_error").assertDoesNotExist()
            tag("registration_password_error").assertExists()
            assertNull(viewModel.state.value.fieldErrors[Field.EMAIL])
        }
    }
}
