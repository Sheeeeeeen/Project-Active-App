package ph.project.active.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Reads a [StateFlow] on the thread that updates it. ViewModel success paths set navigation
 * from inside `viewModelScope`, and the test clock does not always flush that collector before
 * `waitForIdle`. Unconfined collection applies the new value before the caller returns.
 */
@Composable
fun <T> StateFlow<T>.collectNow(): T {
    val state = remember(this) { mutableStateOf(value) }
    DisposableEffect(this) {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        scope.launch { collect { state.value = it } }
        onDispose { scope.cancel() }
    }
    return state.value
}
