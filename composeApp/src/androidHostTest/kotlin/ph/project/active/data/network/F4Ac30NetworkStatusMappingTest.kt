package ph.project.active.data.network

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.NetworkStatus

/**
 * F4-AC30: the Android network callback maps to ONLINE only when the default network has
 * INTERNET and VALIDATED. A fake signal stands in for NetworkCapabilities, so no device is needed.
 */
class F4Ac30NetworkStatusMappingTest {
    @Test
    fun F4_AC30_internetAndValidatedIsOnline() {
        assertEquals(NetworkStatus.ONLINE, networkStatusOf(NetworkSignal(hasInternet = true, isValidated = true)))
    }

    @Test
    fun F4_AC30_internetWithoutValidatedIsOffline() {
        assertEquals(NetworkStatus.OFFLINE, networkStatusOf(NetworkSignal(hasInternet = true, isValidated = false)))
    }

    @Test
    fun F4_AC30_validatedWithoutInternetIsOffline() {
        assertEquals(NetworkStatus.OFFLINE, networkStatusOf(NetworkSignal(hasInternet = false, isValidated = true)))
    }

    @Test
    fun F4_AC30_aLostNetworkIsOffline() {
        assertEquals(NetworkStatus.OFFLINE, networkStatusOf(null))
    }
}
