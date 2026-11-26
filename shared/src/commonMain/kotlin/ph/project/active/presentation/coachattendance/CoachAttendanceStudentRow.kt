package ph.project.active.presentation.coachattendance

import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.CoachAttendanceStatus

data class CoachAttendanceStudentRow(
    val studentId: String,
    val name: String,
    val employeeId: String,
    val status: CoachAttendanceStatus,
    val chosenClassName: String?,
    val scannedAt: Instant?,
    val scannedTime: LocalTime? = null,
)
