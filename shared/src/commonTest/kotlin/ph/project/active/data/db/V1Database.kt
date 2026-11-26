package ph.project.active.data.db

import app.cash.sqldelight.db.SqlDriver

/**
 * A version 1 database, built from the `CREATE TABLE` statements on `main` at 67f8231 (flow 5 spec C4).
 * [create] can leave out `membership.joined_by`, as on a pre-PR-12 install.
 */
object V1Database {
    fun create(driver: SqlDriver, withJoinedBy: Boolean) {
        for (statement in TABLES) {
            val sql = if (withJoinedBy) statement else statement.replace(",\n  joined_by TEXT", "")
            driver.execute(identifier = null, sql = sql, parameters = 0)
        }
        driver.execute(identifier = null, sql = "PRAGMA user_version = 1", parameters = 0)
    }

    private val TABLES = listOf(
        """
        CREATE TABLE signed_in_account (
          id TEXT NOT NULL PRIMARY KEY,
          name TEXT NOT NULL,
          email TEXT NOT NULL,
          role TEXT NOT NULL,
          employee_id TEXT
        );
        """.trimIndent(),
        """
        CREATE TABLE person (
          id TEXT NOT NULL PRIMARY KEY,
          name TEXT NOT NULL,
          role TEXT NOT NULL,
          employee_id TEXT
        );
        """.trimIndent(),
        """
        CREATE TABLE trx_class (
          id TEXT NOT NULL PRIMARY KEY,
          coach_id TEXT NOT NULL,
          name TEXT NOT NULL,
          visibility TEXT NOT NULL,
          end_date TEXT,
          created_on TEXT NOT NULL,
          cancelled_at TEXT,
          sort_order INTEGER NOT NULL
        );
        """.trimIndent(),
        """
        CREATE TABLE class_slot (
          class_id TEXT NOT NULL,
          weekday TEXT NOT NULL,
          start_time TEXT NOT NULL,
          end_time TEXT NOT NULL,
          position INTEGER NOT NULL,
          PRIMARY KEY (class_id, weekday, position)
        );
        """.trimIndent(),
        """
        CREATE TABLE session (
          id TEXT NOT NULL PRIMARY KEY,
          class_id TEXT NOT NULL,
          date TEXT NOT NULL,
          start_time TEXT NOT NULL,
          end_time TEXT NOT NULL,
          session_number INTEGER NOT NULL,
          status TEXT NOT NULL
        );
        """.trimIndent(),
        """
        CREATE TABLE membership (
          id TEXT NOT NULL PRIMARY KEY,
          class_id TEXT NOT NULL,
          student_id TEXT NOT NULL,
          status TEXT NOT NULL,
          sort_order INTEGER NOT NULL,
          joined_by TEXT
        );
        """.trimIndent(),
        """
        CREATE TABLE attendance (
          id TEXT NOT NULL PRIMARY KEY,
          session_id TEXT NOT NULL,
          class_id TEXT NOT NULL,
          student_id TEXT NOT NULL,
          scanned_at TEXT NOT NULL,
          status TEXT NOT NULL,
          upload_state TEXT NOT NULL,
          UNIQUE (session_id, student_id)
        );
        """.trimIndent(),
        """
        CREATE TABLE coach_public_key (
          coach_id TEXT NOT NULL PRIMARY KEY,
          public_key BLOB NOT NULL
        );
        """.trimIndent(),
        """
        CREATE TABLE id_counter (
          name TEXT NOT NULL PRIMARY KEY,
          value INTEGER NOT NULL
        );
        """.trimIndent(),
    )
}
