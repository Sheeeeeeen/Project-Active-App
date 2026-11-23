package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F3StudentNavTest {
    @Test
    fun F3_AC19_signingInWithoutAClassLandsOnTheClassesTab() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.seedC1()
            val nav = mountFlow3(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_signin_email_field", "paolo@company.com")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            waitForIdle()

            assertEquals(listOf("StudentHome(null)"), Flow1Compose.screenRoutes(nav))
            tag("student_home").assertExists()
            tag("student_classes_title").assertExists()
            tag("student_tab_classes").assertIsSelected()
        }
    }

    @Test
    fun F3_AC19_signingInFromAClassPushesDetailOnceAndBackShowsC1WithTheTabBar() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            val nav = mountFlow3(world)
            val everyDay = c1.everyDay20.id
            tag("guest_list_card_$everyDay").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            typeInto("registration_signin_email_field", "paolo@company.com")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            waitForIdle()

            assertEquals(listOf("StudentHome($everyDay)", "ClassDetail($everyDay)"), Flow1Compose.screenRoutes(nav))
            tag("class_detail_status_title").assertTextContains("Awaiting approval")

            tag("class_detail_back").performClick()
            waitForIdle()
            assertEquals(listOf("StudentHome($everyDay)"), Flow1Compose.screenRoutes(nav))
            tag("student_home").assertExists()
            tag("student_tab_classes").assertIsSelected()
        }
    }

    @Test
    fun F3_AC19_signingUpFromAClassPushesDetailOnceAndBackShowsC1WithTheTabBar() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            val nav = mountFlow3(world)
            val everyDay = c1.everyDay20.id
            tag("guest_list_card_$everyDay").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20611")
            clickTag("registration_submit_button")
            waitForIdle()

            assertEquals(listOf("StudentHome($everyDay)", "ClassDetail($everyDay)"), Flow1Compose.screenRoutes(nav))
            tag("class_detail_status_title").assertTextContains("Awaiting approval")

            tag("class_detail_back").performClick()
            waitForIdle()
            tag("student_home").assertExists()
            tag("student_tab_classes").assertIsSelected()
        }
    }
}
