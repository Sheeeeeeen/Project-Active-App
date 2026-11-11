package ph.project.active.presentation.classdetail

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot

data class ClassContent(
    val name: String,
    val visibility: Visibility,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val endDate: LocalDate?,
)
