package ph.project.active.presentation.coachclasses

import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.model.TimeSlot

data class CoachClassItem(
    val classId: String,
    val name: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    val rosterCount: Int,
    val pendingRequestCount: Int,
    val isCancelled: Boolean,
)
