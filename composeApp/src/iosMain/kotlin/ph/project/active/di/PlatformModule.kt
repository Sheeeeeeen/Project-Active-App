package ph.project.active.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import ph.project.active.data.db.TrxSchema
import ph.project.active.data.repository.SecretKeyStore
import ph.project.active.data.secure.IosSecretKeyStore
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.platform.ShakeSource

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        NativeSqliteDriver(TrxSchema, "trx.db")
    }
    single<ShakeSource> { NoShakeSource }
    single<SecretKeyStore> { IosSecretKeyStore() }
}
