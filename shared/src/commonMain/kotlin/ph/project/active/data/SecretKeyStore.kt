package ph.project.active.data

interface SecretKeyStore {
    fun putPrivateKey(coachId: String, key: ByteArray)
    fun privateKey(coachId: String): ByteArray?
}
