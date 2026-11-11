package ph.project.active.data.repository

interface SecretKeyStore {
    fun putPrivateKey(coachId: String, key: ByteArray)
    fun privateKey(coachId: String): ByteArray?
}
