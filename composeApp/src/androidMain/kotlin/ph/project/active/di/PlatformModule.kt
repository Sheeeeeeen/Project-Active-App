package ph.project.active.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import ph.project.active.data.db.TrxSchema
import ph.project.active.data.repository.SecretKeyStore
import ph.project.active.data.secure.AndroidSecretKeyStore
import ph.project.active.ui.platform.AndroidShakeSource
import ph.project.active.ui.platform.ShakeSource

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = TrxSchema,
            context = androidContext(),
            name = "trx.db",
        )
    }
    single<ShakeSource> { AndroidShakeSource(androidContext()) }
    single<SecretKeyStore> { AndroidSecretKeyStore(androidContext()) }
}
