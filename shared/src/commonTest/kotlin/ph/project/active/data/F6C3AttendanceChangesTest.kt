package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Instant
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.inmemory.InMemoryAttendanceRepository

/** Flow 6 spec C3: `AttendanceRepository.changes` counts each successful write, for both stores. */
class F6C3AttendanceChangesTest {

    // C3: "the value starts at 0; after add() it is 1; after update() it is 2" — in-memory store
    @Test
    fun F6_C3_inMemoryStoreCountsEachAddAndUpdate() {
        countsEachAddAndUpdate(InMemoryAttendanceRepository())
    }

    // C3: "a failed update() of an unknown record does not change it" — in-memory store
    @Test
    fun F6_C3_inMemoryStoreDoesNotCountAFailedUpdate() {
        doesNotCountAFailedUpdate(InMemoryAttendanceRepository())
    }

    // C3: "the value starts at 0; after add() it is 1; after update() it is 2" — SQL store
    @Test
    fun F6_C3_sqlStoreCountsEachAddAndUpdate() {
        inMemoryDb().use { db -> countsEachAddAndUpdate(SqlAttendanceRepository(db.database)) }
    }

    // C3: "a failed update() of an unknown record does not change it" — SQL store
    @Test
    fun F6_C3_sqlStoreDoesNotCountAFailedUpdate() {
        inMemoryDb().use { db -> doesNotCountAFailedUpdate(SqlAttendanceRepository(db.database)) }
    }

    private fun countsEachAddAndUpdate(store: AttendanceRepository) {
        assertEquals(0, store.changes.value, "starts at 0")
        val record = record(store.nextId())
        store.add(record)
        assertEquals(1, store.changes.value, "after add()")
        store.update(record.copy(uploadState = UploadState.SYNCED))
        assertEquals(2, store.changes.value, "after update()")
    }

    private fun doesNotCountAFailedUpdate(store: AttendanceRepository) {
        store.add(record(store.nextId()))
        assertEquals(1, store.changes.value)
        assertFails { store.update(record("attendance-unknown")) }
        assertEquals(1, store.changes.value, "a failed update() does not move the counter")
    }

    private fun record(id: String): AttendanceRecord = AttendanceRecord(
        id = id,
        sessionId = "session-1",
        classId = "class-1",
        studentId = "student-1",
        scannedAt = Instant.parse("2026-09-28T09:41:00Z"),
    )
}
