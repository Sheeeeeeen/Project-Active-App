package ph.project.active.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.TimeSlot
import ph.project.active.ui.testing.Flow2Harness
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowClassDetail
import ph.project.active.ui.testing.ShowClassDetailState
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F2ClassDetailScreenTest {
    @Test
    fun F2_AC10_guestDetailShowsThePublicClassAndRequestFootnote() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            setContent { ShowClassDetail(world.detail(trxClass.id)) }
            tag("class_detail_chip").assertTextContains("Public")
            tag("class_detail_name").assertTextContains("TRX Core Burn")
            tag("class_detail_name").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            tag("class_detail_days").assertTextContains("Monday and Wednesday")
            tag("class_detail_time").assertTextContains("5:30 PM \u2013 6:30 PM")
            tag("class_detail_runs").assertTextContains("Until Sat 31 Oct 2026")
            tag("class_detail_request_button").assertTextContains("Request to join")
            tag("class_detail_footnote").assertTextContains(
                "You\u2019ll create a student account first. Your request is sent after the account exists.",
            )
            onNodeWithText("Description").assertDoesNotExist()
            assertTrue(onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).fetchSemanticsNodes().isEmpty())
        }
    }

    @Test
    fun F2_AC11_noEndDateAndDifferentDayTimesGetTheirOwnLines() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val open = world.coreBurn(endDate = null)
            setContent { ShowClassDetail(world.detail(open.id)) }
            tag("class_detail_runs").assertTextContains("No end date")
            val morning = TimeSlot(LocalTime(7, 0), LocalTime(8, 0))
            val split = world.saveClass(
                name = "Split Times",
                slots = mapOf(
                    DayOfWeek.MONDAY to listOf(TimeSlot(LocalTime(17, 30), LocalTime(18, 30))),
                    DayOfWeek.WEDNESDAY to listOf(morning),
                ),
            )
            setContent { ShowClassDetail(world.detail(split.id)) }
            tag("class_detail_time").assertTextContains("Mon 5:30 PM \u2013 6:30 PM")
            tag("class_detail_time").assertTextContains("Wed 7:00 AM \u2013 8:00 AM")
        }
    }

    @Test
    fun F2_AC12_studentWhoHasNotRequestedSeesTheCoachFootnote() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            setContent { ShowClassDetail(world.detail(trxClass.id, student)) }
            tag("class_detail_request_button").assertTextContains("Request to join")
            tag("class_detail_footnote").assertTextContains("Your request goes to Coach Mara Santos for approval.")
        }
    }

    @Test
    fun F2_AC13_studentRequestShowsPendingWithoutARequestedLine() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            val viewModel = world.detail(trxClass.id, student)
            setContent { ShowClassDetail(viewModel) }
            tag("class_detail_request_button").performClick()
            tag("class_detail_chip").assertTextContains("Pending")
            tag("class_detail_status_title").assertTextContains("Awaiting approval")
            tag("class_detail_status_title").assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            tag("class_detail_status_body").assertTextContains(
                "Coach Mara Santos will approve or decline your request. You don\u2019t need to send it again.",
            )
            tag("class_detail_request_button").assertDoesNotExist()
            onNodeWithText("Requested", substring = true).assertDoesNotExist()
            val pending = world.memberships.forClass(trxClass.id).filter { it.status == MembershipStatus.PENDING_REQUEST }
            assertEquals(1, pending.size)
        }
    }

    @Test
    fun F2_AC14_twoQuickTapsCreateOneMembership() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            val viewModel = world.detail(trxClass.id, student)
            setContent { ShowClassDetail(viewModel) }
            val button = tag("class_detail_request_button")
            val click = button.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action
            checkNotNull(click).invoke()
            checkNotNull(click).invoke()
            waitForIdle()
            assertEquals(1, world.memberships.forClass(trxClass.id).size)
        }
    }

    @Test
    fun F2_AC15_sendingDisablesTheRequestButtonAndShowsASpinner() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val ready = world.detail(trxClass.id).state.value
            val sending = ready.copy(isSending = true)
            setContent { ShowClassDetailState(sending) }
            tag("class_detail_request_progress").assertExists()
            tag("class_detail_request_button").assertIsNotEnabled()
        }
    }

    @Test
    fun F2_AC16_refreshAfterApprovalShowsJoined() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            world.request(trxClass.id, student.id)
            world.approve(trxClass.id, student.id)
            val viewModel = world.detail(trxClass.id, student)
            setContent { ShowClassDetail(viewModel) }
            viewModel.refresh()
            waitForIdle()
            tag("class_detail_chip").assertTextContains("Joined")
            tag("class_detail_status_title").assertTextContains("You\u2019re on this class")
            tag("class_detail_status_body").assertTextContains(
                "Record attendance from the Attend tab during each session. Your dates are in Sessions.",
            )
            tag("class_detail_request_button").assertDoesNotExist()
        }
    }

    @Test
    fun F2_AC17_declineOrRemovalShowsRequestAgain() = Flow2Ui.run {
        Flow2Harness().use { world ->
            assertRequestAgain(world, decline = true)
        }
        Flow2Harness().use { world ->
            assertRequestAgain(world, decline = false)
        }
    }

    private suspend fun androidx.compose.ui.test.ComposeUiTest.assertRequestAgain(world: Flow2Harness, decline: Boolean) {
        val trxClass = world.coreBurn()
        val student = world.account(world.student())
        world.request(trxClass.id, student.id)
        if (decline) {
            world.decline(trxClass.id, student.id)
        } else {
            world.approve(trxClass.id, student.id)
            world.remove(trxClass.id, student.id)
        }
        val viewModel = world.detail(trxClass.id, student)
        setContent { ShowClassDetail(viewModel) }
        viewModel.refresh()
        waitForIdle()
        tag("class_detail_callout").assertTextContains("You can request again")
        tag("class_detail_callout").assertTextContains(
            "You can send a new request. The coach will need to approve it again.",
        )
        tag("class_detail_request_button").assertTextContains("Request to join")
    }
}
