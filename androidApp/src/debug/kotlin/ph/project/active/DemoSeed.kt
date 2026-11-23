package ph.project.active

import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.koin.core.context.GlobalContext
import ph.project.active.data.model.PersonProfile
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.crypto.PasswordHasher
import ph.project.active.domain.model.JoinPath
import ph.project.active.domain.model.Membership
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.UserRepository
import ph.project.active.domain.usecase.CancelClass
import ph.project.active.domain.usecase.CreateClass

/**
 * Debug-only demo data: four coaches, six public classes, the demo student Paolo
 * (paolo@company.com / password1) in the C1 snapshot, and the demo coach Mara
 * (mara@company.com / password1) with one private and one cancelled class added after the six.
 * Core Burn also gets Dana, Gia and Hana after Paolo, as in the Manage example data.
 * Sign-in accounts live in memory, so both are added on every launch; classes and memberships are saved once.
 */
object DemoSeed {
    fun install() {
        val koin = GlobalContext.get()
        val people = koin.get<PersonRepository>()
        val users = koin.get<UserRepository>()
        val hasher = koin.get<PasswordHasher>()
        addUser(users, hasher, people, PAOLO_ID, "Paolo Reyes", "paolo@company.com", Role.STUDENT, "EMP-20417")
        addUser(users, hasher, people, MARA_ID, "Mara Santos", "mara@company.com", Role.COACH, null)

        val classes = koin.get<ClassRepository>()
        if (classes.allClasses().isNotEmpty()) return
        people.upsert(PersonProfile(LEO_ID, "Leo Ramirez", Role.COACH, null))
        people.upsert(PersonProfile(JUN_ID, "Jun Tan", Role.COACH, null))
        people.upsert(PersonProfile(ANA_ID, "Ana Cruz", Role.COACH, null))
        val create = CreateClass(
            classes = classes,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            openEndedHorizon = DatePeriod(months = 12),
        )
        fun save(
            coachId: String,
            name: String,
            days: List<DayOfWeek>,
            slot: TimeSlot,
            visibility: Visibility = Visibility.PUBLIC,
        ): TrxClass = create(
            coachId = coachId,
            name = name,
            visibility = visibility,
            slotsByWeekday = days.associateWith { listOf(slot) },
            endDate = null,
        )

        val coreBurn = save(
            MARA_ID,
            "TRX Core Burn",
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            slot(17, 30, 18, 30),
        )
        val lunchExpress = save(
            MARA_ID,
            "Lunch Express TRX",
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(12, 15, 12, 45),
        )
        val mobilityFlow = save(
            LEO_ID,
            "TRX Mobility Flow",
            listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(19, 0, 20, 0),
        )
        save(JUN_ID, "TRX Every Day 20", DayOfWeek.entries, slot(6, 30, 6, 50))
        val bootcamp = save(ANA_ID, "Weekend TRX Bootcamp", listOf(DayOfWeek.SATURDAY), slot(8, 0, 9, 30))
        val sunrise = save(
            MARA_ID,
            "Sunrise TRX Strength",
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            slot(6, 0, 6, 45),
        )
        save(
            MARA_ID,
            "TRX Leadership Circle",
            listOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(7, 0, 7, 45),
            Visibility.PRIVATE,
        )
        val cancelled = save(MARA_ID, "TRX Friday Finisher", listOf(DayOfWeek.FRIDAY), slot(18, 0, 18, 45))
        CancelClass(classes, Clock.System, TimeZone.currentSystemDefault()).invoke(cancelled.id)

        val memberships = koin.get<MembershipRepository>()
        fun member(
            trxClass: TrxClass,
            status: MembershipStatus,
            studentId: String = PAOLO_ID,
            joinedBy: JoinPath? = if (status == MembershipStatus.ACTIVE) JoinPath.REQUEST else null,
        ) {
            memberships.add(Membership(memberships.nextId(), trxClass.id, studentId, status, joinedBy))
        }
        member(sunrise, MembershipStatus.ACTIVE)
        member(lunchExpress, MembershipStatus.ACTIVE)
        member(coreBurn, MembershipStatus.ACTIVE)
        member(mobilityFlow, MembershipStatus.INVITED)
        member(bootcamp, MembershipStatus.PENDING_REQUEST)

        // Core Burn's roster after Paolo matches the Manage example data, so D12 shows both join paths.
        people.upsert(PersonProfile(DANA_ID, "Dana Uy", Role.STUDENT, "EMP-20155"))
        people.upsert(PersonProfile(GIA_ID, "Gia Torres", Role.STUDENT, "EMP-20249"))
        people.upsert(PersonProfile(HANA_ID, "Hana Cho", Role.STUDENT, "EMP-20877"))
        member(coreBurn, MembershipStatus.ACTIVE, DANA_ID, JoinPath.INVITE)
        member(coreBurn, MembershipStatus.ACTIVE, GIA_ID, JoinPath.INVITE)
        member(coreBurn, MembershipStatus.ACTIVE, HANA_ID, JoinPath.REQUEST)
    }

    private fun addUser(
        users: UserRepository,
        hasher: PasswordHasher,
        people: PersonRepository,
        id: String,
        name: String,
        email: String,
        role: Role,
        employeeId: String?,
    ) {
        if (users.findById(id) != null) return
        val user = User(
            id = id,
            name = name,
            email = email,
            role = role,
            employeeId = employeeId,
            passwordHash = hasher.hash("password1"),
        )
        users.add(user)
        people.upsert(PersonProfile(user.id, user.name, user.role, user.employeeId))
    }

    private fun slot(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot =
        TimeSlot(LocalTime(startHour, startMinute), LocalTime(endHour, endMinute))

    private const val MARA_ID = "demo-mara"
    private const val LEO_ID = "demo-leo"
    private const val JUN_ID = "demo-jun"
    private const val ANA_ID = "demo-ana"
    private const val PAOLO_ID = "demo-paolo"
    private const val DANA_ID = "demo-dana"
    private const val GIA_ID = "demo-gia"
    private const val HANA_ID = "demo-hana"
}
