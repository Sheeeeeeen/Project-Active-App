package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.countTagPrefixUnder
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.hasTestTagPrefix
import ph.project.active.ui.testing.openCoachAttendance
import ph.project.active.ui.testing.rowTag
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.sectionTag
import ph.project.active.ui.testing.seedD19
import ph.project.active.ui.testing.seedD22
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOf

/** Flow 6 decisions 8 and 9: a date with no sessions, a coach with no classes, and offline (F6-AC9, F6-AC10). */
@OptIn(ExperimentalTestApi::class)
class F6AttendanceEmptyAndOfflineTest {

    // F6-AC9: "On a date with no sessions (Sun 27 Sep) … coach_attendance_empty reads "No sessions on this date.", there are no section tags, and the Present count is "0". On a past date the Absent chip shows "0""
    @Test
    fun F6_AC9_aPastDateWithNoSessionsShowsTheEmptyLineAndZeroCounts() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openCoachAttendance(world)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            assertTextIs("coach_attendance_date", "Sun 27 Sep 2026")
            assertTextIs("coach_attendance_empty", "No sessions on this date.")
            assertNoSections()
            assertTextIs("coach_attendance_present_count", "0")
            assertTextIs("coach_attendance_absent_count", "0")
            tag("coach_attendance_footnote").assertDoesNotExist()
        }
    }

    // F6-AC9: "and separately for a coach with no classes … on today the footnote shows instead" (decision 8: keep the date card and the summary row)
    @Test
    fun F6_AC9_aCoachWithNoClassesSeesTheEmptyLineTheFootnoteAndNoAbsentCount() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.mara()
            openCoachAttendance(world)

            assertTextIs("coach_attendance_date", "Mon 28 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Today \u00B7 in progress")
            assertTextIs("coach_attendance_empty", "No sessions on this date.")
            assertNoSections()
            assertTextIs("coach_attendance_present_count", "0")
            assertTextIs("coach_attendance_footnote", "\u00B7 Absent is set after the day ends")
            tag("coach_attendance_absent_count").assertDoesNotExist()
        }
    }

    // F6-AC9 / decision 8: "show one line, "No sessions on this date." No other empty-state copy." — and it is not shown when there are sessions
    @Test
    fun F6_AC9_theEmptyLineIsNotShownOnADateWithSessions() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            tag("coach_attendance_empty").assertDoesNotExist()
            assertTextNotShown("No sessions on this date.")
        }
    }

    // F6-AC10: "offline_banner shows under the title with "No internet connection" and no subtitle"
    @Test
    fun F6_AC10_offlineShowsTheBannerUnderTheTitleWithNoSubtitle() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            world.network.set(NetworkStatus.OFFLINE)
            openCoachAttendance(world)

            assertTextIs("offline_banner", "No internet connection")
            assertTrue(topOf("offline_banner") > topOf("coach_attendance_title"), "the banner sits under the title")
            assertTextNotShown("Showing today\u2019s code saved on this phone.")
            assertTextNotShown("Upload will retry when your connection returns.")
            assertTextNotShown("Today\u2019s class list can\u2019t load")
        }
    }

    // F6-AC10: "and all three sections and their rows still show"
    @Test
    fun F6_AC10_offlineStillShowsEverySectionAndRowFromThePhonesStore() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            world.network.set(NetworkStatus.OFFLINE)
            openCoachAttendance(world)

            for (sessionId in listOf(d22.sunriseSessionId, d22.lunchExpressSessionId, d22.coreBurnSessionId)) {
                tag(sectionTag(sessionId)).assertExists()
            }
            assertEquals(3, countTagPrefixUnder(sectionTag(d22.sunriseSessionId), "coach_attendance_row_"))
            assertEquals(3, countTagPrefixUnder(sectionTag(d22.coreBurnSessionId), "coach_attendance_row_"))
            tag(rowTag(d22.sunriseSessionId, d22.paolo)).assertExists()
            assertTextIs("coach_attendance_present_count", "3")
        }
    }

    // F6-AC10: "Going back online hides the banner without leaving the tab."
    @Test
    fun F6_AC10_goingBackOnlineHidesTheBannerWithoutLeavingTheTab() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            world.network.set(NetworkStatus.OFFLINE)
            openCoachAttendance(world)
            tag("offline_banner").assertExists()

            runOnIdle { world.network.set(NetworkStatus.ONLINE) }
            waitForIdle()

            tag("offline_banner").assertDoesNotExist()
            tag("coach_attendance").assertExists()
            tag("coach_tab_attendance").assertIsSelected()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.assertNoSections() {
        val sections = onAllNodes(hasTestTagPrefix("coach_attendance_section_"), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
        assertEquals(0, sections.size, "no section tags")
        assertEquals(0, countTagged("placeholder_tag"))
    }
}
