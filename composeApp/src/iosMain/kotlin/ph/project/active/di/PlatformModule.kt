package ph.project.active.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import ph.project.active.data.db.TrxDatabase
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.platform.ShakeSource

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        NativeSqliteDriver(TrxDatabase.Schema, "trx.db")
    }
    single<ShakeSource> { NoShakeSource }
}
