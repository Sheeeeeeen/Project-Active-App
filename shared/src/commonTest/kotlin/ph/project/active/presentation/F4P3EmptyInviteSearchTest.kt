package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class F4P3EmptyInviteSearchTest {
    @Test
    fun F4_P3_aBlankOrWhitespaceQueryLeavesTheStateUnchanged() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)

            for (query in listOf("", "   ")) {
                viewModel.onSearchTextChange(query)
                val before = viewModel.state.value
                viewModel.onSearch()
                assertEquals(before, viewModel.state.value, "query \"$query\" must not search")
            }
        }
    }

    @Test
    fun F4_P3_aRealUnknownIdStillSetsNoMatch() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            viewModel.onSearchTextChange("EMP-99999")
            viewModel.onSearch()
            assertTrue(viewModel.state.value.noMatch)
        }
    }
}
