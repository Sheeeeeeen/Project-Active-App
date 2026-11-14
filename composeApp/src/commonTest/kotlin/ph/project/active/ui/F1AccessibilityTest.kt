package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import ph.project.active.ui.testing.runFlow1UiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.showBound
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1AccessibilityTest {
    @Test
    fun F1_AC39_registrationControlsMeetTouchTargets() = runFlow1UiTest {
        Flow1Harness().use { world ->
            showBound(world.registration(RegistrationOrigin.FromClassList))
            listOf(
                "registration_name_field",
                "registration_email_field",
                "registration_password_field",
                "registration_employee_id_field",
                "registration_signin_email_field",
                "registration_signin_password_field",
                "registration_password_toggle",
                "registration_signin_password_toggle",
                "registration_back",
            ).forEach { name -> tag(name).assertHeightIsAtLeast(48.dp) }
            tag("registration_submit_button").assertHeightIsEqualTo(52.dp)
            tag("registration_signin_button").assertHeightIsEqualTo(52.dp)
            tag("registration_submit_button").assertHeightIsAtLeast(48.dp)
            tag("registration_signin_button").assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun F1_AC39_guestRegisterButtonIsAtLeast48Dp() = runFlow1UiTest {
        Flow1Harness().use { world ->
            mountNav(world)
            tag("guest_home_register_button").assertHeightIsAtLeast(48.dp)
        }
    }

    @Test
    fun F1_AC40_errorsPairAnIconWithTextAndPasswordTogglesAreNamed() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            showBound(viewModel)
            typeInto("registration_password_field", "password1")
            typeInto("registration_signin_password_field", "password1")
            tag("registration_password_toggle").assertContentDescriptionEquals("Show password")
            tag("registration_signin_password_toggle").assertContentDescriptionEquals("Show password")
            clickTag("registration_password_toggle")
            tag("registration_password_toggle").assertContentDescriptionEquals("Hide password")
            clickTag("registration_signin_password_toggle")
            tag("registration_signin_password_toggle").assertContentDescriptionEquals("Hide password")
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            val error = tag("registration_email_error").fetchSemanticsNode()
            val icon = tag("registration_email_error_icon").fetchSemanticsNode()
            assertTrue(error.parent?.id == icon.parent?.id)
            tag("registration_email_error").assertTextContains(Flow1Copy.invalidEmail)
            tag("registration_email_field").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error))
        }
    }
}
