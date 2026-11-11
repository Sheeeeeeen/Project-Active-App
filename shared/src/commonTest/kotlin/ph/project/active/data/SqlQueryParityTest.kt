package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.atTime
import ph.project.active.domain.model.CoachAttendanceStatus
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.WED_30_SEP_2026

class SqlQueryParityTest {

    @Test
    fun AC9_student_sessions_and_todays_choices_match_phase1() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val joined = world.createMonWedEveningClass(coach.id, name = "Joined")
            val pending = world.createMonWedEveningClass(coach.id, name = "Pending")
            val student = world.student()
            world.makeActive(joined.id, student.id)
            world.requestToJoin(pending.id, student.id)
            world.cancelDate(joined.id, kotlinx.datetime.LocalDate(2026, 10, 12))
            world.setLocal(MON_28_SEP_2026, 17, 40)
            world.recordAttendance(student.id, world.refOf(world.sessionOn(joined.id, MON_28_SEP_2026)), world.todaysCode(coach.id))

            world.setLocal(kotlinx.datetime.LocalDate(2026, 10, 5), 12)
            val rows = world.studentSessions(student.id)
            assertEquals(9, rows.size)
            assertTrue(rows.none { it.session.status == ph.project.active.domain.model.SessionStatus.CANCELLED })
            assertTrue(rows.all { it.session.classId == joined.id })
            val byDate = rows.associate { it.session.date to it.status }
            assertEquals(StudentSessionStatus.PRESENT, byDate[MON_28_SEP_2026])
            assertEquals(StudentSessionStatus.ABSENT, byDate[WED_30_SEP_2026])
            assertEquals(StudentSessionStatus.UPCOMING, byDate[kotlinx.datetime.LocalDate(2026, 10, 5)])

            world.setLocal(MON_28_SEP_2026, 23, 59)
            val monday = world.sessionOn(joined.id, MON_28_SEP_2026)
            fun statusNow() = world.studentSessions(student.id).single { it.session.id == monday.id }.status
            assertEquals(StudentSessionStatus.PRESENT, statusNow())

            val fresh = Phase2World(start = FRI_25_SEP_2026.atTime(9, 0))
            fresh.use {
                val freshCoach = it.coach()
                val freshClass = it.createMonWedEveningClass(freshCoach.id)
                val freshStudent = it.student()
                it.makeActive(freshClass.id, freshStudent.id)
                val session = it.sessionOn(freshClass.id, MON_28_SEP_2026)
                it.setLocal(MON_28_SEP_2026, 23, 59)
                assertEquals(
                    StudentSessionStatus.UPCOMING,
                    it.studentSessions(freshStudent.id).single { row -> row.session.id == session.id }.status,
                )
                it.setLocal(TUE_29_SEP_2026, 0, 0)
                assertEquals(
                    StudentSessionStatus.ABSENT,
                    it.studentSessions(freshStudent.id).single { row -> row.session.id == session.id }.status,
                )
                it.setLocal(MON_28_SEP_2026, 6, 30)
                assertEquals(listOf(session.id), it.todaysChoosableSessions(freshStudent.id).map { choice -> choice.id })
                it.setLocal(TUE_29_SEP_2026, 0, 30)
                assertTrue(it.todaysChoosableSessions(freshStudent.id).isEmpty())
            }
        }
    }

    @Test
    fun AC9_coach_attendance_for_a_date_matches_phase1() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach(1)
            world.giveKeys(coach.id)
            val trxClass = world.createTwoSlotMondayClass(coach.id)
            val other = world.coach(2)
            world.createMonWedEveningClass(other.id, name = "Theirs", visibility = Visibility.PUBLIC)
            val first = world.student(1)
            val second = world.student(2)
            val pending = world.student(3)
            world.makeActive(trxClass.id, first.id)
            world.makeActive(trxClass.id, second.id)
            world.requestToJoin(trxClass.id, pending.id)
            val morning = world.sessionOn(trxClass.id, MON_28_SEP_2026, MORNING)
            val evening = world.sessionOn(trxClass.id, MON_28_SEP_2026, ph.project.active.testkit.EVENING)
            world.setLocal(MON_28_SEP_2026, 7, 10)
            world.recordAttendance(first.id, world.refOf(morning), world.todaysCode(coach.id))

            world.setLocal(MON_28_SEP_2026, 12)
            val today = world.coachAttendanceForDate(coach.id, MON_28_SEP_2026).associateBy { it.session.id }
            assertEquals(setOf(morning.id, evening.id), today.keys)
            assertEquals(CoachAttendanceStatus.PRESENT, today.getValue(morning.id).students.single { it.studentId == first.id }.status)
            assertEquals(CoachAttendanceStatus.NOT_SCANNED, today.getValue(evening.id).students.single { it.studentId == second.id }.status)
            assertTrue(today.getValue(morning.id).students.none { it.studentId == pending.id })

            world.setLocal(TUE_29_SEP_2026, 0, 0)
            val ended = world.coachAttendanceForDate(coach.id, MON_28_SEP_2026).associateBy { it.session.id }
            assertEquals(CoachAttendanceStatus.ABSENT, ended.getValue(evening.id).students.single { it.studentId == first.id }.status)
            assertEquals(CoachAttendanceStatus.PRESENT, ended.getValue(morning.id).students.single { it.studentId == first.id }.status)
            assertTrue(world.coachAttendanceForDate(coach.id, MON_28_SEP_2026).all { it.session.classId == trxClass.id })
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
