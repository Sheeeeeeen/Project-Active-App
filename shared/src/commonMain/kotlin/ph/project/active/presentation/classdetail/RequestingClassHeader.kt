package ph.project.active.presentation.classdetail

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.TimeSlot

data class RequestingClassHeader(
    val name: String,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
)
