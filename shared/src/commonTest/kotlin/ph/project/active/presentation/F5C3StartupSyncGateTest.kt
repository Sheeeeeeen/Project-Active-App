package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.sync.OutboxSync
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.UnconfirmedNetworkMonitor
import ph.project.active.testkit.idle
import ph.project.active.testkit.runVm

/** Flow 5 spec C3, decision 15 and F5-AC13: start-up sync waits for the first real network reading. */
class F5C3StartupSyncGateTest {

    // Decision 15: "MutableNetworkMonitor and AndroidNetworkMonitor are real from the start"
    @Test
    fun F5_AC13_mutableNetworkMonitorIsARealReadingFromTheStart() {
        assertTrue(MutableNetworkMonitor(NetworkStatus.ONLINE).hasRealReading.value)
        assertTrue(MutableNetworkMonitor(NetworkStatus.OFFLINE).hasRealReading.value)
    }

    // F5-AC13 / C3: "MutableNetworkMonitor starting ONLINE still syncs once on start"
    @Test
    fun F5_AC13_aMonitorThatStartsWithARealOnlineSyncsOnceOnStart() = runVm {
        Phase2World().use { world ->
            world.attendance.add(pending("a"))
            val uploader = FakeAttendanceUploader()
            val root = rootWith(world, MutableNetworkMonitor(NetworkStatus.ONLINE), uploader)
            idle()

            assertEquals(listOf("a"), uploader.uploaded.map { it.id })
            assertEquals(UploadState.SYNCED, world.attendance.all().single().uploadState)
            root.onNavigationHandled()
        }
    }

    // Decision 15: "IosNetworkMonitor’s pre-callback ONLINE … is not a real reading" (nothing uploads before a real reading)
    @Test
    fun F5_AC13_anUnconfirmedOnlineDoesNotUpload() = runVm {
        Phase2World().use { world ->
            world.attendance.add(pending("a"))
            val uploader = FakeAttendanceUploader()
            val root = rootWith(world, UnconfirmedNetworkMonitor(), uploader)
            idle()

            assertTrue(uploader.uploaded.isEmpty(), "no upload before the first real reading")
            assertEquals(UploadState.PENDING, world.attendance.all().single().uploadState)
            root.onNavigationHandled()
        }
    }

    // F5-AC13 / C3: "An unconfirmed initial ONLINE followed by a real OFFLINE does not upload. A later real ONLINE syncs once."
    @Test
    fun F5_AC13_unconfirmedOnlineThenRealOfflineDoesNotUploadAndALaterRealOnlineSyncsOnce() = runVm {
        Phase2World().use { world ->
            world.attendance.add(pending("a"))
            val uploader = FakeAttendanceUploader()
            val network = UnconfirmedNetworkMonitor()
            val root = rootWith(world, network, uploader)
            idle()

            network.reading(NetworkStatus.OFFLINE)
            idle()
            assertTrue(uploader.uploaded.isEmpty(), "a real OFFLINE after the unconfirmed ONLINE uploads nothing")
            assertEquals(UploadState.PENDING, world.attendance.all().single().uploadState)

            network.reading(NetworkStatus.ONLINE)
            idle()
            assertEquals(listOf("a"), uploader.uploaded.map { it.id }, "the later real ONLINE syncs once")
            assertEquals(UploadState.SYNCED, world.attendance.all().single().uploadState)
            root.onNavigationHandled()
        }
    }

    // Decision 15: "the previous real reading was not ONLINE (including "no previous real reading", so a restart that is actually online still syncs)"
    @Test
    fun F5_AC13_aFirstRealOnlineAfterTheUnconfirmedOnlineSyncsOnce() = runVm {
        Phase2World().use { world ->
            world.attendance.add(pending("a"))
            val uploader = FakeAttendanceUploader()
            val network = UnconfirmedNetworkMonitor()
            val root = rootWith(world, network, uploader)
            idle()

            network.reading(NetworkStatus.ONLINE)
            idle()
            assertEquals(listOf("a"), uploader.uploaded.map { it.id })
            assertEquals(UploadState.SYNCED, world.attendance.all().single().uploadState)
            root.onNavigationHandled()
        }
    }

    // C3: "A failed sync is retried on the next real ONLINE"
    @Test
    fun F5_AC13_aFailedSyncIsRetriedOnTheNextRealOnline() = runVm {
        Phase2World().use { world ->
            world.attendance.add(pending("a"))
            var calls = 0
            val uploader = FakeAttendanceUploader { calls += 1; calls > 1 }
            val network = UnconfirmedNetworkMonitor()
            val root = rootWith(world, network, uploader)
            idle()

            network.reading(NetworkStatus.ONLINE)
            idle()
            assertEquals(1, uploader.uploaded.size, "the first real ONLINE tries once")
            assertEquals(UploadState.PENDING, world.attendance.all().single().uploadState, "the failed row stays pending")

            network.reading(NetworkStatus.OFFLINE)
            network.reading(NetworkStatus.ONLINE)
            idle()
            assertEquals(listOf("a", "a"), uploader.uploaded.map { it.id }, "retried on the next real ONLINE")
            assertEquals(UploadState.SYNCED, world.attendance.all().single().uploadState)
            root.onNavigationHandled()
        }
    }

    private fun rootWith(world: Phase2World, network: NetworkMonitor, uploader: FakeAttendanceUploader): RootViewModel =
        RootViewModel(world.sessionStore, network, OutboxSync(world.attendance, uploader, network))

    private fun pending(id: String): AttendanceRecord = AttendanceRecord(
        id = id,
        sessionId = "session-1",
        classId = "class-1",
        studentId = id,
        scannedAt = Instant.parse("2026-09-28T01:00:00Z"),
    )
}
