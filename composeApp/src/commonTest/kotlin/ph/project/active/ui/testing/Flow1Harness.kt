package ph.project.active.ui.testing

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import ph.project.active.data.db.TrxDatabase
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.FakeAuthRepository
import ph.project.active.data.fake.FakePasswordHasher
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.model.PersonProfile
import ph.project.active.data.repository.AuthRepository
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.data.repository.SqlClassRepository
import ph.project.active.data.repository.SqlMembershipRepository
import ph.project.active.data.repository.SqlPersonRepository
import ph.project.active.data.repository.SqlSessionStore
import ph.project.active.data.sync.OutboxSync
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.usecase.CreateClass
import ph.project.active.domain.usecase.RegisterCoach
import ph.project.active.domain.usecase.RegisterStudent
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel

class Flow1Harness(
    path: String? = null,
    createSchema: Boolean = true,
) : AutoCloseable {
    val network = MutableNetworkMonitor(NetworkStatus.ONLINE)
    val users = InMemoryUserRepository()
    val hasher = FakePasswordHasher()
    val driver: SqlDriver = if (path == null) {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    } else {
        JdbcSqliteDriver("jdbc:sqlite:$path")
    }
    val database: TrxDatabase
    val session: SqlSessionStore
    val people: SqlPersonRepository
    val classes: SqlClassRepository
    val memberships: SqlMembershipRepository
    val auth: FakeAuthRepository
    private val outbox: OutboxSync

    private val requestToJoin: ph.project.active.domain.usecase.RequestToJoin
    private var rootRef: RootViewModel? = null
    private var guestRef: GuestClassListViewModel? = null

    init {
        if (createSchema) TrxDatabase.Schema.create(driver)
        database = TrxDatabase(driver)
        session = SqlSessionStore(database)
        people = SqlPersonRepository(database)
        classes = SqlClassRepository(database)
        memberships = SqlMembershipRepository(database)
        val attendance = SqlAttendanceRepository(database)
        outbox = OutboxSync(attendance, FakeAttendanceUploader(), network)
        requestToJoin = ph.project.active.domain.usecase.RequestToJoin(classes, memberships)
        auth = FakeAuthRepository(users, hasher, network, session, people)
    }

    fun root(): RootViewModel = rootRef ?: RootViewModel(session, network, outbox).also { rootRef = it }

    fun guest(): GuestClassListViewModel = guestRef ?: GuestClassListViewModel(classes, people, network).also {
        guestRef = it
    }

    fun registration(
        origin: RegistrationOrigin,
        authRepository: AuthRepository = auth,
    ): RegistrationViewModel = RegistrationViewModel(
        origin = origin,
        auth = authRepository,
        session = session,
        people = people,
        classes = classes,
        requestToJoin = requestToJoin,
        network = network,
    )

    fun factory(authRepository: AuthRepository = auth): (RegistrationOrigin) -> RegistrationViewModel {
        val cache = mutableMapOf<RegistrationOrigin, RegistrationViewModel>()
        return { origin -> cache.getOrPut(origin) { registration(origin, authRepository) } }
    }

    fun coreBurn(): TrxClass {
        people.upsert(PersonProfile(CORE_BURN_COACH_ID, "Mara Santos", Role.COACH, null))
        return CreateClass(
            classes = classes,
            clock = Clock.System,
            timeZone = TimeZone.of("Asia/Manila"),
            openEndedHorizon = DatePeriod(months = 12),
        ).invoke(
            coachId = CORE_BURN_COACH_ID,
            name = Flow1Copy.className,
            visibility = Visibility.PUBLIC,
            slotsByWeekday = mapOf(
                DayOfWeek.MONDAY to listOf(EVENING),
                DayOfWeek.WEDNESDAY to listOf(EVENING),
            ),
            endDate = null,
        )
    }

    fun seedStudent(
        name: String = "Ella Navarro",
        email: String = "ella.navarro@acme.test",
        password: String = "password1",
        employeeId: String = "EMP-20611",
    ): User = RegisterStudent(users, hasher, requestToJoin).invoke(name, email, password, employeeId)

    fun seedCoach(
        name: String = "Mara Santos",
        email: String = "mara.santos@acme.test",
        password: String = "password1",
    ): User = RegisterCoach(users, hasher).invoke(name, email, password)

    override fun close() {
        driver.close()
    }
}

private const val CORE_BURN_COACH_ID = "coach-mara"
private val EVENING = TimeSlot(start = LocalTime(17, 30), end = LocalTime(18, 30))
