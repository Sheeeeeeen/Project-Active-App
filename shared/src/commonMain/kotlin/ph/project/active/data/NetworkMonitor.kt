package ph.project.active.data

import kotlinx.coroutines.flow.StateFlow
import ph.project.active.domain.NetworkStatus

interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
}
