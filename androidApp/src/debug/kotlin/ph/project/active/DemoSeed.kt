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
import ph.project.active.domain.usecase.CreateClass

/**
 * Debug-only demo data: four coaches, six public classes, and the demo student Paolo
 * (paolo@company.com / password1) in the C1 snapshot. Sign-in accounts live in memory,
 * so Paolo is added on every launch; classes and memberships are saved once.
 */
object DemoSeed {
    fun install() {
        val koin = GlobalContext.get()
        val people = koin.get<PersonRepository>()
        addPaolo(koin.get<UserRepository>(), koin.get<PasswordHasher>(), people)

        val classes = koin.get<ClassRepository>()
        if (classes.allClasses().isNotEmpty()) return
        people.upsert(PersonProfile(MARA_ID, "Mara Santos", Role.COACH, null))
        people.upsert(PersonProfile(LEO_ID, "Leo Ramirez", Role.COACH, null))
        people.upsert(PersonProfile(JUN_ID, "Jun Tan", Role.COACH, null))
        people.upsert(PersonProfile(ANA_ID, "Ana Cruz", Role.COACH, null))
        val create = CreateClass(
            classes = classes,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            openEndedHorizon = DatePeriod(months = 12),
        )
        fun save(coachId: String, name: String, days: List<DayOfWeek>, slot: TimeSlot): TrxClass = create(
            coachId = coachId,
            name = name,
            visibility = Visibility.PUBLIC,
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

        val memberships = koin.get<MembershipRepository>()
        fun member(trxClass: TrxClass, status: MembershipStatus) {
            memberships.add(Membership(memberships.nextId(), trxClass.id, PAOLO_ID, status))
        }
        member(sunrise, MembershipStatus.ACTIVE)
        member(lunchExpress, MembershipStatus.ACTIVE)
        member(coreBurn, MembershipStatus.ACTIVE)
        member(mobilityFlow, MembershipStatus.INVITED)
        member(bootcamp, MembershipStatus.PENDING_REQUEST)
    }

    private fun addPaolo(users: UserRepository, hasher: PasswordHasher, people: PersonRepository) {
        if (users.findById(PAOLO_ID) != null) return
        val paolo = User(
            id = PAOLO_ID,
            name = "Paolo Reyes",
            email = "paolo@company.com",
            role = Role.STUDENT,
            employeeId = "EMP-20417",
            passwordHash = hasher.hash("password1"),
        )
        users.add(paolo)
        people.upsert(PersonProfile(paolo.id, paolo.name, paolo.role, paolo.employeeId))
    }

    private fun slot(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot =
        TimeSlot(LocalTime(startHour, startMinute), LocalTime(endHour, endMinute))

    private const val MARA_ID = "demo-mara"
    private const val LEO_ID = "demo-leo"
    private const val JUN_ID = "demo-jun"
    private const val ANA_ID = "demo-ana"
    private const val PAOLO_ID = "demo-paolo"
}
