package ph.project.active.domain.repository.inmemory

import ph.project.active.domain.repository.CoachKeyRepository
import ph.project.active.domain.crypto.Ed25519KeyPair

class InMemoryCoachKeyRepository : CoachKeyRepository {
    private val keys = mutableMapOf<String, Ed25519KeyPair>()
    override fun save(coachId: String, keyPair: Ed25519KeyPair) {
        keys[coachId] = keyPair
    }

    override fun find(coachId: String): Ed25519KeyPair? = keys[coachId]
}
