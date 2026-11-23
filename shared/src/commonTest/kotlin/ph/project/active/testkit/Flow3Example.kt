package ph.project.active.testkit

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility

/**
 * Flow 3 example data (spec section 7). The clock is set to Mon 28 Sep 2026, 9:00 AM,
 * then the four coaches, the six classes in creation order, and Paolo are saved.
 *
 * C1 (`invitedToBootcamp = false`): Paolo is ACTIVE on Sunrise, Lunch Express and Core Burn,
 * INVITED on Mobility Flow, PENDING_REQUEST on Bootcamp, and has nothing on Every Day 20.
 *
 * C2 (`invitedToBootcamp = true`): the same, but Bootcamp is INVITED instead of pending.
 * Bootcamp's invite is stored first, then Mobility Flow's.
 */
class Flow3Example(val world: Phase2World, invitedToBootcamp: Boolean = false) {
    val mara: User
    val leo: User
    val jun: User
    val ana: User
    val coreBurn: TrxClass
    val lunchExpress: TrxClass
    val mobilityFlow: TrxClass
    val everyDay20: TrxClass
    val bootcamp: TrxClass
    val sunrise: TrxClass
    val paolo: User

    init {
        world.setLocal(MON_28_SEP_2026, 9)
        mara = namedCoach("Mara Santos", "mara.santos@acme.test")
        leo = namedCoach("Leo Ramirez", "leo.ramirez@acme.test")
        jun = namedCoach("Jun Tan", "jun.tan@acme.test")
        ana = namedCoach("Ana Cruz", "ana.cruz@acme.test")
        coreBurn = save(mara, "TRX Core Burn", days(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30))
        lunchExpress = save(
            mara,
            "Lunch Express TRX",
            days(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(12, 15, 12, 45),
        )
        mobilityFlow = save(leo, "TRX Mobility Flow", days(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), slot(19, 0, 20, 0))
        everyDay20 = save(jun, "TRX Every Day 20", DayOfWeek.entries, slot(6, 30, 6, 50))
        bootcamp = save(ana, "Weekend TRX Bootcamp", days(DayOfWeek.SATURDAY), slot(8, 0, 9, 30))
        sunrise = save(
            mara,
            "Sunrise TRX Strength",
            days(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            slot(6, 0, 6, 45),
        )
        paolo = world.registerStudent(
            name = "Paolo Reyes",
            email = "paolo@company.com",
            password = "password1",
            employeeId = "EMP-20417",
            requestToJoinClassId = null,
        ).also { world.remember(it) }
        world.makeActive(sunrise.id, paolo.id)
        world.makeActive(lunchExpress.id, paolo.id)
        world.makeActive(coreBurn.id, paolo.id)
        if (invitedToBootcamp) {
            world.inviteByEmployeeId(bootcamp.id, "EMP-20417")
            world.inviteByEmployeeId(mobilityFlow.id, "EMP-20417")
        } else {
            world.inviteByEmployeeId(mobilityFlow.id, "EMP-20417")
            world.requestToJoin(bootcamp.id, paolo.id)
        }
    }

    val account: SignedInAccount get() = paolo.toAccount()

    private fun namedCoach(name: String, email: String): User =
        world.registerCoach(name = name, email = email, password = "password1").also { world.remember(it) }

    private fun save(coach: User, name: String, weekdays: List<DayOfWeek>, time: TimeSlot): TrxClass =
        world.createClass(
            coachId = coach.id,
            name = name,
            visibility = Visibility.PUBLIC,
            slotsByWeekday = weekdays.associateWith { listOf(time) },
            endDate = null,
        )

    private fun days(vararg weekdays: DayOfWeek): List<DayOfWeek> = weekdays.toList()

    private fun slot(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot =
        TimeSlot(LocalTime(startHour, startMinute), LocalTime(endHour, endMinute))
}
