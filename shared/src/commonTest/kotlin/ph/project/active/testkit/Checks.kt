package ph.project.active.testkit

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.presentation.ConnectionUi

private val URL_SAFE_ID = Regex("[A-Za-z0-9_.~-]+")

fun assertUrlSafeId(id: String) {
    assertTrue(URL_SAFE_ID.matches(id), id)
}

fun assertConnection(connection: ConnectionUi, offline: Boolean) {
    assertEquals(offline, connection.isOffline)
    assertEquals(if (offline) "No internet connection" else null, connection.banner)
}
