package ph.project.active.di

import org.koin.core.scope.Scope
import ph.project.active.data.network.IosNetworkMonitor
import ph.project.active.data.repository.NetworkMonitor

actual fun Scope.platformNetworkMonitor(): NetworkMonitor = IosNetworkMonitor()
