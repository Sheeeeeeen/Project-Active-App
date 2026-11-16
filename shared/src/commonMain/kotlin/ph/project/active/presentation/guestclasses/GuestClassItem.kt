package ph.project.active.presentation.guestclasses

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatDaysLong
import ph.project.active.presentation.common.formatSlot

data class GuestClassItem(
    val classId: String,
    val name: String,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)

fun GuestClassItem.dayLine(): String = formatDays(slotsByWeekday.keys)

fun GuestClassItem.timeLine(): String =
    slotsByWeekday.values
        .flatten()
        .sortedBy { it.start }
        .distinct()
        .joinToString(", ") { slot -> formatSlot(slot) }

fun GuestClassItem.spokenCard(): String =
    "$name, Coach $coachName, ${formatDaysLong(slotsByWeekday.keys)}, ${timeLine().replace("\u2013", "to")}"
