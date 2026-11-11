package ph.project.active.presentation.attend

import ph.project.active.domain.model.TimeSlot

data class AttendChoice(
    val sessionId: String,
    val className: String,
    val slot: TimeSlot,
    val coachName: String,
)
