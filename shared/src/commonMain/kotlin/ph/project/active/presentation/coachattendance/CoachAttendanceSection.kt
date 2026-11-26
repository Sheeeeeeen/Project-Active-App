package ph.project.active.presentation.coachattendance

import ph.project.active.domain.model.TimeSlot

data class CoachAttendanceSection(
    val slot: TimeSlot,
    val cancelled: Boolean,
    val students: List<CoachAttendanceStudentRow>,
    val sessionId: String = "",
    val className: String = "",
    val sessionNumber: Int = 0,
)
