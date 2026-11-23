package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.User
import ph.project.active.domain.repository.currentStatus
import ph.project.active.ui.testing.Flow4Example
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertNoUndo
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openManageTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOfText

@OptIn(ExperimentalTestApi::class)
class F4ManageRequestsRosterTest {
    @Test
    fun F4_AC19_requestsListsThreeRowsAndApproveOrDeclineActWithoutADialog() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openCoreBurn(world, d1)
            openManageTab("requests")

            assertTextShown("WAITING FOR YOU")
            assertTextShown("3 requests")
            assertEquals(listOf(d1.ella.id, d1.bea.id, d1.carlo.id), cardIdsTopToBottom("manage_approve_"))
            for (student in listOf(d1.ella, d1.bea, d1.carlo)) {
                assertTextShown(student.name)
                assertTextShown(student.employeeId!!)
                assertTextUnder("manage_decline_${student.id}", "Decline")
                assertTextUnder("manage_approve_${student.id}", "Approve")
            }
            assertTextShown("Approve adds the student to the roster. Decline leaves them off; they can request again.")
            assertTextNotShown("Requested", substring = true)

            tag("manage_approve_${d1.ella.id}").performClick()
            waitForIdle()
            assertNoDialog()
            tag("manage_approve_${d1.ella.id}").assertDoesNotExist()
            assertEquals(MembershipStatus.ACTIVE, status(world, d1, d1.ella))

            tag("manage_decline_${d1.bea.id}").performClick()
            waitForIdle()
            assertNoDialog()
            tag("manage_decline_${d1.bea.id}").assertDoesNotExist()
            assertEquals(MembershipStatus.DECLINED, status(world, d1, d1.bea))
            assertTextUnder("manage_tab_requests_badge", "1")

            openManageTab("roster")
            tag("manage_remove_${d1.ella.id}").assertExists()
            tag("manage_remove_${d1.bea.id}").assertDoesNotExist()
        }
    }

    @Test
    fun F4_AC19_noRequestsShowsTheD11EmptyState() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openCoachClass(world, d1.sunrise.id)
            tag("manage_tab_requests_badge").assertDoesNotExist()
            openManageTab("requests")
            assertTextUnder("manage_requests_empty", "No requests")
            assertTextUnder("manage_requests_empty", "Students who request this class will appear here.")
        }
    }

    @Test
    fun F4_AC20_rosterListsFourStudentsInJoinOrderWithTheirJoinPath() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openCoreBurn(world, d1)
            openManageTab("roster")

            assertTextShown("ON THE ROSTER")
            assertTextShown("4 students")
            val roster = listOf(d1.paolo, d1.dana, d1.gia, d1.hana)
            assertEquals(roster.map { it.id }, cardIdsTopToBottom("manage_remove_"))
            for (student in roster) {
                assertTextShown(student.employeeId!!)
                assertTextUnder("manage_remove_${student.id}", "Remove")
            }
            // Name, then its join-path line, for each student top to bottom.
            val lines = listOf(
                "Paolo Reyes" to "Joined by request",
                "Dana Uy" to "Joined by invite",
                "Gia Torres" to "Joined by invite",
                "Hana Cho" to "Joined by request",
            )
            val nameTops = lines.map { (name, _) -> topOfText(name) }
            assertEquals(nameTops.sorted(), nameTops, "roster is in join order")
            for ((index, line) in lines.withIndex()) {
                val joinTops = joinLineTops(line.second)
                val below = nameTops.getOrElse(index + 1) { Float.MAX_VALUE }
                assertTrue(
                    joinTops.any { it >= nameTops[index] && it < below },
                    "${line.first} shows \"${line.second}\"",
                )
            }
            assertTextShown("A removed student is taken off this class only and can request to join again.")
        }
    }

    @Test
    fun F4_AC20_removeConfirmsKeepsOrRemovesAndPastAttendanceStays() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            val pastRecord = world.recordAttendance(d1.coreBurn.id, d1.gia, LocalDate(2026, 9, 28))
            world.setToday(LocalDate(2026, 10, 5))
            openCoreBurn(world, d1)
            openManageTab("roster")

            tag("manage_remove_${d1.gia.id}").performClick()
            waitForIdle()
            assertTextUnder("manage_dialog", "Remove Gia Torres?")
            assertTextUnder(
                "manage_dialog",
                "Gia will be taken off TRX Core Burn and can\u2019t record attendance for it. " +
                    "They can request to join again. Past attendance stays as recorded.",
            )
            assertTextUnder("manage_dialog_keep", "Keep")
            assertTextUnder("manage_dialog_confirm", "Remove")

            tag("manage_dialog_keep").performClick()
            waitForIdle()
            tag("manage_dialog").assertDoesNotExist()
            assertEquals(MembershipStatus.ACTIVE, status(world, d1, d1.gia))
            assertTextShown("4 students")

            tag("manage_remove_${d1.gia.id}").performClick()
            waitForIdle()
            tag("manage_dialog_confirm").performClick()
            waitForIdle()

            tag("manage_dialog").assertDoesNotExist()
            tag("manage_remove_${d1.gia.id}").assertDoesNotExist()
            assertEquals(MembershipStatus.REMOVED, status(world, d1, d1.gia))
            assertTextShown("3 students")
            assertEquals(pastRecord, world.attendance.find(pastRecord.sessionId, d1.gia.id))
            assertNoUndo()
        }
    }

    private fun ComposeUiTest.openCoreBurn(world: Flow4Harness, d1: Flow4Example) = openCoachClass(world, d1.coreBurn.id)

    private fun ComposeUiTest.openCoachClass(world: Flow4Harness, classId: String) {
        world.signInAsMara()
        mountFlow4(world)
        openCoachCard(classId)
    }

    private fun ComposeUiTest.assertNoDialog() {
        tag("manage_dialog").assertDoesNotExist()
        assertTrue(onAllNodes(isDialog()).fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty(), "no dialog")
    }

    private fun ComposeUiTest.joinLineTops(text: String): List<Float> =
        onAllNodes(androidx.compose.ui.test.hasText(text), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .map { it.boundsInRoot.top }

    private fun status(world: Flow4Harness, d1: Flow4Example, student: User): MembershipStatus? =
        world.memberships.currentStatus(d1.coreBurn.id, student.id)
}
