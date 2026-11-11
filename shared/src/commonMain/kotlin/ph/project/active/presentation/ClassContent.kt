package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility

data class ClassContent(
    val name: String,
    val visibility: Visibility,
    val coachName: String,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val endDate: LocalDate?,
)
