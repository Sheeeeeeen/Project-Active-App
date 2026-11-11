package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.InviteResult
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.SessionStatus
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.WED_30_SEP_2026
import kotlinx.datetime.atTime

class SqlCancelParityTest {

    @Test
    fun AC9_cancel_date_keeps_numbers_and_rejects_a_past_date() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            val trxClass = world.createTwoSlotMondayClass(coach.id)
            val before = world.sessionsOf(trxClass.id)
            val target = LocalDate(2026, 10, 5)
            world.cancelDate(trxClass.id, target)
            val after = world.sessionsOf(trxClass.id)
            assertEquals(before.map { it.id to it.sessionNumber }, after.map { it.id to it.sessionNumber })
            assertTrue(after.filter { it.date == target }.all { it.status == SessionStatus.CANCELLED })
            assertTrue(after.filter { it.date != target }.all { it.status == SessionStatus.SCHEDULED })

            world.setLocal(TUE_29_SEP_2026, 0, 10)
            val snapshot = world.sessionsOf(trxClass.id)
            assertFailsWith<IllegalArgumentException> { world.cancelDate(trxClass.id, MON_28_SEP_2026) }
            assertEquals(snapshot, world.sessionsOf(trxClass.id))
        }
    }

    @Test
    fun AC9_cancel_class_keeps_past_attendance_and_rejects_new_writes() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
            val monday = world.sessionOn(trxClass.id, MON_28_SEP_2026)
            assertIs<AttendanceResult.Recorded>(
                world.recordAttendance(student.id, world.refOf(monday), world.todaysCode(coach.id)),
            )
            val attendanceBefore = world.attendance.all()

            world.setLocal(WED_30_SEP_2026, 10)
            val cancelledAt = world.clock.now()
            world.cancelClass(trxClass.id)
            assertEquals(cancelledAt, world.classes.findClass(trxClass.id)!!.cancelledAt)
            val sessions = world.sessionsOf(trxClass.id)
            assertEquals(SessionStatus.SCHEDULED, sessions.single { it.date == MON_28_SEP_2026 }.status)
            assertTrue(sessions.filter { it.date >= WED_30_SEP_2026 }.all { it.status == SessionStatus.CANCELLED })
            assertEquals(attendanceBefore, world.attendance.all())

            val other = world.student(2)
            assertEquals(JoinResult.ClassCancelled, world.requestToJoin(trxClass.id, other.id))
            assertEquals(InviteResult.ClassCancelled, world.inviteByEmployeeId(trxClass.id, "EMP-2"))
            world.setLocal(WED_30_SEP_2026, 17, 40)
            assertEquals(
                AttendanceResult.ClassCancelled,
                world.recordAttendance(student.id, world.refOf(world.sessionOn(trxClass.id, WED_30_SEP_2026)), world.todaysCode(coach.id)),
            )
            assertEquals(Role.STUDENT, other.role)
            assertEquals(1, world.attendance.all().size)
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
