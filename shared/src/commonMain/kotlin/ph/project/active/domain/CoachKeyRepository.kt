package ph.project.active.domain

interface CoachKeyRepository {
    fun save(coachId: String, keyPair: Ed25519KeyPair)
    fun find(coachId: String): Ed25519KeyPair?
}
