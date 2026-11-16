package ph.project.active.ui

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import ph.project.active.presentation.classdetail.ClassDetailUiState
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.guestclasses.GuestClassListUiState
import ph.project.active.presentation.manageclass.RequestAction
import ph.project.active.ui.home.CoachHomePlaceholder
import ph.project.active.ui.home.StudentHomePlaceholder
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowClassDetailState
import ph.project.active.ui.testing.ShowGuestListState

@OptIn(ExperimentalTestApi::class)
class F2PlaceholderTagTest {
    @Test
    fun F2_AC34_studentHomeShowsOnePlaceholderTag() = Flow2Ui.run {
        setContent { StudentHomePlaceholder(name = "Ella Navarro", openClassId = null) }
        assertOnePlaceholder()
    }

    @Test
    fun F2_AC34_coachHomeShowsOnePlaceholderTag() = Flow2Ui.run {
        setContent { CoachHomePlaceholder(name = "Mara Santos") }
        assertOnePlaceholder()
    }

    @Test
    fun F2_AC34_guestListAndClassDetailShowNoPlaceholderTag() = Flow2Ui.run {
        val online = ConnectionUi(isOffline = false, banner = null)
        setContent {
            ShowGuestListState(
                GuestClassListUiState(
                    items = emptyList(),
                    isEmpty = true,
                    connection = online,
                    navigation = null,
                ),
            )
        }
        assertNoPlaceholder()
        setContent {
            ShowClassDetailState(
                ClassDetailUiState(
                    content = null,
                    unavailable = true,
                    requestAction = RequestAction.None,
                    requestAgain = false,
                    error = null,
                    connection = online,
                    navigation = null,
                ),
            )
        }
        assertNoPlaceholder()
    }
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.assertOnePlaceholder() {
    val nodes = onAllNodesWithTag("placeholder_tag").fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertEquals(1, nodes.size)
    val pill = onNodeWithTag("placeholder_tag")
    pill.assertTextEquals("PLACEHOLDER")
    pill.assertContentDescriptionEquals("Placeholder")
    assertFalse(SemanticsActions.OnClick in nodes.single().config)
}

@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.assertNoPlaceholder() {
    val nodes = onAllNodesWithTag("placeholder_tag").fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertEquals(0, nodes.size)
}
