package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.presentation.common.UiError
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026

class P6DetailErrorClearsTest {
    @Test
    fun P6_requestClearsThePreviousErrorAndRefreshClearsErrorAndNotice() {
        Phase2World().use { world ->
            val coach = world.coach()
            val student = world.studentAccount()
            val trxClass = world.createMonWedEveningClass(coach.id, name = "TRX Core Burn", endDate = SAT_31_OCT_2026)
            val viewModel = world.classDetail(trxClass.id, student)
            world.network.set(NetworkStatus.OFFLINE)
            viewModel.onRequestToJoin()
            assertEquals(UiError.Offline, viewModel.state.value.error)
            world.network.set(NetworkStatus.ONLINE)
            viewModel.refresh()
            assertNull(viewModel.state.value.error)
            assertNull(viewModel.state.value.notice)
        }
    }
}
