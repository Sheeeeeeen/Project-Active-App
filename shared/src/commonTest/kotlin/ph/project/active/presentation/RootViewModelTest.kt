package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import ph.project.active.testkit.idle
import kotlinx.datetime.atTime
import ph.project.active.data.FakeAttendanceUploader
import ph.project.active.data.tempDbPath
import ph.project.active.domain.AttendanceRecord
import ph.project.active.domain.NetworkStatus
import ph.project.active.domain.Role
import ph.project.active.domain.UploadState
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.phase2WorldOnFile
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class RootViewModelTest {

    @Test
    fun AC23_root_follows_the_cached_account_including_after_restart() = runVm {
        val path = tempDbPath()
        val student = phase2WorldOnFile(path).use { world ->
            val account = world.studentAccount()
            assertEquals(Root.Guest, world.root().state.value.root)
            world.sessionStore.save(account)
            idle()
            assertEquals(Root.Student(account), world.root().state.value.root)
            account
        }
        phase2WorldOnFile(path).use { world ->
            idle()
            assertEquals(Root.Student(student), world.root().state.value.root)
        }
    }

    @Test
    fun AC24_saving_a_session_switches_root_and_there_is_no_role_switch_action() = runVm {
        Phase2World().use { world ->
            val root = world.root()
            val student = world.studentAccount()
            val coach = world.coachAccount()
            idle()
            assertEquals(Root.Guest, root.state.value.root)
            world.sessionStore.save(student)
            idle()
            assertEquals(Root.Student(student), root.state.value.root)
            world.sessionStore.save(coach)
            idle()
            assertEquals(Root.Coach(coach), root.state.value.root)
            val names = RootViewModel::class.java.methods.map { it.name }
            assertTrue(names.none { it == "setRole" || it == "onSwitchRole" || it == "switchRole" || it == "setMode" })
            assertEquals(Role.COACH, (root.state.value.root as Root.Coach).account.role)
        }
    }

    @Test
    fun AC20_root_syncs_once_on_each_offline_to_online_transition_and_after_restart() = runVm {
        val uploader = FakeAttendanceUploader()
        Phase2World(uploader = uploader).use { world ->
            world.attendance.add(pending("a", "2026-09-28T09:00:00Z"))
            world.network.set(NetworkStatus.OFFLINE)
            val root = world.root()
            idle()
            assertEquals(UploadState.PENDING, world.attendance.all().single().uploadState)
            assertTrue(uploader.uploaded.isEmpty())

            world.network.set(NetworkStatus.ONLINE)
            idle()
            assertEquals(UploadState.SYNCED, world.attendance.all().single().uploadState)
            assertEquals(listOf("a"), uploader.uploaded.map { it.id })

            world.attendance.add(pending("b", "2026-09-28T10:00:00Z"))
            idle()
            assertEquals(listOf("a"), uploader.uploaded.map { it.id })
            world.network.set(NetworkStatus.OFFLINE)
            world.network.set(NetworkStatus.ONLINE)
            idle()
            assertEquals(listOf("a", "b"), uploader.uploaded.map { it.id })
            assertEquals(UploadState.SYNCED, world.attendance.find("session-1", "b")!!.uploadState)
        }

        val path = tempDbPath()
        val restartUploader = FakeAttendanceUploader()
        phase2WorldOnFile(path, start = MON_28_SEP_2026.atTime(9, 0)).use { world ->
            world.attendance.add(pending("c", "2026-09-28T09:00:00Z"))
        }
        phase2WorldOnFile(
            path = path,
            start = MON_28_SEP_2026.atTime(9, 0),
            network = ph.project.active.data.MutableNetworkMonitor(NetworkStatus.ONLINE),
        ).use { world ->
            val syncing = ph.project.active.data.OutboxSync(world.attendance, restartUploader, world.network)
            val root = ph.project.active.presentation.RootViewModel(world.sessionStore, world.network, syncing)
            idle()
            assertEquals(UploadState.SYNCED, world.attendance.find("session-1", "c")!!.uploadState)
            assertEquals(listOf("c"), restartUploader.uploaded.map { it.id })
            root.onNavigationHandled()
        }
    }
}

private fun pending(id: String, instant: String): AttendanceRecord = AttendanceRecord(
    id = id,
    sessionId = "session-1",
    classId = "class-1",
    studentId = id,
    scannedAt = Instant.parse(instant),
)
