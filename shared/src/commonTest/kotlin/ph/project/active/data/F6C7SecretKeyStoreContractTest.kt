package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.data.fake.InMemorySecretKeyStore
import ph.project.active.data.repository.SecretKeyStore

/**
 * Flow 6 spec C7 (decision 15, F6-AC18): the signing-key store contract. `AndroidSecretKeyStore` and
 * `IosSecretKeyStore` must meet the same rules; this suite pins them on the shared in-memory store.
 */
class F6C7SecretKeyStoreContractTest {

    private fun store(): SecretKeyStore = InMemorySecretKeyStore()

    // C7: "privateKey of an unknown coach is null"
    @Test
    fun F6_C7_anUnknownCoachHasNoPrivateKey() {
        assertNull(store().privateKey("coach-unknown"))
    }

    // C7: "after putPrivateKey it returns equal bytes"
    @Test
    fun F6_C7_putThenReadReturnsEqualBytes() {
        val store = store()
        store.putPrivateKey("coach-1", byteArrayOf(1, 2, 3))
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(store.privateKey("coach-1")))
    }

    // C7: "a second putPrivateKey replaces the first"
    @Test
    fun F6_C7_aSecondPutReplacesTheFirst() {
        val store = store()
        store.putPrivateKey("coach-1", byteArrayOf(1, 2, 3))
        store.putPrivateKey("coach-1", byteArrayOf(9, 8))
        assertTrue(byteArrayOf(9, 8).contentEquals(store.privateKey("coach-1")))
    }

    // C7: "changing the returned array does not change what is stored"
    @Test
    fun F6_C7_changingTheReturnedArrayDoesNotChangeTheStoredKey() {
        val store = store()
        store.putPrivateKey("coach-1", byteArrayOf(1, 2, 3))
        store.privateKey("coach-1")!![0] = 42
        assertTrue(byteArrayOf(1, 2, 3).contentEquals(store.privateKey("coach-1")))
    }
}
