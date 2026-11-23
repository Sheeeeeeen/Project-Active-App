package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.common.formatSlot

/** Flow 5 spec C2 and F5-AC17: the D19 copy, exact strings from the copy table. */
class F5C2DailyQrCopyTest {
    private val monday = LocalDate(2026, 9, 28)

    // F5-AC17: "dailyQrOverline() … "DAILY QR""
    @Test
    fun F5_AC17_overlineReadsDailyQr() {
        assertEquals("DAILY QR", UiCopy.dailyQrOverline())
    }

    // F5-AC17: "todaysCode() … "Today’s code"" (typographic apostrophe U+2019)
    @Test
    fun F5_AC17_titleReadsTodaysCode() {
        assertEquals("Today\u2019s code", UiCopy.todaysCode())
    }

    // F5-AC17: "dailyQrSubtitle(date, coachName) … "Mon 28 Sep 2026 · Coach Mara Santos""
    @Test
    fun F5_AC17_subtitleIsTheDateAndCoachName() {
        assertEquals("Mon 28 Sep 2026 \u00B7 Coach Mara Santos", UiCopy.dailyQrSubtitle(monday, "Mara Santos"))
    }

    // F5-AC17: "validForDate(date) … "Valid Mon 28 Sep 2026 only""
    @Test
    fun F5_AC17_validLineNamesTheDate() {
        assertEquals("Valid Mon 28 Sep 2026 only", UiCopy.validForDate(monday))
        assertEquals("Valid Tue 29 Sep 2026 only", UiCopy.validForDate(LocalDate(2026, 9, 29)))
    }

    // F5-AC17: "brightnessRaised() … "Brightness is raised while this code is open""
    @Test
    fun F5_AC17_brightnessCaption() {
        assertEquals("Brightness is raised while this code is open", UiCopy.brightnessRaised())
    }

    // F5-AC17: "thisCodeCoversToday() / oneCodeAllClasses() … "This code covers today" / "One code · all classes""
    @Test
    fun F5_AC17_sectionHeader() {
        assertEquals("This code covers today", UiCopy.thisCodeCoversToday())
        assertEquals("One code \u00B7 all classes", UiCopy.oneCodeAllClasses())
    }

    // C2 / F5-AC17: "Existing QR and offline strings are unchanged", and formatDate(28 Sep 2026) is still "Mon 28 Sep 2026"
    @Test
    fun F5_AC17_existingQrAndOfflineCopyIsUnchanged() {
        assertEquals("No internet connection", UiCopy.offlineBanner())
        assertEquals("Showing today\u2019s code saved on this phone.", UiCopy.qrOfflineSubtitle())
        assertEquals("Today\u2019s class list can\u2019t load", UiCopy.classListUnavailableTitle())
        assertEquals(
            "The code still works. Students on the roster can scan it now; their phones upload later.",
            UiCopy.classListUnavailableBody(),
        )
        assertEquals("Scheduled", UiCopy.scheduled())
        assertEquals("Cancelled", UiCopy.cancelled())
        assertEquals("QR", UiCopy.tabQr())
        assertEquals("Attendance", UiCopy.tabAttendance())
        assertEquals("Classes", UiCopy.tabClasses())
        assertEquals("Mon 28 Sep 2026", formatDate(monday))
        assertEquals("6:00 AM \u2013 6:45 AM", formatSlot(TimeSlot(LocalTime(6, 0), LocalTime(6, 45))))
        assertEquals("12:15 PM \u2013 12:45 PM", formatSlot(TimeSlot(LocalTime(12, 15), LocalTime(12, 45))))
        assertEquals("5:30 PM \u2013 6:30 PM", formatSlot(TimeSlot(LocalTime(17, 30), LocalTime(18, 30))))
    }
}
