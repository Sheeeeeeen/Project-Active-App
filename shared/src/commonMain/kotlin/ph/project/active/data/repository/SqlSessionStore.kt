package ph.project.active.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ph.project.active.data.db.TrxDatabase
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.Role

class SqlSessionStore(private val database: TrxDatabase) : SessionStore {
    private val _account = MutableStateFlow(read())
    override val account: StateFlow<SignedInAccount?> = _account.asStateFlow()

    override fun save(account: SignedInAccount) {
        database.transaction {
            database.trxDatabaseQueries.deleteSignedInAccounts()
            database.trxDatabaseQueries.insertSignedInAccount(
                account.id,
                account.name,
                account.email,
                account.role.name,
                account.employeeId,
            )
        }
        _account.value = account
    }

    private fun read(): SignedInAccount? =
        database.trxDatabaseQueries.selectSignedInAccount().executeAsOneOrNull()?.let { row ->
            SignedInAccount(
                id = row.id,
                name = row.name,
                email = row.email,
                role = Role.valueOf(row.role),
                employeeId = row.employeeId,
            )
        }
}
