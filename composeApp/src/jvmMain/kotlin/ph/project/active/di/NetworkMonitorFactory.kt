package ph.project.active.di

import org.koin.core.scope.Scope
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.model.NetworkStatus

// Desktop has no platform monitor in this app; it stays online (Planner ruling, flow 4).
actual fun Scope.platformNetworkMonitor(): NetworkMonitor = MutableNetworkMonitor(NetworkStatus.ONLINE)
