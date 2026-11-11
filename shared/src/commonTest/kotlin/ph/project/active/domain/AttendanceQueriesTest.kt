package ph.project.active.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.CoachAttendanceStatus
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.domain.model.Visibility

class AttendanceQueriesTest {

    private val MON_5_OCT = LocalDate(2026, 10, 5)
    private val MON_12_OCT = LocalDate(2026, 10, 12)

    private fun TestWorld.scan(studentId: String, coachId: String, session: Session) =
        assertIs<AttendanceResult.Recorded>(
            recordAttendance(studentId = studentId, session = refOf(session), code = todaysCode(coachId)),
        )

    // ----- AC37 -----

    @Test
    fun AC37_student_sessions_one_row_per_session_with_upcoming_present_absent_and_no_cancelled() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val cls = world.createMonWedEveningClass(coach.id)
        val student = world.student()
        world.makeActive(cls.id, student.id)
        world.cancelDate(classId = cls.id, date = MON_12_OCT)

        // Present on Mon 28 Sep, missed Wed 30 Sep.
        world.setLocal(MON_28_SEP_2026, 17, 40)
        world.scan(student.id, coach.id, world.sessionOn(cls.id, MON_28_SEP_2026))

        // Now: Monday 5 Oct at noon, no scan yet today.
        world.setLocal(MON_5_OCT, 12)
        val rows = world.studentSessions(studentId = student.id)

        val byDate = rows.associate { it.session.date to it.status }
        assertEquals(9, rows.size, "10 sessions minus 1 cancelled")
        assertEquals(rows.size, rows.map { it.session.id }.toSet().size, "one row per session")
        assertTrue(rows.none { it.session.date == MON_12_OCT }, "cancelled sessions are omitted")
        assertTrue(rows.none { it.session.status == SessionStatus.CANCELLED })
        assertEquals(StudentSessionStatus.PRESENT, byDate[MON_28_SEP_2026])
        assertEquals(StudentSessionStatus.ABSENT, byDate[WED_30_SEP_2026])
        assertEquals(StudentSessionStatus.UPCOMING, byDate[MON_5_OCT])
        assertEquals(StudentSessionStatus.UPCOMING, byDate[LocalDate(2026, 10, 7)])
        assertEquals(StudentSessionStatus.UPCOMING, byDate[LocalDate(2026, 10, 28)])

