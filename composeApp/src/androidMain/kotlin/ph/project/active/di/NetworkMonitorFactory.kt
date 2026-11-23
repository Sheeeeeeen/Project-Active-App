package ph.project.active.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.scope.Scope
import ph.project.active.data.network.AndroidNetworkMonitor
import ph.project.active.data.repository.NetworkMonitor

actual fun Scope.platformNetworkMonitor(): NetworkMonitor = AndroidNetworkMonitor(androidContext())
