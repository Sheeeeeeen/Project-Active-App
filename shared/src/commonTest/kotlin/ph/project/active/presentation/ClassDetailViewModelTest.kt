package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.testkit.idle
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount
import ph.project.active.presentation.classdetail.ClassDetailUiState
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.manageclass.RequestAction
import ph.project.active.presentation.common.UiError

class ClassDetailViewModelTest {

    @Test
    fun AC39_shows_class_content_without_roster_attendance_or_qr_and_hides_private_classes() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val publicClass = world.createMonWedEveningClass(coach.id, name = "Public")
            val privateClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Private")
            val active = world.student(1)
            val invited = world.student(2)
            world.makeActive(privateClass.id, active.id)
            world.inviteByEmployeeId(privateClass.id, invited.employeeId!!)

            val guest = world.classDetail(publicClass.id, null)
            assertEquals("Public", guest.state.value.content!!.name)
            assertEquals(Visibility.PUBLIC, guest.state.value.content!!.visibility)
            assertEquals("Coach 1", guest.state.value.content!!.coachName)
            assertEquals(publicClass.slotsByWeekday, guest.state.value.content!!.slotsByWeekday)
            assertEquals(publicClass.endDate, guest.state.value.content!!.endDate)
            assertFalse(guest.state.value.unavailable)
            val names = ClassDetailUiState::class.java.declaredFields.map { it.name.lowercase() }
            assertTrue(names.none { "roster" in it || "attendance" in it || "qr" in it })

            assertTrue(world.classDetail("missing", null).state.value.unavailable)
            assertNull(world.classDetail("missing", null).state.value.content)
            assertTrue(world.classDetail(privateClass.id, null).state.value.unavailable)
            assertTrue(world.classDetail(privateClass.id, invited.toAccount()).state.value.unavailable)
            assertFalse(world.classDetail(privateClass.id, coach.toAccount()).state.value.unavailable)
            assertFalse(world.classDetail(privateClass.id, active.toAccount()).state.value.unavailable)
        }
    }

    @Test
    fun AC40_a_guest_request_opens_registration_and_creates_no_membership() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val viewModel = world.classDetail(trxClass.id, null)
            assertEquals(RequestAction.RequestToJoin(opensRegistration = true), viewModel.state.value.requestAction)
            viewModel.onRequestToJoin()
            assertEquals(
                Navigation.OpenRegistration(RegistrationOrigin.FromRequestToJoin(trxClass.id)),
                viewModel.state.value.navigation,
            )
            assertTrue(world.memberships.all().isEmpty())
        }
    }

    @Test
    fun AC41_request_action_follows_the_student_membership_including_invited() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val student = world.student().toAccount()
            assertEquals(RequestAction.RequestToJoin(opensRegistration = false), world.classDetail(trxClass.id, student).state.value.requestAction)
            assertFalse(world.classDetail(trxClass.id, student).state.value.requestAgain)

            world.requestToJoin(trxClass.id, student.id)
            world.declineRequest(trxClass.id, student.id)
            val declined = world.classDetail(trxClass.id, student)
            assertEquals(RequestAction.RequestToJoin(opensRegistration = false), declined.state.value.requestAction)
            assertTrue(declined.state.value.requestAgain)

            world.requestToJoin(trxClass.id, student.id)
            world.approveRequest(trxClass.id, student.id)
            world.removeFromRoster(trxClass.id, student.id)
            assertTrue(world.classDetail(trxClass.id, student).state.value.requestAgain)

            world.requestToJoin(trxClass.id, student.id)
            assertEquals(RequestAction.Pending, world.classDetail(trxClass.id, student).state.value.requestAction)
            world.approveRequest(trxClass.id, student.id)
            assertEquals(RequestAction.Joined, world.classDetail(trxClass.id, student).state.value.requestAction)

            val invited = world.student(2)
            world.inviteByEmployeeId(trxClass.id, invited.employeeId!!)
            val invitedDetail = world.classDetail(trxClass.id, invited.toAccount())
            assertEquals(RequestAction.Invited, invitedDetail.state.value.requestAction)
            assertFalse(invitedDetail.state.value.requestAgain)
            assertEquals(MembershipStatus.INVITED, world.statusOf(trxClass.id, invited.id))
        }
    }

    @Test
    fun AC42_request_to_join_is_single_and_refresh_shows_joined_or_request_again() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student().toAccount()
            val viewModel = world.classDetail(trxClass.id, student)
            viewModel.onRequestToJoin()
            viewModel.onRequestToJoin()
            idle()
            assertEquals(1, world.membershipsOf(trxClass.id, student.id).size)
            assertEquals(RequestAction.Pending, viewModel.state.value.requestAction)

            world.approveRequest(trxClass.id, student.id)
            viewModel.refresh()
            assertEquals(RequestAction.Joined, viewModel.state.value.requestAction)

            world.removeFromRoster(trxClass.id, student.id)
            viewModel.refresh()
            assertEquals(RequestAction.RequestToJoin(opensRegistration = false), viewModel.state.value.requestAction)
            assertTrue(viewModel.state.value.requestAgain)
        }
    }

    @Test
    fun AC43_a_coach_sees_no_request_action() = runVm {
        Phase2World().use { world ->
            val coach = world.coach().toAccount()
            val own = world.createMonWedEveningClass(coach.id)
            val other = world.createMonWedEveningClass(world.coach(2).id, name = "Other")
            assertEquals(RequestAction.None, world.classDetail(own.id, coach).state.value.requestAction)
            assertEquals(RequestAction.None, world.classDetail(other.id, coach).state.value.requestAction)
        }
    }

    @Test
    fun AC44_cancelled_and_private_results_set_typed_errors_and_create_nothing() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val publicClass = world.createMonWedEveningClass(coach.id)
            val privateClass = world.createMonWedEveningClass(coach.id, visibility = Visibility.PRIVATE, name = "Private")
            world.cancelClass(publicClass.id)
            val student = world.student().toAccount()
            val cancelled = world.classDetail(publicClass.id, student)
            cancelled.onRequestToJoin()
            assertEquals(UiError.ClassCancelled, cancelled.state.value.error)
            assertTrue(world.memberships.forClass(publicClass.id).isEmpty())

            assertFalse(world.classDetail(privateClass.id, coach.toAccount()).state.value.unavailable)
            val studentOnPrivate = world.classDetail(privateClass.id, student)
            studentOnPrivate.onRequestToJoin()
            assertEquals(UiError.PrivateClass, studentOnPrivate.state.value.error)
            assertTrue(world.memberships.forClass(privateClass.id).isEmpty())
        }
    }
}
