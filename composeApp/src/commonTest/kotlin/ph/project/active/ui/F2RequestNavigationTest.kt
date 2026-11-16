package ph.project.active.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.ui.navigation.ClassDetail
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.Flow2Harness
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.SaveableRoundTrip
import ph.project.active.ui.testing.ShowClassDetail
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F2RequestNavigationTest {
    @Test
    fun F2_AC25_guestRequestOpensRegistrationWithTheClassCard() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            assertEquals(
                listOf("GuestHome", "ClassDetail(${trxClass.id})", "Registration(${trxClass.id})"),
                Flow1Compose.screenRoutes(nav),
            )
            tag("registration_requesting_card").assertTextContains("TRX Core Burn")
        }
    }

    @Test
    fun F2_AC26_offlineGuestRequestStillOpensRegistration() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            world.network.set(NetworkStatus.OFFLINE)
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            assertTrue(Flow1Compose.screenRoutes(nav).contains("Registration(${trxClass.id})"))
        }
    }

    @Test
    fun F2_AC27_signUpOrSignInLandsOnPendingDetailAndBackShowsStudentHome() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            openDetailAndRegister(trxClass.id)
            assertEquals(
                listOf("StudentHome(${trxClass.id})", "ClassDetail(${trxClass.id})"),
                Flow1Compose.screenRoutes(nav),
            )
            tag("class_detail_chip").assertTextContains("Pending")
            tag("class_detail_status_title").assertTextContains("Awaiting approval")
            assertEquals(1, world.memberships.forClass(trxClass.id).count { it.status == MembershipStatus.PENDING_REQUEST })
            tag("class_detail_back").performClick()
            waitForIdle()
            tag("student_home").assertExists()
        }
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            world.seedStudent(employeeId = "EMP-20417")
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            typeInto("registration_signin_email_field", "ella.navarro@acme.test")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            assertEquals(
                listOf("StudentHome(${trxClass.id})", "ClassDetail(${trxClass.id})"),
                Flow1Compose.screenRoutes(nav),
            )
            tag("class_detail_chip").assertTextContains("Pending")
        }
    }

    // emulateSaveAndRestore throws NotImplementedError on Compose 1.12 desktop.
    // StateRestorationTester.skiko.kt platformEncodeDecode is a stub. SaveableStateRegistry
    // is the supported round trip: NavHost saves its back stack there, we dispose, then
    // compose the same ViewModels again from that saved map.
    @Test
    fun F2_AC28_classDetailStaysOnceAfterRecompositionAndRestoration() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val tick = mutableStateOf(0)
            val trip = SaveableRoundTrip()
            lateinit var nav: androidx.navigation.NavHostController
            val root = world.root()
            val guest = world.guest()
            val factory = world.factory()
            setContent {
                trip.Wrap {
                    tick.value
                    Flow1Compose.Nav(
                        rootViewModel = root,
                        guestClassListViewModel = guest,
                        registrationViewModel = factory,
                        onNavController = { nav = it },
                    )
                }
            }
            waitForIdle()
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            repeat(3) {
                tick.value = tick.value + 1
                waitForIdle()
            }
            val savedRoutes = Flow1Compose.screenRoutes(nav)
            runOnIdle { trip.saveForNextComposition() }
            setContent { }
            waitForIdle()
            setContent {
                trip.Wrap {
                    tick.value
                    Flow1Compose.Nav(
                        rootViewModel = root,
                        guestClassListViewModel = guest,
                        registrationViewModel = factory,
                        onNavController = { nav = it },
                    )
                }
            }
            waitForIdle()
            assertEquals(savedRoutes, Flow1Compose.screenRoutes(nav))
            tick.value = tick.value + 1
            waitForIdle()
            val details = Flow1Compose.screenRoutes(nav).filter { it.startsWith("ClassDetail(") }
            assertEquals(listOf("ClassDetail(${trxClass.id})"), details)
        }
    }

    @Test
    fun F2_AC29_cancelledOrUnavailableClassStillLandsWithTheOneTimeNote() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            world.classes.saveClass(world.classes.findClass(trxClass.id)!!.copy(cancelledAt = kotlin.time.Clock.System.now()))
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            tag("class_detail_notice").assertTextContains(
                "Your account is ready, but no request was sent because this class was cancelled.",
            )
            tag("class_detail_status_body").assertTextContains("This class has been cancelled.")
            assertTrue(world.memberships.forClass(trxClass.id).isEmpty())
            assertEquals("ClassDetail(${trxClass.id})", Flow1Compose.screenRoutes(nav).last())
        }
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            val saved = world.classes.findClass(trxClass.id)!!
            world.classes.saveClass(saved.copy(visibility = Visibility.PRIVATE))
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella2.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20418")
            clickTag("registration_submit_button")
            tag("class_detail_unavailable").assertTextContains("Class not available")
            tag("class_detail_notice").assertTextContains(
                "Your account is ready, but no request was sent because this class isn\u2019t available.",
            )
            assertEquals("ClassDetail(${trxClass.id})", Flow1Compose.screenRoutes(nav).last())
        }
    }

    @Test
    fun F2_AC30_theOneTimeNoteIsGoneAfterRefreshOrAFreshOpen() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            world.cancel(trxClass.id)
            val viewModel = world.detail(trxClass.id, joinOutcome = JoinOutcome.ClassCancelled)
            setContent { ShowClassDetail(viewModel) }
            tag("class_detail_notice").assertExists()
            viewModel.refresh()
            waitForIdle()
            tag("class_detail_notice").assertDoesNotExist()
        }
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_request_button").performClick()
            waitForIdle()
            world.classes.saveClass(world.classes.findClass(trxClass.id)!!.copy(cancelledAt = kotlin.time.Clock.System.now()))
            typeInto("registration_name_field", "Ella Navarro")
            typeInto("registration_email_field", "ella.navarro@acme.test")
            typeInto("registration_password_field", "password1")
            typeInto("registration_employee_id_field", "EMP-20417")
            clickTag("registration_submit_button")
            tag("class_detail_notice").assertExists()
            tag("class_detail_back").performClick()
            waitForIdle()
            runOnIdle { nav.navigate(ClassDetail(trxClass.id)) }
            waitForIdle()
            tag("class_detail_notice").assertDoesNotExist()
        }
    }

    private suspend fun androidx.compose.ui.test.ComposeUiTest.openDetailAndRegister(classId: String) {
        tag("guest_list_card_$classId").performClick()
        waitForIdle()
        tag("class_detail_request_button").performClick()
        waitForIdle()
        typeInto("registration_name_field", "Ella Navarro")
        typeInto("registration_email_field", "ella.navarro@acme.test")
        typeInto("registration_password_field", "password1")
        typeInto("registration_employee_id_field", "EMP-20417")
        clickTag("registration_submit_button")
    }
}
