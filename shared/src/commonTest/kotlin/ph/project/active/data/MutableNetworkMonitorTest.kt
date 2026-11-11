package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.NetworkStatus

class MutableNetworkMonitorTest {

    @Test
    fun AC16_monitor_exposes_status_and_set() {
        val monitor = MutableNetworkMonitor(NetworkStatus.ONLINE)
        assertEquals(NetworkStatus.ONLINE, monitor.status.value)
        monitor.set(NetworkStatus.OFFLINE)
        assertEquals(NetworkStatus.OFFLINE, monitor.status.value)
        monitor.set(NetworkStatus.ONLINE)
        assertEquals(NetworkStatus.ONLINE, monitor.status.value)
    }
}
