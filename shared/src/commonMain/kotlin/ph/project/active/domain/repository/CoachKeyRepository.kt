package ph.project.active.domain.repository

import ph.project.active.domain.crypto.Ed25519KeyPair

interface CoachKeyRepository {
    fun save(coachId: String, keyPair: Ed25519KeyPair)
    fun find(coachId: String): Ed25519KeyPair?
}
