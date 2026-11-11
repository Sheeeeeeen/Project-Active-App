package ph.project.active.data.repository

import ph.project.active.data.db.TrxDatabase
import ph.project.active.domain.repository.current

internal class SqlIdSource(private val database: TrxDatabase) {
    fun next(prefix: String): String = database.transactionWithResult {
        val current = database.trxDatabaseQueries.selectIdCounter(prefix).executeAsOneOrNull() ?: 0L
        val next = current + 1
        database.trxDatabaseQueries.upsertIdCounter(prefix, next)
        "$prefix-$next"
    }
}
