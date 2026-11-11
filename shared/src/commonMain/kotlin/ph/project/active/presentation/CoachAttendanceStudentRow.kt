package ph.project.active.presentation

import ph.project.active.domain.CoachAttendanceStatus
import kotlin.time.Instant

data class CoachAttendanceStudentRow(
    val studentId: String,
    val name: String,
    val employeeId: String,
    val status: CoachAttendanceStatus,
    val chosenClassName: String?,
    val scannedAt: Instant?,
)
