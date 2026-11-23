package ph.project.active.data.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus

/**
 * Follows the device path with nw_path_monitor on a private serial queue. It starts ONLINE so the
 * offline banner never flashes on cold start; the first path update corrects it within milliseconds.
 */
class IosNetworkMonitor(
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) : NetworkMonitor {
    override val status: StateFlow<NetworkStatus> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            val online = nw_path_get_status(path) == nw_path_status_satisfied
            trySend(if (online) NetworkStatus.ONLINE else NetworkStatus.OFFLINE)
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
        awaitClose { nw_path_monitor_cancel(monitor) }
    }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, NetworkStatus.ONLINE)
}
