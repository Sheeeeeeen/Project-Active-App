package ph.project.active.presentation.studentclasses

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.classdetail.ClassChip
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatDaysLong
import ph.project.active.presentation.common.formatSlot

data class StudentClassItem(
    val classId: String,
    val name: String,
    val chip: ClassChip?,
    val coachName: String = "",
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>> = emptyMap(),
)

fun StudentClassItem.dayLine(): String = formatDays(slotsByWeekday.keys)

fun StudentClassItem.timeLine(): String =
    slotsByWeekday.values
        .flatten()
        .sortedBy { it.start }
        .distinct()
        .joinToString(", ") { slot -> formatSlot(slot) }

fun StudentClassItem.spokenCard(): String =
    "$name, Coach $coachName, ${formatDaysLong(slotsByWeekday.keys)}, ${timeLine().replace("\u2013", "to")}"
