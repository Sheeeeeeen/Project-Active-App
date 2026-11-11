package ph.project.active.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CancelTest {

    // ----- AC13 -----

    @Test
    fun AC13_cancel_future_date_cancels_every_session_on_that_date_and_keeps_numbers() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val cls = world.createTwoSlotMondayClass(coach.id)
        val before = world.sessionsOf(cls.id)
        val target = LocalDate(2026, 10, 5)

        world.cancelDate(classId = cls.id, date = target)

        val after = world.sessionsOf(cls.id)
        assertEquals(before.map { it.id to it.sessionNumber }, after.map { it.id to it.sessionNumber })
        val onTarget = after.filter { it.date == target }
        assertEquals(2, onTarget.size)
        assertTrue(onTarget.all { it.status == SessionStatus.CANCELLED })
        assertTrue(after.filter { it.date != target }.all { it.status == SessionStatus.SCHEDULED })
        assertEquals(
            before.filter { it.date == target }.map { it.sessionNumber },
            onTarget.map { it.sessionNumber },
        )
    }

    @Test
    fun AC13_cancel_today_is_allowed_and_keeps_numbers() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val cls = world.createMonWedEveningClass(coach.id)
        world.setLocal(MON_28_SEP_2026, 12)

        world.cancelDate(classId = cls.id, date = MON_28_SEP_2026)

        val sessions = world.sessionsOf(cls.id)
        assertEquals(SessionStatus.CANCELLED, sessions.single { it.date == MON_28_SEP_2026 }.status)
        assertEquals(1, sessions.single { it.date == MON_28_SEP_2026 }.sessionNumber)
        assertTrue(sessions.filter { it.date != MON_28_SEP_2026 }.all { it.status == SessionStatus.SCHEDULED })
        assertEquals((1..10).toList(), sessions.map { it.sessionNumber })
    }

    // ----- AC14 -----

    @Test
    fun AC14_cancel_past_date_throws_and_leaves_all_sessions_unchanged() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val cls = world.createMonWedEveningClass(coach.id)
        world.setLocal(TUE_29_SEP_2026, 9)
        val before = world.sessionsOf(cls.id)

        assertFailsWith<IllegalArgumentException> { world.cancelDate(classId = cls.id, date = MON_28_SEP_2026) }

        assertEquals(before, world.sessionsOf(cls.id))
    }

    @Test
    fun AC14_past_is_judged_in_the_phone_timezone() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val cls = world.createMonWedEveningClass(coach.id)
        // Tuesday 29 Sep 00:10 in Manila is still Monday 28 Sep in UTC; Monday is past locally.
        world.setLocal(TUE_29_SEP_2026, 0, 10)
        val before = world.sessionsOf(cls.id)

        assertFailsWith<IllegalArgumentException> { world.cancelDate(classId = cls.id, date = MON_28_SEP_2026) }
        assertEquals(before, world.sessionsOf(cls.id))
    }

    // ----- AC15 -----

    @Test
    fun AC15_cancel_class_sets_cancelledAt_cancels_today_and_future_and_keeps_past() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val cls = world.createMonWedEveningClass(coach.id)
        val student = world.student()
        world.makeActive(cls.id, student.id)

        // Student attends Monday 28 Sep.
        world.setLocal(MON_28_SEP_2026, 17, 40)
        val monday = world.sessionOn(cls.id, MON_28_SEP_2026)
        assertIs<AttendanceResult.Recorded>(
            world.recordAttendance(studentId = student.id, session = world.refOf(monday), code = world.todaysCode(coach.id)),
        )
        val attendanceBefore = world.attendance.all()

        // Coach cancels the whole class on Wednesday 30 Sep at 10:00 (today's session is still ahead).
        world.setLocal(WED_30_SEP_2026, 10)
        val cancelInstant = world.clock.now()
        world.cancelClass(classId = cls.id)

        val stored = world.classes.findClass(cls.id)
        assertNotNull(stored)
        assertEquals(cancelInstant, stored.cancelledAt)

        val sessions = world.sessionsOf(cls.id)
        assertEquals(SessionStatus.SCHEDULED, sessions.single { it.date == MON_28_SEP_2026 }.status)
        assertTrue(sessions.filter { it.date >= WED_30_SEP_2026 }.all { it.status == SessionStatus.CANCELLED })
        assertEquals((1..10).toList(), sessions.map { it.sessionNumber })
        assertEquals(attendanceBefore, world.attendance.all())
    }

    // ----- AC16 -----

    private fun cancelledClassWorld(): Triple<TestWorld, User, TrxClass> {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val cls = world.createMonWedEveningClass(coach.id)
        world.setLocal(MON_28_SEP_2026, 9)
        world.cancelClass(classId = cls.id)
        return Triple(world, coach, cls)
    }

    @Test
    fun AC16_after_cancel_class_new_requests_are_rejected() {
        val (world, _, cls) = cancelledClassWorld()
        val student = world.student()

        val result = world.requestToJoin(classId = cls.id, studentId = student.id)

        assertEquals(JoinResult.ClassCancelled, result)
        assertTrue(world.memberships.forClass(cls.id).isEmpty())
    }

    @Test
    fun AC16_after_cancel_class_guest_registration_path_creates_no_request() {
        val (world, _, cls) = cancelledClassWorld()

        val student = world.student(targetClassId = cls.id)

        assertEquals(Role.STUDENT, student.role)
        assertTrue(world.memberships.forClass(cls.id).isEmpty())
    }

    @Test
    fun AC16_after_cancel_class_invites_are_rejected() {
        val (world, _, cls) = cancelledClassWorld()
        world.student(1)

        val result = world.inviteByEmployeeId(classId = cls.id, employeeId = "EMP-1")

        assertEquals(InviteResult.ClassCancelled, result)
        assertTrue(world.memberships.forClass(cls.id).isEmpty())
    }

    @Test
    fun AC16_after_cancel_class_scans_for_remaining_dates_are_rejected() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val cls = world.createMonWedEveningClass(coach.id)
        val student = world.student()
        world.makeActive(cls.id, student.id)
        world.setLocal(MON_28_SEP_2026, 9)
        world.cancelClass(classId = cls.id)

        world.setLocal(MON_28_SEP_2026, 17, 40)
        val result = world.recordAttendance(
            studentId = student.id,
            session = world.refOf(world.sessionOn(cls.id, MON_28_SEP_2026)),
            code = world.todaysCode(coach.id),
        )

        assertEquals(AttendanceResult.ClassCancelled, result)
        assertTrue(world.attendance.all().isEmpty())
    }
}
