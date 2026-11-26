package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.common.formatSlot

/** Flow 6 spec C5 and F6-AC17: the new `UiCopy` lines, and the existing ones the screen reuses. */
class F6C5CoachAttendanceCopyTest {

    private val sunrise = TimeSlot(LocalTime(6, 0), LocalTime(6, 45))
    private val lunch = TimeSlot(LocalTime(12, 15), LocalTime(12, 45))
    private val mon28 = LocalDate(2026, 9, 28)

    // Copy table: attendanceSlotLine(slot, sessionNumber) → "6:00 AM – 6:45 AM · Session 10"
    @Test
    fun F6_AC17_attendanceSlotLine() {
        assertEquals("6:00 AM \u2013 6:45 AM \u00B7 Session 10", UiCopy.attendanceSlotLine(sunrise, 10))
        assertEquals("6:00 AM \u2013 6:45 AM \u00B7 Session 1", UiCopy.attendanceSlotLine(sunrise, 1))
    }

    // Copy table: cancelledSlotLine(slot) → "12:15 PM – 12:45 PM · no attendance"
    @Test
    fun F6_AC17_cancelledSlotLine() {
        assertEquals("12:15 PM \u2013 12:45 PM \u00B7 no attendance", UiCopy.cancelledSlotLine(lunch))
    }

    // Copy table: choseAndScanned(className, time) → "Chose Sunrise TRX Strength · scanned 6:02 AM"
    @Test
    fun F6_AC17_choseAndScanned() {
        assertEquals(
            "Chose Sunrise TRX Strength \u00B7 scanned 6:02 AM",
            UiCopy.choseAndScanned("Sunrise TRX Strength", LocalTime(6, 2)),
        )
        assertEquals("Chose TRX Core Burn \u00B7 scanned 5:33 PM", UiCopy.choseAndScanned("TRX Core Burn", LocalTime(17, 33)))
    }

    // Copy table: absentSetAfterDayEnds() → "· Absent is set after the day ends"
    @Test
    fun F6_AC17_absentSetAfterDayEnds() {
        assertEquals("\u00B7 Absent is set after the day ends", UiCopy.absentSetAfterDayEnds())
    }

    // Copy table: noSessionsOnDate() → "No sessions on this date."
    @Test
    fun F6_AC17_noSessionsOnDate() {
        assertEquals("No sessions on this date.", UiCopy.noSessionsOnDate())
    }

    // Copy table: previousDay() / nextDay() → "Previous day" / "Next day"
    @Test
    fun F6_AC17_chevronLabels() {
        assertEquals("Previous day", UiCopy.previousDay())
        assertEquals("Next day", UiCopy.nextDay())
    }

    // Copy table: qrCodeDescription(date) → "Today’s attendance code, valid Mon 28 Sep 2026 only"
    @Test
    fun F6_AC17_qrCodeDescription() {
        assertEquals("Today\u2019s attendance code, valid Mon 28 Sep 2026 only", UiCopy.qrCodeDescription(mon28))
    }

    // F6-AC17: "Existing strings are unchanged" (copy table, existing functions)
    @Test
    fun F6_AC17_existingStringsTheScreenReusesAreUnchanged() {
        assertEquals("TRX ATTENDANCE", UiCopy.largeTitleEyebrow())
        assertEquals("Attendance", UiCopy.tabAttendance())
        assertEquals("Present", UiCopy.present())
        assertEquals("Absent", UiCopy.absent())
        assertEquals("Cancelled", UiCopy.cancelled())
        assertEquals("Not scanned yet", UiCopy.notScannedYet())
        assertEquals("Day ended", UiCopy.dayEnded())
        assertEquals("Today \u00B7 in progress", UiCopy.inProgress())
        assertEquals("No internet connection", UiCopy.offlineBanner())
        assertEquals("Mon 28 Sep 2026", formatDate(mon28))
        assertEquals("6:00 AM \u2013 6:45 AM", formatSlot(sunrise))
        assertEquals("SESSION 10", UiCopy.sessionLabel(10))
    }
}
