package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.User
import ph.project.active.domain.repository.currentStatus
import ph.project.active.ui.testing.Flow4Example
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.OFFLINE_NOTHING_SAVED
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.clickText
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.confirmTheDialog
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openManageTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.submitInviteSearch
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.textsUnder

private val MON_5_OCT_2026 = LocalDate(2026, 10, 5)

@OptIn(ExperimentalTestApi::class)
class F4ManageOfflineTest {
    @Test
    fun F4_AC24_offlineCancelDateOpensNoDialogAndSavesNothingThenWorksOnline() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = openCoreBurnOffline(world)
            tag("offline_banner").assertExists()

            tag("manage_cancel_date_2026-10-05").performClick()
            waitForIdle()
            tag("manage_dialog").assertDoesNotExist()
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertEquals(SessionStatus.SCHEDULED, sessionStatus(world, d1, MON_5_OCT_2026))

            mainClock.advanceTimeBy(10_000)
            waitForIdle()
            assertEquals(SessionStatus.SCHEDULED, sessionStatus(world, d1, MON_5_OCT_2026), "no retry on its own")

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("manage_cancel_date_2026-10-05").performClick()
            waitForIdle()
            tag("manage_dialog_confirm").performClick()
            waitForIdle()
            assertEquals(SessionStatus.CANCELLED, sessionStatus(world, d1, MON_5_OCT_2026))
            assertTextUnder("manage_toast", "Mon 5 Oct cancelled. Other dates stay scheduled.")
            tag("manage_form_error").assertDoesNotExist()
        }
    }

    @Test
    fun F4_AC24_offlineApproveAndDeclineSaveNothingThenWorkOnline() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = openCoreBurnOffline(world)
            openManageTab("requests")

            tag("manage_approve_${d1.ella.id}").performClick()
            waitForIdle()
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertEquals(MembershipStatus.PENDING_REQUEST, status(world, d1, d1.ella))
            tag("manage_approve_${d1.ella.id}").assertExists()

            tag("manage_decline_${d1.bea.id}").performClick()
            waitForIdle()
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertEquals(MembershipStatus.PENDING_REQUEST, status(world, d1, d1.bea))

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("manage_approve_${d1.ella.id}").performClick()
            waitForIdle()
            assertEquals(MembershipStatus.ACTIVE, status(world, d1, d1.ella))
            tag("manage_form_error").assertDoesNotExist()
            tag("manage_decline_${d1.bea.id}").performClick()
            waitForIdle()
            assertEquals(MembershipStatus.DECLINED, status(world, d1, d1.bea))
        }
    }

    @Test
    fun F4_AC24_offlineRemoveSavesNothingThenWorksOnline() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = openCoreBurnOffline(world)
            openManageTab("roster")

            tag("manage_remove_${d1.gia.id}").performClick()
            waitForIdle()
            confirmTheDialog() // section 10 Q7: dialog first, then the error
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertEquals(MembershipStatus.ACTIVE, status(world, d1, d1.gia))
            tag("manage_remove_${d1.gia.id}").assertExists()

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("manage_remove_${d1.gia.id}").performClick()
            waitForIdle()
            tag("manage_dialog_confirm").performClick()
            waitForIdle()
            assertEquals(MembershipStatus.REMOVED, status(world, d1, d1.gia))
            tag("manage_form_error").assertDoesNotExist()
        }
    }

    @Test
    fun F4_AC24_offlineInviteSearchKeepsTheTextAndSavesNothingThenWorksOnline() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = openCoreBurnOffline(world)
            val before = world.memberships.all()
            openManageTab("invite")

            submitInviteSearch("EMP-20733")
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertTrue("EMP-20733" in textsUnder("manage_invite_search"), "typed search text stays")
            tag("manage_invite_send").assertDoesNotExist()
            assertEquals(before, world.memberships.all())

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            submitInviteSearch("EMP-20733")
            tag("manage_form_error").assertDoesNotExist()
            assertTextShown("Felix Ong")
            tag("manage_invite_send").performClick()
            waitForIdle()
            assertEquals(MembershipStatus.INVITED, status(world, d1, d1.felix))
        }
    }

    @Test
    fun F4_AC24_offlineSendInviteSavesNothingThenWorksOnline() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = openCoreBurnOnline(world)
            openManageTab("invite")
            submitInviteSearch("EMP-20733")
            assertTextShown("Felix Ong")

            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            tag("offline_banner").assertExists()
            tag("manage_invite_send").performClick()
            waitForIdle()
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertEquals(null, status(world, d1, d1.felix))
            assertTrue("EMP-20733" in textsUnder("manage_invite_search"), "typed search text stays")
            assertTextUnder("manage_invite_send", "Send invite")

            mainClock.advanceTimeBy(10_000)
            waitForIdle()
            assertEquals(null, status(world, d1, d1.felix), "no retry on its own")

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("manage_invite_send").performClick()
            waitForIdle()
            assertEquals(MembershipStatus.INVITED, status(world, d1, d1.felix))
            assertTextUnder("manage_toast", "Invite sent to Felix Ong. They\u2019ll see it in Invites.")
            tag("manage_form_error").assertDoesNotExist()
        }
    }

    @Test
    fun F4_AC24_offlineCancelClassSavesNothingThenWorksOnline() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.network.set(NetworkStatus.OFFLINE)
            val d1 = world.seedD1()
            world.setToday(MON_5_OCT_2026)
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCoachCard(d1.coreBurn.id)

            tag("manage_class_options").performClick()
            waitForIdle()
            clickText("Cancel whole class")
            confirmTheDialog() // section 10 Q7: dialog first, then the error
            assertTextUnder("manage_form_error", OFFLINE_NOTHING_SAVED)
            assertFalse(world.classes.findClass(d1.coreBurn.id)!!.isCancelled)
            assertEquals(listOf("CoachHome", "ManageClass(${d1.coreBurn.id})"), coachRoutes(nav))

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("manage_class_options").performClick()
            waitForIdle()
            clickText("Cancel whole class")
            tag("manage_dialog_confirm").performClick()
            waitForIdle()
            assertTrue(world.classes.findClass(d1.coreBurn.id)!!.isCancelled)
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    @Test
    fun F4_AC24_typedSearchTextStaysWhenTheConnectionDrops() = runFlow4UiTest {
        Flow4Harness().use { world ->
            openCoreBurnOnline(world)
            openManageTab("invite")
            tag("manage_invite_search").performTextReplacement("EMP-207")
            waitForIdle()
            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            assertTrue("EMP-207" in textsUnder("manage_invite_search"))
        }
    }

    private fun ComposeUiTest.openCoreBurnOffline(world: Flow4Harness): Flow4Example {
        val d1 = openCoreBurnOnline(world)
        world.network.set(NetworkStatus.OFFLINE)
        waitForIdle()
        return d1
    }

    private fun ComposeUiTest.openCoreBurnOnline(world: Flow4Harness): Flow4Example {
        val d1 = world.seedD1()
        world.setToday(MON_5_OCT_2026)
        world.signInAsMara()
        mountFlow4(world)
        openCoachCard(d1.coreBurn.id)
        return d1
    }

    private fun status(world: Flow4Harness, d1: Flow4Example, student: User): MembershipStatus? =
        world.memberships.currentStatus(d1.coreBurn.id, student.id)

    private fun sessionStatus(world: Flow4Harness, d1: Flow4Example, date: LocalDate): SessionStatus =
        world.classes.sessionsOf(d1.coreBurn.id).single { it.date == date }.status
}
