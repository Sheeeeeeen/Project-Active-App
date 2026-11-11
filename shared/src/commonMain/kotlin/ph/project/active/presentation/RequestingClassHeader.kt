package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.TimeSlot

data class RequestingClassHeader(
    val name: String,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)
