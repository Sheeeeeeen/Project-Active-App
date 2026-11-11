package ph.project.active.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.AuthFailed
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.User

class RegisterAndSignInTest {

    private fun registerStudent(
        world: TestWorld,
        name: String = "Ana Reyes",
        email: String = "ana@trx.test",
        password: String = "pw-ana-123",
        employeeId: String = "EMP-100",
    ): User = world.registerStudent(name = name, email = email, password = password, employeeId = employeeId)

    // ----- AC1 -----

    @Test
    fun AC1_register_student_creates_student_who_can_sign_in_immediately() {
        val world = TestWorld()

        val created = registerStudent(world)

        assertEquals(Role.STUDENT, created.role)
        assertEquals("Ana Reyes", created.name)
        assertEquals("ana@trx.test", created.email)
        assertEquals("EMP-100", created.employeeId)
        assertEquals(listOf(created), world.users.all())

        val signedIn = world.signIn(email = "ana@trx.test", password = "pw-ana-123")
        assertEquals(created, signedIn)
    }

    // ----- AC2 -----

    @Test
    fun AC2_blank_name_throws_and_creates_no_user() {
        val world = TestWorld()
        assertFailsWith<IllegalArgumentException> { registerStudent(world, name = "   ") }
        assertTrue(world.users.all().isEmpty())
    }

    @Test
    fun AC2_blank_email_throws_and_creates_no_user() {
        val world = TestWorld()
        assertFailsWith<IllegalArgumentException> { registerStudent(world, email = "") }
        assertTrue(world.users.all().isEmpty())
    }

    @Test
    fun AC2_blank_password_throws_and_creates_no_user() {
        val world = TestWorld()
        assertFailsWith<IllegalArgumentException> { registerStudent(world, password = " ") }
        assertTrue(world.users.all().isEmpty())
    }

    @Test
    fun AC2_blank_employee_id_throws_and_creates_no_user() {
        val world = TestWorld()
        assertFailsWith<IllegalArgumentException> { registerStudent(world, employeeId = "\t ") }
        assertTrue(world.users.all().isEmpty())
    }

    @Test
    fun AC2_blank_coach_fields_throw_and_create_no_user() {
        val world = TestWorld()
        assertFailsWith<IllegalArgumentException> { world.registerCoach(name = "", email = "c@trx.test", password = "pw-c-1") }
        assertFailsWith<IllegalArgumentException> { world.registerCoach(name = "Coach", email = " ", password = "pw-c-1") }
        assertFailsWith<IllegalArgumentException> { world.registerCoach(name = "Coach", email = "c@trx.test", password = "") }
        assertTrue(world.users.all().isEmpty())
    }

    @Test
    fun AC2_email_and_employee_id_are_trimmed() {
        val world = TestWorld()

        val created = registerStudent(world, email = "  ana@trx.test \n", employeeId = " EMP-100  ")

        assertEquals("ana@trx.test", created.email)
        assertEquals("EMP-100", created.employeeId)
        assertEquals(created, world.signIn(email = "ana@trx.test", password = "pw-ana-123"))
    }

    @Test
    fun AC2_password_is_stored_only_as_a_hash() {
        val world = TestWorld()
        val password = "pw-ana-123"

        val created = registerStudent(world, password = password)

        assertEquals(world.hasher.hash(password), created.passwordHash)
        assertNotEquals(password, created.passwordHash)
        val stored = world.users.all().single()
        assertFalse(stored.toString().contains(password), "plain-text password leaked into the domain model")
        assertFalse(stored.passwordHash.contains(password))
    }

    @Test
    fun AC2_coach_password_is_stored_only_as_a_hash() {
        val world = TestWorld()

        val coach = world.registerCoach(name = "Coach", email = "coach@trx.test", password = "pw-coach-9")

        assertEquals(world.hasher.hash("pw-coach-9"), coach.passwordHash)
        assertFalse(coach.toString().contains("pw-coach-9"))
    }

    // ----- AC3 -----

    @Test
    fun AC3_duplicate_email_throws_and_creates_no_user() {
        val world = TestWorld()
        val first = registerStudent(world, email = "ana@trx.test", employeeId = "EMP-100")

        assertFailsWith<IllegalArgumentException> {
            registerStudent(world, name = "Other", email = " ana@trx.test ", employeeId = "EMP-200")
        }
        assertEquals(listOf(first), world.users.all())
    }

