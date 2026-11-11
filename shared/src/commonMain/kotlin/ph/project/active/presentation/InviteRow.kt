package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility

data class InviteRow(
    val classId: String,
    val className: String,
    val coachName: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)
