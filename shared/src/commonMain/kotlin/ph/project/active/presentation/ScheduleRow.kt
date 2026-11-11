package ph.project.active.presentation

import kotlinx.datetime.LocalDate
import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot

data class ScheduleRow(
    val sessionNumber: Int,
    val date: LocalDate,
    val slot: TimeSlot,
    val status: SessionStatus,
    val isToday: Boolean,
    val isPast: Boolean,
    val canCancelDate: Boolean,
)
