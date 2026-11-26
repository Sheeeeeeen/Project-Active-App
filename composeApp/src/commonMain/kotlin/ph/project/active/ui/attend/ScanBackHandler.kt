package ph.project.active.ui.attend

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/** System back on the scanner and the result screen, the same path as the × button. */
@Composable
fun ScanBackHandler(onBack: () -> Unit) {
    val state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(state = state, isBackEnabled = true, onBackCompleted = onBack)
}
