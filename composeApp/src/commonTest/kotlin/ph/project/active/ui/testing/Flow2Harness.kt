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
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.model.PersonProfile
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.SqlClassRepository
import ph.project.active.data.repository.SqlMembershipRepository
import ph.project.active.data.repository.SqlPersonRepository
import ph.project.active.data.fake.FakePasswordHasher
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.usecase.ApproveRequest
import ph.project.active.domain.usecase.CancelClass
import ph.project.active.domain.usecase.CreateClass
import ph.project.active.domain.usecase.DeclineRequest
import ph.project.active.domain.usecase.InviteByEmployeeId
import ph.project.active.domain.usecase.RegisterStudent
import ph.project.active.domain.usecase.RemoveFromRoster
import ph.project.active.domain.usecase.RequestToJoin
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.guestclasses.GuestClassListViewModel

class Flow2Harness : AutoCloseable {
    val timeZone: TimeZone = TimeZone.of("Asia/Manila")
    val clock = FixedClock(LocalDateTime(2026, 9, 28, 9, 0).toInstant(timeZone))
    val network = MutableNetworkMonitor(NetworkStatus.ONLINE)
    val users = InMemoryUserRepository()
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    val database: TrxDatabase
    val people: SqlPersonRepository
    val classes: SqlClassRepository
    val memberships: SqlMembershipRepository
    private val requestToJoin: RequestToJoin

    init {
        TrxDatabase.Schema.create(driver)
        database = TrxDatabase(driver)
        people = SqlPersonRepository(database)
        classes = SqlClassRepository(database)
        memberships = SqlMembershipRepository(database)
        requestToJoin = RequestToJoin(classes, memberships)
        people.upsert(PersonProfile(CORE_BURN_COACH_ID, "Mara Santos", Role.COACH, null))
    }

    fun setToday(date: LocalDate) {
        clock.current = LocalDateTime(date.year, date.month, date.day, 9, 0).toInstant(timeZone)
    }

    fun coreBurn(endDate: LocalDate? = LocalDate(2026, 10, 31)): TrxClass = saveClass(
        name = "TRX Core Burn",
        slots = mapOf(DayOfWeek.MONDAY to listOf(EVENING), DayOfWeek.WEDNESDAY to listOf(EVENING)),
        endDate = endDate,
    )

    fun saveClass(
        name: String,
        slots: Map<DayOfWeek, List<TimeSlot>> = mapOf(
            DayOfWeek.MONDAY to listOf(EVENING),
            DayOfWeek.WEDNESDAY to listOf(EVENING),
        ),
        visibility: Visibility = Visibility.PUBLIC,
        endDate: LocalDate? = LocalDate(2026, 10, 31),
    ): TrxClass = CreateClass(
        classes = classes,
        clock = clock,
        timeZone = timeZone,
        openEndedHorizon = DatePeriod(months = 12),
    ).invoke(
        coachId = CORE_BURN_COACH_ID,
        name = name,
        visibility = visibility,
        slotsByWeekday = slots,
        endDate = endDate,
    )

    fun guestList(): GuestClassListViewModel = GuestClassListViewModel(
        classes = classes,
        people = people,
        network = network,
        clock = clock,
        timeZone = timeZone,
    )

    fun detail(
        classId: String,
        viewer: SignedInAccount? = null,
        joinOutcome: JoinOutcome? = null,
    ): ClassDetailViewModel = ClassDetailViewModel(
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

    fun student(employeeId: String = "EMP-20417"): User {
        return RegisterStudent(users, FakePasswordHasher(), requestToJoin).invoke(
            name = "Ella Navarro",
            email = "$employeeId@acme.test",
            password = "password1",
            employeeId = employeeId,
        ).also { people.upsert(PersonProfile(it.id, it.name, it.role, it.employeeId)) }
    }

    fun account(user: User): SignedInAccount = SignedInAccount(user.id, user.name, user.email, user.role, user.employeeId)

    fun request(classId: String, studentId: String) = requestToJoin(classId, studentId)

    fun cancel(classId: String) = CancelClass(classes, clock, timeZone).invoke(classId)

    fun approve(classId: String, studentId: String) = ApproveRequest(memberships).invoke(classId, studentId)

    fun decline(classId: String, studentId: String) = DeclineRequest(memberships).invoke(classId, studentId)

    fun remove(classId: String, studentId: String) = RemoveFromRoster(memberships).invoke(classId, studentId)

    fun invite(classId: String, employeeId: String) = InviteByEmployeeId(users, classes, memberships).invoke(classId, employeeId)

    override fun close() {
        driver.close()
    }
}

private const val CORE_BURN_COACH_ID = "coach-mara"
private val EVENING = TimeSlot(LocalTime(17, 30), LocalTime(18, 30))
