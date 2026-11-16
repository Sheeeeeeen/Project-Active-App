package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ph.project.active.domain.model.nextSessionStart
import ph.project.active.testkit.Flow3Example
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F3P4SortingTest {
    @Test
    fun F3_P4_myClassesSortBySoonestSessionCountedFromMidnightToday() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            world.setLocal(MON_28_SEP_2026, 19)
            val ids = world.studentClasses(example.account).state.value.myClasses.map { it.classId }
            assertEquals(listOf(example.sunrise.id, example.lunchExpress.id, example.coreBurn.id), ids)
        }
    }

    @Test
    fun F3_P4_joinedClassesWithNoUpcomingSessionSortLastAndTiesKeepCreationOrder() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.student()
            world.setLocal(LocalDate(2026, 9, 25), 9)
            val ended = world.createMonWedEveningClass(coach.id, endDate = LocalDate(2026, 9, 27), name = "Ended")
            val firstTie = world.createMonWedEveningClass(coach.id, endDate = null, name = "First Tie")
            val secondTie = world.createMonWedEveningClass(coach.id, endDate = null, name = "Second Tie")
            listOf(ended, secondTie, firstTie).forEach { world.makeActive(it.id, student.id) }
            world.setLocal(MON_28_SEP_2026, 9)

            val ids = world.studentClasses(student.toAccount()).state.value.myClasses.map { it.classId }
            assertEquals(listOf(firstTie.id, secondTie.id, ended.id), ids)
        }
    }

    @Test
    fun F3_P4_publicClassesKeepCreationOrderWithPendingAndInvitedInPlace() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            val ids = world.studentClasses(example.account).state.value.publicClasses.map { it.classId }
            assertEquals(listOf(example.mobilityFlow.id, example.everyDay20.id, example.bootcamp.id), ids)
        }
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.student()
            val openBefore = world.createMonWedEveningClass(coach.id, name = "Open Before")
            val pending = world.createMonWedEveningClass(coach.id, name = "Pending")
            val openAfter = world.createMonWedEveningClass(coach.id, name = "Open After")
            world.requestToJoin(classId = pending.id, studentId = student.id)
            val ids = world.studentClasses(student.toAccount()).state.value.publicClasses.map { it.classId }
            assertEquals(listOf(openBefore.id, pending.id, openAfter.id), ids)
        }
    }

    @Test
    fun F3_P4_invitesAreNewestFirstByTheOrderTheyWereStored() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world, invitedToBootcamp = true)
            val ids = world.invites(example.account).state.value.invites.map { it.classId }
            assertEquals(listOf(example.mobilityFlow.id, example.bootcamp.id), ids)
        }
        // Twelve invites, so stored ids run past "membership-9" into "membership-10" and beyond.
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.student()
            val invited = (1..12).map { n -> world.createMonWedEveningClass(coach.id, name = "Class $n") }
            invited.forEach { world.inviteByEmployeeId(it.id, student.employeeId!!) }
            val ids = world.invites(student.toAccount()).state.value.invites.map { it.classId }
            assertEquals(invited.map { it.id }.reversed(), ids)
        }
    }

    @Test
    fun F3_P4_nextSessionStartIsTheFirstSessionAtOrAfterMidnightToday() {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            assertEquals(LocalDateTime(2026, 9, 28, 6, 0), example.sunrise.nextSessionStart(MON_28_SEP_2026))
            assertEquals(LocalDateTime(2026, 9, 28, 17, 30), example.coreBurn.nextSessionStart(MON_28_SEP_2026))
            assertEquals(LocalDateTime(2026, 10, 3, 8, 0), example.bootcamp.nextSessionStart(MON_28_SEP_2026))
            val endsSunday = example.coreBurn.copy(endDate = LocalDate(2026, 10, 4))
            assertNull(endsSunday.nextSessionStart(LocalDate(2026, 10, 1)))
            val endedYesterday = example.coreBurn.copy(endDate = LocalDate(2026, 9, 27))
            assertNull(endedYesterday.nextSessionStart(MON_28_SEP_2026))
        }
    }
}
