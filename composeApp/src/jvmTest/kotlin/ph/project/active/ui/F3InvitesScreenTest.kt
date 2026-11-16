package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.textUnder
import ph.project.active.ui.testing.topOf

@OptIn(ExperimentalTestApi::class)
class F3InvitesScreenTest {
    @Test
    fun F3_AC12_c2ListsTwoInvitesNewestFirstWithNoDateAndTheCardBodyDoesNothing() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c2 = world.seedC2()
            world.signInAsPaolo()
            val nav = mountFlow3(world)
            openTab("invites")

            assertTextUnder("invites_title", "Invites")
            assertTextUnder("invites_subtitle", "Accept to join a class roster.")
            assertEquals(listOf(c2.mobilityFlow.id, c2.bootcamp.id), cardIdsTopToBottom("invites_card_"))

            val mobility = "invites_card_${c2.mobilityFlow.id}"
            assertTextUnder(mobility, "TRX Mobility Flow")
            assertTextUnder(mobility, "Coach Leo Ramirez invited you.")
            assertTextUnder(mobility, "Public")
            assertTextUnder(mobility, "Tue \u00B7 Thu")
            assertTextUnder(mobility, "7:00 PM \u2013 8:00 PM")
            val bootcamp = "invites_card_${c2.bootcamp.id}"
            assertTextUnder(bootcamp, "Coach Ana Cruz invited you.")
            assertTextUnder(bootcamp, "Public")
            assertTextUnder(bootcamp, "Sat")
            assertTextUnder(bootcamp, "8:00 AM \u2013 9:30 AM")
            for (card in listOf(mobility, bootcamp)) {
                assertFalse(DATE.containsMatchIn(textUnder(card)), "invite cards show no date:\n${textUnder(card)}")
            }

            for (trxClass in listOf(c2.mobilityFlow, c2.bootcamp)) {
                assertTextUnder("invites_decline_${trxClass.id}", "Decline")
                assertTextUnder("invites_accept_${trxClass.id}", "Accept")
                tag("invites_decline_${trxClass.id}").assertHeightIsAtLeast(48.dp)
                tag("invites_accept_${trxClass.id}").assertHeightIsAtLeast(48.dp)
            }
            tag("student_tab_invites").assertIsSelected()
            assertTextUnder("student_tab_invites_badge", "2")

