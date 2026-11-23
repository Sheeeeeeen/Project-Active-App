package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.repository.currentStatus
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F3ClassesNavTest {
    @Test
    fun F3_AC8_aMyClassesCardOpensJoinedDetailOnceWithNoTabBarAndBackReturnsToC1() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            world.signInAsPaolo()
            val nav = mountFlow3(world)

            val card = tag("student_class_card_${c1.sunrise.id}").fetchSemanticsNode()
            val click = checkNotNull(card.config[SemanticsActions.OnClick].action)
            click()
            click()
            waitForIdle()

            assertEquals(
                listOf("StudentHome(null)", "ClassDetail(${c1.sunrise.id})"),
                Flow1Compose.screenRoutes(nav),
            )
            tag("class_detail_chip").assertTextContains("Joined")
            tag("class_detail_status_title").assertTextContains("You\u2019re on this class")
            assertNoTabBar()

            tag("class_detail_back").assertTextContains("Classes")
            tag("class_detail_back").performClick()
            waitForIdle()
            assertEquals(listOf("StudentHome(null)"), Flow1Compose.screenRoutes(nav))
            tag("student_home").assertExists()
            tag("student_tab_classes").assertIsSelected()
        }
    }

    @Test
    fun F3_AC9_publicCardsOpenRequestPendingAndInvitedDetailAndADeclinedCardCanRequestAgain() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)

            openCard(c1.everyDay20.id)
            tag("class_detail_request_button").assertTextContains("Request to join")
            assertNoTabBar()
            back()

            openCard(c1.bootcamp.id)
            tag("class_detail_status_title").assertTextContains("Awaiting approval")
            tag("class_detail_request_button").assertDoesNotExist()
            back()

            openCard(c1.mobilityFlow.id)
            tag("class_detail_chip").assertTextContains("Invited")
            tag("class_detail_status_body").assertTextContains(
                "Coach Leo Ramirez invited you. Accept or decline it in Invites.",
            )
            tag("class_detail_request_button").assertDoesNotExist()
            back()

            world.declineRequest(c1.bootcamp.id)
            openTab("attend")
            openTab("classes")
            val bootcampCard = "student_class_card_${c1.bootcamp.id}"
            for (chip in listOf("Pending", "Joined", "Invited")) assertNoTextUnder(bootcampCard, chip)

            openCard(c1.bootcamp.id)
            // A5 renders its decision 17 text in the flow 2 callout (F2-AC17, F2ClassDetailScreenTest).
            tag("class_detail_callout").assertTextContains(
                "You can send a new request. The coach will need to approve it again.",
            )
            tag("class_detail_request_button").assertTextContains("Request to join")
            tag("class_detail_request_button").performClick()
            waitForIdle()
            tag("class_detail_status_title").assertTextContains("Awaiting approval")
            assertEquals(
                MembershipStatus.PENDING_REQUEST,
                world.memberships.currentStatus(c1.bootcamp.id, world.paolo().id),
            )
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.openCard(classId: String) {
        tag("student_class_card_$classId").performClick()
        waitForIdle()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.back() {
        tag("class_detail_back").performClick()
        waitForIdle()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.assertNoTabBar() {
        for (name in listOf("classes", "attend", "sessions", "invites")) {
            assertEquals(0, countTagged("student_tab_$name"), "class detail has no tab bar")
        }
    }
}
