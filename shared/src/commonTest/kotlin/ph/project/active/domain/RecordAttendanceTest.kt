package ph.project.active.domain

import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.Visibility

class RecordAttendanceTest {

    /** Class saved Fri 25 Sep; student ACTIVE; coach has keys. Clock left at Mon 28 Sep 17:40 local. */
    private class Setup(twoSlotMonday: Boolean = false) {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach(1)
        val cls = if (twoSlotMonday) world.createTwoSlotMondayClass(coach.id) else world.createMonWedEveningClass(coach.id)
        val student = world.student(1)

        init {
            world.giveKeys(coach.id)
            world.makeActive(cls.id, student.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
        }

        fun record(session: Session?, code: String = world.todaysCode(coach.id), studentId: String = student.id) =
            world.recordAttendance(studentId = studentId, session = session?.let { world.refOf(it) }, code = code)
    }

    // ----- AC32 -----

    @Test
    fun AC32_valid_scan_writes_one_present_record_at_clock_now_with_pending_upload() {
        val s = Setup()
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)
        val scanTime = s.world.clock.now()

        val result = s.record(session)

        val recorded = assertIs<AttendanceResult.Recorded>(result)
        val stored = s.world.attendance.all().single()
        assertEquals(recorded.attendance, stored)
        assertEquals(session.id, stored.sessionId)
        assertEquals(s.student.id, stored.studentId)
        assertEquals(scanTime, stored.scannedAt)
        assertEquals(UploadState.PENDING, stored.uploadState)
    }

    @Test
    fun AC32_scannedAt_is_the_scan_time_not_a_later_upload_time() {
        val s = Setup()
        val scanTime = s.world.clock.now()
        s.record(s.world.sessionOn(s.cls.id, MON_28_SEP_2026))

        s.world.clock.current = scanTime + 3.hours
        s.world.flushOutbox()

        assertEquals(scanTime, s.world.attendance.all().single().scannedAt)
    }

    // ----- AC33 -----

    @Test
    fun AC33_chosen_session_is_used_not_the_one_matching_scan_time() {
        val s = Setup(twoSlotMonday = true)
        // Scan during the morning slot, but the student chose the evening session.
        s.world.setLocal(MON_28_SEP_2026, 7, 15)
        val evening = s.world.sessionOn(s.cls.id, MON_28_SEP_2026, EVENING)

        val result = s.record(evening)

        assertIs<AttendanceResult.Recorded>(result)
        assertEquals(evening.id, s.world.attendance.all().single().sessionId)
    }

    @Test
    fun AC33_scan_outside_any_slot_time_still_records_the_chosen_session() {
        val s = Setup()
        s.world.setLocal(MON_28_SEP_2026, 12, 0)
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)

