package ph.project.active.presentation

import ph.project.active.domain.TimeSlot

data class AttendChoice(
    val sessionId: String,
    val className: String,
    val slot: TimeSlot,
    val coachName: String,
)
