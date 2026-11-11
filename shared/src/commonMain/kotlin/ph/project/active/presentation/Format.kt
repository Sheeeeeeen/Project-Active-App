package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.TimeSlot

private val WEEKDAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

fun formatDate(date: LocalDate): String =
    "${WEEKDAYS[date.dayOfWeek.ordinal]} ${date.day} ${MONTHS[date.month.ordinal]} ${date.year}"

fun formatShortDate(date: LocalDate): String =
    "${WEEKDAYS[date.dayOfWeek.ordinal]} ${date.day} ${MONTHS[date.month.ordinal]}"

fun formatTime(time: LocalTime): String {
    val suffix = if (time.hour < 12) "AM" else "PM"
    val hour12 = when (val hour = time.hour % 12) {
        0 -> 12
        else -> hour
    }
    val minute = time.minute.toString().padStart(2, '0')
    return "$hour12:$minute $suffix"
}

fun formatSlot(slot: TimeSlot): String = "${formatTime(slot.start)} \u2013 ${formatTime(slot.end)}"

fun formatDays(days: Set<DayOfWeek>): String {
    if (days.containsAll(DayOfWeek.entries)) return "Every day"
    return days.sortedBy { it.ordinal }.joinToString(" \u00B7 ") { day -> WEEKDAYS[day.ordinal] }
}
