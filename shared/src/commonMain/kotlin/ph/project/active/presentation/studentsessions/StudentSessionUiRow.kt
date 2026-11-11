package ph.project.active.presentation.studentsessions

import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.domain.model.TimeSlot

data class StudentSessionUiRow(
    val className: String,
    val sessionNumber: Int,
    val date: LocalDate,
    val slot: TimeSlot,
    val status: StudentSessionStatus,
    val uploadPending: Boolean,
)
