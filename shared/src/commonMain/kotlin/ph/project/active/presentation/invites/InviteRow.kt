package ph.project.active.presentation.invites

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatSlot

data class InviteRow(
    val classId: String,
    val className: String,
    val coachName: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)

fun InviteRow.dayLine(): String = formatDays(slotsByWeekday.keys)

fun InviteRow.timeLine(): String =
    slotsByWeekday.values
        .flatten()
        .sortedBy { it.start }
        .distinct()
        .joinToString(", ") { slot -> formatSlot(slot) }
