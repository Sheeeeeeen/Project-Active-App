package ph.project.active.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus

/** Follows the default network with ConnectivityManager. Starts from the current network, never from ONLINE. */
class AndroidNetworkMonitor(
    context: Context,
    scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : NetworkMonitor {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)

    // The starting value is read from the system, so it is already real.
    override val hasRealReading: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    override val status: StateFlow<NetworkStatus> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            // Below API 26, onCapabilitiesChanged is not guaranteed after onAvailable, so read them here too.
            override fun onAvailable(network: Network) {
                trySend(networkStatusOf(connectivity.getNetworkCapabilities(network)?.toSignal()))
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(networkStatusOf(capabilities.toSignal()))
            }

            override fun onLost(network: Network) {
                trySend(networkStatusOf(null))
            }
        }
        connectivity.registerDefaultNetworkCallback(callback)
        trySend(currentStatus())
        awaitClose { connectivity.unregisterNetworkCallback(callback) }
    }
        .distinctUntilChanged()
        .stateIn(scope, SharingStarted.Eagerly, currentStatus())

    private fun currentStatus(): NetworkStatus {
        val capabilities = connectivity.activeNetwork?.let { connectivity.getNetworkCapabilities(it) }
        return networkStatusOf(capabilities?.toSignal())
    }
}

private fun NetworkCapabilities.toSignal() = NetworkSignal(
    hasInternet = hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET),
    isValidated = hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
)
