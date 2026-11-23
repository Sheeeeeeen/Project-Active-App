package ph.project.active.ui.testing

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ph.project.active.data.db.TrxDatabase
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.FakeAuthRepository
import ph.project.active.data.fake.FakePasswordHasher
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.model.PersonProfile
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.data.repository.SqlClassRepository
import ph.project.active.data.repository.SqlMembershipRepository
import ph.project.active.data.repository.SqlPersonRepository
import ph.project.active.data.repository.SqlSessionStore
import ph.project.active.data.sync.OutboxSync
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.usecase.AcceptInvite
import ph.project.active.domain.usecase.ApproveRequest
import ph.project.active.domain.usecase.CancelClass
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.domain.usecase.CreateClass
import ph.project.active.domain.usecase.DeclineInvite
import ph.project.active.domain.usecase.DeclineRequest
import ph.project.active.domain.usecase.InviteByEmployeeId
import ph.project.active.domain.usecase.RegisterCoach
import ph.project.active.domain.usecase.RegisterStudent
import ph.project.active.domain.usecase.RemoveFromRoster
import ph.project.active.domain.usecase.RequestToJoin
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.coachclasses.CoachClassesViewModel
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.createclass.CreateClassViewModel
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.manageclass.ManageClassViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel

/**
 * Flow 4 test world: one in-memory database, a clock fixed on Mon 28 Sep 2026 at 9:00 AM
 * (Asia/Manila), coach Mara Santos, and ViewModel factories for every screen on the NavHost. No Koin.
 * The create and manage factories remember the last ViewModel they made, so a test can fill the
 * platform time and date pickers through the ViewModel.
 */
class Flow4Harness : AutoCloseable {
    val timeZone: TimeZone = TimeZone.of("Asia/Manila")
    val clock = FixedClock(LocalDateTime(2026, 9, 28, 9, 0).toInstant(timeZone))
    val network = MutableNetworkMonitor(NetworkStatus.ONLINE)
    val users = InMemoryUserRepository()
    val hasher = FakePasswordHasher()
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    val database: TrxDatabase
    val session: SqlSessionStore
    val people: SqlPersonRepository
    val classes: SqlClassRepository
    val memberships: SqlMembershipRepository
    val attendance: SqlAttendanceRepository
    val auth: FakeAuthRepository
    var lastCreateClass: CreateClassViewModel? = null
        private set
    var lastManage: ManageClassViewModel? = null
        private set
    private val outbox: OutboxSync
    private val requestToJoin: RequestToJoin
    private var rootRef: RootViewModel? = null
    private var guestRef: GuestClassListViewModel? = null
    private val registrations = mutableMapOf<RegistrationOrigin, RegistrationViewModel>()
    private var maraRef: User? = null

    init {
        TrxDatabase.Schema.create(driver)
        database = TrxDatabase(driver)
        session = SqlSessionStore(database)
        people = SqlPersonRepository(database)
        classes = SqlClassRepository(database)
        memberships = SqlMembershipRepository(database)
        attendance = SqlAttendanceRepository(database)
        outbox = OutboxSync(attendance, FakeAttendanceUploader(), network)
        requestToJoin = RequestToJoin(classes, memberships)
        auth = FakeAuthRepository(users, hasher, network, session, people)
    }

    // People

    fun mara(): User = maraRef ?: RegisterCoach(users, hasher).invoke("Mara Santos", "mara@company.com", "password1")
        .also {
            remember(it)
            maraRef = it
        }

    fun maraAccount(): SignedInAccount {
        val user = mara()
        return SignedInAccount(user.id, user.name, user.email, user.role, user.employeeId)
    }

    /** Saves Mara's session before anything is mounted, so the app starts on CoachHome. */
    fun signInAsMara() {
        session.save(maraAccount())
    }

    fun coach(name: String): User =
        RegisterCoach(users, hasher).invoke(name, "${name.lowercase().replace(' ', '.')}@acme.test", "password1")
            .also { remember(it) }

