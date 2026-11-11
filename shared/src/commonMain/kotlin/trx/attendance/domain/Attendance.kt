package trx.attendance.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

sealed interface AttendanceResult {
    data class Recorded(val attendance: AttendanceRecord) : AttendanceResult
    data object AlreadyRecorded : AttendanceResult
    data object NoSessionChosen : AttendanceResult
    /** The chosen class/date/slot is not in the local store. */
    data object SessionUnknown : AttendanceResult
    /** The whole class was cancelled (wins over [SessionCancelled]). */
    data object ClassCancelled : AttendanceResult
    /** This date was cancelled while the class is still open. */
    data object SessionCancelled : AttendanceResult
    /** The student's current membership is not ACTIVE (or not in the local store). */
    data object NotOnRoster : AttendanceResult
    data object InvalidCode : AttendanceResult
    data object WrongCoach : AttendanceResult
    /** The code is for another date, or the chosen session is not today. */
    data object WrongDate : AttendanceResult
}

/**
 * AC32-AC36, AC40: records PRESENT for the session the student chose, after the daily code
 * verifies for that session's coach and today's date. Everything is checked against the local
 * store, so a valid scan is kept as PENDING even while offline; [FlushOutbox] uploads it later.
 */
class RecordAttendance(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    private val verifyDailyCode: VerifyDailyCode,
    private val clock: Clock,
    private val timeZone: TimeZone,
    @Suppress("unused") private val networkStatus: () -> NetworkStatus,
) {
    operator fun invoke(studentId: String, session: SessionRef?, code: String): AttendanceResult {
        if (session == null) return AttendanceResult.NoSessionChosen
        val trxClass = classes.findClass(session.classId) ?: return AttendanceResult.SessionUnknown
        val chosen = classes.findSession(session) ?: return AttendanceResult.SessionUnknown
        if (trxClass.isCancelled) return AttendanceResult.ClassCancelled
        if (chosen.status == SessionStatus.CANCELLED) return AttendanceResult.SessionCancelled
        if (memberships.currentStatus(trxClass.id, studentId) != MembershipStatus.ACTIVE) {
            return AttendanceResult.NotOnRoster
        }

        when (verifyDailyCode.check(code, expectedCoachId = trxClass.coachId)) {
            DailyCodeCheck.VALID -> Unit
            DailyCodeCheck.INVALID -> return AttendanceResult.InvalidCode
            DailyCodeCheck.WRONG_COACH -> return AttendanceResult.WrongCoach
            DailyCodeCheck.WRONG_DATE -> return AttendanceResult.WrongDate
        }
        if (chosen.date != clock.todayIn(timeZone)) return AttendanceResult.WrongDate
        if (attendance.find(chosen.id, studentId) != null) return AttendanceResult.AlreadyRecorded

        val record = AttendanceRecord(
            id = attendance.nextId(),
            sessionId = chosen.id,
            classId = trxClass.id,
            studentId = studentId,
            scannedAt = clock.now(),
            uploadState = UploadState.PENDING,
        )
        attendance.add(record)
        return AttendanceResult.Recorded(record)
    }
}

/** AC41: while ONLINE, marks pending records SYNCED (scannedAt is untouched). Offline: no-op. */
class FlushOutbox(
    private val attendance: AttendanceRepository,
    private val networkStatus: () -> NetworkStatus,
) {
    /** Returns how many records were marked SYNCED. */
    operator fun invoke(): Int {
        if (networkStatus() != NetworkStatus.ONLINE) return 0
        val pending = attendance.all().filter { it.uploadState == UploadState.PENDING }
        pending.forEach { attendance.update(it.copy(uploadState = UploadState.SYNCED)) }
        return pending.size
    }
}
