package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import ph.project.active.ui.testing.runFlow1UiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.ui.testing.FakeShakeSource
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Copy
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.GatedAuthRepository
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.openRegistration
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1SignInTest {
    @Test
    fun F1_AC27_blankSignInShowsRequiredAndDoesNotCallSignIn() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val gated = GatedAuthRepository(world.auth)
            val nav = mountNav(world, auth = gated)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            clickTag("registration_signin_button")
            tag("registration_signin_email_error").assertTextContains(Flow1Copy.required)
            tag("registration_signin_password_error").assertTextContains(Flow1Copy.required)
            assertEquals(0, gated.signInCalls)
            assertTrue(Flow1Compose.screenRoutes(nav).contains("Registration(null)"))
        }
    }

    @Test
    fun F1_AC28_wrongPasswordShowsAuthFailedAndStaysOnRegistration() = runFlow1UiTest {
        Flow1Harness().use { world ->
            world.seedStudent()
            val nav = mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_signin_email_field", "ella.navarro@acme.test")
            typeInto("registration_signin_password_field", "wrong-password")
            clickTag("registration_signin_button")
            tag("registration_signin_error").assertTextContains(Flow1Copy.authFailed)
            assertNull(world.session.account.value)
            assertEquals(listOf("GuestHome", "Registration(null)"), Flow1Compose.screenRoutes(nav))
        }
    }

    @Test
    fun F1_AC29_signInLandsOnTheAccountRoleFromEitherMode() {
        signInLands("student", shakeFirst = false, homeTag = "student_home")
        signInLands("coach", shakeFirst = true, homeTag = "coach_home")
        signInLands("coach", shakeFirst = false, homeTag = "coach_home")
        signInLands("student", shakeFirst = true, homeTag = "student_home")
    }

    @Test
    fun F1_AC30_studentSignInFromRequestSendsOnePendingRequest() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            world.seedStudent()
            val nav = mountNav(world)
            openRegistration(nav, trxClass.id)
            typeInto("registration_signin_email_field", "ella.navarro@acme.test")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            assertEquals(listOf("StudentHome(${trxClass.id})"), Flow1Compose.screenRoutes(nav))
            tag("student_home").assertExists()
            val pending = world.memberships.forClass(trxClass.id)
                .filter { it.status == MembershipStatus.PENDING_REQUEST }
            assertEquals(1, pending.size)
        }
    }

    @Test
    fun F1_AC30_coachSignInFromRequestCreatesNoMembership() = runFlow1UiTest {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            world.seedCoach()
            val nav = mountNav(world)
            openRegistration(nav, trxClass.id)
            typeInto("registration_signin_email_field", "mara.santos@acme.test")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            assertEquals(listOf("CoachHome"), Flow1Compose.screenRoutes(nav))
            tag("coach_home").assertExists()
            assertTrue(world.memberships.all().isEmpty())
        }
    }

    private fun signInLands(role: String, shakeFirst: Boolean, homeTag: String) = runFlow1UiTest {
        Flow1Harness().use { world ->
            val email = if (role == "coach") {
                world.seedCoach()
                "mara.santos@acme.test"
            } else {
                world.seedStudent(email = "ella.navarro@acme.test", employeeId = "EMP-20417")
                "ella.navarro@acme.test"
            }
            val shakes = FakeShakeSource()
            mountNav(world, shakeSource = shakes)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            if (shakeFirst) {
                shakes.emitShake()
                waitForIdle()
            }
            typeInto("registration_signin_email_field", email)
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            tag(homeTag).assertExists()
        }
    }
}
