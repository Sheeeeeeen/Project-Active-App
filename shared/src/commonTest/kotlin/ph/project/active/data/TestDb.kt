package ph.project.active.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import ph.project.active.data.db.TrxDatabase

class TestDb(val driver: SqlDriver) : AutoCloseable {
    val database: TrxDatabase = TrxDatabase(driver)

    override fun close() {
        driver.close()
    }

    fun tableNames(): Set<String> = queryStrings(
        sql = "SELECT name FROM sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
        column = 0,
    ).toSet()

    fun columnNames(table: String): List<String> {
        require(table.matches(Regex("[A-Za-z0-9_]+"))) { table }
        return queryStrings(sql = "PRAGMA table_info($table)", column = 1)
    }

    /** Text of every cell, plus hex for blobs, so tests can see that secrets never landed in SQLite. */
    fun dump(): String {
        val chunks = mutableListOf<String>()
        for (table in tableNames().sorted()) {
            val width = columnNames(table).size
            chunks += queryTable(table, width)
        }
        return chunks.joinToString("\n")
    }

    private fun queryStrings(sql: String, column: Int): List<String> {
        val values = mutableListOf<String>()
        driver.executeQuery(
            identifier = null,
            sql = sql,
            mapper = { cursor ->
                while (cursor.next().value) {
                    cursor.getString(column)?.let { values += it }
                }
                QueryResult.Value(values)
            },
            parameters = 0,
        )
        return values
    }

    private fun queryTable(table: String, width: Int): String {
        val lines = mutableListOf<String>()
        driver.executeQuery(
            identifier = null,
            sql = "SELECT * FROM $table",
            mapper = { cursor ->
                while (cursor.next().value) {
                    val cells = mutableListOf<String>()
                    for (index in 0 until width) {
                        cursor.getString(index)?.let { cells += it }
                        cursor.getBytes(index)?.let { cells += it.toHex() }
                    }
                    lines += cells.joinToString("|")
                }
                QueryResult.Value(lines)
            },
            parameters = 0,
        )
        return lines.joinToString("\n")
    }
}

fun inMemoryDb(createSchema: Boolean = true): TestDb {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    if (createSchema) TrxDatabase.Schema.create(driver)
    return TestDb(driver)
}

fun fileDb(path: String, createSchema: Boolean = true): TestDb {
    val driver = JdbcSqliteDriver("jdbc:sqlite:$path")
    if (createSchema) TrxDatabase.Schema.create(driver)
    return TestDb(driver)
}

fun tempDbPath(): String {
    val dir = java.nio.file.Files.createTempDirectory("trx-attendance")
    return dir.resolve("attendance.db").toString()
}

private fun ByteArray.toHex(): String = joinToString(separator = "") { byte ->
    (byte.toInt() and 0xff).toString(16).padStart(2, '0')
}
