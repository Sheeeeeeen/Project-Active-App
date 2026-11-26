package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertLineUnder
import ph.project.active.ui.testing.assertNoSpinner
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextBeside
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.openCoachAttendance
import ph.project.active.ui.testing.rowTag
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.sectionTag
import ph.project.active.ui.testing.seedD22
import ph.project.active.ui.testing.tag

/** Flow 6 D22: the coach's real Attendance tab on today, in progress (F6-AC1 to F6-AC5). */
@OptIn(ExperimentalTestApi::class)
class F6AttendanceTodayTest {

    // F6-AC1: "coach_attendance exists; coach_attendance_title reads "Attendance"; coach_attendance_overline reads "TRX ATTENDANCE"; coach_tab_attendance is selected"
    @Test
    fun F6_AC1_theAttendanceTabShowsTheRealScreenHeader() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            tag("coach_attendance").assertExists()
            assertTextIs("coach_attendance_title", "Attendance")
            assertTextIs("coach_attendance_overline", "TRX ATTENDANCE")
            tag("coach_tab_attendance").assertIsSelected()
        }
    }

    // F6-AC1: "There is no placeholder_tag anywhere on the tab, no coach_attendance_account, no add button, and no loading spinner."
    @Test
    fun F6_AC1_noPlaceholderNoAccountNoAddButtonAndNoSpinner() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            assertEquals(0, countTaggedUnder("coach_attendance", "placeholder_tag"), "no placeholder under coach_attendance")
            assertEquals(0, countTagged("placeholder_tag"), "no placeholder anywhere on the tab")
            tag("coach_attendance_account").assertDoesNotExist()
            tag("coach_attendance_add").assertDoesNotExist()
            tag("coach_classes_add").assertDoesNotExist()
            assertNoSpinner()
        }
    }

    // F6-AC2: "coach_attendance_date = "Mon 28 Sep 2026" and coach_attendance_day_state = "Today · in progress""
    @Test
    fun F6_AC2_theDateCardShowsTodayInProgress() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            assertTextIs("coach_attendance_date", "Mon 28 Sep 2026")
            assertTextIs("coach_attendance_day_state", "Today \u00B7 in progress")
        }
    }

    // F6-AC2: "coach_attendance_next_day is not enabled. coach_attendance_prev_day is enabled."
    @Test
    fun F6_AC2_onTodayOnlyThePreviousDayChevronIsEnabled() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            tag("coach_attendance_next_day").assertIsNotEnabled()
            tag("coach_attendance_prev_day").assertIsEnabled()
        }
    }

    // F6-AC3: "a "Present" chip with coach_attendance_present_count = "3", and coach_attendance_footnote = "· Absent is set after the day ends""
    @Test
    fun F6_AC3_theSummaryShowsPresentThreeAndTheFootnote() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            assertTextIs("coach_attendance_present_count", "3")
            assertTextBeside("Present", "coach_attendance_present_count")
            assertTextIs("coach_attendance_footnote", "\u00B7 Absent is set after the day ends")
        }
    }

    // F6-AC3: "There is no coach_attendance_absent_count and no "Absent" chip anywhere on the screen."
    @Test
    fun F6_AC3_todayHasNoAbsentCountAndNoAbsentChip() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD22()
            openCoachAttendance(world)

            tag("coach_attendance_absent_count").assertDoesNotExist()
            assertTextNotShown("Absent")
        }
    }

    // F6-AC4: "the sections appear top to bottom as Sunrise TRX Strength, Lunch Express TRX, TRX Core Burn (each tagged coach_attendance_section_{sessionId})"
    @Test
    fun F6_AC4_sectionsAppearInStartTimeOrderTaggedBySessionId() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            assertEquals(
                listOf(d22.sunriseSessionId, d22.lunchExpressSessionId, d22.coreBurnSessionId),
                cardIdsTopToBottom("coach_attendance_section_"),
            )
            assertLineUnder(sectionTag(d22.sunriseSessionId), "Sunrise TRX Strength")
            assertLineUnder(sectionTag(d22.lunchExpressSessionId), "Lunch Express TRX")
            assertLineUnder(sectionTag(d22.coreBurnSessionId), "TRX Core Burn")
        }
    }

    // F6-AC4: "Sunrise’s line reads "6:00 AM – 6:45 AM · Session 1" and Core Burn’s reads "5:30 PM – 6:30 PM · Session 1""
    @Test
    fun F6_AC4_eachSectionShowsItsSlotAndSessionNumber() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            assertEquals(1, countTextUnder(sectionTag(d22.sunriseSessionId), "6:00 AM \u2013 6:45 AM \u00B7 Session 1"))
            assertEquals(1, countTextUnder(sectionTag(d22.coreBurnSessionId), "5:30 PM \u2013 6:30 PM \u00B7 Session 1"))
        }
    }

    // F6-AC5: "Sunrise rows appear in join order Paolo, Dana, Hana. Each row (tag coach_attendance_row_{sessionId}_{studentId}) shows … name and employee ID"
    @Test
    fun F6_AC5_sunriseRowsAreInJoinOrderWithNameAndEmployeeId() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            val sunrise = d22.sunriseSessionId
            assertEquals(
                listOf(d22.paolo.id, d22.dana.id, d22.hana.id),
                cardIdsTopToBottom("coach_attendance_row_${sunrise}_"),
                "join order, not A to Z",
            )
            for ((student, employeeId) in listOf(d22.paolo to "EMP-20417", d22.dana to "EMP-20155", d22.hana to "EMP-20877")) {
                assertEquals(1, countTaggedUnder(sectionTag(sunrise), rowTag(sunrise, student)), "the row sits in its section")
                assertLineUnder(rowTag(sunrise, student), student.name)
                assertLineUnder(rowTag(sunrise, student), employeeId)
            }
        }
    }

    // F6-AC5: "Paolo: "Chose Sunrise TRX Strength · scanned 6:02 AM" and a "Present" chip. Dana: "Chose Sunrise TRX Strength · scanned 6:05 AM" and "Present"."
    @Test
    fun F6_AC5_presentSunriseRowsShowTheChoseLineAndAPresentChip() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            val paolo = rowTag(d22.sunriseSessionId, d22.paolo)
            val dana = rowTag(d22.sunriseSessionId, d22.dana)
            assertEquals(1, countTextUnder(paolo, "Chose Sunrise TRX Strength \u00B7 scanned 6:02 AM"))
            assertEquals(1, countTextUnder(paolo, "Present"))
            assertEquals(1, countTextUnder(dana, "Chose Sunrise TRX Strength \u00B7 scanned 6:05 AM"))
            assertEquals(1, countTextUnder(dana, "Present"))
            assertNoTextUnder(paolo, "Not scanned yet")
        }
    }

    // F6-AC5: "Hana: "Not scanned yet", with no chip and no "Chose" line."
    @Test
    fun F6_AC5_anUnscannedRowSaysNotScannedYetWithNoChipAndNoChoseLine() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            val hana = rowTag(d22.sunriseSessionId, d22.hana)
            assertEquals(1, countTextUnder(hana, "Not scanned yet"))
            assertNoTextUnder(hana, "Present")
            assertNoTextUnder(hana, "Absent")
            assertNoTextUnder(hana, "Chose")
        }
    }

    // F6-AC5: "Core Burn: Paolo "Chose TRX Core Burn · scanned 5:31 PM" and "Present"; Dana and Gia "Not scanned yet"."
    @Test
    fun F6_AC5_coreBurnRowsShowPaoloPresentAndDanaAndGiaNotScannedYet() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d22 = world.seedD22()
            openCoachAttendance(world)

            val coreBurn = d22.coreBurnSessionId
            assertEquals(listOf(d22.paolo.id, d22.dana.id, d22.gia.id), cardIdsTopToBottom("coach_attendance_row_${coreBurn}_"))
            assertEquals(1, countTextUnder(rowTag(coreBurn, d22.paolo), "Chose TRX Core Burn \u00B7 scanned 5:31 PM"))
            assertEquals(1, countTextUnder(rowTag(coreBurn, d22.paolo), "Present"))
            for (student in listOf(d22.dana, d22.gia)) {
                assertEquals(1, countTextUnder(rowTag(coreBurn, student), "Not scanned yet"))
                assertNoTextUnder(rowTag(coreBurn, student), "Present")
                assertNoTextUnder(rowTag(coreBurn, student), "Chose")
            }
        }
    }
}
