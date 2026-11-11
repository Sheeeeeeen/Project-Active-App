package ph.project.active.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.project.active.domain.NetworkStatus

class MutableNetworkMonitor(initial: NetworkStatus) : NetworkMonitor {
    private val _status = MutableStateFlow(initial)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()

    fun set(status: NetworkStatus) {
        _status.value = status
    }
}
