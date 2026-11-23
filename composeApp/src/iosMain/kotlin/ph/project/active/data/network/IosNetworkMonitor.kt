package ph.project.active.data.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus

/**
 * Follows the device path with nw_path_monitor on a private serial queue. It starts ONLINE so the
 * offline banner never flashes on cold start; the first path update corrects it within milliseconds.
 * That starting ONLINE is a guess, so [hasRealReading] stays false until the first path update.
 */
class IosNetworkMonitor(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) : NetworkMonitor {
    private val _status = MutableStateFlow(NetworkStatus.ONLINE)
    private val _hasRealReading = MutableStateFlow(false)
    override val status: StateFlow<NetworkStatus> = _status.asStateFlow()
    override val hasRealReading: StateFlow<Boolean> = _hasRealReading.asStateFlow()

    init {
        scope.launch {
            callbackFlow {
                val monitor = nw_path_monitor_create()
                nw_path_monitor_set_update_handler(monitor) { path ->
                    val online = nw_path_get_status(path) == nw_path_status_satisfied
                    trySend(if (online) NetworkStatus.ONLINE else NetworkStatus.OFFLINE)
                }
                nw_path_monitor_set_queue(monitor, dispatch_queue_create(QUEUE_LABEL, null))
                nw_path_monitor_start(monitor)
                awaitClose { nw_path_monitor_cancel(monitor) }
            }.collect { reading ->
                // Status first, so a real-reading observer never sees the starting guess as real.
                _status.value = reading
                _hasRealReading.value = true
            }
        }
    }
}

private const val QUEUE_LABEL = "ph.project.active.network"
