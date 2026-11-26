package ph.project.active.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import ph.project.active.data.db.TrxDatabase
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.domain.Flow7Example
import ph.project.active.domain.model.AttendanceResult

/** Flow 7 spec S4 and F7-AC5: SqlAttendanceRepository keeps the code's coach, date and sig. */
class F7Ac5SqlStoredCodeTest {

    // F7-AC5: "A copy saved and read back through SqlAttendanceRepository … keeps all three."
    @Test
    fun F7_AC5_sqlAttendanceRepositoryKeepsTheThreeValues() {
        val example = Flow7Example()
        val record = assertIs<AttendanceResult.Recorded>(example.record(example.coreBurn, example.marasCode)).attendance
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            TrxDatabase.Schema.create(driver)
            val store = SqlAttendanceRepository(TrxDatabase(driver))

            store.add(record)
            val back = store.find(record.sessionId, record.studentId)!!

            assertEquals(record.codeCoach, back.codeCoach)
            assertEquals(record.codeDate, back.codeDate)
            assertEquals(record.codeSig, back.codeSig)
            assertEquals(record, back)
            assertEquals(record, store.all().single())
        } finally {
            driver.close()
        }
    }
}
