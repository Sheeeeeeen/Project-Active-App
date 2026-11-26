package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.ui.testing.Flow3Harness
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.mountFlow3
import ph.project.active.ui.testing.runFlow3UiTest
import ph.project.active.ui.testing.tag

/** Flow 6 decision 20 and F6-AC23: TalkBack reads the student home greeting once. */
@OptIn(ExperimentalTestApi::class)
class F6StudentGreetingTest {

    private val greeting = "Hi Paolo \u00B7 EMP-20417"

    // F6-AC23: "the greeting text appears exactly once in the merged semantics tree under student_home (on student_classes_greeting)"
    @Test
    fun F6_AC23_theGreetingAppearsOnceInTheMergedTree() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)

            val inHome = hasTestTag("student_home") or hasAnyAncestor(hasTestTag("student_home"))
            val merged = onAllNodes(hasText(greeting) and inHome).fetchSemanticsNodes(atLeastOneRootRequired = false)
            assertEquals(1, merged.size, "the greeting is read once")
            assertEquals("student_classes_greeting", merged.single().config.getOrNull(SemanticsProperties.TestTag))
            assertTextUnder("student_classes_greeting", greeting)
        }
    }

    // F6-AC23: "The screen root no longer carries the greeting as its own text."
    @Test
    fun F6_AC23_theScreenRootNoLongerCarriesTheGreetingAsItsOwnText() = runFlow3UiTest {
        Flow3Harness().use { world ->
            world.seedC1()
            world.signInAsPaolo()
            mountFlow3(world)

            val own = tag("student_home").fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
            assertTrue(greeting !in own, "student_home's own text is $own")
        }
    }
}
