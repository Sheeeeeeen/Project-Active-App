package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.DailyCodeCheck
import ph.project.active.domain.Ed25519KeyPair
import ph.project.active.domain.GenerateDailyCode
import ph.project.active.domain.VerifyDailyCode
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.FakeClock
import ph.project.active.testkit.PHONE_TZ

class SqlCoachKeyRepositoryTest {

    @Test
    fun AC13_private_key_stays_in_the_secret_store_and_codes_verify() {
        inMemoryDb().use { db ->
            val secrets = InMemorySecretKeyStore()
            val repository = SqlCoachKeyRepository(db.database, secrets)
            val pair = Ed25519KeyPair.generate()
            repository.save("coach-1", pair)

            val storedPrivate = secrets.privateKey("coach-1")
            assertNotNull(storedPrivate)
            assertFalse(db.dump().contains(storedPrivate.toHex()))
            val found = repository.find("coach-1")
            assertNotNull(found)
            assertTrue(found.publicKey.contentEquals(pair.publicKey))
            val message = "trx-daily".encodeToByteArray()
            assertTrue(pair.verify(message, found.sign(message)))

            val clock = FakeClock(FRI_25_SEP_2026.atTime(9, 0).toInstant(PHONE_TZ))
            val code = GenerateDailyCode(repository)("coach-1", FRI_25_SEP_2026)
            val verdict = VerifyDailyCode(repository, clock, PHONE_TZ).check(code, "coach-1")
            assertEquals(DailyCodeCheck.VALID, verdict)
        }
    }

    @Test
    fun AC14_public_key_alone_verifies_and_neither_key_returns_null() {
        inMemoryDb().use { db ->
            val coachSecrets = InMemorySecretKeyStore()
            val coachKeys = SqlCoachKeyRepository(db.database, coachSecrets)
            val pair = Ed25519KeyPair.generate()
            coachKeys.save("coach-1", pair)
            val studentSecrets = InMemorySecretKeyStore()
            val studentKeys = SqlCoachKeyRepository(db.database, studentSecrets)

            assertNull(studentSecrets.privateKey("coach-1"))
            val verifyOnly = studentKeys.find("coach-1")
            assertNotNull(verifyOnly)
            val clock = FakeClock(FRI_25_SEP_2026.atTime(9, 0).toInstant(PHONE_TZ))
            val code = GenerateDailyCode(coachKeys)("coach-1", LocalDate(2026, 9, 25))
            assertEquals(DailyCodeCheck.VALID, VerifyDailyCode(studentKeys, clock, PHONE_TZ).check(code, "coach-1"))

        }
        inMemoryDb().use { emptyDb ->
            val repository = SqlCoachKeyRepository(emptyDb.database, InMemorySecretKeyStore())
            assertNull(repository.find("coach-1"))
        }
    }

    @Test
    fun AC15_a_new_pair_replaces_both_keys_and_restart_keeps_todays_code() {
        val path = tempDbPath()
        val secrets = InMemorySecretKeyStore()
        val clock = FakeClock(FRI_25_SEP_2026.atTime(9, 0).toInstant(PHONE_TZ))
        val oldCode = fileDb(path).use { db ->
            val repository = SqlCoachKeyRepository(db.database, secrets)
            repository.save("coach-1", Ed25519KeyPair.generate())
            val first = GenerateDailyCode(repository)("coach-1", FRI_25_SEP_2026)
            repository.save("coach-1", Ed25519KeyPair.generate())
            val verify = VerifyDailyCode(repository, clock, PHONE_TZ)
            assertFalse(verify(first, "coach-1"))
            val second = GenerateDailyCode(repository)("coach-1", FRI_25_SEP_2026)
            assertTrue(verify(second, "coach-1"))
            second
        }
        fileDb(path).use { db ->
            val repository = SqlCoachKeyRepository(db.database, secrets)
            val verify = VerifyDailyCode(repository, clock, PHONE_TZ)
            assertEquals(DailyCodeCheck.VALID, verify.check(oldCode, "coach-1"))
        }
    }
}

private fun ByteArray.toHex(): String = joinToString(separator = "") { byte ->
    (byte.toInt() and 0xff).toString(16).padStart(2, '0')
}

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
