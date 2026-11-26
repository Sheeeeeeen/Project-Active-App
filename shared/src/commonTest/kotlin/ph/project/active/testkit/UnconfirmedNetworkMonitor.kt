package ph.project.active.testkit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus

/**
 * Behaves like `IosNetworkMonitor` before its first path update (flow 5 decision 15): it starts
 * ONLINE, but that value is not a real reading. [reading] delivers a real reading.
 */
class UnconfirmedNetworkMonitor : NetworkMonitor {
    private val _status = MutableStateFlow(NetworkStatus.ONLINE)
    private val _hasRealReading = MutableStateFlow(false)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()
    override val hasRealReading: StateFlow<Boolean> = _hasRealReading.asStateFlow()

    fun reading(status: NetworkStatus) {
        _status.value = status
        _hasRealReading.value = true
    }
}
