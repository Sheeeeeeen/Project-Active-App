package ph.project.active.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.sync.OutboxSync
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.repository.inmemory.InMemoryAttendanceRepository

/** Flow 7 spec S4: the record keeps the code's coach, date and sig, for the server to check after flow 8. F7-AC5 and F7-AC6. */
class F7StoredCodeTest {

    // F7-AC5: "The Core Burn record from F7-AC2 has codeCoach = "coach-mara", codeDate = "2026-09-28" and codeSig equal to the link's sig."
    @Test
    fun F7_AC5_theRecordKeepsTheLinksCoachDateAndSig() {
        val example = Flow7Example()
        val code = example.marasCode

        val record = assertIs<AttendanceResult.Recorded>(example.record(example.coreBurn, code)).attendance

        assertEquals(rawValue(code, "coach"), record.codeCoach)
        assertEquals("2026-09-28", record.codeDate)
        assertEquals(rawValue(code, "sig"), record.codeSig)
        assertEquals(record, example.world.attendance.find(record.sessionId, example.paolo.id), "the stored row is the same record")
    }

    // F7-AC5: "A copy saved and read back … through InMemoryAttendanceRepository keeps all three."
    @Test
    fun F7_AC5_inMemoryAttendanceRepositoryKeepsTheThreeValues() {
        val example = Flow7Example()
        val record = assertIs<AttendanceResult.Recorded>(example.record(example.coreBurn, example.marasCode)).attendance
        val store = InMemoryAttendanceRepository()

        store.add(record.copy(id = store.nextId()))
        val back = store.find(record.sessionId, record.studentId)!!

        assertEquals(record.codeCoach, back.codeCoach)
        assertEquals(record.codeDate, back.codeDate)
        assertEquals(record.codeSig, back.codeSig)
    }

    // F7-AC6: "Online, after the scan, FakeAttendanceUploader.uploaded holds one record with those same three values. The stored row is SYNCED and still has them."
    @Test
    fun F7_AC6_theOutboxUploadsTheThreeValuesAndKeepsThemWhenSynced() = runTest {
        val example = Flow7Example()
        val code = example.marasCode
        val record = assertIs<AttendanceResult.Recorded>(example.record(example.coreBurn, code)).attendance
        val uploader = FakeAttendanceUploader()

        OutboxSync(example.world.attendance, uploader, MutableNetworkMonitor(NetworkStatus.ONLINE)).sync()

        val uploaded = uploader.uploaded.single()
        assertEquals(record.id, uploaded.id)
        assertEquals(rawValue(code, "coach"), uploaded.codeCoach)
        assertEquals("2026-09-28", uploaded.codeDate)
        assertEquals(rawValue(code, "sig"), uploaded.codeSig)
        val stored = example.world.attendance.find(record.sessionId, example.paolo.id)!!
        assertEquals(UploadState.SYNCED, stored.uploadState)
        assertEquals(uploaded.codeCoach, stored.codeCoach)
        assertEquals(uploaded.codeDate, stored.codeDate)
        assertEquals(uploaded.codeSig, stored.codeSig)
    }
}

/** The value of [name] exactly as it appears in the link [code]. */
fun rawValue(code: String, name: String): String = code.substringAfter("$name=").substringBefore("&")
