package ph.project.active.testkit

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ph.project.active.data.repository.AttendanceUploader
import ph.project.active.data.repository.AuthRepository
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.FakeAuthRepository
import ph.project.active.data.fake.InMemorySecretKeyStore
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.sync.OutboxSync
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.data.repository.SqlClassRepository
import ph.project.active.data.repository.SqlCoachKeyRepository
import ph.project.active.data.repository.SqlMembershipRepository
import ph.project.active.data.repository.SqlPersonRepository
import ph.project.active.data.repository.SqlSessionStore
import ph.project.active.data.TestDb
import ph.project.active.data.inMemoryDb
import ph.project.active.domain.usecase.AcceptInvite
import ph.project.active.domain.usecase.ApproveRequest
import ph.project.active.domain.usecase.CancelClass
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.domain.usecase.CoachAttendanceForDate
import ph.project.active.domain.usecase.CreateClass
import ph.project.active.domain.usecase.DeclineInvite
import ph.project.active.domain.usecase.DeclineRequest
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.usecase.FlushOutbox
import ph.project.active.domain.usecase.GenerateDailyCode
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.usecase.InviteByEmployeeId
import ph.project.active.domain.usecase.ListVisibleClasses
import ph.project.active.domain.model.Membership
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.NetworkStatus
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
import ph.project.active.domain.repository.UserRepository
import ph.project.active.domain.usecase.VerifyDailyCode
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.attend.AttendViewModel
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.coachattendance.CoachAttendanceViewModel
import ph.project.active.presentation.coachclasses.CoachClassesViewModel
import ph.project.active.presentation.createclass.CreateClassViewModel
import ph.project.active.presentation.dailyqr.DailyQrViewModel
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.manageclass.ManageClassViewModel
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.presentation.studentsessions.StudentSessionsViewModel

/**
 * One [ph.project.active.data.db.TrxDatabase] shared by coach and student ViewModels.
 * Credentials stay on [users], an in-memory Phase 1 directory.
 */
