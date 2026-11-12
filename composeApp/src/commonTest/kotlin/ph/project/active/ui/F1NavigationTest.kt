package ph.project.active.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.root.Root
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.openRegistration
import ph.project.active.ui.testing.showApp
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F1NavigationTest {
    @Test
    fun F1_AC32_guestLaunchOpensRegistrationOnce() = runComposeUiTest {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            assertEquals(listOf("GuestHome"), Flow1Compose.screenRoutes(nav))
            assertTrue(Flow1Compose.guestHomeIsInsideGuestGraph(nav))
            tag("guest_home_register_button").performClick()
            waitForIdle()
            assertEquals(listOf("GuestHome", "Registration(null)"), Flow1Compose.screenRoutes(nav))
            assertNull(world.guest().state.value.navigation)
        }
    }

    @Test
    fun F1_AC32_tappingRegisterTwiceLeavesOneRegistrationEntry() = runComposeUiTest {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            val node = tag("guest_home_register_button").fetchSemanticsNode()
            val click = checkNotNull(node.config[SemanticsActions.OnClick].action)
            click.invoke()
            click.invoke()
            waitForIdle()
            assertEquals(listOf("GuestHome", "Registration(null)"), Flow1Compose.screenRoutes(nav))
        }
    }

    @Test
    fun F1_AC33_studentRegistrationReplacesTheGuestGraph() = runComposeUiTest {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            fillStudent()
            clickTag("registration_submit_button")
            assertSignedInOnly(nav, "StudentHome(null)")
        }
    }

    @Test
    fun F1_AC33_coachRegistrationReplacesTheGuestGraph() = runComposeUiTest {
        Flow1Harness().use { world ->
            val shakes = ph.project.active.ui.testing.FakeShakeSource()
            val nav = mountNav(world, shakeSource = shakes)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            shakes.emitShake()
            waitForIdle()
            typeInto("registration_name_field", "Mara Santos")
            typeInto("registration_email_field", "mara.santos@acme.test")
            typeInto("registration_password_field", "password1")
            clickTag("registration_submit_button")
            assertSignedInOnly(nav, "CoachHome")
        }
    }

    @Test
    fun F1_AC34_requestRegistrationOpensStudentHomeAndSendsOneRequest() = runComposeUiTest {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            openRegistration(nav, trxClass.id)
            fillStudent()
            clickTag("registration_submit_button")
            assertSignedInOnly(nav, "StudentHome(${trxClass.id})")
            val pending = world.memberships.forClass(trxClass.id)
                .filter { it.status == MembershipStatus.PENDING_REQUEST }
            assertEquals(1, pending.size)
        }
    }

    @Test
    fun F1_AC35_successLeavesOneSignedInEntryAfterRecomposition() = runComposeUiTest {
        Flow1Harness().use { world ->
            val factory = world.factory()
            val tick = mutableStateOf(0)
            val root = world.root()
            lateinit var nav: androidx.navigation.NavHostController
            setContent {
                tick.value
                Flow1Compose.Nav(
                    rootViewModel = root,
                    guestClassListViewModel = world.guest(),
                    registrationViewModel = factory,
                    onNavController = { nav = it },
                )
            }
            waitForIdle()
            tag("guest_home_register_button").performClick()
            waitForIdle()
            fillStudent()
            clickTag("registration_submit_button")
            assertSignedInOnly(nav, "StudentHome(null)")
            repeat(3) {
                tick.value = tick.value + 1
                waitForIdle()
            }
            assertSignedInOnly(nav, "StudentHome(null)")
            assertTrue(root.state.value.root is Root.Student)
            assertNull(factory(RegistrationOrigin.FromClassList).state.value.navigation)
        }
    }

    @Test
    fun F1_AC36_navBarBackPopsToGuestHomeAndCreatesNoAccount() = runComposeUiTest {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_name_field", "Ella Navarro")
            tag("registration_back").performClick()
            waitForIdle()
            assertEquals(listOf("GuestHome"), Flow1Compose.screenRoutes(nav))
            assertNull(world.session.account.value)
            assertEquals(0, world.users.all().size)
        }
    }

    @Test
    fun F1_AC37_cachedStudentOrCoachSkipsTheGuestGraph() = runComposeUiTest {
        Flow1Harness().use { world ->
            world.session.save(SignedInAccount("s", "Ella Navarro", "ella.navarro@acme.test", Role.STUDENT, "EMP-20417"))
            val nav = mountNav(world)
            assertEquals(listOf("StudentHome(null)"), Flow1Compose.screenRoutes(nav))
            assertFalse(Flow1Compose.hasGuestRoute(nav))
            tag("student_home").assertExists()
        }
        Flow1Harness().use { world ->
            world.session.save(SignedInAccount("c", "Mara Santos", "mara.santos@acme.test", Role.COACH, null))
            val nav = mountNav(world)
            assertEquals(listOf("CoachHome"), Flow1Compose.screenRoutes(nav))
            assertFalse(Flow1Compose.hasGuestRoute(nav))
            tag("coach_home").assertExists()
        }
    }

    @Test
    fun F1_AC37_fileBackedSessionSurvivesANewTrxApp() {
        val path = Files.createTempFile("trx-session", ".db").toString()
        val account = SignedInAccount("s", "Ella Navarro", "ella.navarro@acme.test", Role.STUDENT, "EMP-20417")
        Flow1Harness(path = path, createSchema = true).use { first ->
            first.session.save(account)
            runComposeUiTest {
                showApp(first)
                tag("student_home").assertTextContains("Ella Navarro")
            }
        }
        Flow1Harness(path = path, createSchema = false).use { second ->
            runComposeUiTest {
                showApp(second)
                tag("student_home").assertTextContains("Ella Navarro")
            }
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.fillStudent() {
        typeInto("registration_name_field", "Ella Navarro")
        typeInto("registration_email_field", "ella.navarro@acme.test")
        typeInto("registration_password_field", "password1")
        typeInto("registration_employee_id_field", "EMP-20417")
    }

    private fun assertSignedInOnly(nav: androidx.navigation.NavHostController, route: String) {
        assertEquals(listOf(route), Flow1Compose.screenRoutes(nav))
        assertNull(nav.previousBackStackEntry)
        assertFalse(Flow1Compose.hasGuestRoute(nav))
    }
}
