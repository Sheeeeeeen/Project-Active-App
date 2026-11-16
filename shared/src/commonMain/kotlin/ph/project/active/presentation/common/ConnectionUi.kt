package ph.project.active.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus

data class ConnectionUi(val isOffline: Boolean, val banner: String?)

internal fun NetworkStatus.toConnection(): ConnectionUi = ConnectionUi(
    isOffline = this == NetworkStatus.OFFLINE,
    banner = if (this == NetworkStatus.OFFLINE) UiCopy.offlineBanner() else null,
)

internal fun NetworkMonitor.isOffline(): Boolean = status.value == NetworkStatus.OFFLINE

internal fun <T> ViewModel.watchConnection(
    network: NetworkMonitor,
    state: MutableStateFlow<T>,
    copyConnection: T.(ConnectionUi) -> T,
) {
    viewModelScope.launch {
        network.status.collect { status ->
            state.update { it.copyConnection(status.toConnection()) }
        }
    }
}
