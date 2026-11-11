package ph.project.active.data.fake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import ph.project.active.data.RegisterResult
import ph.project.active.data.SignInResult
import ph.project.active.domain.NetworkStatus
import ph.project.active.domain.Role

class FakeAuthRepositoryTest {

    @Test
    fun AC36_duplicate_email_or_employee_id_creates_nobody() = runTest {
        authFixture().use { fixture ->
            assertIs<RegisterResult.Success>(fixture.auth.registerStudent("Ada", "ada@trx.test", "password1", "EMP-1"))
            assertEquals(RegisterResult.EmailTaken, fixture.auth.registerStudent("Other", "ADA@trx.test", "password2", "EMP-2"))
            assertEquals(RegisterResult.EmployeeIdTaken, fixture.auth.registerStudent("Ann", "ann@trx.test", "password2", "EMP-1"))
            assertEquals(1, fixture.users.all().size)
            assertEquals("Ada", fixture.users.all().single().name)
        }
    }

    @Test
    fun AC36_a_new_account_can_sign_in_and_secrets_stay_out_of_sqlite() = runTest {
        authFixture().use { fixture ->
            val account = assertIs<RegisterResult.Success>(
                fixture.auth.registerStudent("Ada", " ada@trx.test ", "password1", " EMP-1 "),
            ).account
            assertEquals("Ada", account.name)
            assertEquals("ada@trx.test", account.email)
            assertEquals("EMP-1", account.employeeId)
            assertEquals(account, assertIs<SignInResult.Success>(fixture.auth.signIn(" ada@trx.test ", "password1")).account)
            assertEquals(SignInResult.Failed, fixture.auth.signIn("missing@trx.test", "password1"))
            assertEquals(SignInResult.Failed, fixture.auth.signIn("ada@trx.test", "wrong-password"))
            assertEquals(account, fixture.session.account.value)
            assertEquals(account.name, fixture.people.find(account.id)!!.name)
            assertEquals(Role.STUDENT, fixture.people.find(account.id)!!.role)

            val coach = assertIs<RegisterResult.Success>(
                fixture.auth.registerCoach("Bea", "bea@trx.test", "coach-password"),
            ).account
            assertNull(coach.employeeId)
            assertEquals(coach, assertIs<SignInResult.Success>(fixture.auth.signIn("bea@trx.test", "coach-password")).account)

            val dump = fixture.db.dump()
            assertTrue("password1" !in dump)
            assertTrue("coach-password" !in dump)
            assertTrue("hash:" !in dump)
        }
    }

    @Test
    fun AC36_offline_register_and_sign_in_store_nothing() = runTest {
        authFixture(NetworkStatus.OFFLINE).use { fixture ->
            assertEquals(RegisterResult.Offline, fixture.auth.registerStudent("Ada", "ada@trx.test", "password1", "EMP-1"))
            assertEquals(RegisterResult.Offline, fixture.auth.registerCoach("Bea", "bea@trx.test", "coach-password"))
            assertEquals(SignInResult.Offline, fixture.auth.signIn("ada@trx.test", "password1"))
            assertTrue(fixture.users.all().isEmpty())
            assertNull(fixture.session.account.value)
        }
    }
}

private inline fun <R> AuthFixture.use(block: (AuthFixture) -> R): R = try {
    block(this)
} finally {
    close()
}
