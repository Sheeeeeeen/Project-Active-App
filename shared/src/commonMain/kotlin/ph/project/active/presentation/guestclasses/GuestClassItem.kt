package ph.project.active.presentation.guestclasses

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.TimeSlot

data class GuestClassItem(
    val classId: String,
    val name: String,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)
