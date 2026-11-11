package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.data.fake.InMemorySecretKeyStore

class InMemorySecretKeyStoreTest {

    @Test
    fun AC13_put_replaces_the_private_key_for_a_coach() {
        val store = InMemorySecretKeyStore()
        assertNull(store.privateKey("coach-1"))
        store.putPrivateKey("coach-1", byteArrayOf(1, 2, 3))
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(store.privateKey("coach-1")))
        store.putPrivateKey("coach-1", byteArrayOf(9))
        assertTrue(byteArrayOf(9).contentEquals(store.privateKey("coach-1")))
        assertNull(store.privateKey("coach-2"))
    }
}
