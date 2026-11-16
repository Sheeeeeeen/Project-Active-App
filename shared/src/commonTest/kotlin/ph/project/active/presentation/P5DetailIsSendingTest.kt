package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026

class P5DetailIsSendingTest {
    @Test
    fun P5_isSendingIsFalseAroundARequestAndASecondCallIsIgnored() {
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.studentAccount()
            val trxClass = world.createMonWedEveningClass(coach.id, name = "TRX Core Burn", endDate = SAT_31_OCT_2026)
            val viewModel = world.classDetail(trxClass.id, student)
            assertFalse(viewModel.state.value.isSending)
            viewModel.onRequestToJoin()
            viewModel.onRequestToJoin()
            assertFalse(viewModel.state.value.isSending)
            val pending = world.membershipsOf(trxClass.id, student.id)
                .filter { it.status == MembershipStatus.PENDING_REQUEST }
            assertEquals(1, pending.size)
        }
    }
}
