package ph.project.active.data.db

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.data.TestDb
import ph.project.active.data.inMemoryDb

class SchemaTest {

    // Flow 5 C4 / F5-AC19: the only edit is the version, which is now 2 (was 1).
    @Test
    fun AC1_schema_version_2_creates_tables_on_an_empty_jvm_driver() {
        val db = inMemoryDb(createSchema = false)
        db.use {
            // Flow 7 S4: the attendance code columns bump the schema to version 3 (flagged: not in the F7-AC47 list).
            assertEquals(3L, TrxDatabase.Schema.version)
            assertTrue(REQUIRED_TABLES.none { it in db.tableNames() })
            TrxDatabase.Schema.create(db.driver)
            assertTrue(REQUIRED_TABLES.all { it in db.tableNames() })
        }
    }

    @Test
    fun AC2_no_column_is_named_like_a_password_or_private_key() {
        val db = inMemoryDb()
        db.use {
            val columns = db.tableNames().flatMap { table -> db.columnNames(table).map { table to it } }
            val banned = columns.filter { (_, name) ->
                val normalized = name.lowercase()
                "password" in normalized || "private_key" in normalized
            }
            assertEquals(emptyList(), banned)
        }
    }
}

private val REQUIRED_TABLES = setOf(
    "signed_in_account",
    "person",
    "trx_class",
    "class_slot",
    "session",
    "membership",
    "attendance",
    "coach_public_key",
)

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
