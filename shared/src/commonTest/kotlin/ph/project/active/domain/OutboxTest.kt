package ph.project.active.domain

import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class OutboxTest {

    private class Setup {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach(1)
        val cls = world.createMonWedEveningClass(coach.id)
        val student = world.student(1)

        init {
            world.giveKeys(coach.id)
            world.makeActive(cls.id, student.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
        }

        val monday get() = world.sessionOn(cls.id, MON_28_SEP_2026)
    }

    // ----- AC40 -----

    @Test
    fun AC40_offline_valid_scan_with_cached_membership_and_schedule_is_kept_pending() {
        val s = Setup()
        s.world.network = NetworkStatus.OFFLINE
        val scanTime = s.world.clock.now()

        val result = s.world.recordAttendance(
            studentId = s.student.id,
            session = s.world.refOf(s.monday),
            code = s.world.todaysCode(s.coach.id),
        )

        assertIs<AttendanceResult.Recorded>(result)
        val stored = s.world.attendance.all().single()
        assertEquals(UploadState.PENDING, stored.uploadState)
        assertEquals(scanTime, stored.scannedAt)
        assertEquals(s.monday.id, stored.sessionId)
    }

    @Test
    fun AC40_offline_scan_without_membership_in_local_store_stores_nothing_NotOnRoster() {
        val s = Setup()
        s.world.network = NetworkStatus.OFFLINE
        val stranger = s.world.student(2) // no membership on this class in the local store

        val result = s.world.recordAttendance(
            studentId = stranger.id,
            session = s.world.refOf(s.monday),
            code = s.world.todaysCode(s.coach.id),
        )

        assertEquals(AttendanceResult.NotOnRoster, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC40_offline_scan_for_session_not_in_local_store_stores_nothing_SessionUnknown() {
        val s = Setup()
        s.world.network = NetworkStatus.OFFLINE
        s.world.setLocal(TUE_29_SEP_2026, 17, 40)

        // The student is ACTIVE on this class, but no session exists for it on Tuesday.
        val tuesday = SessionRef(classId = s.cls.id, date = TUE_29_SEP_2026, slot = EVENING)
        val code = s.world.todaysCode(s.coach.id)

        assertEquals(AttendanceResult.SessionUnknown, s.world.recordAttendance(studentId = s.student.id, session = tuesday, code = code))
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC40_wrong_slot_on_a_scheduled_date_is_SessionUnknown() {
        val s = Setup()

        val morningRef = SessionRef(classId = s.cls.id, date = MON_28_SEP_2026, slot = MORNING)
        val result = s.world.recordAttendance(studentId = s.student.id, session = morningRef, code = s.world.todaysCode(s.coach.id))

        assertEquals(AttendanceResult.SessionUnknown, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    // ----- AC41 -----

    @Test
    fun AC41_flush_online_marks_pending_records_synced_without_changing_scannedAt() {
        val s = Setup()
        s.world.network = NetworkStatus.OFFLINE
        val other = s.world.student(2)
        s.world.makeActive(s.cls.id, other.id)
        val code = s.world.todaysCode(s.coach.id)
        s.world.recordAttendance(studentId = s.student.id, session = s.world.refOf(s.monday), code = code)
        s.world.clock.current = s.world.clock.now() + 5.minutes
        s.world.recordAttendance(studentId = other.id, session = s.world.refOf(s.monday), code = code)
        val before = s.world.attendance.all()
        assertEquals(2, before.size)
        assertTrue(before.all { it.uploadState == UploadState.PENDING })

        s.world.clock.current = s.world.clock.now() + 60.minutes
        s.world.network = NetworkStatus.ONLINE
        s.world.flushOutbox()

        val after = s.world.attendance.all()
        assertTrue(after.all { it.uploadState == UploadState.SYNCED })
        assertEquals(before.map { it.id to it.scannedAt }, after.map { it.id to it.scannedAt })
        assertEquals(before.map { it.copy(uploadState = UploadState.SYNCED) }, after)
    }

    @Test
    fun AC41_flush_while_offline_leaves_the_outbox_unchanged() {
        val s = Setup()
        s.world.network = NetworkStatus.OFFLINE
        s.world.recordAttendance(studentId = s.student.id, session = s.world.refOf(s.monday), code = s.world.todaysCode(s.coach.id))
        val before = s.world.attendance.all()

        s.world.clock.current = s.world.clock.now() + 30.minutes
        s.world.flushOutbox()

        assertEquals(before, s.world.attendance.all())
        assertEquals(UploadState.PENDING, s.world.attendance.all().single().uploadState)
    }

    @Test
    fun AC41_flush_online_with_nothing_pending_changes_nothing() {
        val s = Setup()
        s.world.recordAttendance(studentId = s.student.id, session = s.world.refOf(s.monday), code = s.world.todaysCode(s.coach.id))
        s.world.flushOutbox()
        val synced = s.world.attendance.all()

        s.world.flushOutbox()

        assertEquals(synced, s.world.attendance.all())
        assertTrue(synced.all { it.uploadState == UploadState.SYNCED })
    }
}
