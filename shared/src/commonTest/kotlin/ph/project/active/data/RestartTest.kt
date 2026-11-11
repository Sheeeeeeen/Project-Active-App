package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import ph.project.active.testkit.idle
import kotlinx.datetime.atTime
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.phase2WorldOnFile
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class RestartTest {

    @Test
    fun AC10_reopen_keeps_rows_and_view_models_show_the_same_lists() = runVm {
        val path = tempDbPath()
        val prepared = phase2WorldOnFile(path).use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.giveKeys(coach.id)
            world.sessionStore.save(student.toAccount())
            world.setLocal(MON_28_SEP_2026, 17, 40)
            world.network.set(NetworkStatus.OFFLINE)
            world.recordAttendance(student.id, world.refOf(world.sessionOn(trxClass.id, MON_28_SEP_2026)), world.todaysCode(coach.id))
            world.cancelDate(trxClass.id, kotlinx.datetime.LocalDate(2026, 10, 5))
            idle()
            val account = student.toAccount()
            val coachAccount = coach.toAccount()
            PreparedRestart(
                student = account,
                coach = coachAccount,
                classId = trxClass.id,
                publicKey = world.coachKeys.find(coach.id)!!.publicKey.copyOf(),
                sessions = world.sessionsOf(trxClass.id).map { it.date to it.status },
                scannedAt = world.attendance.all().single().scannedAt,
                studentRows = world.studentSessionsVm(account).state.value.let { state ->
                    (state.upcoming + state.today + state.earlier).map { row ->
                        listOf(row.className, row.sessionNumber.toString(), row.status.name, row.uploadPending.toString())
                    }
                },
                choices = world.attend(account).let { attend ->
                    attend.onOpened()
                    attend.state.value.choices.map { it.sessionId to it.className }
                },
                recordedToday = world.attend(account).state.value.recordedToday.map { it.sessionNumber to it.uploadPending },
                coachRows = world.coachClasses(coachAccount).state.value.classes.map { row ->
                    Triple(row.classId, row.rosterCount, row.isCancelled)
                },
            )
        }

        phase2WorldOnFile(path, start = MON_28_SEP_2026.atTime(17, 40)).use { world ->
            idle()
            assertEquals(prepared.student, world.sessionStore.account.value)
            assertEquals(prepared.sessions, world.sessionsOf(prepared.classId).map { it.date to it.status })
            assertTrue(world.sessionsOf(prepared.classId).any { it.status == SessionStatus.CANCELLED })
            val attendance = world.attendance.all().single()
            assertEquals(UploadState.PENDING, attendance.uploadState)
            assertEquals(prepared.scannedAt, attendance.scannedAt)
            val found = world.coachKeys.find(prepared.coach.id)
            assertNotNull(found)
            assertTrue(found.publicKey.contentEquals(prepared.publicKey))

            val sessions = world.studentSessionsVm(prepared.student).state.value
            assertEquals(
                prepared.studentRows,
                (sessions.upcoming + sessions.today + sessions.earlier).map { row ->
                    listOf(row.className, row.sessionNumber.toString(), row.status.name, row.uploadPending.toString())
                },
            )
            val attend = world.attend(prepared.student)
            attend.onOpened()
            assertEquals(prepared.choices, attend.state.value.choices.map { it.sessionId to it.className })
            assertEquals(prepared.recordedToday, attend.state.value.recordedToday.map { it.sessionNumber to it.uploadPending })
            assertEquals(
                prepared.coachRows,
                world.coachClasses(prepared.coach).state.value.classes.map {
                    Triple(it.classId, it.rosterCount, it.isCancelled)
                },
            )
        }
    }
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
