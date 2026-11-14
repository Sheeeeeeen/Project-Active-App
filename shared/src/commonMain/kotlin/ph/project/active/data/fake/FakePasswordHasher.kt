package ph.project.active.data.fake

import ph.project.active.domain.crypto.PasswordHasher

/**
 * Deterministic stand-in until Phase 5, when Firebase Auth owns passwords.
 * Not a password hash. Do not use it outside the Phase 2 fakes.
 */
class FakePasswordHasher : PasswordHasher {
    override fun hash(password: String): String = "fake:" + password.reversed()

    override fun verify(password: String, hash: String): Boolean = hash(password) == hash
}
