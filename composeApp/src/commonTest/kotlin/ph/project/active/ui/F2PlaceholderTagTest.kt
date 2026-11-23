package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.presentation.classdetail.ClassDetailUiState
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.guestclasses.GuestClassListUiState
import ph.project.active.presentation.manageclass.RequestAction
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowClassDetailState
import ph.project.active.ui.testing.ShowGuestListState

@OptIn(ExperimentalTestApi::class)
class F2PlaceholderTagTest {
    // Flow 3 (F3-AC18, F3-AC23): StudentHome is no longer a placeholder. Attend and Sessions
    // carry the tag now, and F3TabsTest.F3_AC18 checks them.
    // Flow 4 (F4-AC25, F4-AC31): CoachHome is no longer a placeholder either. The coach account
    // button, QR and Attendance carry the tag now, and F4CoachTabsTest.F4_AC25 checks them.

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
private fun ComposeUiTest.assertNoPlaceholder() {
    val nodes = onAllNodesWithTag("placeholder_tag").fetchSemanticsNodes(atLeastOneRootRequired = false)
    assertEquals(0, nodes.size)
}
