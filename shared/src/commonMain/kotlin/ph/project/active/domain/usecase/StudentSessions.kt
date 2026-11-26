package ph.project.active.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.StudentSessionRow
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.activeClassIds

/**
 * One row per session of every class the student is ACTIVE on. A cancelled date of a class that
 * is still open is CANCELLED (and wins over a record). A cancelled class lists none of its
 * cancelled dates; its past scheduled dates stay Present or Absent.
 */
class StudentSessions(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(studentId: String): List<StudentSessionRow> {
        val today = clock.todayIn(timeZone)
        return memberships.activeClassIds(studentId)
            .flatMap { classId -> rowsOf(classId, studentId, today) }
            .sortedWith(compareBy({ it.session.date }, { it.session.slot.start }, { it.session.classId }))
    }

    private fun rowsOf(classId: String, studentId: String, today: kotlinx.datetime.LocalDate): List<StudentSessionRow> {
        val trxClass = classes.findClass(classId) ?: return emptyList()
        return classes.sessionsOf(classId).mapNotNull { session ->
            val listed = when (session.status) {
                SessionStatus.SCHEDULED -> true
                SessionStatus.CANCELLED -> !trxClass.isCancelled
            }
            if (!listed) return@mapNotNull null
            val status = when {
                session.status == SessionStatus.CANCELLED -> StudentSessionStatus.CANCELLED
                attendance.find(session.id, studentId) != null -> StudentSessionStatus.PRESENT
                session.date < today -> StudentSessionStatus.ABSENT
                else -> StudentSessionStatus.UPCOMING
            }
            StudentSessionRow(session, status)
        }
    }
}
