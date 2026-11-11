package ph.project.active.data

class InMemorySecretKeyStore : SecretKeyStore {
    private val keys = mutableMapOf<String, ByteArray>()

    override fun putPrivateKey(coachId: String, key: ByteArray) {
        keys[coachId] = key.copyOf()
    }

    override fun privateKey(coachId: String): ByteArray? = keys[coachId]?.copyOf()
}
