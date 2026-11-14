package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import ph.project.active.ui.testing.runFlow1UiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.openRegistration
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1HomePlaceholderTest {
    @Test
    fun F1_AC38_homesShowTheAccountNameOpenClassIdAndNoSignOut() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            openRegistration(nav, trxClass.id)
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            tag("student_home").assertTextContains("Ella Navarro")
            tag("student_home_open_class").assertTextEquals(trxClass.id)
            assertNoSignOut()
            assertEquals(listOf("StudentHome(${trxClass.id})"), Flow1Compose.screenRoutes(nav))
        }
    }

    @Test
    fun F1_AC38_coachHomeShowsTheCoachNameAndNoSignOut() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val shakes = ph.project.active.ui.testing.FakeShakeSource()
            mountNav(world, shakeSource = shakes)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            shakes.emitShake()
            waitForIdle()
            typeInto("registration_name_field", "Mara Santos")
            typeInto("registration_email_field", "mara.santos@acme.test")
            typeInto("registration_password_field", "password1")
            clickTag("registration_submit_button")
            tag("coach_home").assertTextContains("Mara Santos")
            tag("student_home_open_class").assertDoesNotExist()
            assertNoSignOut()
        }
    }

    @Test
    fun F1_AC38_studentHomeWithoutAClassOmitsTheOpenClassTag() = runFlow1UiTest {
        Flow1Harness().use { world ->
            mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            tag("student_home").assertTextContains("Ella Navarro")
            tag("student_home_open_class").assertDoesNotExist()
            assertNoSignOut()
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.assertNoSignOut() {
        val signOut = onAllNodesWithText("sign out", substring = true, ignoreCase = true).fetchSemanticsNodes()
        val hyphenated = onAllNodesWithText("sign-out", substring = true, ignoreCase = true).fetchSemanticsNodes()
        assertTrue(signOut.isEmpty())
        assertTrue(hyphenated.isEmpty())
    }
}
