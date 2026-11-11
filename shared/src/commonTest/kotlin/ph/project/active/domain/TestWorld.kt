package ph.project.active.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ph.project.active.domain.usecase.AcceptInvite
import ph.project.active.domain.usecase.ApproveRequest
import ph.project.active.domain.usecase.CancelClass
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.domain.usecase.CoachAttendanceForDate
import ph.project.active.domain.usecase.CreateClass
import ph.project.active.domain.usecase.DeclineInvite
import ph.project.active.domain.usecase.DeclineRequest
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.domain.usecase.FlushOutbox
import ph.project.active.domain.usecase.GenerateDailyCode
import ph.project.active.domain.repository.inmemory.InMemoryAttendanceRepository
import ph.project.active.domain.repository.inmemory.InMemoryClassRepository
import ph.project.active.domain.repository.inmemory.InMemoryCoachKeyRepository
import ph.project.active.domain.repository.inmemory.InMemoryMembershipRepository
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.usecase.InviteByEmployeeId
import ph.project.active.domain.usecase.ListVisibleClasses
import ph.project.active.domain.model.Membership
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.crypto.PasswordHasher
import ph.project.active.domain.usecase.RecordAttendance
import ph.project.active.domain.usecase.RegisterCoach
import ph.project.active.domain.usecase.RegisterStudent
import ph.project.active.domain.usecase.RemoveFromRoster
import ph.project.active.domain.usecase.RequestToJoin
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.SessionRef
import ph.project.active.domain.usecase.SignIn
import ph.project.active.domain.usecase.StudentSessions
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.usecase.TodaysChoosableSessions
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.usecase.VerifyDailyCode
import ph.project.active.domain.model.Visibility

// ---------------------------------------------------------------------------------------------
// Test-only helpers. Everything referenced here that is not declared in this file is production
// API that Builder implements in shared/src/commonMain/kotlin/trx/attendance/domain/.
// ---------------------------------------------------------------------------------------------

/** Phone timezone used by every test ("today" and "the day is over" are evaluated here). */
val PHONE_TZ: TimeZone = TimeZone.of("Asia/Manila")

val FRI_25_SEP_2026 = LocalDate(2026, 9, 25)
val MON_28_SEP_2026 = LocalDate(2026, 9, 28)
val TUE_29_SEP_2026 = LocalDate(2026, 9, 29)
val WED_30_SEP_2026 = LocalDate(2026, 9, 30)
val SAT_31_OCT_2026 = LocalDate(2026, 10, 31)

val EVENING = TimeSlot(start = LocalTime(17, 30), end = LocalTime(18, 30))
val MORNING = TimeSlot(start = LocalTime(7, 0), end = LocalTime(8, 0))

val MON_WED_EVENING: Map<DayOfWeek, List<TimeSlot>> =
    mapOf(DayOfWeek.MONDAY to listOf(EVENING), DayOfWeek.WEDNESDAY to listOf(EVENING))

/** Mutable clock injected into every time-sensitive use case. */
class FakeClock(var current: Instant) : Clock {
    override fun now(): Instant = current
}

/**
 * Deterministic fake. The hash never contains the plain-text password as a substring
 * (for the non-palindromic passwords used in these tests).
 */
class FakePasswordHasher : PasswordHasher {
    override fun hash(password: String): String = "hash:" + password.reversed()
    override fun verify(password: String, hash: String): Boolean = hash == hash(password)
}

/**
 * Wires every Phase 1 use case to in-memory repositories, a [FakeClock] in [PHONE_TZ],
 * and a switchable network status. Default start: Friday 25 Sep 2026, 09:00 local.
 */