            val before = Flow1Compose.screenRoutes(nav)
            tag(mobility).performClick()
            waitForIdle()
            assertEquals(before, Flow1Compose.screenRoutes(nav))
            tag(mobility).assertExists()
            tag("student_tab_invites").assertIsSelected()
        }
    }

    @Test
    fun F3_AC13_acceptRemovesTheCardShowsAToastForFourSecondsAndMovesTheClassToMyClasses() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c2 = world.seedC2()
            world.signInAsPaolo()
            mountFlow3(world)
            openTab("invites")

            tag("invites_accept_${c2.mobilityFlow.id}").performClick()
            waitForIdle()
            tag("invites_card_${c2.mobilityFlow.id}").assertDoesNotExist()
            assertTextUnder("invites_toast", "You joined TRX Mobility Flow. It\u2019s now in My classes.")
            tag("student_tab_invites").assertIsSelected()
            tag("invites_card_${c2.bootcamp.id}").assertExists()
            assertTextUnder("student_tab_invites_badge", "1")

            mainClock.advanceTimeBy(4_100)
            waitForIdle()
            tag("invites_toast").assertDoesNotExist()

            openTab("classes")
            val card = "student_class_card_${c2.mobilityFlow.id}"
            assertTrue(topOf(card) < topOf("student_public_classes_header"), "Mobility Flow is in My classes")
            assertTextUnder(card, "Joined")
        }
    }

    @Test
    fun F3_AC14_declineHasNoDialogRemovesTheCardAndTheLastDeclineShowsC4() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c2 = world.seedC2()
            world.signInAsPaolo()
            mountFlow3(world)
            openTab("invites")

            tag("invites_decline_${c2.mobilityFlow.id}").performClick()
            waitForIdle()
            assertTrue(onAllNodes(isDialog()).fetchSemanticsNodes(atLeastOneRootRequired = false).isEmpty())
            tag("invites_card_${c2.mobilityFlow.id}").assertDoesNotExist()
            assertTextUnder("invites_toast", "Invite declined")
            tag("invites_card_${c2.bootcamp.id}").assertExists()
            assertTextUnder("student_tab_invites_badge", "1")

            mainClock.advanceTimeBy(4_100)
            waitForIdle()
            tag("invites_toast").assertDoesNotExist()

            tag("invites_decline_${c2.bootcamp.id}").performClick()
            waitForIdle()
            assertTextUnder("invites_empty", "No invites yet")
            tag("student_tab_invites_badge").assertDoesNotExist()
        }
    }

    @Test
    fun F3_AC15_c4ShowsTheEmptyStateWithNoSubtitleAndNoBadge() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.signInAsPaolo()
            mountFlow3(world)
            openTab("invites")
            assertTextUnder("invites_title", "Invites")
            tag("invites_subtitle").assertDoesNotExist()
            assertTextUnder("invites_empty", "No invites yet")
            assertTextUnder(
                "invites_empty",
                "When a coach invites you to a class, it shows up here. You can accept or decline it.",
            )
            tag("student_tab_invites_badge").assertDoesNotExist()
            tag("student_tab_invites").assertIsSelected()
        }
    }

    @Test
    fun F3_AC16_offlineAcceptOrDeclineKeepsTheInviteAndShowsTheErrorWithNoToastOrRetry() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c2 = world.seedC2()
            world.signInAsPaolo()
            mountFlow3(world)
            openTab("invites")
            val mobility = c2.mobilityFlow.id
            val bootcamp = c2.bootcamp.id

            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            tag("invites_accept_$mobility").performClick()
            waitForIdle()
            assertOfflineFailure(mobility)
            tag("invites_decline_$bootcamp").performClick()
            waitForIdle()
            assertOfflineFailure(bootcamp)

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("invites_accept_$mobility").performClick()
            waitForIdle()
            tag("invites_form_error").assertDoesNotExist()
            tag("invites_card_$mobility").assertDoesNotExist()
            assertTextUnder("invites_toast", "You joined TRX Mobility Flow. It\u2019s now in My classes.")
            mainClock.advanceTimeBy(4_100)
            waitForIdle()

            world.network.set(NetworkStatus.OFFLINE)
            waitForIdle()
            tag("invites_decline_$bootcamp").performClick()
            waitForIdle()
            assertOfflineFailure(bootcamp)

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("invites_decline_$bootcamp").performClick()
            waitForIdle()
            tag("invites_form_error").assertDoesNotExist()
            assertTextUnder("invites_toast", "Invite declined")
        }
    }

    @Test
    fun F3_AC17_aPrivateInviteShowsThePrivateChipAndTheSameActions() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val mara = world.coach("Mara Santos")
            val privateClass = world.saveClass(mara, "Private Strength", visibility = Visibility.PRIVATE)
            world.invite(privateClass.id)
            world.signInAsPaolo()
            mountFlow3(world)
            openTab("invites")
            val card = "invites_card_${privateClass.id}"
            assertTextUnder(card, "Private")
            assertTextUnder(card, "Coach Mara Santos invited you.")
            assertTextUnder("invites_accept_${privateClass.id}", "Accept")
            assertTextUnder("invites_decline_${privateClass.id}", "Decline")
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.assertOfflineFailure(classId: String) {
        tag("invites_card_$classId").assertExists()
        assertTextUnder("invites_form_error", "You\u2019re offline. Nothing was saved.")
        tag("invites_toast").assertDoesNotExist()
        val retry = onAllNodesWithText("Retry", substring = true, ignoreCase = true, useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertTrue(retry.isEmpty(), "a failed invite action has no Retry button")
    }
}

/** A Figma-style invite date such as "Sun 27 Sep". */
private val DATE = Regex("\\b(Mon|Tue|Wed|Thu|Fri|Sat|Sun) \\d{1,2} (Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\b")
