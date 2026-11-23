package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.presentation.studentclasses.dayLine
import ph.project.active.presentation.studentclasses.spokenCard
import ph.project.active.presentation.studentclasses.timeLine
import ph.project.active.testkit.Flow3Example
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

class F3P1StudentCardFieldsTest {
    @Test
    fun F3_P1_cardsCarryTheCoachNameAndSlotsAndTheC1Lines() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            val state = world.studentClasses(example.account).state.value

            val coreBurn = state.myClasses.single { it.classId == example.coreBurn.id }
            assertEquals("Mara Santos", coreBurn.coachName)
            assertEquals(example.coreBurn.slotsByWeekday, coreBurn.slotsByWeekday)
            assertEquals("Mon \u00B7 Wed", coreBurn.dayLine())
            assertEquals("5:30 PM \u2013 6:30 PM", coreBurn.timeLine())

            val sunrise = state.myClasses.single { it.classId == example.sunrise.id }
            assertEquals(
                "Sunrise TRX Strength, Coach Mara Santos, Monday, Wednesday and Friday, 6:00 AM to 6:45 AM",
                sunrise.spokenCard(),
            )

            val mobility = state.publicClasses.single { it.classId == example.mobilityFlow.id }
            assertEquals("Leo Ramirez", mobility.coachName)
            assertEquals(example.mobilityFlow.slotsByWeekday, mobility.slotsByWeekday)
            assertEquals("Tue \u00B7 Thu", mobility.dayLine())
            assertEquals("7:00 PM \u2013 8:00 PM", mobility.timeLine())
        }
    }
}
