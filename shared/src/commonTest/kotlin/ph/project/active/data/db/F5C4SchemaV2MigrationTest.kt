package ph.project.active.data.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.data.TestDb
import ph.project.active.data.inMemoryDb

/**
 * Flow 5 spec C4, F5-AC14 and Planner ruling 1: schema v2, with a code-side v1 → v2 step that adds
 * `membership.joined_by` (default REQUEST) only when it is missing. The tests check outcomes only.
 * Seam (spec question for Planner): `TrxSchema` is the schema the platform drivers use, i.e.
 * `TrxDatabase.Schema` plus the code-side step, so `TrxSchema.migrate(driver, 1, 2)` is the upgrade path.
 */
class F5C4SchemaV2MigrationTest {

    // C4: "a fresh create is version 2 and still has every required table, including membership.joined_by"
    @Test
    fun F5_AC14_aFreshCreateIsVersion2WithJoinedBy() {
        val db = inMemoryDb(createSchema = false)
        try {
            assertEquals(2L, TrxDatabase.Schema.version)
            TrxDatabase.Schema.create(db.driver)
            assertTrue(V2_TABLES.all { it in db.tableNames() }, "tables: ${db.tableNames()}")
            assertTrue("joined_by" in db.columnNames("membership"))
        } finally {
            db.close()
        }
    }

    // Ruling 1: "v1 without the column gets REQUEST on every row" (C4: "adds the column and keeps existing rows")
    @Test
    fun F5_AC14_aV1DatabaseWithoutJoinedByGetsRequestOnEveryRow() {
        val db = TestDb(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
        try {
            V1Database.create(db.driver, withJoinedBy = false)
            insertMembership(db.driver, id = "m1", status = "ACTIVE", sortOrder = 0, joinedBy = null, withJoinedBy = false)
            insertMembership(db.driver, id = "m2", status = "PENDING_REQUEST", sortOrder = 1, joinedBy = null, withJoinedBy = false)

            TrxSchema.migrate(db.driver, 1, 2)

            assertTrue("joined_by" in db.columnNames("membership"), "the column is added")
            assertEquals(
                listOf(
                    MembershipRowText("m1", "class-1", "ACTIVE", "0", "REQUEST"),
                    MembershipRowText("m2", "class-1", "PENDING_REQUEST", "1", "REQUEST"),
                ),
                membershipRows(db.driver),
            )
        } finally {
            db.close()
        }
    }

    // Ruling 1: "v1 with the column keeps each row's value" (C4: "a v1 database that already has joined_by also succeeds")
    @Test
    fun F5_AC14_aV1DatabaseWithJoinedByKeepsEachRowsValue() {
        val db = TestDb(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
        try {
            V1Database.create(db.driver, withJoinedBy = true)
            insertMembership(db.driver, id = "m1", status = "ACTIVE", sortOrder = 0, joinedBy = "INVITE", withJoinedBy = true)
            insertMembership(db.driver, id = "m2", status = "ACTIVE", sortOrder = 1, joinedBy = "REQUEST", withJoinedBy = true)
            insertMembership(db.driver, id = "m3", status = "PENDING_REQUEST", sortOrder = 2, joinedBy = null, withJoinedBy = true)

            TrxSchema.migrate(db.driver, 1, 2)

            assertEquals(1, db.columnNames("membership").count { it == "joined_by" }, "still one joined_by column")
            assertEquals(
                listOf(
                    MembershipRowText("m1", "class-1", "ACTIVE", "0", "INVITE"),
                    MembershipRowText("m2", "class-1", "ACTIVE", "1", "REQUEST"),
                    MembershipRowText("m3", "class-1", "PENDING_REQUEST", "2", null),
                ),
                membershipRows(db.driver),
            )
        } finally {
            db.close()
        }
    }

    // Ruling 1: "the version is 2 afterwards", for both v1 shapes (read as SQLite's user_version, plus the schema's own version)
    @Test
    fun F5_AC14_bothV1ShapesAreVersion2AfterMigrating() {
        assertEquals(2L, TrxSchema.version)
        for (withJoinedBy in listOf(false, true)) {
            val db = TestDb(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY))
            try {
                V1Database.create(db.driver, withJoinedBy)
                TrxSchema.migrate(db.driver, 1, 2)
                assertEquals(2L, userVersion(db.driver), "user_version after migrating (withJoinedBy = $withJoinedBy)")
            } finally {
                db.close()
            }
        }
    }

    private fun insertMembership(
        driver: SqlDriver,
        id: String,
        status: String,
        sortOrder: Int,
        joinedBy: String?,
        withJoinedBy: Boolean,
    ) {
        val sql = if (withJoinedBy) {
            val value = if (joinedBy == null) "NULL" else "'$joinedBy'"
            "INSERT INTO membership(id, class_id, student_id, status, sort_order, joined_by) " +
                "VALUES ('$id', 'class-1', 'student-$id', '$status', $sortOrder, $value)"
        } else {
            "INSERT INTO membership(id, class_id, student_id, status, sort_order) " +
                "VALUES ('$id', 'class-1', 'student-$id', '$status', $sortOrder)"
        }
        driver.execute(identifier = null, sql = sql, parameters = 0)
    }

    private fun membershipRows(driver: SqlDriver): List<MembershipRowText> {
        val rows = mutableListOf<MembershipRowText>()
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, class_id, status, sort_order, joined_by FROM membership ORDER BY id",
            mapper = { cursor ->
                while (cursor.next().value) {
                    rows += MembershipRowText(
                        id = cursor.getString(0)!!,
                        classId = cursor.getString(1)!!,
                        status = cursor.getString(2)!!,
                        sortOrder = cursor.getLong(3)!!.toString(),
                        joinedBy = cursor.getString(4),
                    )
                }
                QueryResult.Value(rows)
            },
            parameters = 0,
        )
        return rows
    }

    private fun userVersion(driver: SqlDriver): Long = driver.executeQuery(
        identifier = null,
        sql = "PRAGMA user_version",
        mapper = { cursor ->
            cursor.next()
            QueryResult.Value(cursor.getLong(0)!!)
        },
        parameters = 0,
    ).value
}

private val V2_TABLES = setOf(
    "signed_in_account",
    "person",
    "trx_class",
    "class_slot",
    "session",
    "membership",
    "attendance",
    "coach_public_key",
)
