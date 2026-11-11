package ph.project.active.presentation

import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot

data class TodaySessionRow(
    val className: String,
    val slot: TimeSlot,
    val status: SessionStatus,
)
