package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026

class P8DetailNoticeTest {
    @Test
    fun P8_noticeComesFromJoinOutcomeAndClearsOnRefresh() {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id, name = "TRX Core Burn", endDate = SAT_31_OCT_2026)
            val withNote = detail(world, trxClass.id, JoinOutcome.ClassCancelled)
            assertEquals(JoinOutcome.ClassCancelled, withNote.state.value.notice)
            withNote.refresh()
            assertNull(withNote.state.value.notice)

            val withoutNote = ClassDetailViewModel(
                classId = trxClass.id,
                viewer = null,
                classes = world.classes,
                memberships = world.memberships,
                people = world.people,
                requestToJoin = world.requestToJoin,
                network = world.network,
            )
            assertNull(withoutNote.state.value.notice)
        }
    }
}

private fun detail(world: Phase2World, classId: String, joinOutcome: JoinOutcome?) = ClassDetailViewModel(
    classId = classId,
    viewer = null,
    classes = world.classes,
    memberships = world.memberships,
    people = world.people,
    requestToJoin = world.requestToJoin,
    network = world.network,
    joinOutcome = joinOutcome,
)
