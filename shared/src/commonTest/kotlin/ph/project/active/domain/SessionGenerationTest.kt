package ph.project.active.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.Visibility

class SessionGenerationTest {

    // ----- AC7 -----

    @Test
    fun AC7_monday_wednesday_class_saved_friday_creates_ten_numbered_scheduled_sessions() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()

        val cls = world.createClass(
            coachId = coach.id,
            name = "TRX Evening",
            visibility = Visibility.PUBLIC,
            slotsByWeekday = MON_WED_EVENING,
            endDate = SAT_31_OCT_2026,
        )

        val sessions = world.sessionsOf(cls.id)
        val expectedDates = listOf(
            LocalDate(2026, 9, 28), LocalDate(2026, 9, 30),
            LocalDate(2026, 10, 5), LocalDate(2026, 10, 7),
            LocalDate(2026, 10, 12), LocalDate(2026, 10, 14),
            LocalDate(2026, 10, 19), LocalDate(2026, 10, 21),
            LocalDate(2026, 10, 26), LocalDate(2026, 10, 28),
        )
        assertEquals(expectedDates, sessions.map { it.date })
        assertEquals((1..10).toList(), sessions.map { it.sessionNumber })
        assertTrue(sessions.all { it.status == SessionStatus.SCHEDULED })
        assertTrue(sessions.all { it.slot == EVENING })
        assertTrue(sessions.all { it.classId == cls.id })
        assertEquals(10, sessions.map { it.id }.toSet().size, "session ids must be unique")
    }

    @Test
    fun AC7_saved_class_keeps_its_definition() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()

        val cls = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE)

        assertEquals(coach.id, cls.coachId)
        assertEquals("TRX Evening", cls.name)
        assertEquals(Visibility.PRIVATE, cls.visibility)
        assertEquals(MON_WED_EVENING, cls.slotsByWeekday)
        assertEquals(SAT_31_OCT_2026, cls.endDate)
        assertEquals(FRI_25_SEP_2026, cls.createdOn)
        assertNull(cls.cancelledAt)
        assertEquals(cls, world.classes.findClass(cls.id))
    }

    // ----- AC8 -----

    @Test
    fun AC8_save_date_is_included_when_it_matches_a_selected_weekday() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(9, 0))
        val coach = world.coach()

        val cls = world.createMonWedEveningClass(coach.id, endDate = LocalDate(2026, 10, 5))

        assertEquals(
            listOf(MON_28_SEP_2026, WED_30_SEP_2026, LocalDate(2026, 10, 5)),
            world.sessionsOf(cls.id).map { it.date },
        )
        assertEquals(listOf(1, 2, 3), world.sessionsOf(cls.id).map { it.sessionNumber })
    }

    @Test
    fun AC8_save_date_is_the_local_date_in_the_phone_timezone() {
        // 2026-09-27T16:30Z is Monday 28 Sep 00:30 in Asia/Manila (UTC+8), still Sunday in UTC.
        val world = TestWorld()
        world.clock.current = Instant.parse("2026-09-27T16:30:00Z")
        val coach = world.coach()

        val cls = world.createMonWedEveningClass(coach.id, endDate = WED_30_SEP_2026)

        assertEquals(MON_28_SEP_2026, cls.createdOn)
        assertEquals(listOf(MON_28_SEP_2026, WED_30_SEP_2026), world.sessionsOf(cls.id).map { it.date })
    }

    // ----- AC9 -----

    @Test
    fun AC9_open_ended_class_creates_sessions_through_the_twelve_month_test_horizon() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0), openEndedHorizon = DatePeriod(months = 12))
        val coach = world.coach()

        val cls = world.createClass(
            coachId = coach.id,
            name = "TRX Monday",
            visibility = Visibility.PUBLIC,
            slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(EVENING)),
            endDate = null,
        )

        // Horizon end is Saturday 25 Sep 2027 (not a Monday), so inclusive vs exclusive does not matter.
        val sessions = world.sessionsOf(cls.id)
        assertEquals(52, sessions.size)
        assertEquals(MON_28_SEP_2026, sessions.first().date)
        assertEquals(LocalDate(2027, 9, 20), sessions.last().date)
        assertEquals((1..52).toList(), sessions.map { it.sessionNumber })
        assertTrue(sessions.all { it.date.dayOfWeek == DayOfWeek.MONDAY })
        assertTrue(sessions.all { it.date <= FRI_25_SEP_2026.plus(DatePeriod(months = 12)) })
    }

    @Test
    fun AC9_open_ended_horizon_is_configurable() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0), openEndedHorizon = DatePeriod(months = 1))
        val coach = world.coach()

        val cls = world.createClass(
            coachId = coach.id,
            name = "TRX Monday",
            visibility = Visibility.PUBLIC,
            slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(EVENING)),
            endDate = null,
        )

        // Horizon end is Sunday 25 Oct 2026.
        assertEquals(
            listOf(MON_28_SEP_2026, LocalDate(2026, 10, 5), LocalDate(2026, 10, 12), LocalDate(2026, 10, 19)),
            world.sessionsOf(cls.id).map { it.date },
        )
    }

    @Test
    fun AC9_open_ended_class_stays_open_until_cancelled() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()

        val cls = world.createMonWedEveningClass(coach.id, endDate = null)

        val stored = world.classes.findClass(cls.id)!!
        assertNull(stored.endDate)
        assertNull(stored.cancelledAt)
        assertTrue(world.sessionsOf(cls.id).all { it.status == SessionStatus.SCHEDULED })
        // Still joinable months later (open until cancelled).
        world.setLocal(LocalDate(2027, 3, 1), 9)
        val student = world.student()
        assertIs<JoinResult.Requested>(world.requestToJoin(classId = cls.id, studentId = student.id))
    }

    // ----- AC10 -----

    @Test
    fun AC10_every_day_class_creates_one_session_for_every_remaining_date() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()

        val cls = world.createClass(
            coachId = coach.id,
            name = "TRX Daily",
            visibility = Visibility.PUBLIC,
            slotsByWeekday = DayOfWeek.entries.associateWith { listOf(EVENING) },
            endDate = WED_30_SEP_2026,
        )

        val sessions = world.sessionsOf(cls.id)
        assertEquals((25..30).map { LocalDate(2026, 9, it) }, sessions.map { it.date })
        assertEquals((1..6).toList(), sessions.map { it.sessionNumber })
        assertTrue(sessions.all { it.status == SessionStatus.SCHEDULED && it.slot == EVENING })
    }

    // ----- AC11 -----

    @Test
    fun AC11_two_slots_on_one_weekday_create_two_sessions_per_date_numbered_by_start_time() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()

        val cls = world.createTwoSlotMondayClass(coach.id, endDate = LocalDate(2026, 10, 5))

        val sessions = world.sessionsOf(cls.id)
        assertEquals(
            listOf(
                MON_28_SEP_2026 to MORNING,
                MON_28_SEP_2026 to EVENING,
                WED_30_SEP_2026 to EVENING,
                LocalDate(2026, 10, 5) to MORNING,
                LocalDate(2026, 10, 5) to EVENING,
            ),
            sessions.map { it.date to it.slot },
        )
        assertEquals(listOf(1, 2, 3, 4, 5), sessions.map { it.sessionNumber })
        assertTrue(sessions.all { it.status == SessionStatus.SCHEDULED })
    }

    @Test
    fun AC11_attendance_is_recorded_per_session_so_each_slot_can_be_marked() {
        val world = TestWorld(start = FRI_25_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val cls = world.createTwoSlotMondayClass(coach.id)
        val student = world.student()
        world.makeActive(cls.id, student.id)

        world.setLocal(MON_28_SEP_2026, 7, 10)
        val morning = world.sessionOn(cls.id, MON_28_SEP_2026, MORNING)
        val first = world.recordAttendance(studentId = student.id, session = world.refOf(morning), code = world.todaysCode(coach.id))

        world.setLocal(MON_28_SEP_2026, 17, 40)
        val evening = world.sessionOn(cls.id, MON_28_SEP_2026, EVENING)
        val second = world.recordAttendance(studentId = student.id, session = world.refOf(evening), code = world.todaysCode(coach.id))

        assertIs<AttendanceResult.Recorded>(first)
        assertIs<AttendanceResult.Recorded>(second)
        assertEquals(setOf(morning.id, evening.id), world.attendance.all().map { it.sessionId }.toSet())
        assertEquals(2, world.attendance.all().size)
    }

    // ----- AC12 -----

    @Test
    fun AC12_dates_before_the_save_date_are_never_created_even_if_they_match() {
        // Saved on Thursday 1 Oct 2026; Monday 28 Sep and Wednesday 30 Sep match but are past.
        val world = TestWorld(start = LocalDate(2026, 10, 1).atTime(9, 0))
        val coach = world.coach()

        val cls = world.createMonWedEveningClass(coach.id, endDate = LocalDate(2026, 10, 14))

        val sessions = world.sessionsOf(cls.id)
        assertEquals(
            listOf(LocalDate(2026, 10, 5), LocalDate(2026, 10, 7), LocalDate(2026, 10, 12), LocalDate(2026, 10, 14)),
            sessions.map { it.date },
        )
        assertEquals(listOf(1, 2, 3, 4), sessions.map { it.sessionNumber })
        assertTrue(sessions.none { it.date < LocalDate(2026, 10, 1) })
    }

    @Test
    fun AC12_save_date_in_phone_timezone_decides_what_is_past() {
        // 2026-09-30T16:30Z is Thursday 1 Oct 00:30 in Manila: Wednesday 30 Sep is already past locally.
        val world = TestWorld()
        world.clock.current = Instant.parse("2026-09-30T16:30:00Z")
        val coach = world.coach()

        val cls = world.createMonWedEveningClass(coach.id, endDate = LocalDate(2026, 10, 7))

        assertEquals(listOf(LocalDate(2026, 10, 5), LocalDate(2026, 10, 7)), world.sessionsOf(cls.id).map { it.date })
    }
}
