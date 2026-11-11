package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.domain.Role

class SqlPersonRepositoryTest {

    @Test
    fun AC12_upsert_replaces_a_person_by_id() {
        inMemoryDb().use { db ->
            val people = SqlPersonRepository(db.database)
            val student = PersonProfile("student-1", "Ada", Role.STUDENT, "EMP-1")
            people.upsert(student)
            assertEquals(student, people.find("student-1"))
            assertNull(people.find("missing"))

            val renamed = student.copy(name = "Ada Lovelace", employeeId = "EMP-1")
            people.upsert(renamed)
            assertEquals(renamed, people.find("student-1"))

            val coach = PersonProfile("coach-1", "Bea", Role.COACH, null)
            people.upsert(coach)
            assertEquals(coach, people.find("coach-1"))
            assertEquals(renamed, people.find("student-1"))
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
