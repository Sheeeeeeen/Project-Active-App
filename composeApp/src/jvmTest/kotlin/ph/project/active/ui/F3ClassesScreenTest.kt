package ph.project.active.ui

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.mergedTag
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOf

@OptIn(ExperimentalTestApi::class)
class F3ClassesScreenTest {
    @Test
    fun F3_AC1_c1ShowsJoinedCardsBySoonestSessionThenPublicClassesInCreationOrder() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)

            tag("student_home").assertExists()
            assertTextUnder("student_classes_title", "Classes")
            assertTextUnder("student_classes_greeting", "Hi Paolo \u00B7 EMP-20417")
            assertTextUnder("student_my_classes_header", "MY CLASSES")
            assertTextUnder("student_my_classes_count", "3 joined")
            assertTextUnder("student_public_classes_header", "Public classes")

            val publicTop = topOf("student_public_classes_header")
            val cards = cardIdsTopToBottom("student_class_card_")
            val myCards = cards.filter { topOf("student_class_card_$it") < publicTop }
            val publicCards = cards.filter { topOf("student_class_card_$it") > publicTop }
            assertEquals(listOf(c1.sunrise.id, c1.lunchExpress.id, c1.coreBurn.id), myCards)
            assertEquals(listOf(c1.mobilityFlow.id, c1.everyDay20.id, c1.bootcamp.id), publicCards)

            val sunrise = "student_class_card_${c1.sunrise.id}"
            assertTextUnder(sunrise, "Sunrise TRX Strength")
            assertTextUnder(sunrise, "Mon \u00B7 Wed \u00B7 Fri")
            assertTextUnder(sunrise, "6:00 AM \u2013 6:45 AM")
            val lunch = "student_class_card_${c1.lunchExpress.id}"
            assertTextUnder(lunch, "Mon \u00B7 Tue \u00B7 Thu")
            assertTextUnder(lunch, "12:15 PM \u2013 12:45 PM")
            val core = "student_class_card_${c1.coreBurn.id}"
            assertTextUnder(core, "Mon \u00B7 Wed")
            assertTextUnder(core, "5:30 PM \u2013 6:30 PM")
            for (card in listOf(sunrise, lunch, core)) {
                assertTextUnder(card, "Joined")
                assertTextUnder(card, "Coach Mara Santos")
            }

            val mobility = "student_class_card_${c1.mobilityFlow.id}"
            assertTextUnder(mobility, "Invited")
            assertTextUnder(mobility, "Coach Leo Ramirez")
            assertTextUnder(mobility, "Tue \u00B7 Thu")
            assertTextUnder(mobility, "7:00 PM \u2013 8:00 PM")
            val everyDay = "student_class_card_${c1.everyDay20.id}"
            assertTextUnder(everyDay, "Coach Jun Tan")
            assertTextUnder(everyDay, "Every day")
            for (chip in listOf("Joined", "Invited", "Pending")) assertNoTextUnder(everyDay, chip)
            val bootcamp = "student_class_card_${c1.bootcamp.id}"
            assertTextUnder(bootcamp, "Pending")
            assertTextUnder(bootcamp, "Coach Ana Cruz")
            assertTextUnder(bootcamp, "Sat")
            assertTextUnder(bootcamp, "8:00 AM \u2013 9:30 AM")

            val requests = onAllNodesWithText("Requests", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
            assertTrue(requests.isEmpty(), "flow 3 has no Requests section or tab")
            assertEquals(0, countTagged("student_tab_requests"))
            for (name in listOf("classes", "attend", "sessions", "invites")) tag("student_tab_$name").assertExists()
            tag("student_tab_classes").assertIsSelected()
            assertTextUnder("student_tab_invites_badge", "1")

            val spinners = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
            assertTrue(spinners.isEmpty(), "C1 has no loading spinner")
        }
    }

    @Test
    fun F3_AC2_countReadsOneJoinedOrNJoined() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val mara = world.coach("Mara Santos")
            val only = world.saveClass(mara, "TRX Core Burn")
            world.join(only.id)
            world.signInAsPaolo()
            mountFlow3(world)
            assertTextUnder("student_my_classes_count", "1 joined")
        }
        Flow3Harness().use { world ->
            world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)
            assertTextUnder("student_my_classes_count", "3 joined")
        }
    }

    @Test
    fun F3_AC3_anEmptySectionHidesItsHeaderAndCountAndTheOtherSectionStays() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val mara = world.coach("Mara Santos")
            val open = world.saveClass(mara, "TRX Core Burn")
            world.signInAsPaolo()
            mountFlow3(world)
            tag("student_my_classes_header").assertDoesNotExist()
            tag("student_my_classes_count").assertDoesNotExist()
            tag("student_public_classes_header").assertExists()
            tag("student_class_card_${open.id}").assertExists()
        }
        Flow3Harness().use { world ->
            val mara = world.coach("Mara Santos")
            val privateJoined = world.saveClass(mara, "Private Strength", visibility = Visibility.PRIVATE)
            world.join(privateJoined.id)
            world.signInAsPaolo()
            mountFlow3(world)
            tag("student_public_classes_header").assertDoesNotExist()
            tag("student_my_classes_header").assertExists()
            tag("student_class_card_${privateJoined.id}").assertExists()
        }
    }

    @Test
    fun F3_AC4_noJoinedAndNoPublicClassesShowsTheEmptyStateAndKeepsTheTabBar() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.signInAsPaolo()
            mountFlow3(world)
            assertTextUnder("student_classes_empty", "No classes yet")
            assertTextUnder(
                "student_classes_empty",
                "When you join a class, or when a coach creates a public class, it shows up here.",
            )
            tag("student_my_classes_header").assertDoesNotExist()
            tag("student_my_classes_count").assertDoesNotExist()
            tag("student_public_classes_header").assertDoesNotExist()
            tag("student_tab_classes").assertExists()
            tag("student_tab_invites").assertExists()
        }
    }

    @Test
    fun F3_AC5_privateCancelledAndEndedPublicClassesStayOffAndAnEndedJoinedClassStays() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val mara = world.coach("Mara Santos")
            world.setToday(LocalDate(2026, 9, 25))
            val sunday = LocalDate(2026, 9, 27)
            val privateOther = world.saveClass(mara, "Private Other", visibility = Visibility.PRIVATE)
            val cancelledJoined = world.saveClass(mara, "Cancelled Joined")
            val cancelledOpen = world.saveClass(mara, "Cancelled Open")
            val endedYesterday = world.saveClass(mara, "Ended Yesterday", endDate = sunday)
            val endsToday = world.saveClass(mara, "Ends Today", endDate = LocalDate(2026, 9, 28))
            val endedJoined = world.saveClass(mara, "Ended Joined", endDate = sunday)
            world.join(cancelledJoined.id)
            world.join(endedJoined.id)
            world.cancel(cancelledJoined.id)
            world.cancel(cancelledOpen.id)
            world.setToday(LocalDate(2026, 9, 28))
            world.signInAsPaolo()
            val nav = mountFlow3(world)

            tag("student_class_card_${privateOther.id}").assertDoesNotExist()
            tag("student_class_card_${cancelledJoined.id}").assertDoesNotExist()
            tag("student_class_card_${cancelledOpen.id}").assertDoesNotExist()
            tag("student_class_card_${endedYesterday.id}").assertDoesNotExist()
            tag("student_class_card_${endsToday.id}").assertExists()
            val endedCard = "student_class_card_${endedJoined.id}"
            assertTrue(topOf(endedCard) < topOf("student_public_classes_header"), "an ended Joined class stays in My classes")

            tag(endedCard).performClick()
            waitForIdle()
            assertEquals("ClassDetail(${endedJoined.id})", Flow1Compose.screenRoutes(nav).last())
            tag("class_detail_status_body").assertExists()
            assertShown("This class ended on Sun 27 Sep 2026.")
        }
    }

    @Test
    fun F3_AC6_offlineShowsTheBannerUnderTheTitleAndCardsStillOpen() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            world.network.set(NetworkStatus.OFFLINE)
            world.signInAsPaolo()
            val nav = mountFlow3(world)
            assertTextUnder("offline_banner", "No internet connection")
            assertTrue(topOf("offline_banner") > topOf("student_classes_title"), "the banner sits under the title")
            tag("student_class_card_${c1.sunrise.id}").assertExists()
            tag("student_class_card_${c1.everyDay20.id}").assertExists()
            tag("student_class_card_${c1.everyDay20.id}").performClick()
            waitForIdle()
            assertEquals("ClassDetail(${c1.everyDay20.id})", Flow1Compose.screenRoutes(nav).last())
        }
    }

    @Test
    fun F3_AC7_showingTheClassesTabAgainReloadsMyClasses() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)
            world.accept(c1.mobilityFlow.id)
            openTab("attend")
            openTab("classes")

            val mobility = "student_class_card_${c1.mobilityFlow.id}"
            assertTrue(topOf(mobility) < topOf("student_public_classes_header"), "Mobility Flow moved to My classes")
            assertTextUnder(mobility, "Joined")
            assertTextUnder("student_my_classes_count", "4 joined")
            assertEquals(1, countTagged(mobility))
        }
    }

    @Test
    fun F3_AC10_eachCardIsOneSpokenButtonAtLeast48dpHigh() = runFlow3UiTest {
        Flow3Harness().use { world ->
            val c1 = world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)
            val sunrise = "student_class_card_${c1.sunrise.id}"
            mergedTag(sunrise).assertContentDescriptionEquals(
                "Sunrise TRX Strength, Coach Mara Santos, Monday, Wednesday and Friday, 6:00 AM to 6:45 AM",
            )
            val classes = listOf(c1.coreBurn, c1.lunchExpress, c1.mobilityFlow, c1.everyDay20, c1.bootcamp, c1.sunrise)
            for (trxClass in classes) {
                val card = "student_class_card_${trxClass.id}"
                assertEquals(1, onAllNodesWithTag(card).fetchSemanticsNodes().size, "$card is one merged node")
                mergedTag(card).assertHeightIsAtLeast(48.dp)
                mergedTag(card).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            }
        }
    }

    @Test
    fun F3_AC11_theAccountIconDoesNothingOnClassesAndInvites() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.seedC1()
            world.signInAsPaolo()
            val nav = mountFlow3(world)
            val before = Flow1Compose.screenRoutes(nav)

            tag("student_classes_account").assertExists()
            tag("student_classes_account").performClick()
            waitForIdle()
            assertEquals(before, Flow1Compose.screenRoutes(nav))
            tag("student_tab_classes").assertIsSelected()

            openTab("invites")
            tag("invites_account").assertExists()
            tag("invites_account").performClick()
            waitForIdle()
            assertEquals(before, Flow1Compose.screenRoutes(nav))
            tag("student_tab_invites").assertIsSelected()
        }
    }
}
