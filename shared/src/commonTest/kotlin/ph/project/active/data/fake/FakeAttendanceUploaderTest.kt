package ph.project.active.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.domain.model.AttendanceRecord

class FakeAttendanceUploaderTest {

    @Test
    fun AC18_uploader_records_calls_and_can_fail_or_throw() = runTest {
        val first = sample("a")
        val second = sample("b")
        val uploader = FakeAttendanceUploader()
        assertTrue(uploader.upload(first))
        assertTrue(uploader.upload(second))
        assertEquals(listOf(first, second), uploader.uploaded)

        val failing = FakeAttendanceUploader { it.id != "b" }
        assertTrue(failing.upload(first))
        assertFalse(failing.upload(second))
        assertEquals(listOf("a", "b"), failing.uploaded.map { it.id })

        val throwing = FakeAttendanceUploader { error("upload failed") }
        assertFails { throwing.upload(first) }
    }
}

private fun sample(id: String): AttendanceRecord = AttendanceRecord(
    id = id,
    sessionId = "session-1",
    classId = "class-1",
    studentId = id,
    scannedAt = Instant.parse("2026-09-28T09:00:00Z"),
)
