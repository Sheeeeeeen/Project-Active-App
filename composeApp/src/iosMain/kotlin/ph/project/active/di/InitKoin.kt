package ph.project.active.di

import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

fun initKoin() {
    // startKoin throws if this process already has a Koin instance.
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            modules(appModules())
        }
    }
}
