package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.TimeSlot

class FormatTest {

    @Test
    fun AC82_dates_use_the_short_weekday_month_pattern() {
        assertEquals("Mon 28 Sep 2026", formatDate(LocalDate(2026, 9, 28)))
        assertEquals("Wed 30 Sep 2026", formatDate(LocalDate(2026, 9, 30)))
        assertEquals("Mon 5 Oct", formatShortDate(LocalDate(2026, 10, 5)))
        assertEquals("Wed 30 Sep", formatShortDate(LocalDate(2026, 9, 30)))
    }

    @Test
    fun AC83_times_slots_and_days_use_the_specified_glyphs() {
        assertEquals("5:30 PM", formatTime(LocalTime(17, 30)))
        assertEquals("6:00 AM", formatTime(LocalTime(6, 0)))
        assertEquals("12:15 PM", formatTime(LocalTime(12, 15)))
        assertEquals(
            "5:30 PM – 6:30 PM",
            formatSlot(TimeSlot(LocalTime(17, 30), LocalTime(18, 30))),
        )
        assertEquals("Mon · Wed", formatDays(setOf(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY)))
        assertEquals("Mon · Sun", formatDays(setOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY)))
        assertEquals("Fri", formatDays(setOf(DayOfWeek.FRIDAY)))
        assertEquals("Every day", formatDays(DayOfWeek.entries.toSet()))
    }
}
