package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextBeside
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.countTagPrefixUnder
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.mergedTag
import ph.project.active.ui.testing.openCoachAttendance
import ph.project.active.ui.testing.rowTag
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.sectionTag
import ph.project.active.ui.testing.seedD19
import ph.project.active.ui.testing.seedD21
import ph.project.active.ui.testing.seedD22
import ph.project.active.ui.testing.tag
import ph.project.active.ui.theme.TrxSpacing

/** Flow 6 D21 and the date card: cancelled sections, an ended day, and paging (F6-AC6 to F6-AC8). */
@OptIn(ExperimentalTestApi::class)
class F6AttendancePastDayTest {

    // F6-AC6: "Lunch Express (cancelled on that date) shows the line "12:15 PM – 12:45 PM · no attendance" and a "Cancelled" chip. It has no student rows." — today (D22 clock)
    @Test
    fun F6_AC6_aCancelledSessionTodayShowsNoAttendanceAndACancelledChip() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            val lunch = sectionTag(d22.lunchExpressSessionId)
            assertEquals(1, countTextUnder(lunch, "Lunch Express TRX"))
            assertEquals(1, countTextUnder(lunch, "12:15 PM \u2013 12:45 PM \u00B7 no attendance"))
            assertEquals(1, countTextUnder(lunch, "Cancelled"))
            assertEquals(0, countTagPrefixUnder(lunch, "coach_attendance_row_"), "no student rows")
        }
    }

    // F6-AC6: "This holds on today (D22 clock) and on a past date (D21 clock)."
    @Test
    fun F6_AC6_aCancelledSessionOnAPastDateShowsTheSameCard() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d21 = world.seedD21()
            openCoachAttendance(world)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            val lunch = sectionTag(d21.lunchExpressSessionId)
            assertEquals(1, countTextUnder(lunch, "12:15 PM \u2013 12:45 PM \u00B7 no attendance"))
            assertEquals(1, countTextUnder(lunch, "Cancelled"))
            assertEquals(0, countTagPrefixUnder(lunch, "coach_attendance_row_"), "no student rows")
            assertNoTextUnder(lunch, "Absent")
        }
    }

    // F6-AC7: "On the D21 clock the tab opens on Tue 29 Sep ("Today · in progress"). Tapping coach_attendance_prev_day shows "Mon 28 Sep 2026" / "Day ended"."
    @Test
    fun F6_AC7_theTabOpensOnTodayAndThePreviousDayHasEnded() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD21()
            openCoachAttendance(world)
            assertTextIs("coach_attendance_date", "Tue 29 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Today \u00B7 in progress")

            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            assertTextIs("coach_attendance_date", "Mon 28 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Day ended")
        }
    }

    // F6-AC7: "The summary shows Present "4" and an "Absent" chip with coach_attendance_absent_count = "2". There is no footnote."
    @Test
    fun F6_AC7_anEndedDayShowsPresentAndAbsentCountsAndNoFootnote() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD21()
            openCoachAttendance(world)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            assertTextIs("coach_attendance_present_count", "4")
            assertTextBeside("Present", "coach_attendance_present_count")
            assertTextIs("coach_attendance_absent_count", "2")
            assertTextBeside("Absent", "coach_attendance_absent_count")
            tag("coach_attendance_footnote").assertDoesNotExist()
        }
    }

    // F6-AC7: "Hana (Sunrise) and Gia (Core Burn) show an "Absent" chip and no "Chose" line. Dana on Core Burn shows "Chose TRX Core Burn · scanned 5:33 PM"."
    @Test
    fun F6_AC7_unscannedStudentsAreAbsentAndDanasLateScanShows() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d21 = world.seedD21()
            openCoachAttendance(world)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            val hana = rowTag(d21.sunriseSessionId, d21.hana)
            val gia = rowTag(d21.coreBurnSessionId, d21.gia)
            for (row in listOf(hana, gia)) {
                assertEquals(1, countTextUnder(row, "Absent"), "$row has an Absent chip")
                assertNoTextUnder(row, "Chose")
                assertNoTextUnder(row, "Not scanned yet")
            }
            val dana = rowTag(d21.coreBurnSessionId, d21.dana)
            assertEquals(1, countTextUnder(dana, "Chose TRX Core Burn \u00B7 scanned 5:33 PM"))
            assertEquals(1, countTextUnder(dana, "Present"))
        }
    }

    // F6-AC7: "coach_attendance_next_day is enabled; tapping it returns to Tue 29 Sep, where it is disabled again."
    @Test
    fun F6_AC7_nextDayIsEnabledOnAPastDateAndReturnsToToday() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD21()
            openCoachAttendance(world)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()
            tag("coach_attendance_next_day").assertIsEnabled()

            tag("coach_attendance_next_day").performClick()
            waitForIdle()

            assertTextIs("coach_attendance_date", "Tue 29 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Today \u00B7 in progress")
            tag("coach_attendance_next_day").assertIsNotEnabled()
        }
    }

    // F6-AC8: "The left chevron has no limit: from Mon 28 Sep, tapping it twice shows "Sat 26 Sep 2026" / "Day ended"."
    @Test
    fun F6_AC8_thePreviousDayChevronHasNoLimit() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openCoachAttendance(world)

            tag("coach_attendance_prev_day").performClick()
            waitForIdle()
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            assertTextIs("coach_attendance_date", "Sat 26 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Day ended")
            tag("coach_attendance_prev_day").assertIsEnabled()
        }
    }

    // F6-AC8: "The right chevron never shows a date after today."
    @Test
    fun F6_AC8_theNextDayChevronNeverGoesPastToday() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openCoachAttendance(world)
            tag("coach_attendance_prev_day").performClick()
            waitForIdle()

            repeat(3) {
                tag("coach_attendance_next_day").performClick()
                waitForIdle()
            }

            assertTextIs("coach_attendance_date", "Mon 28 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Today \u00B7 in progress")
            tag("coach_attendance_next_day").assertIsNotEnabled()
        }
    }

    // F6-AC8: "Both chevrons have spoken labels ("Previous day", "Next day")"
    @Test
    fun F6_AC8_bothChevronsHaveSpokenLabels() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openCoachAttendance(world)

            mergedTag("coach_attendance_prev_day").assertContentDescriptionEquals("Previous day")
            mergedTag("coach_attendance_next_day").assertContentDescriptionEquals("Next day")
        }
    }

    // F6-AC8: "and a touch target of at least 48dp (TrxSpacing.touchTarget)"
    @Test
    fun F6_AC8_bothChevronsAreAtLeastTheTouchTarget() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openCoachAttendance(world)

            for (chevron in listOf("coach_attendance_prev_day", "coach_attendance_next_day")) {
                tag(chevron).assertHeightIsAtLeast(TrxSpacing.touchTarget)
                tag(chevron).assertWidthIsAtLeast(TrxSpacing.touchTarget)
            }
        }
    }
}
