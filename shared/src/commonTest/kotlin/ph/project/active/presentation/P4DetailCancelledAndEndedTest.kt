package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.manageclass.RequestAction
import ph.project.active.testkit.Phase2World

class P4DetailCancelledAndEndedTest {
    @Test
    fun P4_cancelledAndEndedClassesOfferNoRequestAndEndedCreatesNoMembership() {
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.studentAccount()
            world.setLocal(LocalDate(2026, 10, 31), 9)
            val cancelled = world.createMonWedEveningClass(coach.id, endDate = LocalDate(2026, 11, 30), name = "Cancelled")
            world.cancelClass(cancelled.id)
            val cancelledDetail = detail(world, cancelled.id, student)
            assertTrue(cancelledDetail.state.value.content!!.isCancelled)
            assertFalse(cancelledDetail.state.value.content!!.isEnded)
            assertEquals(RequestAction.None, cancelledDetail.state.value.requestAction)
            assertFalse(cancelledDetail.state.value.requestAgain)

            val endingToday = world.createMonWedEveningClass(coach.id, endDate = LocalDate(2026, 10, 31), name = "Ends Today")
            world.setLocal(LocalDate(2026, 11, 1), 9)
            val endedDetail = detail(world, endingToday.id, student)
            assertTrue(endedDetail.state.value.content!!.isEnded)
            assertEquals(RequestAction.None, endedDetail.state.value.requestAction)
            assertFalse(endedDetail.state.value.requestAgain)
            val before = world.memberships.all()
            endedDetail.onRequestToJoin()
            assertEquals(before, world.memberships.all())
        }
    }
}

private fun detail(world: Phase2World, classId: String, viewer: ph.project.active.data.model.SignedInAccount) =
    ClassDetailViewModel(
        classId = classId,
        viewer = viewer,
        classes = world.classes,
        memberships = world.memberships,
        people = world.people,
        requestToJoin = world.requestToJoin,
        network = world.network,
        clock = world.clock,
        timeZone = world.timeZone,
    )
