package ph.project.active.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** AC39: per session of the coach's classes on [date], the status of every ACTIVE roster student. */
class CoachAttendanceForDate(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(coachId: String, date: LocalDate): List<SessionAttendance> {
        val dayIsOver = date < clock.todayIn(timeZone)
        return classes.allClasses()
            .filter { it.coachId == coachId }
            .flatMap { classes.sessionsOn(it.id, date) }
            .filter { it.status == SessionStatus.SCHEDULED }
            .sortedWith(SESSION_ORDER)
            .map { session ->
                val students = memberships.activeRoster(session.classId).map { studentId ->
                    val status = when {
                        attendance.find(session.id, studentId) != null -> CoachAttendanceStatus.PRESENT
                        dayIsOver -> CoachAttendanceStatus.ABSENT
                        else -> CoachAttendanceStatus.NOT_SCANNED
                    }
                    StudentAttendance(studentId, status)
                }
                SessionAttendance(session, students)
            }
    }
}
