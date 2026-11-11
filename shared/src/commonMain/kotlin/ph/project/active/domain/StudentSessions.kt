package ph.project.active.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** AC37: one row per non-cancelled session of every class the student is ACTIVE on. */
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
            .flatMap { classes.sessionsOf(it) }
            .filter { it.status == SessionStatus.SCHEDULED }
            .sortedWith(SESSION_ORDER)
            .map { session ->
                val status = when {
                    attendance.find(session.id, studentId) != null -> StudentSessionStatus.PRESENT
                    session.date < today -> StudentSessionStatus.ABSENT
                    else -> StudentSessionStatus.UPCOMING
                }
                StudentSessionRow(session, status)
            }
    }
}
