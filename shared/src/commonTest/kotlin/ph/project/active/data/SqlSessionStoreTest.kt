package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.domain.Role
import ph.project.active.testkit.phase2WorldOnFile

class SqlSessionStoreTest {

    @Test
    fun AC11_save_replaces_the_only_account_and_survives_restart() {
        val path = tempDbPath()
        val student = SignedInAccount("student-1", "Ada", "ada@trx.test", Role.STUDENT, "EMP-1")
        val coach = SignedInAccount("coach-1", "Bea", "bea@trx.test", Role.COACH, null)
        phase2WorldOnFile(path).use { world ->
            assertNull(world.sessionStore.account.value)
            world.sessionStore.save(student)
            assertEquals(student, world.sessionStore.account.value)
            world.sessionStore.save(coach)
            assertEquals(coach, world.sessionStore.account.value)
        }
        phase2WorldOnFile(path).use { reopened ->
            assertEquals(coach, reopened.sessionStore.account.value)
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
