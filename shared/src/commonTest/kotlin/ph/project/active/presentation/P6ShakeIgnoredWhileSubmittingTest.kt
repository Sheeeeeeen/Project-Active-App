package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.data.model.RegisterResult
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.ScriptedAuthRepository
import ph.project.active.testkit.idle
import ph.project.active.testkit.runVm

class P6ShakeIgnoredWhileSubmittingTest {
    @Test
    fun P6_onShakeDoesNothingWhileSubmitting() = runVm {
        Phase2World().use { world ->
            val auth = ScriptedAuthRepository().apply { holdStudents = true }
            val viewModel = world.registration(RegistrationOrigin.FromClassList, auth)
            viewModel.onNameChange("Ada")
            viewModel.onEmailChange("ada@trx.test")
            viewModel.onPasswordChange("password1")
            viewModel.onEmployeeIdChange("EMP-9")
            viewModel.onSubmit()
            assertTrue(viewModel.state.value.isSubmitting)
            val held = viewModel.state.value
            viewModel.onShake()
            assertEquals(Role.STUDENT, viewModel.state.value.mode)
            assertEquals(held.showEmployeeId, viewModel.state.value.showEmployeeId)
            assertEquals(held.name, viewModel.state.value.name)
            assertEquals(held.email, viewModel.state.value.email)
            assertEquals(held.password, viewModel.state.value.password)
            assertEquals(held.employeeId, viewModel.state.value.employeeId)

            auth.releaseStudents(RegisterResult.Success(SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")))
            idle()
            assertFalse(viewModel.state.value.isSubmitting)
            viewModel.onShake()
            assertEquals(Role.COACH, viewModel.state.value.mode)
            assertFalse(viewModel.state.value.showEmployeeId)
            assertEquals("Ada", viewModel.state.value.name)
            assertEquals("ada@trx.test", viewModel.state.value.email)
            assertEquals("password1", viewModel.state.value.password)
            assertEquals("EMP-9", viewModel.state.value.employeeId)
        }
    }
}
