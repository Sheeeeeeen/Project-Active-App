package trx.attendance.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** AC17: public classes for everyone; private classes only for their coach and ACTIVE students. */
class ListVisibleClasses(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
) {
    operator fun invoke(viewerId: String?): List<TrxClass> = classes.allClasses().filter { trxClass ->
        trxClass.visibility == Visibility.PUBLIC ||
            (viewerId != null && (trxClass.coachId == viewerId ||
                memberships.currentStatus(trxClass.id, viewerId) == MembershipStatus.ACTIVE))
    }
}

enum class StudentSessionStatus { UPCOMING, PRESENT, ABSENT }

data class StudentSessionRow(val session: Session, val status: StudentSessionStatus)

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

/** AC38: today's SCHEDULED sessions (one per slot) on classes the student is ACTIVE on. */
class TodaysChoosableSessions(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(studentId: String): List<Session> {
        val today = clock.todayIn(timeZone)
        return memberships.activeClassIds(studentId)
            .mapNotNull { classes.findClass(it) }
            .filterNot { it.isCancelled }
            .flatMap { classes.sessionsOn(it.id, today) }
            .filter { it.status == SessionStatus.SCHEDULED }
            .sortedWith(SESSION_ORDER)
    }
}

enum class CoachAttendanceStatus { PRESENT, NOT_SCANNED, ABSENT }

data class StudentAttendance(val studentId: String, val status: CoachAttendanceStatus)

data class SessionAttendance(val session: Session, val students: List<StudentAttendance>)

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

private val SESSION_ORDER: Comparator<Session> =
    compareBy<Session>({ it.date }, { it.slot.start }, { it.classId }, { it.sessionNumber })
