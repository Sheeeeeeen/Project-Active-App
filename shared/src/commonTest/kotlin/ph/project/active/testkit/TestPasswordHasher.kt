package ph.project.active.testkit

import ph.project.active.domain.PasswordHasher

/** Hash is the reversed password, so the plain text is not a substring of the hash. */
class TestPasswordHasher : PasswordHasher {
    override fun hash(password: String): String = "hash:" + password.reversed()

    override fun verify(password: String, hash: String): Boolean = hash == hash(password)
}
