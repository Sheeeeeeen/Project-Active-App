package ph.project.active.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.model.DailyCodeCheck
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.repository.currentStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.SessionRef

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
