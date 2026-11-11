package ph.project.active.data.repository

import ph.project.active.data.db.TrxDatabase
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Membership

class SqlMembershipRepository(private val database: TrxDatabase) : MembershipRepository {
    private val ids = SqlIdSource(database)

    override fun nextId(): String = ids.next("membership")

    override fun forClass(classId: String): List<Membership> =
        database.trxDatabaseQueries.selectMembershipsForClass(classId).executeAsList().map { it.toMembership() }

    override fun all(): List<Membership> =
        database.trxDatabaseQueries.selectAllMemberships().executeAsList().map { it.toMembership() }

    override fun add(membership: Membership) {
        database.transaction {
            val order = database.trxDatabaseQueries.maxMembershipSortOrder().executeAsOne() + 1
            database.trxDatabaseQueries.insertMembership(
                membership.id,
                membership.classId,
                membership.studentId,
                membership.status.name,
                order,
            )
        }
    }

    override fun update(membership: Membership) {
        val existing = database.trxDatabaseQueries.selectMembershipById(membership.id).executeAsOneOrNull()
            ?: throw IllegalArgumentException("Unknown membership ${membership.id}")
        database.trxDatabaseQueries.updateMembership(
            membership.classId,
            membership.studentId,
            membership.status.name,
            existing.id,
        )
    }
}

private fun ph.project.active.data.db.SelectMembershipsForClass.toMembership(): Membership = Membership(
    id = id,
    classId = classId,
    studentId = studentId,
    status = MembershipStatus.valueOf(status),
)

private fun ph.project.active.data.db.SelectAllMemberships.toMembership(): Membership = Membership(
    id = id,
    classId = classId,
    studentId = studentId,
    status = MembershipStatus.valueOf(status),
)