class Phase2World(
    start: LocalDateTime = FRI_25_SEP_2026.atTime(9, 0),
    val timeZone: TimeZone = PHONE_TZ,
    openEndedHorizon: DatePeriod = DatePeriod(months = 12),
    val testDb: TestDb = inMemoryDb(),
    val network: MutableNetworkMonitor = MutableNetworkMonitor(NetworkStatus.ONLINE),
    val secrets: InMemorySecretKeyStore = InMemorySecretKeyStore(),
    val uploader: AttendanceUploader = FakeAttendanceUploader(),
    directory: InMemoryUserRepository = InMemoryUserRepository(),
) : AutoCloseable {
    val clock: FakeClock = FakeClock(start.toInstant(timeZone))
    val database = testDb.database
    val users: InMemoryUserRepository = directory
    val hasher: TestPasswordHasher = TestPasswordHasher()
    val classes: SqlClassRepository = SqlClassRepository(database)
    val memberships: SqlMembershipRepository = SqlMembershipRepository(database)
    val attendance: SqlAttendanceRepository = SqlAttendanceRepository(database)
    val people: SqlPersonRepository = SqlPersonRepository(database)
    val sessionStore: SqlSessionStore = SqlSessionStore(database)
    val coachKeys: SqlCoachKeyRepository = SqlCoachKeyRepository(database, secrets)
    val outbox: OutboxSync = OutboxSync(attendance, uploader, network)
    val auth: FakeAuthRepository = FakeAuthRepository(users, hasher, network, sessionStore, people)

    val requestToJoin: RequestToJoin = RequestToJoin(classes = classes, memberships = memberships)
    val registerStudent: RegisterStudent = RegisterStudent(users = users, hasher = hasher, requestToJoin = requestToJoin)
    val registerCoach: RegisterCoach = RegisterCoach(users = users, hasher = hasher)
    val signIn: SignIn = SignIn(users = users, hasher = hasher)
    val createClass: CreateClass = CreateClass(
        classes = classes,
        clock = clock,
        timeZone = timeZone,
        openEndedHorizon = openEndedHorizon,
    )
    val cancelDate: CancelDate = CancelDate(classes = classes, clock = clock, timeZone = timeZone)
    val cancelClass: CancelClass = CancelClass(classes = classes, clock = clock, timeZone = timeZone)
    val approveRequest: ApproveRequest = ApproveRequest(memberships = memberships)
    val declineRequest: DeclineRequest = DeclineRequest(memberships = memberships)
    val inviteByEmployeeId: InviteByEmployeeId = InviteByEmployeeId(
        users = users,
        classes = classes,
        memberships = memberships,
    )
    val acceptInvite: AcceptInvite = AcceptInvite(memberships = memberships)
    val declineInvite: DeclineInvite = DeclineInvite(memberships = memberships)
    val removeFromRoster: RemoveFromRoster = RemoveFromRoster(memberships = memberships)
    val generateDailyCode: GenerateDailyCode = GenerateDailyCode(keys = coachKeys)
    val verifyDailyCode: VerifyDailyCode = VerifyDailyCode(keys = coachKeys, clock = clock, timeZone = timeZone)
    val recordAttendance: RecordAttendance = RecordAttendance(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        verifyDailyCode = verifyDailyCode,
        clock = clock,
        timeZone = timeZone,
        networkStatus = { network.status.value },
    )
    val flushOutbox: FlushOutbox = FlushOutbox(
        attendance = attendance,
        networkStatus = { network.status.value },
    )
    val listVisibleClasses: ListVisibleClasses = ListVisibleClasses(classes = classes, memberships = memberships)
    val studentSessions: StudentSessions = StudentSessions(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        clock = clock,
        timeZone = timeZone,
    )
    val todaysChoosableSessions: TodaysChoosableSessions = TodaysChoosableSessions(
        classes = classes,
        memberships = memberships,
        clock = clock,
        timeZone = timeZone,
    )
    val coachAttendanceForDate: CoachAttendanceForDate = CoachAttendanceForDate(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        clock = clock,
        timeZone = timeZone,
    )

    override fun close() {
        testDb.close()
    }

    fun setLocal(dateTime: LocalDateTime) {
        clock.current = dateTime.toInstant(timeZone)
    }

    fun setLocal(date: LocalDate, hour: Int, minute: Int = 0) {
        setLocal(date.atTime(hour, minute))
    }

    fun today(): LocalDate = clock.current.toLocalDateTime(timeZone).date

    fun remember(user: User) {
        people.upsert(user.toProfile())
    }

    fun student(n: Int = 1, targetClassId: String? = null): User {
        val user = registerStudent(
            name = "Student $n",
            email = "student$n@trx.test",
            password = "pw-student-$n",
            employeeId = "EMP-$n",
            requestToJoinClassId = targetClassId,
        )
        remember(user)
        return user
    }

    fun coach(n: Int = 1): User {
        val user = registerCoach(
            name = "Coach $n",
            email = "coach$n@trx.test",
            password = "pw-coach-$n",
        )
        remember(user)
        return user
    }

    fun coachAccount(n: Int = 1): SignedInAccount = coach(n).toAccount()

    fun studentAccount(n: Int = 1, targetClassId: String? = null): SignedInAccount =
        student(n, targetClassId).toAccount()

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

    fun createTwoSlotMondayClass(coachId: String, endDate: LocalDate? = SAT_31_OCT_2026): TrxClass = createClass(
        coachId = coachId,
        name = "TRX Double Monday",
        visibility = Visibility.PUBLIC,
        slotsByWeekday = mapOf(
            DayOfWeek.MONDAY to listOf(EVENING, MORNING),
            DayOfWeek.WEDNESDAY to listOf(EVENING),
        ),
        endDate = endDate,
    )

    fun makeActive(classId: String, studentId: String) {
        val trxClass = classes.findClass(classId)!!
        if (trxClass.visibility == Visibility.PUBLIC) {
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

    fun refOf(session: Session): SessionRef = SessionRef(classId = session.classId, date = session.date, slot = session.slot)

    fun statusOf(classId: String, studentId: String): MembershipStatus? =
        memberships.forClass(classId).lastOrNull { it.studentId == studentId }?.status

    fun membershipsOf(classId: String, studentId: String): List<Membership> =
        memberships.forClass(classId).filter { it.studentId == studentId }

    fun giveKeys(coachId: String): Ed25519KeyPair = Ed25519KeyPair.generate().also { coachKeys.save(coachId, it) }

    fun todaysCode(coachId: String): String = generateDailyCode(coachId = coachId, date = today())

    fun root(): RootViewModel = RootViewModel(
        session = sessionStore,
        network = network,
        outbox = outbox,
    )

    fun registration(
        origin: RegistrationOrigin,
        authRepository: AuthRepository = auth,
    ): RegistrationViewModel = RegistrationViewModel(
        origin = origin,
        auth = authRepository,
        session = sessionStore,
        people = people,
        classes = classes,
        requestToJoin = requestToJoin,
        network = network,
    )

    fun guestClasses(): GuestClassListViewModel = GuestClassListViewModel(
        classes = classes,
        people = people,
        network = network,
    )

    fun classDetail(classId: String, viewer: SignedInAccount?): ClassDetailViewModel =
        ClassDetailViewModel(
            classId = classId,
            viewer = viewer,
            classes = classes,
            memberships = memberships,
            people = people,
            requestToJoin = requestToJoin,
            network = network,
        )

    fun studentClasses(account: SignedInAccount): StudentClassesViewModel =
        StudentClassesViewModel(
            account = account,
            classes = classes,
            memberships = memberships,
            people = people,
            network = network,
        )

    fun attend(account: SignedInAccount): AttendViewModel = AttendViewModel(
        account = account,
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        people = people,
        recordAttendance = recordAttendance,
        outbox = outbox,
        clock = clock,
        timeZone = timeZone,
        network = network,
    )

    fun studentSessionsVm(account: SignedInAccount): StudentSessionsViewModel =
        StudentSessionsViewModel(
            account = account,
            classes = classes,
            memberships = memberships,
            attendance = attendance,
            clock = clock,
            timeZone = timeZone,
            network = network,
        )

    fun invites(account: SignedInAccount): InvitesViewModel = InvitesViewModel(
        account = account,
        classes = classes,
        memberships = memberships,
        people = people,
        acceptInvite = acceptInvite,
        declineInvite = declineInvite,
        network = network,
    )

    fun coachClasses(account: SignedInAccount): CoachClassesViewModel =
        CoachClassesViewModel(
            account = account,
            classes = classes,
            memberships = memberships,
            network = network,
        )

    fun createClassVm(account: SignedInAccount): CreateClassViewModel =
        CreateClassViewModel(
            account = account,
            classes = classes,
            clock = clock,
            timeZone = timeZone,
            network = network,
        )

    fun manage(
        account: SignedInAccount,
        classId: String,
        directory: UserRepository = users,
    ): ManageClassViewModel = ManageClassViewModel(
        account = account,
        classId = classId,
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        people = people,
        users = directory,
        approveRequest = approveRequest,
        declineRequest = declineRequest,
        removeFromRoster = removeFromRoster,
        inviteByEmployeeId = inviteByEmployeeId,
        cancelDate = cancelDate,
        cancelClass = cancelClass,
        clock = clock,
        timeZone = timeZone,
        network = network,
    )

    fun dailyQr(account: SignedInAccount): DailyQrViewModel = DailyQrViewModel(
        account = account,
        classes = classes,
        keys = coachKeys,
        people = people,
        clock = clock,
        timeZone = timeZone,
        network = network,
    )

    fun coachAttendance(account: SignedInAccount): CoachAttendanceViewModel =
        CoachAttendanceViewModel(
            account = account,
            classes = classes,
            memberships = memberships,
            attendance = attendance,
            people = people,
            clock = clock,
            timeZone = timeZone,
            network = network,
        )

}
