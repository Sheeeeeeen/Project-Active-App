package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.FieldError
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.assertShown
import ph.project.active.ui.testing.showBound
import ph.project.active.ui.testing.showScreen
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F1RegistrationCopyTest {
    @Test
    fun F1_AC10_freshStudentScreenShowsB1CopyAndNoRequestingCard() = runComposeUiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            assertShown(Flow1Copy.overlineStudent)
            tag("registration_title").assertTextContains(Flow1Copy.createAccount)
            assertShown(Flow1Copy.subtitleStudent)
            assertShown(Flow1Copy.fullName)
            assertShown(Flow1Copy.email)
            assertShown(Flow1Copy.password)
            assertShown(Flow1Copy.employeeId)
            assertShown(Flow1Copy.namePlaceholder)
            assertShown(Flow1Copy.emailPlaceholder)
            assertShown(Flow1Copy.passwordPlaceholder)
            assertShown(Flow1Copy.employeePlaceholder)
            assertShown(Flow1Copy.signInPasswordPlaceholder)
            tag("registration_password_helper").assertTextContains(Flow1Copy.passwordHelper)
            tag("registration_employee_id_helper").assertTextContains(Flow1Copy.employeeHelper)
            assertShown(Flow1Copy.alreadyHaveAccount)
            assertShown(Flow1Copy.signIn)
            tag("registration_submit_button").assertTextContains(Flow1Copy.createAccount)
            tag("registration_signin_button").assertTextContains(Flow1Copy.signIn)
            tag("registration_back").assertTextContains(Flow1Copy.backClasses)
            tag("registration_requesting_card").assertDoesNotExist()
            tag("registration_signin_note").assertDoesNotExist()
        }
    }

    @Test
    fun F1_AC16_eachFieldErrorRendersItsCopy() = runComposeUiTest {
        Flow1Harness().use { world ->
            val base = world.registration(RegistrationOrigin.FromClassList).state.value
            showScreen(
                base.copy(
                    password = "short",
                    fieldErrors = mapOf(Field.PASSWORD to FieldError.PasswordTooShort),
                ),
            )
            tag("registration_password_error").assertTextContains(Flow1Copy.passwordHelper)
            tag("registration_password_error_icon").assertExists()

            showScreen(
                base.copy(
                    email = "ella.navarro@acme.test",
                    fieldErrors = mapOf(Field.EMAIL to FieldError.EmailTaken),
                ),
            )
            tag("registration_email_error").assertTextContains(Flow1Copy.emailTaken)
            tag("registration_email_error_icon").assertExists()

            showScreen(
                base.copy(
                    fieldErrors = mapOf(
                        Field.SIGN_IN_EMAIL to FieldError.Required,
                        Field.SIGN_IN_PASSWORD to FieldError.Required,
                    ),
                ),
            )
            tag("registration_signin_email_error").assertTextContains(Flow1Copy.required)
            tag("registration_signin_password_error").assertTextContains(Flow1Copy.required)
        }
    }
}
