package ph.project.active.data.repository

import ph.project.active.data.db.TrxDatabase
import ph.project.active.data.model.PersonProfile
import ph.project.active.domain.model.Role

class SqlPersonRepository(private val database: TrxDatabase) : PersonRepository {
    override fun find(id: String): PersonProfile? =
        database.trxDatabaseQueries.selectPerson(id).executeAsOneOrNull()?.let { row ->
            PersonProfile(
                id = row.id,
                name = row.name,
                role = Role.valueOf(row.role),
                employeeId = row.employeeId,
            )
        }

    override fun upsert(profile: PersonProfile) {
        database.trxDatabaseQueries.upsertPerson(
            profile.id,
            profile.name,
            profile.role.name,
            profile.employeeId,
        )
    }
}
