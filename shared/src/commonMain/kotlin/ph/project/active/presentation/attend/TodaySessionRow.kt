package ph.project.active.presentation.attend

import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.TimeSlot

data class TodaySessionRow(
    val className: String,
    val slot: TimeSlot,
    val status: SessionStatus,
)
