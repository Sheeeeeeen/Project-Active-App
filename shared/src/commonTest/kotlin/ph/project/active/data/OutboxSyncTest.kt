package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.sync.OutboxSync
import ph.project.active.data.repository.SqlAttendanceRepository

class OutboxSyncTest {

    @Test
    fun AC18_sync_uploads_pending_rows_in_scanned_at_then_id_order() = runTest {
        inMemoryDb().use { db ->
            val attendance = SqlAttendanceRepository(db.database)
            val newer = Instant.parse("2026-09-28T10:00:00Z")
            val older = Instant.parse("2026-09-28T09:00:00Z")
            attendance.add(row("b", newer))
            attendance.add(row("a", newer))
            attendance.add(row("c", older))
            val uploader = FakeAttendanceUploader()
            val network = MutableNetworkMonitor(NetworkStatus.ONLINE)

            val synced = OutboxSync(attendance, uploader, network).sync()

            assertEquals(3, synced)
            assertEquals(listOf("c", "a", "b"), uploader.uploaded.map { it.id })
            assertTrue(attendance.all().all { it.uploadState == UploadState.SYNCED })
            assertEquals(older, attendance.find("session-1", "c")!!.scannedAt)
            assertEquals(newer, attendance.find("session-1", "a")!!.scannedAt)
        }
    }

    @Test
    fun AC19_a_failed_upload_stops_the_sync_and_offline_sync_is_a_noop() = runTest {
        inMemoryDb().use { db ->
            val attendance = SqlAttendanceRepository(db.database)
            val first = Instant.parse("2026-09-28T09:00:00Z")
            val second = Instant.parse("2026-09-28T10:00:00Z")
            val third = Instant.parse("2026-09-28T11:00:00Z")
            attendance.add(row("a", first))
            attendance.add(row("b", second))
            attendance.add(row("c", third))
            val network = MutableNetworkMonitor(NetworkStatus.ONLINE)
            val failing = FakeAttendanceUploader { record -> record.id != "b" }

            assertEquals(1, OutboxSync(attendance, failing, network).sync())
            assertEquals(UploadState.SYNCED, attendance.find("session-1", "a")!!.uploadState)
            assertEquals(UploadState.PENDING, attendance.find("session-1", "b")!!.uploadState)
            assertEquals(UploadState.PENDING, attendance.find("session-1", "c")!!.uploadState)
            assertEquals(listOf("a", "b"), failing.uploaded.map { it.id })
            assertEquals(second, attendance.find("session-1", "b")!!.scannedAt)

            val throwing = FakeAttendanceUploader { record ->
                if (record.id == "b") error("upload failed") else true
            }
            assertFails { OutboxSync(attendance, throwing, network).sync() }
            assertEquals(UploadState.PENDING, attendance.find("session-1", "b")!!.uploadState)
            assertTrue(throwing.uploaded.none { it.id == "c" })

            network.set(NetworkStatus.OFFLINE)
            val offline = FakeAttendanceUploader()
            assertEquals(0, OutboxSync(attendance, offline, network).sync())
            assertTrue(offline.uploaded.isEmpty())
            assertEquals(UploadState.PENDING, attendance.find("session-1", "c")!!.uploadState)

            network.set(NetworkStatus.ONLINE)
            attendance.update(attendance.find("session-1", "a")!!.copy(uploadState = UploadState.SYNCED))
            val again = FakeAttendanceUploader()
            OutboxSync(attendance, again, network).sync()
            assertTrue(again.uploaded.none { it.id == "a" })
        }
    }

    @Test
    fun AC21_flush_outbox_on_sql_still_matches_phase1_ac41() {
        inMemoryDb().use { db ->
            val attendance = SqlAttendanceRepository(db.database)
            val scannedAt = Instant.parse("2026-09-28T09:40:00Z")
            attendance.add(row("student-1", scannedAt))
            var online = false
            val flush = ph.project.active.domain.usecase.FlushOutbox(attendance) { if (online) NetworkStatus.ONLINE else NetworkStatus.OFFLINE }

            assertEquals(0, flush())
            assertEquals(UploadState.PENDING, attendance.all().single().uploadState)
            online = true
            assertEquals(1, flush())
            assertEquals(UploadState.SYNCED, attendance.all().single().uploadState)
            assertEquals(scannedAt, attendance.all().single().scannedAt)
            assertEquals(0, flush())
        }
    }
}

private fun row(studentId: String, scannedAt: Instant): AttendanceRecord = AttendanceRecord(
    id = studentId,
    sessionId = "session-1",
    classId = "class-1",
    studentId = studentId,
    scannedAt = scannedAt,
)

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
