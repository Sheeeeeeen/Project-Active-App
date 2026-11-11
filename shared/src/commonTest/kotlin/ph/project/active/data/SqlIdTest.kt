package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.testkit.assertUrlSafeId
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.data.repository.SqlClassRepository
import ph.project.active.data.repository.SqlMembershipRepository

class SqlIdTest {

    @Test
    fun AC8_ids_stay_unique_and_url_safe_after_the_file_is_reopened() {
        val path = tempDbPath()
        val first = fileDb(path).use { db ->
            val classes = SqlClassRepository(db.database)
            val memberships = SqlMembershipRepository(db.database)
            val attendance = SqlAttendanceRepository(db.database)
            val ids = mutableListOf<String>()
            repeat(5) {
                ids += classes.nextClassId()
                ids += classes.nextSessionId()
                ids += memberships.nextId()
                ids += attendance.nextId()
            }
            ids
        }
        val second = fileDb(path).use { db ->
            val classes = SqlClassRepository(db.database)
            val memberships = SqlMembershipRepository(db.database)
            val attendance = SqlAttendanceRepository(db.database)
            List(5) {
                listOf(classes.nextClassId(), classes.nextSessionId(), memberships.nextId(), attendance.nextId())
            }.flatten()
        }
        val all = first + second
        all.forEach(::assertUrlSafeId)
        assertEquals(all.size, all.toSet().size)
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
