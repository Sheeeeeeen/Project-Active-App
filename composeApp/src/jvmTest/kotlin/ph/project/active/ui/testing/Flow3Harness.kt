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
import ph.project.active.domain.model.NetworkStatus
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
import ph.project.active.presentation.createclass.CreateClassViewModel
import ph.project.active.presentation.manageclass.ManageClassViewModel
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel

/**
 * Flow 3 test world: one in-memory database, a clock fixed on Mon 28 Sep 2026 at 9:00 AM
 * (Asia/Manila), and ViewModel factories for every screen on the NavHost. No Koin.
 */
class Flow3Harness : AutoCloseable {
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
    val auth: FakeAuthRepository
    private val outbox: OutboxSync
    private val requestToJoin: RequestToJoin
    private var rootRef: RootViewModel? = null
    private var guestRef: GuestClassListViewModel? = null
    private val registrations = mutableMapOf<RegistrationOrigin, RegistrationViewModel>()
    private var paoloRef: User? = null

    init {
        TrxDatabase.Schema.create(driver)
        database = TrxDatabase(driver)
        session = SqlSessionStore(database)
        people = SqlPersonRepository(database)
        classes = SqlClassRepository(database)
        memberships = SqlMembershipRepository(database)
        outbox = OutboxSync(SqlAttendanceRepository(database), FakeAttendanceUploader(), network)
        requestToJoin = RequestToJoin(classes, memberships)
        auth = FakeAuthRepository(users, hasher, network, session, people)
    }

    // People and classes

    fun coach(name: String): User =
        RegisterCoach(users, hasher).invoke(name, "${name.lowercase().replace(' ', '.')}@acme.test", "password1")
            .also { remember(it) }

    fun paolo(): User = paoloRef ?: RegisterStudent(users, hasher, requestToJoin).invoke(
        name = "Paolo Reyes",
        email = "paolo@company.com",
        password = "password1",
        employeeId = "EMP-20417",
    ).also {
        remember(it)
        paoloRef = it
    }

    fun paoloAccount(): SignedInAccount {
        val user = paolo()
        return SignedInAccount(user.id, user.name, user.email, user.role, user.employeeId)
    }

    /** Saves Paolo's session before anything is mounted, so the app starts on StudentHome. */
    fun signInAsPaolo() {
        session.save(paoloAccount())
    }

    fun saveClass(
        coach: User,
        name: String,
        weekdays: List<DayOfWeek> = listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        slot: TimeSlot = slot(17, 30, 18, 30),
        visibility: Visibility = Visibility.PUBLIC,
        endDate: LocalDate? = null,
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

    // Paolo's memberships

    fun join(classId: String) {
        val trxClass = classes.findClass(classId)!!
        if (trxClass.visibility == Visibility.PUBLIC) {
            requestToJoin(classId, paolo().id)
            ApproveRequest(memberships).invoke(classId, paolo().id)
        } else {
            invite(classId)
            accept(classId)
        }
    }

    /** Registers Paolo first (paolo() is cached), because an invite by employee ID needs an existing student. */
    fun invite(classId: String) {
        paolo()
        InviteByEmployeeId(users, classes, memberships).invoke(classId, "EMP-20417")
    }

    fun request(classId: String) {
        requestToJoin(classId, paolo().id)
    }

    fun accept(classId: String) {
        AcceptInvite(memberships).invoke(classId, paolo().id)
    }

    fun declineRequest(classId: String) {
        DeclineRequest(memberships).invoke(classId, paolo().id)
    }

    fun cancel(classId: String) {
        CancelClass(classes, clock, timeZone).invoke(classId)
    }

    // Spec section 7 example data

    /** C1: Joined on Sunrise, Lunch Express and Core Burn; invited to Mobility Flow; pending on Bootcamp. */
    fun seedC1(): Flow3Classes {
        val seeded = seedClasses()
        join(seeded.sunrise.id)
        join(seeded.lunchExpress.id)
        join(seeded.coreBurn.id)
        invite(seeded.mobilityFlow.id)
        request(seeded.bootcamp.id)
        return seeded
    }

    /** C2: as C1, but Bootcamp is invited (stored first) instead of pending. */
    fun seedC2(): Flow3Classes {
        val seeded = seedClasses()
        join(seeded.sunrise.id)
        join(seeded.lunchExpress.id)
        join(seeded.coreBurn.id)
        invite(seeded.bootcamp.id)
        invite(seeded.mobilityFlow.id)
        return seeded
    }

    fun seedClasses(): Flow3Classes {
        val mara = coach("Mara Santos")
        val leo = coach("Leo Ramirez")
        val jun = coach("Jun Tan")
        val ana = coach("Ana Cruz")
        paolo()
        return Flow3Classes(
            coreBurn = saveClass(mara, "TRX Core Burn", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30)),
            lunchExpress = saveClass(
                mara,
                "Lunch Express TRX",
                listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                slot(12, 15, 12, 45),
            ),
            mobilityFlow = saveClass(leo, "TRX Mobility Flow", listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), slot(19, 0, 20, 0)),
            everyDay20 = saveClass(jun, "TRX Every Day 20", DayOfWeek.entries, slot(6, 30, 6, 50)),
            bootcamp = saveClass(ana, "Weekend TRX Bootcamp", listOf(DayOfWeek.SATURDAY), slot(8, 0, 9, 30)),
            sunrise = saveClass(
                mara,
                "Sunrise TRX Strength",
                listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                slot(6, 0, 6, 45),
            ),
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

    // Flow 4: CoachHome's tabs need their own factories on the NavHost; flow 3 tests never open them.
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
    )

    fun manageClass(account: SignedInAccount, classId: String): ManageClassViewModel = ManageClassViewModel(
        account = account,
        classId = classId,
        classes = classes,
        memberships = memberships,
        attendance = SqlAttendanceRepository(database),
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
    )

    override fun close() {
        driver.close()
    }

    private fun remember(user: User) {
        people.upsert(PersonProfile(user.id, user.name, user.role, user.employeeId))
    }
}
