package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.Visibility
import ph.project.active.testkit.Flow6Example
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

/** Flow 6 spec C1: each section carries its session id, class name and session number. */
class F6C1SectionHeaderTest {

    // C1: "with the example data, sections are Sunrise, Lunch Express, Core Burn, each with its own session id, class name and session number"
    @Test
    fun F6_C1_sectionsCarryTheSessionIdClassNameAndSessionNumberInStartTimeOrder() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)

            val sections = world.coachAttendance(d22.account).state.value.sections

            assertEquals(listOf("Sunrise TRX Strength", "Lunch Express TRX", "TRX Core Burn"), sections.map { it.className })
            assertEquals(
                listOf(d22.sessionOf(d22.sunrise).id, d22.sessionOf(d22.lunchExpress).id, d22.sessionOf(d22.coreBurn).id),
                sections.map { it.sessionId },
            )
            assertEquals(listOf(1, 1, 1), sections.map { it.sessionNumber }, "each class was created that day")
        }
    }

    // C1: "Lunch Express has cancelled = true and no students" (the header data is filled for cancelled sections too)
    @Test
    fun F6_C1_theCancelledSectionAlsoHasItsHeaderDataAndNoStudents() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)

            val lunch = world.coachAttendance(d22.account).state.value.sections.single { it.cancelled }

            assertEquals("Lunch Express TRX", lunch.className)
            assertEquals(d22.sessionOf(d22.lunchExpress).id, lunch.sessionId)
            assertEquals(1, lunch.sessionNumber)
            assertTrue(lunch.students.isEmpty())
        }
    }

    // C1: "Sort sections by start time, then scheduled before cancelled, then class name"
    @Test
    fun F6_C1_sectionsWithTheSameStartTimeSortScheduledFirstThenByClassName() = runVm {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 6)
            val coach = world.coach()
            val seven = TimeSlot(LocalTime(7, 0), LocalTime(8, 0))
            fun save(name: String) = world.createClass(
                coachId = coach.id,
                name = name,
                visibility = Visibility.PUBLIC,
                slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(seven)),
                endDate = null,
            )
            val aCancelled = save("A Cancelled")
            save("C Scheduled")
            save("B Scheduled")
            world.cancelDate(aCancelled.id, MON_28_SEP_2026)

            val sections = world.coachAttendance(coach.toAccount()).state.value.sections

            assertEquals(listOf("B Scheduled", "C Scheduled", "A Cancelled"), sections.map { it.className })
            assertEquals(listOf(false, false, true), sections.map { it.cancelled })
        }
    }
}
