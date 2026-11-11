package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.testkit.idle
import ph.project.active.data.RegisterResult
import ph.project.active.data.SignInResult
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.JoinResult
import ph.project.active.domain.MembershipStatus
import ph.project.active.domain.Role
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.ScriptedAuthRepository
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount

class RegistrationViewModelTest {

    @Test
    fun AC26_starts_as_a_student_form_and_request_to_join_shows_the_class() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id, name = "Morning TRX")
            val fromList = world.registration(RegistrationOrigin.FromClassList)
            assertEquals(RegistrationOrigin.FromClassList, fromList.state.value.origin)
            assertEquals(Role.STUDENT, fromList.state.value.mode)
            assertTrue(fromList.state.value.showEmployeeId)
            assertEquals("", fromList.state.value.name)
            assertEquals("", fromList.state.value.email)
            assertEquals("", fromList.state.value.password)
            assertEquals("", fromList.state.value.employeeId)
            assertTrue(fromList.state.value.fieldErrors.isEmpty())
            assertFalse(fromList.state.value.isSubmitting)
            assertNull(fromList.state.value.requestingClass)
            assertNull(fromList.state.value.signInError)
            assertNull(fromList.state.value.completion)

            val fromRequest = world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id))
            val header = fromRequest.state.value.requestingClass
            assertEquals("Morning TRX", header!!.name)
            assertEquals("Coach 1", header.coachName)
            assertEquals(trxClass.slotsByWeekday, header.slotsByWeekday)
        }
    }

    @Test
    fun AC27_shake_toggles_role_and_keeps_typed_fields() = runVm {
        Phase2World().use { world ->
            val viewModel = world.registration(RegistrationOrigin.FromClassList)
            viewModel.onNameChange("Ada")
            viewModel.onEmailChange("ada@trx.test")
            viewModel.onPasswordChange("password1")
            viewModel.onEmployeeIdChange("EMP-9")
            viewModel.onShake()
            assertEquals(Role.COACH, viewModel.state.value.mode)
            assertFalse(viewModel.state.value.showEmployeeId)
            assertEquals("Ada", viewModel.state.value.name)
            assertEquals("ada@trx.test", viewModel.state.value.email)
            assertEquals("password1", viewModel.state.value.password)
            viewModel.onShake()
            assertEquals(Role.STUDENT, viewModel.state.value.mode)
            assertTrue(viewModel.state.value.showEmployeeId)
            assertEquals("EMP-9", viewModel.state.value.employeeId)
        }
    }

    @Test
    fun AC28_request_to_join_ignores_shake_and_has_no_set_mode() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val viewModel = world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id))
            viewModel.onShake()
            assertEquals(Role.STUDENT, viewModel.state.value.mode)
            assertTrue(viewModel.state.value.showEmployeeId)
            assertTrue(RegistrationViewModel::class.java.methods.none { it.name == "setMode" })
        }
    }

    @Test
    fun AC29_submit_validates_before_any_repository_call_and_editing_clears_that_field() = runVm {
        Phase2World().use { world ->
            val auth = ScriptedAuthRepository()
            val viewModel = world.registration(RegistrationOrigin.FromClassList, auth)
            viewModel.onSubmit()
            idle()
            assertEquals(FieldError.Required, viewModel.state.value.fieldErrors[Field.NAME])
            assertEquals(FieldError.Required, viewModel.state.value.fieldErrors[Field.EMAIL])
            assertEquals(FieldError.Required, viewModel.state.value.fieldErrors[Field.PASSWORD])
            assertEquals(FieldError.Required, viewModel.state.value.fieldErrors[Field.EMPLOYEE_ID])
            assertTrue(auth.studentCalls.isEmpty())

            viewModel.onNameChange("Ada")
            assertNull(viewModel.state.value.fieldErrors[Field.NAME])
            viewModel.onEmailChange("not-an-email")
            viewModel.onPasswordChange("short")
            viewModel.onEmployeeIdChange("EMP-1")
            viewModel.onSubmit()
            idle()
            assertEquals(FieldError.InvalidEmail, viewModel.state.value.fieldErrors[Field.EMAIL])
            assertEquals(FieldError.PasswordTooShort, viewModel.state.value.fieldErrors[Field.PASSWORD])
            assertTrue(auth.studentCalls.isEmpty())

            viewModel.onEmailChange("  a@b.c  ")
            viewModel.onPasswordChange("password1")
            viewModel.onSubmit()
            idle()
            assertEquals("Ada", auth.studentCalls.single().name)
            assertEquals("a@b.c", auth.studentCalls.single().email)
        }
    }

    @Test
    fun AC30_submit_trims_fields_and_ignores_a_second_submit_while_the_first_is_in_flight() = runVm {
        Phase2World().use { world ->
            val auth = ScriptedAuthRepository().apply { holdStudents = true }
            val viewModel = world.registration(RegistrationOrigin.FromClassList, auth)
            viewModel.onNameChange("  Ada  ")
            viewModel.onEmailChange(" ada@trx.test ")
            viewModel.onPasswordChange("password1")
            viewModel.onEmployeeIdChange(" EMP-9 ")
            viewModel.onSubmit()
            assertTrue(viewModel.state.value.isSubmitting)
            assertEquals(1, auth.studentCalls.size)
            assertEquals("Ada", auth.studentCalls.single().name)
            assertEquals("ada@trx.test", auth.studentCalls.single().email)
            assertEquals("EMP-9", auth.studentCalls.single().employeeId)
            viewModel.onSubmit()
            assertEquals(1, auth.studentCalls.size)

            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")
            auth.releaseStudents(RegisterResult.Success(account))
            idle()
            assertFalse(viewModel.state.value.isSubmitting)
            assertEquals(1, auth.studentCalls.size)
            assertEquals(account, world.sessionStore.account.value)
        }
    }

    @Test
    fun AC30_coach_submit_does_not_send_an_employee_id() = runVm {
        Phase2World().use { world ->
            val auth = ScriptedAuthRepository()
            auth.coachResult = RegisterResult.Success(SignedInAccount("c", "Ada", "ada@trx.test", Role.COACH, null))
            val viewModel = world.registration(RegistrationOrigin.FromClassList, auth)
            viewModel.onNameChange("Ada")
            viewModel.onEmailChange("ada@trx.test")
            viewModel.onPasswordChange("password1")
            viewModel.onEmployeeIdChange("EMP-9")
            viewModel.onShake()
            viewModel.onSubmit()
            idle()
            assertTrue(auth.studentCalls.isEmpty())
            assertEquals(1, auth.coachCalls.size)
            assertNull(world.users.findByEmployeeId("EMP-9"))
        }
    }

    @Test
    fun AC31_taken_email_or_employee_id_sets_a_field_error_and_saves_no_session() = runVm {
        Phase2World().use { world ->
            val auth = ScriptedAuthRepository()
            val viewModel = filled(world.registration(RegistrationOrigin.FromClassList, auth))
            auth.studentResult = RegisterResult.EmailTaken
            viewModel.onSubmit()
            idle()
            assertEquals(FieldError.EmailTaken, viewModel.state.value.fieldErrors[Field.EMAIL])
            assertNull(world.sessionStore.account.value)

            auth.studentResult = RegisterResult.EmployeeIdTaken
            viewModel.onSubmit()
            idle()
            assertEquals(FieldError.EmployeeIdTaken, viewModel.state.value.fieldErrors[Field.EMPLOYEE_ID])
            assertNull(world.sessionStore.account.value)
            assertTrue(world.users.all().isEmpty())
        }
    }

    @Test
    fun AC32_success_saves_the_session_and_completion_without_a_class() = runVm {
        Phase2World().use { world ->
            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")
            val auth = ScriptedAuthRepository().apply { studentResult = RegisterResult.Success(account) }
            val viewModel = filled(world.registration(RegistrationOrigin.FromClassList, auth))
            viewModel.onSubmit()
            idle()
            assertEquals(account, world.sessionStore.account.value)
            assertEquals(AuthCompletion(Role.STUDENT, null), viewModel.state.value.completion)
        }
    }

    @Test
    fun AC33_request_to_join_registration_sends_one_request_or_reports_class_cancelled() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")
            val auth = ScriptedAuthRepository().apply { studentResult = RegisterResult.Success(account) }
            val viewModel = filled(world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth))
            viewModel.onSubmit()
            idle()
            assertEquals(listOf(MembershipStatus.PENDING_REQUEST), world.membershipsOf(trxClass.id, account.id).map { it.status })
            assertEquals(AuthCompletion(Role.STUDENT, trxClass.id), viewModel.state.value.completion)
            val detail = world.classDetail(trxClass.id, account)
            assertEquals(RequestAction.Pending, detail.state.value.requestAction)

            world.cancelClass(trxClass.id)
            val cancelledAccount = account.copy(id = "s2", email = "ada2@trx.test", employeeId = "EMP-10")
            auth.studentResult = RegisterResult.Success(cancelledAccount)
            val again = filled(world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth))
            val before = world.memberships.all()
            again.onSubmit()
            idle()
            assertEquals(cancelledAccount, world.sessionStore.account.value)
            assertEquals(before, world.memberships.all())
            assertEquals(JoinOutcome.ClassCancelled, again.state.value.joinOutcome)
            assertEquals(JoinResult.ClassCancelled, world.requestToJoin(trxClass.id, cancelledAccount.id))
        }
    }

    @Test
    fun AC34_sign_in_uses_its_own_fields_and_opens_the_account_role() = runVm {
        Phase2World().use { world ->
            val auth = ScriptedAuthRepository()
            val viewModel = world.registration(RegistrationOrigin.FromClassList, auth)
            viewModel.onShake()
            viewModel.onSignIn()
            idle()
            assertEquals(FieldError.Required, viewModel.state.value.fieldErrors[Field.SIGN_IN_EMAIL])
            assertEquals(FieldError.Required, viewModel.state.value.fieldErrors[Field.SIGN_IN_PASSWORD])
            assertTrue(auth.signInCalls.isEmpty())

            viewModel.onSignInEmailChange("ada@trx.test")
            viewModel.onSignInPasswordChange("password1")
            auth.signInResult = SignInResult.Failed
            viewModel.onSignIn()
            idle()
            assertEquals(UiError.AuthFailed, viewModel.state.value.signInError)
            assertNull(world.sessionStore.account.value)

            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-1")
            auth.signInResult = SignInResult.Success(account)
            viewModel.onSignIn()
            idle()
            assertEquals(account, world.sessionStore.account.value)
            assertEquals(AuthCompletion(Role.STUDENT, null), viewModel.state.value.completion)
            assertEquals(Root.Student(account), world.root().state.value.root)
        }
    }

    @Test
    fun AC35_sign_in_from_a_class_requests_only_for_a_student() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val student = world.student()
            world.requestToJoin(trxClass.id, student.id)
            val pending = world.memberships.all()
            val auth = ScriptedAuthRepository()
            auth.signInResult = SignInResult.Success(student.toAccount())
            val viewModel = world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth)
            viewModel.onSignInEmailChange(student.email)
            viewModel.onSignInPasswordChange("password1")
            viewModel.onSignIn()
            idle()
            assertEquals(pending, world.memberships.all())
            assertEquals(AuthCompletion(Role.STUDENT, trxClass.id), viewModel.state.value.completion)

            val active = world.student(2)
            world.makeActive(trxClass.id, active.id)
            val afterActive = world.memberships.all()
            auth.signInResult = SignInResult.Success(active.toAccount())
            val activeSignIn = world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth)
            activeSignIn.onSignInEmailChange(active.email)
            activeSignIn.onSignInPasswordChange("password1")
            activeSignIn.onSignIn()
            idle()
            assertEquals(afterActive, world.memberships.all())

            val coach = world.coachAccount(2)
            auth.signInResult = SignInResult.Success(coach)
            val coachSignIn = world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth)
            coachSignIn.onSignInEmailChange(coach.email)
            coachSignIn.onSignInPasswordChange("password1")
            val memberships = world.memberships.all()
            coachSignIn.onSignIn()
            idle()
            assertEquals(memberships, world.memberships.all())
            assertEquals(AuthCompletion(Role.COACH, null), coachSignIn.state.value.completion)
        }
    }
}

private fun filled(viewModel: RegistrationViewModel): RegistrationViewModel {
    viewModel.onNameChange("Ada")
    viewModel.onEmailChange("ada@trx.test")
    viewModel.onPasswordChange("password1")
    viewModel.onEmployeeIdChange("EMP-9")
    return viewModel
}
