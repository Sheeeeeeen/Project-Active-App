package ph.project.active.data.repository

import kotlinx.coroutines.flow.StateFlow
import ph.project.active.domain.model.NetworkStatus

interface NetworkMonitor {
    val status: StateFlow<NetworkStatus>

    /** False only while [status] is a starting guess rather than something the platform reported. */
    val hasRealReading: StateFlow<Boolean>
}
