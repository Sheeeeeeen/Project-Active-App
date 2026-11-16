package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.hasEnded
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026

class P2ClassHasEndedTest {
    @Test
    fun P2_classEndingTodayIsOpenYesterdayIsEndedAndOpenEndedNeverEnds() {
        Phase2World().use { world ->
            val coach = world.coach()
            val today = SAT_31_OCT_2026
            val endingToday = world.createMonWedEveningClass(coach.id, endDate = today, name = "Ends Today")
            val endedYesterday = world.createMonWedEveningClass(
                coach.id,
                endDate = LocalDate(2026, 10, 30),
                name = "Ended Yesterday",
            )
            val openEnded = world.createMonWedEveningClass(coach.id, endDate = null, name = "Open Ended")
            assertFalse(endingToday.hasEnded(today))
            assertTrue(endedYesterday.hasEnded(today))
            assertFalse(openEnded.hasEnded(today))
        }
    }
}
