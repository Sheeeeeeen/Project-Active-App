package ph.project.active.di

import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import ph.project.active.data.repository.NetworkMonitor

fun initKoin() {
    // startKoin throws if this process already has a Koin instance.
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        val koin = startKoin {
            modules(appModules())
        }.koin
        // Resolve the monitor now so the path monitor starts before the first screen.
        koin.get<NetworkMonitor>()
    }
}