    fun student(name: String, employeeId: String): User = RegisterStudent(users, hasher, requestToJoin).invoke(
        name = name,
        email = "${name.lowercase().replace(' ', '.')}@acme.test",
        password = "password1",
        employeeId = employeeId,
    ).also { remember(it) }

    // Classes and the clock

    fun saveClass(
        name: String,
        weekdays: List<DayOfWeek> = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        slot: TimeSlot = slot(17, 30, 18, 30),
        visibility: Visibility = Visibility.PUBLIC,
        endDate: LocalDate? = null,
        coach: User = mara(),
    ): TrxClass = CreateClass(
        classes = classes,
        clock = clock,
        timeZone = timeZone,
        openEndedHorizon = DatePeriod(months = 12),
    ).invoke(
        coachId = coach.id,
        name = name,
        visibility = visibility,
        slotsByWeekday = weekdays.associateWith { listOf(slot) },
        endDate = endDate,
    )

    fun slot(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot =
        TimeSlot(LocalTime(startHour, startMinute), LocalTime(endHour, endMinute))

    fun setToday(date: LocalDate) {
        clock.current = LocalDateTime(date.year, date.month, date.day, 9, 0).toInstant(timeZone)
    }

    fun cancel(classId: String) {
        CancelClass(classes, clock, timeZone).invoke(classId)
    }

    // Memberships

    fun request(classId: String, student: User) {
        requestToJoin(classId, student.id)
    }

    fun approve(classId: String, student: User) {
        ApproveRequest(memberships).invoke(classId, student.id)
    }

    fun invite(classId: String, student: User) {
        InviteByEmployeeId(users, classes, memberships).invoke(classId, student.employeeId!!)
    }

    fun accept(classId: String, student: User) {
        AcceptInvite(memberships).invoke(classId, student.id)
    }

    fun joinByRequest(classId: String, student: User) {
        request(classId, student)
        approve(classId, student)
    }

    fun joinByInvite(classId: String, student: User) {
        invite(classId, student)
        accept(classId, student)
    }

    /**
     * A past attendance record, saved straight to the repository. It is already uploaded (SYNCED),
     * so the start-up outbox sync leaves it alone (F4-AC20).
     */
    fun recordAttendance(classId: String, student: User, date: LocalDate): AttendanceRecord {
        val session = classes.sessionsOf(classId).first { it.date == date }
        val record = AttendanceRecord(
            id = attendance.nextId(),
            sessionId = session.id,
            classId = classId,
            studentId = student.id,
            scannedAt = LocalDateTime(date.year, date.month, date.day, 17, 35).toInstant(timeZone),
            uploadState = UploadState.SYNCED,
        )
        attendance.add(record)
        return record
    }

    // Spec section 7 example data

    /**
     * D1 and Manage example data. The clock stays on Mon 28 Sep 2026; Core Burn ends Sat 31 Oct 2026,
     * so it has 10 dates. The private class has no students. The cancelled class is public.
     */
    fun seedD1(): Flow4Example {
        val mara = mara()
        val coreBurn = saveClass("TRX Core Burn", endDate = LocalDate(2026, 10, 31))
        val lunchExpress = saveClass(
            "Lunch Express TRX",
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(12, 15, 12, 45),
        )
        val sunrise = saveClass(
            "Sunrise TRX Strength",
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            slot(6, 0, 6, 45),
        )
        val privateClass = saveClass(
            "TRX Private Strength",
            listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(19, 0, 20, 0),
            visibility = Visibility.PRIVATE,
        )
        val cancelledClass = saveClass("TRX Saturday Express", listOf(DayOfWeek.SATURDAY), slot(8, 0, 9, 0))
        cancel(cancelledClass.id)

        val paolo = student("Paolo Reyes", "EMP-20417")
        val dana = student("Dana Uy", "EMP-20155")
        val gia = student("Gia Torres", "EMP-20249")
        val hana = student("Hana Cho", "EMP-20877")
        joinByRequest(coreBurn.id, paolo)
        joinByInvite(coreBurn.id, dana)
        joinByInvite(coreBurn.id, gia)
        joinByRequest(coreBurn.id, hana)
        val ella = student("Ella Navarro", "EMP-20611")
        val bea = student("Bea Lim", "EMP-20388")
        val carlo = student("Carlo Diaz", "EMP-20502")
        request(coreBurn.id, ella)
        request(coreBurn.id, bea)
        request(coreBurn.id, carlo)
        val felix = student("Felix Ong", "EMP-20733")

        for (n in 1..8) joinByRequest(lunchExpress.id, student("Lunch Student $n", "EMP-3100$n"))
        for (n in 1..12) joinByRequest(sunrise.id, student("Sunrise Student $n", "EMP-32${n.toString().padStart(3, '0')}"))

        return Flow4Example(
            mara = mara,
            coreBurn = coreBurn,
            lunchExpress = lunchExpress,
            sunrise = sunrise,
            privateClass = privateClass,
            cancelledClass = classes.findClass(cancelledClass.id)!!,
            paolo = paolo,
            dana = dana,
            gia = gia,
            hana = hana,
            ella = ella,
            bea = bea,
            carlo = carlo,
            felix = felix,
        )
    }

    // ViewModels for the NavHost

    fun root(): RootViewModel = rootRef ?: RootViewModel(session, network, outbox).also { rootRef = it }

    fun guest(): GuestClassListViewModel = guestRef ?: GuestClassListViewModel(
        classes = classes,
        people = people,
        network = network,
        clock = clock,
        timeZone = timeZone,
    ).also { guestRef = it }

    fun registration(origin: RegistrationOrigin): RegistrationViewModel = registrations.getOrPut(origin) {
        RegistrationViewModel(
            origin = origin,
            auth = auth,
            session = session,
            people = people,
            classes = classes,
            requestToJoin = requestToJoin,
            network = network,
        )
    }

    fun detail(classId: String, viewer: SignedInAccount?, joinOutcome: JoinOutcome?): ClassDetailViewModel =
        ClassDetailViewModel(
            classId = classId,
            viewer = viewer,
            classes = classes,
            memberships = memberships,
            people = people,
            requestToJoin = requestToJoin,
            network = network,
            clock = clock,
            timeZone = timeZone,
            joinOutcome = joinOutcome,
        )

    fun studentClasses(account: SignedInAccount): StudentClassesViewModel = StudentClassesViewModel(
        account = account,
        classes = classes,
        memberships = memberships,
        people = people,
        network = network,
        clock = clock,
        timeZone = timeZone,
    )

    fun invites(account: SignedInAccount): InvitesViewModel = InvitesViewModel(
        account = account,
        classes = classes,
        memberships = memberships,
        people = people,
        acceptInvite = AcceptInvite(memberships),
        declineInvite = DeclineInvite(memberships),
        network = network,
    )

    fun coachClasses(account: SignedInAccount): CoachClassesViewModel = CoachClassesViewModel(
        account = account,
        classes = classes,
        memberships = memberships,
        network = network,
    )

    fun createClass(account: SignedInAccount): CreateClassViewModel = CreateClassViewModel(
        account = account,
        classes = classes,
        clock = clock,
        timeZone = timeZone,
        network = network,
    ).also { lastCreateClass = it }

    fun manageClass(account: SignedInAccount, classId: String): ManageClassViewModel = ManageClassViewModel(
        account = account,
        classId = classId,
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        people = people,
        users = users,
        approveRequest = ApproveRequest(memberships),
        declineRequest = DeclineRequest(memberships),
        removeFromRoster = RemoveFromRoster(memberships),
        inviteByEmployeeId = InviteByEmployeeId(users, classes, memberships),
        cancelDate = CancelDate(classes, clock, timeZone),
        cancelClass = CancelClass(classes, clock, timeZone),
        clock = clock,
        timeZone = timeZone,
        network = network,
    ).also { lastManage = it }

    override fun close() {
        driver.close()
    }

    private fun remember(user: User) {
        people.upsert(PersonProfile(user.id, user.name, user.role, user.employeeId))
    }
}
