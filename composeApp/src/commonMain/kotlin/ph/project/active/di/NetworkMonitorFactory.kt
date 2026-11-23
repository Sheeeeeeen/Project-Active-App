package ph.project.active.di

import org.koin.core.scope.Scope
import ph.project.active.data.repository.NetworkMonitor

/** The platform's network monitor for the app's single NetworkMonitor binding in FakeDataModule. */
expect fun Scope.platformNetworkMonitor(): NetworkMonitor
