package ph.project.active.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.domain.crypto.parseDailyCodeLink
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.SessionRef
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.currentStatus

/**
 * Records PRESENT for the session the student chose. The phone checks the link format, that the
 * code's coach is the class coach, that the code's date is today, and that the chosen session is
 * today. It does not check the signature (flow 7 decision 1). The server checks [AttendanceRecord.codeSig] later.
 */
class RecordAttendance(
    private val classes: ClassRepository,
    private val memberships: MembershipRepository,
    private val attendance: AttendanceRepository,
    @Suppress("unused") private val verifyDailyCode: VerifyDailyCode,
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

        val link = parseDailyCodeLink(code) ?: return AttendanceResult.InvalidCode
        if (link.coachId != trxClass.coachId) return AttendanceResult.WrongCoach
        val today = clock.todayIn(timeZone)
        if (link.date != today) return AttendanceResult.WrongDate
        if (chosen.date != today) return AttendanceResult.NoSessionToday
        if (attendance.find(chosen.id, studentId) != null) return AttendanceResult.AlreadyRecorded

        val record = AttendanceRecord(
            id = attendance.nextId(),
            sessionId = chosen.id,
            classId = trxClass.id,
            studentId = studentId,
            scannedAt = clock.now(),
            uploadState = UploadState.PENDING,
            codeCoach = link.rawCoach,
            codeDate = link.rawDate,
            codeSig = link.sig,
        )
        attendance.add(record)
        return AttendanceResult.Recorded(record)
    }
}
