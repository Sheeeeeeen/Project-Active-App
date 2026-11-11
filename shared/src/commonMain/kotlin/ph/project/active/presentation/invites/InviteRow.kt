package ph.project.active.presentation.invites

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot

data class InviteRow(
    val classId: String,
    val className: String,
    val coachName: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)
