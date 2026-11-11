package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertFailsWith
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

class RoleGuardTest {

    @Test
    fun AC25_student_and_coach_view_models_reject_the_other_role_and_an_unowned_class() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount(1)
            val otherCoach = world.coachAccount(2)
            val student = world.studentAccount()
            val trxClass = world.createMonWedEveningClass(coach.id)

            assertFailsWith<IllegalArgumentException> { world.studentClasses(coach) }
            assertFailsWith<IllegalArgumentException> { world.attend(coach) }
            assertFailsWith<IllegalArgumentException> { world.studentSessionsVm(coach) }
            assertFailsWith<IllegalArgumentException> { world.invites(coach) }

            assertFailsWith<IllegalArgumentException> { world.coachClasses(student) }
            assertFailsWith<IllegalArgumentException> { world.createClassVm(student) }
            assertFailsWith<IllegalArgumentException> { world.manage(student, trxClass.id) }
            assertFailsWith<IllegalArgumentException> { world.dailyQr(student) }
            assertFailsWith<IllegalArgumentException> { world.coachAttendance(student) }

            assertFailsWith<IllegalArgumentException> { world.manage(otherCoach, trxClass.id) }
            assertFailsWith<IllegalArgumentException> { world.manage(coach, "missing-class") }
            world.studentClasses(student)
            world.manage(coach, trxClass.id)
        }
    }
}
