package ph.project.active.di

import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools

fun initKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            modules(appModules())
        }
    }
}
