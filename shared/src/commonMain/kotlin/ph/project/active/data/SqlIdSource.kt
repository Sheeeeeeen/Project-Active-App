package ph.project.active.data

import ph.project.active.data.db.TrxDatabase

internal class SqlIdSource(private val database: TrxDatabase) {
    fun next(prefix: String): String = database.transactionWithResult {
        val current = database.trxDatabaseQueries.selectIdCounter(prefix).executeAsOneOrNull() ?: 0L
        val next = current + 1
        database.trxDatabaseQueries.upsertIdCounter(prefix, next)
        "$prefix-$next"
    }
}
