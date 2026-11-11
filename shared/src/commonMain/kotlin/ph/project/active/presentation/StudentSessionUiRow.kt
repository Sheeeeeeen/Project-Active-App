package ph.project.active.presentation

import kotlinx.datetime.LocalDate
import ph.project.active.domain.StudentSessionStatus
import ph.project.active.domain.TimeSlot

data class StudentSessionUiRow(
    val className: String,
    val sessionNumber: Int,
    val date: LocalDate,
    val slot: TimeSlot,
    val status: StudentSessionStatus,
    val uploadPending: Boolean,
)
