package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.presentation.classdetail.ClassChip
import ph.project.active.testkit.Flow3Example
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

class F3P2ClassesRefreshTest {
    @Test
    fun F3_P2_refreshMovesANewlyJoinedClassIntoMyClasses() = runVm {
        Phase2World().use { world ->
            val example = Flow3Example(world)
            val viewModel = world.studentClasses(example.account)
            val mobilityId = example.mobilityFlow.id
            assertTrue(viewModel.state.value.publicClasses.any { it.classId == mobilityId })

            world.acceptInvite(classId = mobilityId, studentId = example.paolo.id)
            viewModel.refresh()

            val joined = viewModel.state.value.myClasses.single { it.classId == mobilityId }
            assertEquals(ClassChip.Joined, joined.chip)
            assertTrue(viewModel.state.value.publicClasses.none { it.classId == mobilityId })
        }
    }
}
