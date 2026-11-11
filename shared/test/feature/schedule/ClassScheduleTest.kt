package ph.com.sheen.projectactive.feature.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.com.sheen.projectactive.feature.classes.Class
import ph.com.sheen.projectactive.feature.classes.ClassDay
import ph.com.sheen.projectactive.feature.classes.ClassTime
import ph.com.sheen.projectactive.feature.classes.ClassVisibility

class ClassScheduleTest {
    @Test
    fun `an end date is the last day the class stays open`() {
        val trxClass = morningTrx(end = LocalDate(2026, 10, 31))

        val sessions = ClassSchedule().sessions(
            trxClass,
            savedOn = LocalDate(2026, 9, 25),
            through = LocalDate(2026, 12, 31),
        )

        assertEquals(
            listOf(
                LocalDate(2026, 9, 28),
                LocalDate(2026, 9, 30),
                LocalDate(2026, 10, 5),
                LocalDate(2026, 10, 7),
                LocalDate(2026, 10, 12),
                LocalDate(2026, 10, 14),
                LocalDate(2026, 10, 19),
                LocalDate(2026, 10, 21),
                LocalDate(2026, 10, 26),
                LocalDate(2026, 10, 28),
            ),
            sessions.map { it.date },
        )
    }

    @Test
    fun `a class with no end date stays open past the calendar year`() {
        val trxClass = morningTrx(end = null)

        val sessions = ClassSchedule().sessions(
            trxClass,
            savedOn = LocalDate(2026, 9, 25),
            through = LocalDate(2027, 1, 6),
        )

        assertEquals(LocalDate(2026, 9, 28), sessions.first().date)
        assertEquals(LocalDate(2026, 12, 30), sessions[27].date)
        assertEquals(LocalDate(2027, 1, 4), sessions[28].date)
        assertEquals(LocalDate(2027, 1, 6), sessions.last().date)
        assertEquals(30, sessions.size)
    }

    @Test
    fun `an every-day class is open on each day through the end date`() {
        val trxClass = morningTrx(end = LocalDate(2026, 9, 27), everyDay = true)

        val sessions = ClassSchedule().sessions(
            trxClass,
            savedOn = LocalDate(2026, 9, 25),
            through = LocalDate(2026, 9, 30),
        )

        assertEquals(
            listOf(
                LocalDate(2026, 9, 25),
                LocalDate(2026, 9, 26),
                LocalDate(2026, 9, 27),
            ),
            sessions.map { it.date },
        )
    }

    @Test
    fun `cancelling one date marks that date cancelled`() {
        val trxClass = morningTrx(end = LocalDate(2026, 10, 31))
        val schedule = ClassSchedule()

        schedule.cancel(trxClass, LocalDate(2026, 9, 28))

        assertEquals(
            listOf(
                Session(LocalDate(2026, 9, 28), SessionStatus.Cancelled),
                Session(LocalDate(2026, 9, 30), SessionStatus.Scheduled),
                Session(LocalDate(2026, 10, 5), SessionStatus.Scheduled),
                Session(LocalDate(2026, 10, 7), SessionStatus.Scheduled),
            ),
            schedule.sessions(
                trxClass,
                savedOn = LocalDate(2026, 9, 25),
                through = LocalDate(2026, 10, 7),
            ),
        )
    }
}

private fun morningTrx(end: LocalDate?, everyDay: Boolean = false) = Class(
    name = "Morning TRX",
    coachName = "Ana Reyes",
    days = listOf(
        ClassDay(
            name = "Monday",
            schedules = listOf(
                ClassTime(
                    start = LocalTime(hour = 17, minute = 30),
                    end = LocalTime(hour = 18, minute = 30),
                ),
            ),
        ),
        ClassDay(
            name = "Wednesday",
            schedules = listOf(
                ClassTime(
                    start = LocalTime(hour = 17, minute = 30),
                    end = LocalTime(hour = 18, minute = 30),
                ),
            ),
        ),
    ),
    visibility = ClassVisibility.Public,
    end = end,
    everyDay = everyDay,
)