class TestWorld(
    start: LocalDateTime = FRI_25_SEP_2026.atTime(9, 0),
    val timeZone: TimeZone = PHONE_TZ,
    openEndedHorizon: DatePeriod = DatePeriod(months = 12),
) {
    val clock = FakeClock(start.toInstant(timeZone))
    var network: NetworkStatus = NetworkStatus.ONLINE
    val hasher = FakePasswordHasher()

    // In-memory repositories (production fakes for Phase 1, provided by Builder).
    val users = InMemoryUserRepository()
    val classes = InMemoryClassRepository()
    val memberships = InMemoryMembershipRepository()
    val attendance = InMemoryAttendanceRepository()
    val coachKeys = InMemoryCoachKeyRepository()

    // Accounts
    val requestToJoin = RequestToJoin(classes = classes, memberships = memberships)
    val registerStudent = RegisterStudent(users = users, hasher = hasher, requestToJoin = requestToJoin)
    val registerCoach = RegisterCoach(users = users, hasher = hasher)
    val signIn = SignIn(users = users, hasher = hasher)

    // Classes and sessions
    val createClass = CreateClass(
        classes = classes,
        clock = clock,
        timeZone = timeZone,
        openEndedHorizon = openEndedHorizon,
    )
    val cancelDate = CancelDate(classes = classes, clock = clock, timeZone = timeZone)
    val cancelClass = CancelClass(classes = classes, clock = clock, timeZone = timeZone)

    // Membership
    val approveRequest = ApproveRequest(memberships = memberships)
    val declineRequest = DeclineRequest(memberships = memberships)
    val inviteByEmployeeId = InviteByEmployeeId(users = users, classes = classes, memberships = memberships)
    val acceptInvite = AcceptInvite(memberships = memberships)
    val declineInvite = DeclineInvite(memberships = memberships)
    val removeFromRoster = RemoveFromRoster(memberships = memberships)

    // Daily code
    val generateDailyCode = GenerateDailyCode(keys = coachKeys)
    val verifyDailyCode = VerifyDailyCode(keys = coachKeys, clock = clock, timeZone = timeZone)

    // Attendance
    val recordAttendance = RecordAttendance(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        verifyDailyCode = verifyDailyCode,
        clock = clock,
        timeZone = timeZone,
        networkStatus = { network },
    )
    val flushOutbox = FlushOutbox(attendance = attendance, networkStatus = { network })

    // Queries
    val listVisibleClasses = ListVisibleClasses(classes = classes, memberships = memberships)
    val studentSessions = StudentSessions(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        clock = clock,
        timeZone = timeZone,
    )
    val todaysChoosableSessions = TodaysChoosableSessions(
        classes = classes,
        memberships = memberships,
        clock = clock,
        timeZone = timeZone,
    )
    val coachAttendanceForDate = CoachAttendanceForDate(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        clock = clock,
        timeZone = timeZone,
    )

    // ----- helpers -----

    fun setLocal(dateTime: LocalDateTime) {
        clock.current = dateTime.toInstant(timeZone)
    }

    fun setLocal(date: LocalDate, hour: Int, minute: Int = 0) = setLocal(date.atTime(hour, minute))

    fun student(n: Int = 1, targetClassId: String? = null): User = registerStudent(
        name = "Student $n",
        email = "student$n@trx.test",
        password = "pw-student-$n",
        employeeId = "EMP-$n",
        requestToJoinClassId = targetClassId,
    )

    fun coach(n: Int = 1): User = registerCoach(
        name = "Coach $n",
        email = "coach$n@trx.test",
        password = "pw-coach-$n",
    )

    fun createMonWedEveningClass(
        coachId: String,
        visibility: Visibility = Visibility.PUBLIC,
        endDate: LocalDate? = SAT_31_OCT_2026,
        name: String = "TRX Evening",
    ): TrxClass = createClass(
        coachId = coachId,
        name = name,
        visibility = visibility,
        slotsByWeekday = MON_WED_EVENING,
        endDate = endDate,
    )

    /** Monday with a morning and an evening slot; Wednesday with an evening slot. */
    fun createTwoSlotMondayClass(coachId: String, endDate: LocalDate? = SAT_31_OCT_2026): TrxClass = createClass(
        coachId = coachId,
        name = "TRX Double Monday",
        visibility = Visibility.PUBLIC,
        slotsByWeekday = mapOf(
            DayOfWeek.MONDAY to listOf(EVENING, MORNING), // deliberately unsorted
            DayOfWeek.WEDNESDAY to listOf(EVENING),
        ),
        endDate = endDate,
    )

    /** Puts [studentId] on the roster with ACTIVE membership through the public request flow. */
    fun makeActive(classId: String, studentId: String) {
        val cls = classes.findClass(classId)!!
        if (cls.visibility == Visibility.PUBLIC) {
            requestToJoin(classId = classId, studentId = studentId)
            approveRequest(classId = classId, studentId = studentId)
        } else {
            val employeeId = users.all().single { it.id == studentId }.employeeId!!
            inviteByEmployeeId(classId = classId, employeeId = employeeId)
            acceptInvite(classId = classId, studentId = studentId)
        }
    }

    fun sessionsOf(classId: String): List<Session> = classes.sessionsOf(classId).sortedBy { it.sessionNumber }

    fun sessionOn(classId: String, date: LocalDate, slot: TimeSlot = EVENING): Session =
        classes.sessionsOf(classId).single { it.date == date && it.slot == slot }

    fun refOf(session: Session) = SessionRef(classId = session.classId, date = session.date, slot = session.slot)

    /** Latest membership status of [studentId] on [classId], or null if none was ever created. */
    fun statusOf(classId: String, studentId: String): MembershipStatus? =
        memberships.forClass(classId).lastOrNull { it.studentId == studentId }?.status

    fun membershipsOf(classId: String, studentId: String): List<Membership> =
        memberships.forClass(classId).filter { it.studentId == studentId }

    fun giveKeys(coachId: String): Ed25519KeyPair = Ed25519KeyPair.generate().also { coachKeys.save(coachId, it) }

    fun todaysCode(coachId: String): String = generateDailyCode(coachId = coachId, date = today())

    fun today(): LocalDate = clock.current.toLocalDateTime(timeZone).date
}
