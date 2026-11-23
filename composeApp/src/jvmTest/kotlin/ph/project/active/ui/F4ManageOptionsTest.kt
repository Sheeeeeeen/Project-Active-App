package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.User
import ph.project.active.domain.repository.currentStatus
import ph.project.active.ui.testing.CANCELLED_CLASS_ERRORS
import ph.project.active.ui.testing.Flow4Example
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertNoUndo
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.clickText
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.confirmTheDialog
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openManageTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.submitInviteSearch
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.textsUnder

private val MON_5_OCT_2026 = LocalDate(2026, 10, 5)

@OptIn(ExperimentalTestApi::class)
class F4ManageOptionsTest {
    @Test
    fun F4_AC22_classOptionsOffersOnlyCancelWholeClassAndKeepClassStays() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCoachCard(d1.coreBurn.id)

            tag("manage_class_options").performClick()
            waitForIdle()
            assertTextShown("Class options")
            assertTextShown("Cancel whole class")
            clickText("Cancel whole class")

            assertTextUnder("manage_dialog", "Cancel TRX Core Burn?")
            assertTextUnder(
                "manage_dialog",
                "All remaining dates are cancelled and the class stops repeating. It leaves the public class list. " +
                    "Past sessions and their attendance stay as recorded.",
            )
            assertTextUnder("manage_dialog_keep", "Keep class")
            assertTextUnder("manage_dialog_confirm", "Cancel class")

            tag("manage_dialog_keep").performClick()
            waitForIdle()
            tag("manage_dialog").assertDoesNotExist()
            assertEquals(listOf("CoachHome", "ManageClass(${d1.coreBurn.id})"), coachRoutes(nav))
            assertFalse(world.classes.findClass(d1.coreBurn.id)!!.isCancelled)
        }
    }

    @Test
    fun F4_AC22_cancelClassPopsToD1WhereCoreBurnShowsCancelled() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)
            openCoachCard(d1.coreBurn.id)

            tag("manage_class_options").performClick()
            waitForIdle()
            clickText("Cancel whole class")
            tag("manage_dialog_confirm").performClick()
            waitForIdle()

            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            assertTrue(world.classes.findClass(d1.coreBurn.id)!!.isCancelled)
            assertTextUnder("coach_class_card_${d1.coreBurn.id}", "Cancelled")
            assertNoUndo()
        }
    }

    @Test
    fun F4_AC23_aCancelledClassOpensManageWithItsCancelledDatesAndNoCancelDate() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.setToday(MON_5_OCT_2026)
            world.cancel(d1.coreBurn.id)
            val nav = openCancelledCoreBurn(world, d1)

            assertEquals(listOf("CoachHome", "ManageClass(${d1.coreBurn.id})"), coachRoutes(nav))
            assertTextUnder("manage_date_row_2026-10-05", "Cancelled")
            assertTextUnder("manage_date_row_2026-10-28", "Cancelled")
            assertTextUnder("manage_date_row_2026-09-28", "Scheduled")
            assertTrue(cardIdsTopToBottom("manage_cancel_date_").isEmpty(), "no Cancel date on a cancelled class")
            assertCancelWholeClassNotOffered()
        }
    }

    @Test
    fun F4_AC23_approveDeclineAndRemoveAreBlockedOnACancelledClass() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.setToday(MON_5_OCT_2026)
            world.cancel(d1.coreBurn.id)
            openCancelledCoreBurn(world, d1)

            openManageTab("requests")
            tag("manage_approve_${d1.ella.id}").performClick()
            waitForIdle()
            assertBlocked()
            assertEquals(MembershipStatus.PENDING_REQUEST, status(world, d1, d1.ella))

            tag("manage_decline_${d1.bea.id}").performClick()
            waitForIdle()
            assertBlocked()
            assertEquals(MembershipStatus.PENDING_REQUEST, status(world, d1, d1.bea))

            openManageTab("roster")
            tag("manage_remove_${d1.gia.id}").performClick()
            waitForIdle()
            confirmTheDialog() // section 10 Q7: dialog first, then the error
            assertBlocked()
            assertEquals(MembershipStatus.ACTIVE, status(world, d1, d1.gia))
        }
    }

    @Test
    fun F4_AC23_inviteSearchAndSendAreBlockedOnACancelledClass() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.setToday(MON_5_OCT_2026)
            world.cancel(d1.coreBurn.id)
            openCancelledCoreBurn(world, d1)
            val before = world.memberships.all()

            openManageTab("invite")
            submitInviteSearch("EMP-20733")
            assertBlocked()
            if (countTagged("manage_invite_send") > 0) {
                tag("manage_invite_send").performClick()
                waitForIdle()
                assertBlocked()
            }
            assertEquals(before, world.memberships.all())
            assertEquals(null, status(world, d1, d1.felix))
        }
    }

    private fun ComposeUiTest.openCancelledCoreBurn(world: Flow4Harness, d1: Flow4Example): androidx.navigation.NavHostController {
        world.signInAsMara()
        val nav = mountFlow4(world)
        openCoachCard(d1.coreBurn.id)
        return nav
    }

    private fun ComposeUiTest.assertBlocked() {
        val error = textsUnder("manage_form_error").joinToString("\n")
        assertTrue(CANCELLED_CLASS_ERRORS.any { it in error }, "expected one of $CANCELLED_CLASS_ERRORS, found:\n$error")
    }

    /** Spec P5 / decision 15: "Cancel whole class" is not offered; ··· may be hidden when the menu would be empty. */
    private fun ComposeUiTest.assertCancelWholeClassNotOffered() {
        if (countTagged("manage_class_options") > 0) {
            tag("manage_class_options").performClick()
            waitForIdle()
        }
        assertTextNotShown("Cancel whole class")
    }

    private fun status(world: Flow4Harness, d1: Flow4Example, student: User): MembershipStatus? =
        world.memberships.currentStatus(d1.coreBurn.id, student.id)
}
