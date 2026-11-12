package ph.project.active.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ph.project.active.presentation.common.Navigation

@Composable
fun NavigationEffect(
    navigation: Navigation?,
    onNavigationHandled: () -> Unit,
    onNavigate: (Navigation) -> Unit,
) {
    val navigate by rememberUpdatedState(onNavigate)
    val handled by rememberUpdatedState(onNavigationHandled)
    LaunchedEffect(navigation) {
        val event = navigation ?: return@LaunchedEffect
        navigate(event)
        withContext(NonCancellable) {
            handled()
        }
    }
}
