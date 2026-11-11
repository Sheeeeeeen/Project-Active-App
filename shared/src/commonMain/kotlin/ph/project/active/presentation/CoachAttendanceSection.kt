package ph.project.active.presentation

import ph.project.active.domain.TimeSlot

data class CoachAttendanceSection(
    val slot: TimeSlot,
    val cancelled: Boolean,
    val students: List<CoachAttendanceStudentRow>,
)
