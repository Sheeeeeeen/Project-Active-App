package ph.project.active.data.repository

import kotlinx.coroutines.flow.StateFlow
import ph.project.active.domain.model.NetworkStatus

interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>
}