    @Test
    fun AC3_duplicate_employee_id_throws_and_creates_no_user() {
        val world = TestWorld()
        val first = registerStudent(world, email = "ana@trx.test", employeeId = "EMP-100")

        assertFailsWith<IllegalArgumentException> {
            registerStudent(world, name = "Other", email = "other@trx.test", employeeId = "  EMP-100 ")
        }
        assertEquals(listOf(first), world.users.all())
    }

    @Test
    fun AC3_duplicate_email_across_student_and_coach_throws_and_creates_no_user() {
        val world = TestWorld()
        val coach = world.registerCoach(name = "Coach", email = "shared@trx.test", password = "pw-coach-1")

        assertFailsWith<IllegalArgumentException> { registerStudent(world, email = "shared@trx.test") }
        assertEquals(listOf(coach), world.users.all())
    }

    // ----- AC4 -----

    @Test
    fun AC4_register_coach_creates_coach_without_employee_id() {
        val world = TestWorld()

        val coach = world.registerCoach(name = "Coach Ben", email = " ben@trx.test ", password = "pw-ben-1")

        assertEquals(Role.COACH, coach.role)
        assertEquals("Coach Ben", coach.name)
        assertEquals("ben@trx.test", coach.email)
        assertNull(coach.employeeId)
        assertEquals(listOf(coach), world.users.all())
        assertEquals(coach, world.signIn(email = "ben@trx.test", password = "pw-ben-1"))
    }

    // A coach cannot be registered with an employee ID: RegisterCoach takes only (name, email, password),
    // so passing one does not compile. Here we also check every stored coach has a null employee ID.
    @Test
    fun AC4_coaches_never_have_an_employee_id() {
        val world = TestWorld()
        world.coach(1)
        world.coach(2)
        world.student(1)

        val coaches = world.users.all().filter { it.role == Role.COACH }
        assertEquals(2, coaches.size)
        assertTrue(coaches.all { it.employeeId == null })
    }

    // ----- AC5 -----

    @Test
    fun AC5_sign_in_returns_user_on_match() {
        val world = TestWorld()
        val student = world.student(1)
        val coach = world.coach(1)

        assertEquals(student, world.signIn(email = "student1@trx.test", password = "pw-student-1"))
        assertEquals(coach, world.signIn(email = "coach1@trx.test", password = "pw-coach-1"))
    }

    @Test
    fun AC5_sign_in_with_wrong_password_throws_AuthFailed() {
        val world = TestWorld()
        world.student(1)

        assertFailsWith<AuthFailed> { world.signIn(email = "student1@trx.test", password = "wrong-password") }
        assertFailsWith<AuthFailed> { world.signIn(email = "student1@trx.test", password = "pw-student-2") }
    }

    @Test
    fun AC5_sign_in_with_unknown_email_throws_AuthFailed() {
        val world = TestWorld()
        world.student(1)

        assertFailsWith<AuthFailed> { world.signIn(email = "nobody@trx.test", password = "pw-student-1") }
    }

    // ----- AC6 -----

    @Test
    fun AC6_registering_the_same_email_as_a_coach_does_not_change_the_student_role() {
        val world = TestWorld()
        val student = world.student(1)

        assertFailsWith<IllegalArgumentException> {
            world.registerCoach(name = "Student 1", email = "student1@trx.test", password = "pw-student-1")
        }

        assertEquals(Role.STUDENT, world.users.all().single().role)
        assertEquals(Role.STUDENT, world.signIn(email = "student1@trx.test", password = "pw-student-1").role)
        assertEquals(student, world.users.all().single())
    }

    @Test
    fun AC6_registering_the_same_email_as_a_student_does_not_change_the_coach_role() {
        val world = TestWorld()
        world.coach(1)

        assertFailsWith<IllegalArgumentException> {
            world.registerStudent(name = "Coach 1", email = "coach1@trx.test", password = "pw-coach-1", employeeId = "EMP-9")
        }

        val signedIn = world.signIn(email = "coach1@trx.test", password = "pw-coach-1")
        assertEquals(Role.COACH, signedIn.role)
        assertNull(signedIn.employeeId)
    }

    @Test
    fun AC6_role_is_stable_across_sign_ins_and_later_registrations() {
        val world = TestWorld()
        val student = world.student(1)
        val coach = world.coach(1)
        world.student(2)
        world.coach(2)

        assertEquals(Role.STUDENT, world.signIn(email = "student1@trx.test", password = "pw-student-1").role)
        assertEquals(Role.COACH, world.signIn(email = "coach1@trx.test", password = "pw-coach-1").role)
        assertEquals(Role.STUDENT, world.users.all().single { it.id == student.id }.role)
        assertEquals(Role.COACH, world.users.all().single { it.id == coach.id }.role)
    }
}