        // Scanning today flips today's row to PRESENT.
        world.setLocal(MON_5_OCT, 17, 35)
        world.scan(student.id, coach.id, world.sessionOn(cls.id, MON_5_OCT))
        val todayRow = world.studentSessions(studentId = student.id).single { it.session.date == MON_5_OCT }
        assertEquals(StudentSessionStatus.PRESENT, todayRow.status)
    }

    @Test
    fun AC37_today_without_record_is_upcoming_until_the_local_day_is_over_then_absent() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val cls = world.createMonWedEveningClass(coach.id)
        val student = world.student()
        world.makeActive(cls.id, student.id)
        val monday = world.sessionOn(cls.id, MON_28_SEP_2026)

        fun statusNow() = world.studentSessions(studentId = student.id).single { it.session.id == monday.id }.status

        world.setLocal(FRI_25_SEP_2026, 10)
        assertEquals(StudentSessionStatus.UPCOMING, statusNow())
        world.setLocal(MON_28_SEP_2026, 19, 0) // slot ended, but the day is not over
        assertEquals(StudentSessionStatus.UPCOMING, statusNow())
        world.setLocal(MON_28_SEP_2026, 23, 59)
        assertEquals(StudentSessionStatus.UPCOMING, statusNow())
        world.setLocal(TUE_29_SEP_2026, 0, 0)
        assertEquals(StudentSessionStatus.ABSENT, statusNow())
    }

    @Test
    fun AC37_two_slots_give_two_rows_with_independent_status() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val cls = world.createTwoSlotMondayClass(coach.id, endDate = WED_30_SEP_2026)
        val student = world.student()
        world.makeActive(cls.id, student.id)
        world.setLocal(MON_28_SEP_2026, 7, 5)
        world.scan(student.id, coach.id, world.sessionOn(cls.id, MON_28_SEP_2026, MORNING))

        world.setLocal(TUE_29_SEP_2026, 9)
        val monday = world.studentSessions(studentId = student.id).filter { it.session.date == MON_28_SEP_2026 }

        assertEquals(2, monday.size)
        assertEquals(StudentSessionStatus.PRESENT, monday.single { it.session.slot == MORNING }.status)
        assertEquals(StudentSessionStatus.ABSENT, monday.single { it.session.slot == EVENING }.status)
    }

    @Test
    fun AC37_student_sessions_only_cover_classes_the_student_is_active_on() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val joined = world.createMonWedEveningClass(coach.id, name = "Joined")
        val pending = world.createMonWedEveningClass(coach.id, name = "Pending")
        val student = world.student()
        world.makeActive(joined.id, student.id)
        world.requestToJoin(classId = pending.id, studentId = student.id)

        val rows = world.studentSessions(studentId = student.id)

        assertEquals(10, rows.size)
        assertTrue(rows.all { it.session.classId == joined.id })
    }

    // ----- AC38 -----

    @Test
    fun AC38_todays_choosable_list_is_one_row_per_scheduled_session_today_on_active_classes() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val student = world.student()
        val evening = world.createMonWedEveningClass(coach.id, name = "Evening")
        val double = world.createTwoSlotMondayClass(coach.id)
        val notJoined = world.createMonWedEveningClass(coach.id, name = "Not joined")
        val cancelledToday = world.createMonWedEveningClass(coach.id, name = "Cancelled today")
        val wednesdayOnly = world.createClass(
            coachId = coach.id,
            name = "Wednesday only",
            visibility = Visibility.PUBLIC,
            slotsByWeekday = mapOf(DayOfWeek.WEDNESDAY to listOf(MORNING)),
            endDate = SAT_31_OCT_2026,
        )
        world.makeActive(evening.id, student.id)
        world.makeActive(double.id, student.id)
        world.makeActive(cancelledToday.id, student.id)
        world.makeActive(wednesdayOnly.id, student.id)
        world.requestToJoin(classId = notJoined.id, studentId = student.id)
        world.cancelDate(classId = cancelledToday.id, date = MON_28_SEP_2026)

        world.setLocal(MON_28_SEP_2026, 6, 30)
        val choosable = world.todaysChoosableSessions(studentId = student.id)

        val expected = setOf(
            world.sessionOn(evening.id, MON_28_SEP_2026, EVENING).id,
            world.sessionOn(double.id, MON_28_SEP_2026, MORNING).id,
            world.sessionOn(double.id, MON_28_SEP_2026, EVENING).id,
        )
        assertEquals(3, choosable.size, "one row per slot")
        assertEquals(expected, choosable.map { it.id }.toSet())
        assertTrue(choosable.all { it.status == SessionStatus.SCHEDULED && it.date == MON_28_SEP_2026 })
    }

    @Test
    fun AC38_todays_choosable_list_uses_the_phone_timezone_date() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        val student = world.student()
        val cls = world.createMonWedEveningClass(coach.id)
        world.makeActive(cls.id, student.id)

        world.setLocal(TUE_29_SEP_2026, 0, 30) // still Monday in UTC
        assertTrue(world.todaysChoosableSessions(studentId = student.id).isEmpty())

        world.setLocal(MON_28_SEP_2026, 23, 30)
        assertEquals(
            listOf(world.sessionOn(cls.id, MON_28_SEP_2026).id),
            world.todaysChoosableSessions(studentId = student.id).map { it.id },
        )
    }

    // ----- AC39 -----

    @Test
    fun AC39_coach_attendance_lists_each_session_with_present_and_not_scanned_today() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach(1)
        world.giveKeys(coach.id)
        val cls = world.createTwoSlotMondayClass(coach.id)
        val s1 = world.student(1)
        val s2 = world.student(2)
        val pending = world.student(3)
        world.makeActive(cls.id, s1.id)
        world.makeActive(cls.id, s2.id)
        world.requestToJoin(classId = cls.id, studentId = pending.id)
        val morning = world.sessionOn(cls.id, MON_28_SEP_2026, MORNING)
        val evening = world.sessionOn(cls.id, MON_28_SEP_2026, EVENING)

        world.setLocal(MON_28_SEP_2026, 7, 10)
        world.scan(s1.id, coach.id, morning)

        world.setLocal(MON_28_SEP_2026, 12)
        val report = world.coachAttendanceForDate(coachId = coach.id, date = MON_28_SEP_2026)

        assertEquals(setOf(morning.id, evening.id), report.map { it.session.id }.toSet(), "one entry per session")
        val bySession = report.associateBy { it.session.id }
        assertEquals(
            mapOf(s1.id to CoachAttendanceStatus.PRESENT, s2.id to CoachAttendanceStatus.NOT_SCANNED),
            bySession.getValue(morning.id).students.associate { it.studentId to it.status },
        )
        assertEquals(
            mapOf(s1.id to CoachAttendanceStatus.NOT_SCANNED, s2.id to CoachAttendanceStatus.NOT_SCANNED),
            bySession.getValue(evening.id).students.associate { it.studentId to it.status },
        )
    }

    @Test
    fun AC39_after_the_local_day_is_over_missing_scans_are_absent() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach(1)
        world.giveKeys(coach.id)
        val cls = world.createTwoSlotMondayClass(coach.id)
        val s1 = world.student(1)
        val s2 = world.student(2)
        world.makeActive(cls.id, s1.id)
        world.makeActive(cls.id, s2.id)
        val morning = world.sessionOn(cls.id, MON_28_SEP_2026, MORNING)
        val evening = world.sessionOn(cls.id, MON_28_SEP_2026, EVENING)
        world.setLocal(MON_28_SEP_2026, 7, 10)
        world.scan(s1.id, coach.id, morning)

        world.setLocal(MON_28_SEP_2026, 23, 59)
        val lateSameDay = world.coachAttendanceForDate(coachId = coach.id, date = MON_28_SEP_2026)
        assertEquals(
            CoachAttendanceStatus.NOT_SCANNED,
            lateSameDay.single { it.session.id == evening.id }.students.single { it.studentId == s2.id }.status,
        )

        world.setLocal(TUE_29_SEP_2026, 0, 0)
        val report = world.coachAttendanceForDate(coachId = coach.id, date = MON_28_SEP_2026).associateBy { it.session.id }
        assertEquals(
            mapOf(s1.id to CoachAttendanceStatus.PRESENT, s2.id to CoachAttendanceStatus.ABSENT),
            report.getValue(morning.id).students.associate { it.studentId to it.status },
        )
        assertEquals(
            mapOf(s1.id to CoachAttendanceStatus.ABSENT, s2.id to CoachAttendanceStatus.ABSENT),
            report.getValue(evening.id).students.associate { it.studentId to it.status },
        )
    }

    @Test
    fun AC39_coach_attendance_only_covers_that_coachs_classes_on_that_date() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach(1)
        val otherCoach = world.coach(2)
        val mine = world.createMonWedEveningClass(coach.id, name = "Mine")
        world.createMonWedEveningClass(otherCoach.id, name = "Theirs")
        world.setLocal(MON_28_SEP_2026, 12)

        val report = world.coachAttendanceForDate(coachId = coach.id, date = MON_28_SEP_2026)

        assertEquals(listOf(world.sessionOn(mine.id, MON_28_SEP_2026).id), report.map { it.session.id })
        assertTrue(report.single().students.isEmpty(), "no ACTIVE roster yet")
    }
}
