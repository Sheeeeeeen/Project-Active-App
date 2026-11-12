package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import ph.project.active.ui.testing.runFlow1UiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1OfflineTest {
    @Test
    fun F1_AC31_offlineBannerShowsAndHidesWithoutRecreatingTheScreen() = runFlow1UiTest {
        Flow1Harness().use { world ->
            mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            tag("offline_banner").assertTextContains(Flow1Copy.offlineBanner)
            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("offline_banner").assertDoesNotExist()
        }
    }

    @Test
    fun F1_AC31_offlineSubmitShowsFormErrorAndCreatesNothing() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            clickTag("registration_submit_button")
            tag("registration_form_error").assertTextContains(Flow1Copy.offlineSaved)
            assertNull(world.session.account.value)
            assertEquals(0, world.users.all().size)
            assertEquals(listOf("GuestHome", "Registration(null)"), Flow1Compose.screenRoutes(nav))
        }
    }

    @Test
    fun F1_AC31_offlineSignInShowsTheSameTextOnTheSignInError() = runFlow1UiTest {
        Flow1Harness().use { world ->
            world.seedStudent()
            val nav = mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_signin_email_field", "ella.navarro@acme.test")
            typeInto("registration_signin_password_field", "password1")
            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            clickTag("registration_signin_button")
            tag("registration_signin_error").assertTextContains(Flow1Copy.offlineSaved)
            assertNull(world.session.account.value)
            assertEquals(listOf("GuestHome", "Registration(null)"), Flow1Compose.screenRoutes(nav))
        }
    }
}
