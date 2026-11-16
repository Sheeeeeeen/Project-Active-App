package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026
import ph.project.active.testkit.idle
import ph.project.active.testkit.runVm

class P7JoinOutcomeNavigationTest {
    @Test
    fun P7_joinResultMapsToOutcomeAndNavigationAndANormalRequestLeavesBothNull() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val cancelled = world.createMonWedEveningClass(coach.id, name = "Cancelled", endDate = SAT_31_OCT_2026)
            world.cancelClass(cancelled.id)
            val cancelledVm = filled(world, cancelled.id)
            cancelledVm.onSubmit()
            idle()
            assertEquals(JoinOutcome.ClassCancelled, cancelledVm.state.value.joinOutcome)
            val cancelledNav = cancelledVm.state.value.navigation as Navigation.OpenSignedInRoot
            assertEquals(JoinOutcome.ClassCancelled, cancelledNav.joinOutcome)

            val privateClass = world.createMonWedEveningClass(
                coach.id,
                name = "Private",
                visibility = Visibility.PRIVATE,
                endDate = SAT_31_OCT_2026,
            )
            val privateVm = filled(world, privateClass.id)
            privateVm.onSubmit()
            idle()
            assertEquals(JoinOutcome.ClassUnavailable, privateVm.state.value.joinOutcome)
            val privateNav = privateVm.state.value.navigation as Navigation.OpenSignedInRoot
            assertEquals(JoinOutcome.ClassUnavailable, privateNav.joinOutcome)

            val missingVm = filled(world, "missing-class")
            missingVm.onSubmit()
            idle()
            assertEquals(JoinOutcome.ClassUnavailable, missingVm.state.value.joinOutcome)
            val missingNav = missingVm.state.value.navigation as Navigation.OpenSignedInRoot
            assertEquals(JoinOutcome.ClassUnavailable, missingNav.joinOutcome)

            val open = world.createMonWedEveningClass(coach.id, name = "TRX Core Burn", endDate = SAT_31_OCT_2026)
            val openVm = filled(world, open.id)
            openVm.onSubmit()
            idle()
            assertNull(openVm.state.value.joinOutcome)
            val openNav = openVm.state.value.navigation as Navigation.OpenSignedInRoot
            assertNull(openNav.joinOutcome)
            assertEquals(open.id, openNav.openClassId)
        }
    }
}

private fun filled(world: Phase2World, classId: String) =
    world.registration(RegistrationOrigin.FromRequestToJoin(classId)).also { viewModel ->
        viewModel.onNameChange("Ella Navarro")
        viewModel.onEmailChange("ella-${classId}@acme.test")
        viewModel.onPasswordChange("password1")
        viewModel.onEmployeeIdChange("EMP-${classId.hashCode()}")
    }