        assertIs<AttendanceResult.Recorded>(s.record(session))
        assertEquals(session.id, s.world.attendance.all().single().sessionId)
    }

    // ----- AC34 -----

    @Test
    fun AC34_second_record_for_same_session_returns_AlreadyRecorded_and_writes_nothing() {
        val s = Setup()
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)
        s.record(session)
        val afterFirst = s.world.attendance.all()

        s.world.setLocal(MON_28_SEP_2026, 18, 0)
        val result = s.record(session)

        assertEquals(AttendanceResult.AlreadyRecorded, result)
        assertEquals(afterFirst, s.world.attendance.all())
    }

    @Test
    fun AC34_another_slot_on_the_same_date_succeeds_with_a_new_record() {
        val s = Setup(twoSlotMonday = true)
        val morning = s.world.sessionOn(s.cls.id, MON_28_SEP_2026, MORNING)
        val evening = s.world.sessionOn(s.cls.id, MON_28_SEP_2026, EVENING)
        assertIs<AttendanceResult.Recorded>(s.record(morning))

        val result = s.record(evening)

        assertIs<AttendanceResult.Recorded>(result)
        assertEquals(listOf(morning.id, evening.id), s.world.attendance.all().map { it.sessionId })
    }

    @Test
    fun AC34_another_class_on_the_same_date_succeeds_with_a_new_record() {
        val s = Setup()
        val other = s.world.createClass(
            coachId = s.coach.id,
            name = "TRX Late",
            visibility = Visibility.PUBLIC,
            slotsByWeekday = mapOf(kotlinx.datetime.DayOfWeek.MONDAY to listOf(MORNING)),
            endDate = SAT_31_OCT_2026,
        )
        s.world.makeActive(other.id, s.student.id)
        val first = s.world.sessionOn(s.cls.id, MON_28_SEP_2026, EVENING)
        val second = s.world.sessionOn(other.id, MON_28_SEP_2026, MORNING)
        assertIs<AttendanceResult.Recorded>(s.record(first))

        val result = s.record(second)

        assertIs<AttendanceResult.Recorded>(result)
        assertEquals(setOf(first.id, second.id), s.world.attendance.all().map { it.sessionId }.toSet())
    }

    @Test
    fun AC34_two_students_can_each_record_the_same_session() {
        val s = Setup()
        val other = s.world.student(2)
        s.world.makeActive(s.cls.id, other.id)
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)

        assertIs<AttendanceResult.Recorded>(s.record(session))
        assertIs<AttendanceResult.Recorded>(s.record(session, studentId = other.id))
        assertEquals(2, s.world.attendance.all().size)
    }

    // ----- AC35 -----

    @Test
    fun AC35_membership_not_active_stores_nothing_and_returns_NotOnRoster() {
        val s = Setup()
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)
        val pending = s.world.student(2)
        s.world.requestToJoin(classId = s.cls.id, studentId = pending.id)
        val invited = s.world.student(3)
        s.world.inviteByEmployeeId(classId = s.cls.id, employeeId = "EMP-3")
        val removed = s.world.student(4)
        s.world.makeActive(s.cls.id, removed.id)
        s.world.removeFromRoster(classId = s.cls.id, studentId = removed.id)
        val stranger = s.world.student(5)

        for (id in listOf(pending.id, invited.id, removed.id, stranger.id)) {
            assertEquals(AttendanceResult.NotOnRoster, s.record(session, studentId = id))
        }
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_invalid_code_stores_nothing() {
        val s = Setup()
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)
        val good = s.world.todaysCode(s.coach.id)

        assertEquals(AttendanceResult.InvalidCode, s.record(session, code = "not a code"))
        assertEquals(AttendanceResult.InvalidCode, s.record(session, code = good.substringBefore("&sig=")))
        assertEquals(AttendanceResult.InvalidCode, s.record(session, code = good.dropLast(6)))
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_code_for_another_date_stores_nothing() {
        val s = Setup()
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)
        val sundayCode = s.world.generateDailyCode(coachId = s.coach.id, date = kotlinx.datetime.LocalDate(2026, 9, 27))
        val tuesdayCode = s.world.generateDailyCode(coachId = s.coach.id, date = TUE_29_SEP_2026)

        assertEquals(AttendanceResult.WrongDate, s.record(session, code = sundayCode))
        assertEquals(AttendanceResult.WrongDate, s.record(session, code = tuesdayCode))
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_code_from_another_coach_stores_nothing() {
        val s = Setup()
        val otherCoach = s.world.coach(2)
        s.world.giveKeys(otherCoach.id)
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)

        val result = s.record(session, code = s.world.todaysCode(otherCoach.id))

        assertEquals(AttendanceResult.WrongCoach, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_no_session_chosen_stores_nothing() {
        val s = Setup()

        val result = s.record(session = null)

        assertEquals(AttendanceResult.NoSessionChosen, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_cancelled_session_stores_nothing() {
        val s = Setup()
        s.world.cancelDate(classId = s.cls.id, date = MON_28_SEP_2026)
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)

        val result = s.record(session)

        assertEquals(AttendanceResult.SessionCancelled, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_cancelled_class_stores_nothing() {
        val s = Setup()
        s.world.cancelClass(classId = s.cls.id)
        val session = s.world.sessionOn(s.cls.id, MON_28_SEP_2026)

        val result = s.record(session)

        assertEquals(AttendanceResult.ClassCancelled, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    @Test
    fun AC35_session_on_another_date_than_today_is_rejected_even_with_todays_code() {
        val s = Setup()
        val wednesday = s.world.sessionOn(s.cls.id, WED_30_SEP_2026)

        val result = s.record(wednesday)

        assertEquals(AttendanceResult.WrongDate, result)
        assertTrue(s.world.attendance.all().isEmpty())
    }

    // ----- AC36 -----

    @Test
    fun AC36_other_scheduled_sessions_that_day_stay_unmarked() {
        val s = Setup(twoSlotMonday = true)
        s.world.setLocal(MON_28_SEP_2026, 7, 10)
        val morning = s.world.sessionOn(s.cls.id, MON_28_SEP_2026, MORNING)
        val evening = s.world.sessionOn(s.cls.id, MON_28_SEP_2026, EVENING)

        s.record(morning)

        assertEquals(listOf(morning.id), s.world.attendance.all().map { it.sessionId })
        val rows = s.world.studentSessions(studentId = s.student.id).associateBy { it.session.id }
        assertEquals(StudentSessionStatus.PRESENT, rows.getValue(morning.id).status)
        assertEquals(StudentSessionStatus.UPCOMING, rows.getValue(evening.id).status)
    }
}
