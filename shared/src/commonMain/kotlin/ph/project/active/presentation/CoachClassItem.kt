package ph.project.active.presentation

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.Visibility

data class CoachClassItem(
    val classId: String,
    val name: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val rosterCount: Int,
    val pendingRequestCount: Int,
    val isCancelled: Boolean,
)
