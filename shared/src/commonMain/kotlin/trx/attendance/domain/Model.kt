package trx.attendance.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

enum class Role { STUDENT, COACH }

/** A registered account. The role is fixed at creation (`val`, no setter, no use case changes it). */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
    /** Students only. Coaches always have `null`. */
    val employeeId: String?,
    /** Only the hash is kept; the plain-text password never reaches the domain model. */
    val passwordHash: String,
) {
    override fun toString(): String =
        "User(id=$id, name=$name, email=$email, role=$role, employeeId=$employeeId, passwordHash=***)"
}

enum class Visibility { PUBLIC, PRIVATE }

data class TimeSlot(val start: LocalTime, val end: LocalTime) {
    init {
        require(start < end) { "A time slot must start before it ends" }
    }
}

/** Named `TrxClass` so it never clashes with `java.lang.Class`. */
data class TrxClass(
    val id: String,
    val coachId: String,
    val name: String,
    val visibility: Visibility,
    val slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
    /** `null` means open-ended: the class stays open until cancelled. */
    val endDate: LocalDate?,
    /** Local date (phone timezone) the class was saved on. */
    val createdOn: LocalDate,
    val cancelledAt: Instant? = null,
) {
    val isCancelled: Boolean get() = cancelledAt != null
}

enum class SessionStatus { SCHEDULED, CANCELLED }

/** One occurrence of a class: one date and one time slot. Attendance is recorded per session. */
data class Session(
    val id: String,
    val classId: String,
    val date: LocalDate,
    val slot: TimeSlot,
    val sessionNumber: Int,
    val status: SessionStatus = SessionStatus.SCHEDULED,
)

/** What the student chose before scanning: class + date + slot. */
data class SessionRef(val classId: String, val date: LocalDate, val slot: TimeSlot)

enum class MembershipStatus(val isJoined: Boolean) {
    PENDING_REQUEST(false),
    INVITED(false),
    ACTIVE(true),
    DECLINED(false),
    REMOVED(false),
}

/** One membership record. A student's latest record for a class is their current status. */
data class Membership(
    val id: String,
    val classId: String,
    val studentId: String,
    val status: MembershipStatus,
)

enum class AttendanceStatus { PRESENT }

enum class UploadState { PENDING, SYNCED }

data class AttendanceRecord(
    val id: String,
    val sessionId: String,
    val classId: String,
    val studentId: String,
    /** The scan time on the phone, never the later upload time. */
    val scannedAt: Instant,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val uploadState: UploadState = UploadState.PENDING,
)

enum class NetworkStatus { ONLINE, OFFLINE }

/** Thrown by [SignIn] for an unknown email or a wrong password (deliberately the same error). */
class AuthFailed : Exception("Email or password is incorrect")

interface PasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}
