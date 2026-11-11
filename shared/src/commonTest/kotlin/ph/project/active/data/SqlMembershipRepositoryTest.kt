package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import ph.project.active.domain.repository.inmemory.InMemoryMembershipRepository
import ph.project.active.domain.model.Membership
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.repository.activeClassIds
import ph.project.active.domain.repository.activeRoster
import ph.project.active.domain.repository.current
import ph.project.active.domain.repository.currentStatus
import ph.project.active.data.repository.SqlMembershipRepository

class SqlMembershipRepositoryTest {

    @Test
    fun AC6_history_update_and_helpers_match_the_in_memory_repository() {
        inMemoryDb().use { db ->
            val sql = SqlMembershipRepository(db.database)
            val memory = InMemoryMembershipRepository()
            val rows = listOf(
                Membership("m1", "class-a", "student-1", MembershipStatus.PENDING_REQUEST),
                Membership("m2", "class-a", "student-2", MembershipStatus.PENDING_REQUEST),
                Membership("m3", "class-b", "student-1", MembershipStatus.INVITED),
            )
            rows.forEach {
                sql.add(it)
                memory.add(it)
            }
            val active = rows[0].copy(status = MembershipStatus.ACTIVE)
            sql.update(active)
            memory.update(active)
            val removed = Membership("m4", "class-a", "student-1", MembershipStatus.REMOVED)
            sql.add(removed)
            memory.add(removed)
            val rejoined = Membership("m5", "class-a", "student-1", MembershipStatus.ACTIVE)
            sql.add(rejoined)
            memory.add(rejoined)

            assertEquals(memory.forClass("class-a"), sql.forClass("class-a"))
            assertEquals(memory.all(), sql.all())
            assertEquals(memory.current("class-a", "student-1"), sql.current("class-a", "student-1"))
            assertEquals(memory.currentStatus("class-a", "student-2"), sql.currentStatus("class-a", "student-2"))
            assertEquals(memory.activeRoster("class-a"), sql.activeRoster("class-a"))
            assertEquals(memory.activeClassIds("student-1"), sql.activeClassIds("student-1"))
            assertEquals(listOf("student-1"), sql.activeRoster("class-a"))

            assertFailsWith<IllegalArgumentException> {
                sql.update(Membership("missing", "class-a", "student-9", MembershipStatus.ACTIVE))
            }
            assertEquals(memory.all(), sql.all())
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
