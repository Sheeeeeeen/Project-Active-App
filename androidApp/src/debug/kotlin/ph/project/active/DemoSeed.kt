package ph.project.active

import kotlin.time.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.koin.core.context.GlobalContext
import ph.project.active.data.model.PersonProfile
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.usecase.CreateClass

object DemoSeed {
    fun install() {
        val koin = GlobalContext.get()
        val classes = koin.get<ClassRepository>()
        if (classes.allClasses().isNotEmpty()) return
        val people = koin.get<PersonRepository>()
        people.upsert(PersonProfile(COACH_ID, "Mara Santos", Role.COACH, null))
        val create = CreateClass(
            classes = classes,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            openEndedHorizon = DatePeriod(months = 12),
        )
        val evening = TimeSlot(LocalTime(17, 30), LocalTime(18, 30))
        val lunch = TimeSlot(LocalTime(12, 10), LocalTime(12, 50))
        val mobility = TimeSlot(LocalTime(18, 0), LocalTime(18, 45))
        val twenty = TimeSlot(LocalTime(6, 30), LocalTime(6, 50))
        val weekend = TimeSlot(LocalTime(8, 0), LocalTime(9, 0))
        val weekdays = listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
        )
        listOf(
            "TRX Core Burn" to mapOf(DayOfWeek.MONDAY to listOf(evening), DayOfWeek.WEDNESDAY to listOf(evening)),
            "Lunch Express TRX" to weekdays.associateWith { listOf(lunch) },
            "TRX Mobility Flow" to mapOf(DayOfWeek.TUESDAY to listOf(mobility), DayOfWeek.THURSDAY to listOf(mobility)),
            "TRX Every Day 20" to DayOfWeek.entries.associateWith { listOf(twenty) },
            "Weekend TRX Bootcamp" to mapOf(DayOfWeek.SATURDAY to listOf(weekend), DayOfWeek.SUNDAY to listOf(weekend)),
        ).forEach { (name, slots) ->
            create(
                coachId = COACH_ID,
                name = name,
                visibility = Visibility.PUBLIC,
                slotsByWeekday = slots,
                endDate = null,
            )
        }
    }

    private const val COACH_ID = "demo-mara"
}
