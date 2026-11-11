package ph.project.active.presentation.coachattendance

import ph.project.active.domain.model.TimeSlot

data class CoachAttendanceSection(
    val slot: TimeSlot,
    val cancelled: Boolean,
    val students: List<CoachAttendanceStudentRow>,
)
