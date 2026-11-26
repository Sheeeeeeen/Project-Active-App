package ph.project.active.data.db

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.UploadState

/**
 * Flow 7 spec S4 and F7-AC7: three `TEXT NOT NULL DEFAULT ''` columns (code_coach, code_date, code_sig)
 * arrive in the next schema version through TrxSchema. Old rows read as empty strings.
 */
class F7Ac7AttendanceCodeMigrationTest {

    // F7-AC7: "A database created at the previous schema version with one attendance row opens with TrxSchema and upgrades to the new version."
    @Test
    fun F7_AC7_aVersion2DatabaseUpgradesToVersion3WithTheCodeColumns() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            V2Database.create(driver)
            insertOldRow(driver)

            assertEquals(3L, TrxSchema.version, "the code columns come in schema version 3")
            TrxSchema.migrate(driver, 2, TrxSchema.version)

            assertEquals(3L, userVersion(driver))
            val columns = attendanceColumns(driver)
            for (column in listOf("code_coach", "code_date", "code_sig")) assertTrue(column in columns, "attendance.$column is added: $columns")
        } finally {
            driver.close()
        }
    }

    // F7-AC7: "The old row reads codeCoach = codeDate = codeSig = "", and a new scan saves its values."
    @Test
    fun F7_AC7_theOldRowReadsEmptyCodesAndANewScanKeepsItsValues() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            V2Database.create(driver)
            insertOldRow(driver)
            TrxSchema.migrate(driver, 2, TrxSchema.version)
            val repository = SqlAttendanceRepository(TrxDatabase(driver))

            val old = repository.find("session-1", "student-1")!!
            assertEquals("", old.codeCoach)
            assertEquals("", old.codeDate)
            assertEquals("", old.codeSig)

            val scan = AttendanceRecord(
                id = "attendance-new",
                sessionId = "session-2",
                classId = "class-1",
                studentId = "student-1",
                scannedAt = Instant.parse("2026-09-28T09:31:00Z"),
                uploadState = UploadState.PENDING,
                codeCoach = "coach-mara",
                codeDate = "2026-09-28",
                codeSig = "c2lnbmF0dXJl",
            )
            repository.add(scan)
            assertEquals(scan, repository.find("session-2", "student-1"))
        } finally {
            driver.close()
        }
    }

    private fun insertOldRow(driver: SqlDriver) {
        driver.execute(
            identifier = null,
            sql = "INSERT INTO attendance(id, session_id, class_id, student_id, scanned_at, status, upload_state) " +
                "VALUES ('attendance-old', 'session-1', 'class-1', 'student-1', '2026-09-27T22:02:00Z', 'PRESENT', 'SYNCED')",
            parameters = 0,
        )
    }

    private fun attendanceColumns(driver: SqlDriver): List<String> = driver.executeQuery(
        identifier = null,
        sql = "PRAGMA table_info(attendance)",
        mapper = { cursor ->
            val names = mutableListOf<String>()
            while (cursor.next().value) names += cursor.getString(1).orEmpty()
            QueryResult.Value(names)
        },
        parameters = 0,
    ).value

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
