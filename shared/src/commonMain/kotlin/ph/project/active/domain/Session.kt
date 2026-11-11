package ph.project.active.domain

import kotlinx.datetime.LocalDate

/** One occurrence of a class: one date and one time slot. Attendance is recorded per session. */
data class Session(
    val id: String,
    val classId: String,
    val date: LocalDate,
    val slot: TimeSlot,
    val sessionNumber: Int,
    val status: SessionStatus = SessionStatus.SCHEDULED,
)
