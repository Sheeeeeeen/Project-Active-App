package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.SessionRef
import ph.project.active.domain.model.UploadState
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import kotlinx.datetime.atTime
import ph.project.active.testkit.TUE_29_SEP_2026

class SqlAttendanceParityTest {

    @Test
    fun AC9_record_attendance_uses_the_chosen_session_and_typed_errors() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createTwoSlotMondayClass(coach.id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 7, 15)
            val evening = world.sessionOn(trxClass.id, MON_28_SEP_2026, EVENING)
            val scanTime = world.clock.now()
            val recorded = assertIs<AttendanceResult.Recorded>(
                world.recordAttendance(student.id, world.refOf(evening), world.todaysCode(coach.id)),
            )
            assertEquals(scanTime, recorded.attendance.scannedAt)
            assertEquals(UploadState.PENDING, recorded.attendance.uploadState)
            assertEquals(AttendanceResult.AlreadyRecorded, world.recordAttendance(student.id, world.refOf(evening), world.todaysCode(coach.id)))
            assertEquals(1, world.attendance.all().size)

            val morning = world.sessionOn(trxClass.id, MON_28_SEP_2026, MORNING)
            assertIs<AttendanceResult.Recorded>(
                world.recordAttendance(student.id, world.refOf(morning), world.todaysCode(coach.id)),
            )
            assertEquals(2, world.attendance.all().size)

            val stranger = world.student(2)
            assertEquals(
                AttendanceResult.NotOnRoster,
                world.recordAttendance(stranger.id, world.refOf(morning), world.todaysCode(coach.id)),
            )
            assertEquals(AttendanceResult.InvalidCode, world.recordAttendance(student.id, world.refOf(morning), "not a code"))
            assertEquals(AttendanceResult.NoSessionChosen, world.recordAttendance(student.id, null, world.todaysCode(coach.id)))
            val otherCoach = world.coach(2)
            world.giveKeys(otherCoach.id)
            assertEquals(
                AttendanceResult.WrongCoach,
                world.recordAttendance(student.id, world.refOf(morning), world.todaysCode(otherCoach.id)),
            )
            val sunday = world.generateDailyCode(coach.id, kotlinx.datetime.LocalDate(2026, 9, 27))
            assertEquals(AttendanceResult.WrongDate, world.recordAttendance(student.id, world.refOf(morning), sunday))

            world.cancelDate(trxClass.id, MON_28_SEP_2026)
            val beforeCancelScan = world.attendance.all()
            assertEquals(
                AttendanceResult.SessionCancelled,
                world.recordAttendance(student.id, world.refOf(evening), world.todaysCode(coach.id)),
            )
            assertEquals(beforeCancelScan, world.attendance.all())
        }
    }

    @Test
    fun AC9_offline_cache_and_flush_outbox_match_phase1() {
        Phase2World(start = FRI_25_SEP_2026.atTime(9, 0)).use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
            world.network.set(NetworkStatus.OFFLINE)
            val scanTime = world.clock.now()
            assertIs<AttendanceResult.Recorded>(
                world.recordAttendance(student.id, world.refOf(world.sessionOn(trxClass.id, MON_28_SEP_2026)), world.todaysCode(coach.id)),
            )
            assertEquals(scanTime, world.attendance.all().single().scannedAt)
            assertEquals(0, world.flushOutbox())
            assertEquals(UploadState.PENDING, world.attendance.all().single().uploadState)

            val missing = SessionRef(trxClass.id, TUE_29_SEP_2026, EVENING)
            world.setLocal(TUE_29_SEP_2026, 17, 40)
            assertEquals(
                AttendanceResult.SessionUnknown,
                world.recordAttendance(student.id, missing, world.todaysCode(coach.id)),
            )
            assertEquals(
                AttendanceResult.NotOnRoster,
                world.recordAttendance(
                    world.student(2).id,
                    world.refOf(world.sessionOn(trxClass.id, MON_28_SEP_2026)),
                    world.todaysCode(coach.id),
                ),
            )

            world.setLocal(MON_28_SEP_2026, 17, 40)
            world.network.set(NetworkStatus.ONLINE)
            world.clock.current = world.clock.now() + 3.hours
            assertEquals(1, world.flushOutbox())
            assertEquals(scanTime, world.attendance.all().single().scannedAt)
            assertEquals(UploadState.SYNCED, world.attendance.all().single().uploadState)
            world.clock.current = world.clock.now() + 5.minutes
            assertEquals(0, world.flushOutbox())
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
