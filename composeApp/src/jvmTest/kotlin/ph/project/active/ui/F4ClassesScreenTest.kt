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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.coachclasses.spokenCard
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertLineUnder
import ph.project.active.ui.testing.assertNothingClickableUnder
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.clickText
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.mergedTag
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOf

private const val CALLOUT_TITLE = "Private classes are invite-only"
private const val CALLOUT_BODY = "Guests and students only see your public classes."

@OptIn(ExperimentalTestApi::class)
class F4ClassesScreenTest {
    @Test
    fun F4_AC1_d1ShowsTheCoachsFiveClassesInCreationOrder() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            mountFlow4(world)

            tag("coach_home").assertExists()
            assertTextUnder("coach_classes_title", "Classes")
            assertTextUnder("coach_classes_subtitle", "Coach Mara Santos")
            assertTextUnder("coach_classes_header", "YOUR CLASSES")
            assertTextUnder("coach_classes_count", "5 classes")
            assertEquals(d1.classIds, cardIdsTopToBottom("coach_class_card_"))

            val coreBurn = "coach_class_card_${d1.coreBurn.id}"
            assertTextUnder(coreBurn, "TRX Core Burn")
            assertTextUnder(coreBurn, "Public")
            assertTextUnder(coreBurn, "Mon \u00B7 Wed")
            assertTextUnder(coreBurn, "5:30 PM \u2013 6:30 PM")
            assertLineUnder(coreBurn, "4 students \u00B7 3 requests")

            val lunch = "coach_class_card_${d1.lunchExpress.id}"
            assertTextUnder(lunch, "Lunch Express TRX")
            assertTextUnder(lunch, "Public")
            assertTextUnder(lunch, "Mon \u00B7 Tue \u00B7 Thu")
            assertTextUnder(lunch, "12:15 PM \u2013 12:45 PM")
            assertLineUnder(lunch, "8 students")
            assertNoTextUnder(lunch, "request")

            val sunrise = "coach_class_card_${d1.sunrise.id}"
            assertTextUnder(sunrise, "Sunrise TRX Strength")
            assertTextUnder(sunrise, "Public")
            assertTextUnder(sunrise, "Mon \u00B7 Wed \u00B7 Fri")
            assertTextUnder(sunrise, "6:00 AM \u2013 6:45 AM")
            assertLineUnder(sunrise, "12 students")

            val private = "coach_class_card_${d1.privateClass.id}"
            assertTextUnder(private, "Private")
            assertLineUnder(private, "0 students")

            val cancelled = "coach_class_card_${d1.cancelledClass.id}"
            assertTextUnder(cancelled, "Cancelled")

