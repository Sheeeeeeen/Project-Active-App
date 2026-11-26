package ph.project.active.domain

import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.domain.repository.CoachKeyRepository

/** F7-AC3: a key store that fails on every call, so any key lookup on the student path shows up. */
class ThrowingCoachKeyRepository : CoachKeyRepository {
    var calls = 0
        private set

    override fun save(coachId: String, keyPair: Ed25519KeyPair) {
        calls += 1
        error("the student path must not save coach keys")
    }

    override fun find(coachId: String): Ed25519KeyPair? {
        calls += 1
        error("the student path must not look up coach keys")
    }
}
