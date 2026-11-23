package ph.project.active.data.db

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema

/** [TrxDatabase.Schema] plus the code-side v1 -> v2 step. The Android and iOS drivers open the database with this. */
object TrxSchema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long get() = TrxDatabase.Schema.version

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> = TrxDatabase.Schema.create(driver)

    override fun migrate(
        driver: SqlDriver,
        oldVersion: Long,
        newVersion: Long,
        vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> {
        TrxDatabase.Schema.migrate(driver, oldVersion, newVersion, AfterVersion(1) { addJoinedByIfMissing(it) }, *callbacks)
        // The phone drivers record the same version after this returns; writing it here keeps a direct call correct.
        driver.execute(identifier = null, sql = "PRAGMA user_version = $newVersion", parameters = 0)
        return QueryResult.Unit
    }

    private fun addJoinedByIfMissing(driver: SqlDriver) {
        val columns = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info(membership)",
            mapper = { cursor ->
                val names = mutableListOf<String>()
                while (cursor.next().value) names += cursor.getString(1).orEmpty()
                QueryResult.Value(names)
            },
            parameters = 0,
        ).value
        if ("joined_by" !in columns) {
            driver.execute(identifier = null, sql = "ALTER TABLE membership ADD COLUMN joined_by TEXT DEFAULT 'REQUEST'", parameters = 0)
        }
    }
}