            assertTextUnder("coach_classes_callout", CALLOUT_TITLE)
            assertTextUnder("coach_classes_callout", CALLOUT_BODY)
            assertTextUnder("coach_tab_classes", "Classes")
            assertTextUnder("coach_tab_qr", "QR")
            assertTextUnder("coach_tab_attendance", "Attendance")
            tag("coach_tab_classes").assertIsSelected()
            val spinners = onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
            assertTrue(spinners.isEmpty(), "D1 has no loading spinner")
        }
    }

    @Test
    fun F4_AC1_aCancelledCardHidesThePublicPrivateChip() = runFlow4UiTest {
        // Spec section 9, item 10: the Cancelled chip replaces the Public / Private chip.
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            mountFlow4(world)
            val cancelled = "coach_class_card_${d1.cancelledClass.id}"
            assertNoTextUnder(cancelled, "Public")
            assertNoTextUnder(cancelled, "Private")
        }
    }

    @Test
    fun F4_AC2_theCountReadsOneClassOrFiveClasses() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.saveClass("TRX Core Burn")
            world.signInAsMara()
            mountFlow4(world)
            assertTextUnder("coach_classes_count", "1 class")
            assertNoTextUnder("coach_classes_count", "1 classes")
        }
        Flow4Harness().use { world ->
            world.seedD1()
            world.signInAsMara()
            mountFlow4(world)
            assertTextUnder("coach_classes_count", "5 classes")
        }
    }

    @Test
    fun F4_AC3_withNoClassesD2ShowsTheEmptyStateAndOpensCreate() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            val nav = mountFlow4(world)

            assertTextUnder("coach_classes_empty", "No classes yet")
            assertTextUnder(
                "coach_classes_empty",
                "Create your first class. Public classes appear in the class list for everyone.",
            )
            assertTextUnder("coach_classes_empty_create", "Create class")
            tag("coach_classes_header").assertDoesNotExist()
            tag("coach_classes_count").assertDoesNotExist()
            tag("coach_classes_callout").assertDoesNotExist()
            tag("coach_tab_classes").assertExists()
            tag("coach_tab_qr").assertExists()
            tag("coach_tab_attendance").assertExists()

            tag("coach_classes_empty_create").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav))
            tag("create_class_cancel").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))

            tag("coach_classes_add").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav))
        }
    }

    @Test
    fun F4_AC4_thePrivateCalloutShowsOnlyWithAPrivateClassAndCannotBeDismissed() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.saveClass("TRX Core Burn")
            val cancelled = world.saveClass("TRX Saturday Express", listOf(DayOfWeek.SATURDAY))
            world.cancel(cancelled.id)
            world.signInAsMara()
            mountFlow4(world)
            tag("coach_classes_callout").assertDoesNotExist()

            world.saveClass("TRX Private Strength", visibility = Visibility.PRIVATE)
            openCoachTab("qr")
            openCoachTab("classes")

            assertTextUnder("coach_classes_callout", CALLOUT_TITLE)
            assertTextUnder("coach_classes_callout", CALLOUT_BODY)
            assertNothingClickableUnder("coach_classes_callout")
        }
    }

    @Test
    fun F4_AC5_endedAndCancelledClassesStayOnTheListAndOpenManage() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val ended = world.saveClass("TRX Two Week Trial", endDate = LocalDate(2026, 9, 30))
            val cancelled = world.saveClass("TRX Saturday Express", listOf(DayOfWeek.SATURDAY))
            world.cancel(cancelled.id)
            world.setToday(LocalDate(2026, 10, 5))
            world.signInAsMara()
            val nav = mountFlow4(world)

            assertEquals(listOf(ended.id, cancelled.id), cardIdsTopToBottom("coach_class_card_"))
            assertTextUnder("coach_class_card_${cancelled.id}", "Cancelled")
            assertNoTextUnder("coach_class_card_${ended.id}", "Ended") // section 10 Q14: no Ended chip

            openCoachCard(ended.id)
            assertEquals(listOf("CoachHome", "ManageClass(${ended.id})"), coachRoutes(nav))
            tag("manage_class_back").performClick()
            waitForIdle()

            openCoachCard(cancelled.id)
            assertEquals(listOf("CoachHome", "ManageClass(${cancelled.id})"), coachRoutes(nav))
        }
    }

    @Test
    fun F4_AC6_offlineD1ShowsTheBannerUnderTheTitleAndStillOpensManageAndCreate() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.network.set(NetworkStatus.OFFLINE)
            world.signInAsMara()
            val nav = mountFlow4(world)

            tag("offline_banner").assertExists()
            assertTrue(topOf("offline_banner") > topOf("coach_classes_title"), "the banner sits under the title")
            assertEquals(d1.classIds, cardIdsTopToBottom("coach_class_card_"))

            openCoachCard(d1.coreBurn.id)
            assertEquals(listOf("CoachHome", "ManageClass(${d1.coreBurn.id})"), coachRoutes(nav))
            tag("manage_class_back").performClick()
            waitForIdle()

            tag("coach_classes_add").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav))
        }
    }

    @Test
    fun F4_AC7_selectingTheClassesTabRefreshesTheList() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            mountFlow4(world)
            assertTextUnder("coach_classes_count", "5 classes")

            val added = world.saveClass("TRX Evening Stretch", listOf(DayOfWeek.FRIDAY))
            openCoachTab("qr")
            openCoachTab("classes")

            assertTextUnder("coach_classes_count", "6 classes")
            assertEquals(d1.classIds + added.id, cardIdsTopToBottom("coach_class_card_"))
        }
    }

    @Test
    fun F4_AC7_returningFromCancelWholeClassShowsCoreBurnCancelled() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)
            val card = "coach_class_card_${d1.coreBurn.id}"
            assertNoTextUnder(card, "Cancelled")

            openCoachCard(d1.coreBurn.id)
            tag("manage_class_options").performClick()
            waitForIdle()
            clickText("Cancel whole class")
            tag("manage_dialog_confirm").performClick()
            waitForIdle()

            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            assertTextUnder(card, "Cancelled")
        }
    }

    @Test
    fun F4_AC9_theAccountIconIsTaggedAndDoesNothing() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)

            tag("coach_classes_account").assertExists()
            assertEquals(1, countTaggedUnder("coach_classes_account", "placeholder_tag"))
            tag("coach_classes_account").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            tag("coach_tab_classes").assertIsSelected()

            // Attendance: the account button's tag plus the placeholder's own (F4-AC25). Flow 5 (F5-AC19):
            // the QR tab is the real Daily QR now, with no account button and no placeholder tag.
            openCoachTab("qr")
            assertEquals(0, countTagged("placeholder_tag"))
            openCoachTab("attendance")
            assertEquals(2, countTagged("placeholder_tag"))
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
        }
    }

    @Test
    fun F4_AC10_eachCardIsOneSpokenButtonAtLeast48dpHigh() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            mountFlow4(world)
            val items = world.coachClasses(world.maraAccount()).state.value.classes
            for (item in items) {
                val card = "coach_class_card_${item.classId}"
                assertEquals(1, onAllNodesWithTag(card).fetchSemanticsNodes().size, "$card is one merged node")
                mergedTag(card).assertContentDescriptionEquals(item.spokenCard())
                mergedTag(card).assertHeightIsAtLeast(48.dp)
                mergedTag(card).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            }
            assertEquals(d1.classIds, items.map { it.classId })
            // Section 10 Q8: exact spoken text for two seeded cards.
            mergedTag("coach_class_card_${d1.coreBurn.id}").assertContentDescriptionEquals(
                "TRX Core Burn, Public, Monday and Wednesday, 5:30 PM to 6:30 PM, 4 students, 3 requests",
            )
            mergedTag("coach_class_card_${d1.privateClass.id}").assertContentDescriptionEquals(
                "TRX Private Strength, Private, Tuesday and Thursday, 7:00 PM to 8:00 PM, 0 students",
            )
        }
    }
}
