package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.time.Instant
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.AttendanceStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.data.repository.SqlAttendanceRepository

class SqlAttendanceRepositoryTest {

    @Test
    fun AC7_attendance_round_trips_instants_and_rejects_a_duplicate_pair() {
        inMemoryDb().use { db ->
            val repository = SqlAttendanceRepository(db.database)
            val scannedAt = Instant.parse("2026-09-28T09:40:00Z")
            val first = record(id = "a", sessionId = "session-1", studentId = "student-1", scannedAt = scannedAt)
            repository.add(first)
            val synced = first.copy(uploadState = UploadState.SYNCED)
            repository.update(synced)

            assertEquals(synced, repository.find("session-1", "student-1"))
            assertEquals(scannedAt, repository.find("session-1", "student-1")!!.scannedAt)
            assertEquals(UploadState.SYNCED, repository.all().single().uploadState)
            assertNull(repository.find("session-1", "student-2"))

            val otherStudent = record(
                id = "b",
                sessionId = "session-1",
                studentId = "student-2",
                scannedAt = scannedAt,
            )
            repository.add(otherStudent)
            assertEquals(setOf("a", "b"), repository.all().map { it.id }.toSet())

            assertFails {
                repository.add(
                    record(id = "c", sessionId = "session-1", studentId = "student-1", scannedAt = scannedAt),
                )
            }
            assertEquals(listOf("a", "b"), repository.all().map { it.id }.sorted())
            assertEquals(AttendanceStatus.PRESENT, repository.all().first().status)
        }
    }
}

private fun record(id: String, sessionId: String, studentId: String, scannedAt: Instant): AttendanceRecord =
    AttendanceRecord(
        id = id,
        sessionId = sessionId,
        classId = "class-1",
        studentId = studentId,
        scannedAt = scannedAt,
        status = AttendanceStatus.PRESENT,
        uploadState = UploadState.PENDING,
    )

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
