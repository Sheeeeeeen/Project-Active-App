package ph.project.active.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * A version 2 database, built from the `CREATE TABLE` statements on `main` at edde156 (flow 7 spec S4:
 * the attendance code columns arrive in the next version). Used by F7-AC7.
 */
object V2Database {
    fun create(driver: SqlDriver) {
        for (statement in TABLES) driver.execute(identifier = null, sql = statement, parameters = 0)
        driver.execute(identifier = null, sql = "PRAGMA user_version = 2", parameters = 0)
    }

    private val TABLES = listOf(
        "CREATE TABLE signed_in_account (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL, role TEXT NOT NULL, employee_id TEXT)",
        "CREATE TABLE person (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, role TEXT NOT NULL, employee_id TEXT)",
        "CREATE TABLE trx_class (id TEXT NOT NULL PRIMARY KEY, coach_id TEXT NOT NULL, name TEXT NOT NULL, visibility TEXT NOT NULL, " +
            "end_date TEXT, created_on TEXT NOT NULL, cancelled_at TEXT, sort_order INTEGER NOT NULL)",
        "CREATE TABLE class_slot (class_id TEXT NOT NULL, weekday TEXT NOT NULL, start_time TEXT NOT NULL, end_time TEXT NOT NULL, " +
            "position INTEGER NOT NULL, PRIMARY KEY (class_id, weekday, position))",
        "CREATE TABLE session (id TEXT NOT NULL PRIMARY KEY, class_id TEXT NOT NULL, date TEXT NOT NULL, start_time TEXT NOT NULL, " +
            "end_time TEXT NOT NULL, session_number INTEGER NOT NULL, status TEXT NOT NULL)",
        "CREATE TABLE membership (id TEXT NOT NULL PRIMARY KEY, class_id TEXT NOT NULL, student_id TEXT NOT NULL, status TEXT NOT NULL, " +
            "sort_order INTEGER NOT NULL, joined_by TEXT)",
        "CREATE TABLE attendance (id TEXT NOT NULL PRIMARY KEY, session_id TEXT NOT NULL, class_id TEXT NOT NULL, student_id TEXT NOT NULL, " +
            "scanned_at TEXT NOT NULL, status TEXT NOT NULL, upload_state TEXT NOT NULL, UNIQUE (session_id, student_id))",
        "CREATE TABLE coach_public_key (coach_id TEXT NOT NULL PRIMARY KEY, public_key BLOB NOT NULL)",
        "CREATE TABLE id_counter (name TEXT NOT NULL PRIMARY KEY, value INTEGER NOT NULL)",
    )
}
