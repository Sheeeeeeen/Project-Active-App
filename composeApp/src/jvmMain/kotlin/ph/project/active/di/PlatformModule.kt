package ph.project.active.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module
import ph.project.active.data.db.TrxSchema
import ph.project.active.ui.platform.NoShakeSource
import ph.project.active.ui.platform.ShakeSource

actual fun platformModule(): Module = module {
    single<SqlDriver> {
        // JDBC does not apply the schema itself. The Android and iOS drivers do.
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { driver ->
            TrxSchema.create(driver)
        }
    }
    single<ShakeSource> { NoShakeSource }
}
