package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F3P3PublicClassesHideEndedTest {
    @Test
    fun F3_P3_publicClassesHideAClassThatEndedYesterdayButMyClassesKeepsAnEndedJoinedClass() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.student()
            val sunday = LocalDate(2026, 9, 27)
            val endedYesterday = world.createMonWedEveningClass(coach.id, endDate = sunday, name = "Ended Yesterday")
            val endsToday = world.createMonWedEveningClass(coach.id, endDate = MON_28_SEP_2026, name = "Ends Today")
            val joinedAndEnded = world.createMonWedEveningClass(coach.id, endDate = sunday, name = "Joined And Ended")
            world.makeActive(joinedAndEnded.id, student.id)
            world.setLocal(MON_28_SEP_2026, 9)

            val state = StudentClassesViewModel(
                account = student.toAccount(),
                classes = world.classes,
                memberships = world.memberships,
                people = world.people,
                network = world.network,
                clock = world.clock,
                timeZone = world.timeZone,
            ).state.value

            val publicIds = state.publicClasses.map { it.classId }
            assertFalse(endedYesterday.id in publicIds)
            assertTrue(endsToday.id in publicIds)
            assertTrue(state.myClasses.any { it.classId == joinedAndEnded.id })
        }
    }
}
