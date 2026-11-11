package ph.project.active.presentation.createclass

import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.TimeSlot

data class ScheduleRow(
    val sessionNumber: Int,
    val date: LocalDate,
    val slot: TimeSlot,
    val status: SessionStatus,
    val isToday: Boolean,
    val isPast: Boolean,
    val canCancelDate: Boolean,
)
