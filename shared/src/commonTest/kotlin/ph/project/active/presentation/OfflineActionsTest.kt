package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.testkit.idle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.testkit.CountingUserRepository
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.ScriptedAuthRepository
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiError

class OfflineActionsTest {

    @Test
    fun AC22_online_only_actions_store_nothing_and_report_offline() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student()
            world.inviteByEmployeeId(trxClass.id, student.employeeId!!)
            val account = student.toAccount()
            val auth = ScriptedAuthRepository()
            val counting = CountingUserRepository(world.users)
            val classesBefore = world.classes.allClasses()
            val sessionsBefore = world.sessionsOf(trxClass.id)
            val membershipsBefore = world.memberships.all()

            world.network.set(NetworkStatus.OFFLINE)
            val registration = world.registration(RegistrationOrigin.FromClassList, auth)
            registration.onNameChange("New Student")
            registration.onEmailChange("new@trx.test")
            registration.onPasswordChange("password1")
            registration.onEmployeeIdChange("EMP-9")
            registration.onSubmit()
            registration.onSignInEmailChange(student.email)
            registration.onSignInPasswordChange("password1")
            registration.onSignIn()
            idle()
            assertEquals(UiError.Offline, registration.state.value.error)
            assertTrue(auth.studentCalls.isEmpty())
            assertTrue(auth.coachCalls.isEmpty())
            assertTrue(auth.signInCalls.isEmpty())
            assertNull(world.sessionStore.account.value)

            val detail = world.classDetail(trxClass.id, account)
            detail.onRequestToJoin()
            val invites = world.invites(account)
            invites.onAccept(trxClass.id)
            invites.onDecline(trxClass.id)
            val manage = world.manage(coach, trxClass.id, counting)
            manage.onApprove(student.id)
            manage.onDecline(student.id)
            manage.onSearchTextChange("EMP-1")
            manage.onSearch()
            manage.onSendInvite()
            manage.onRemove(student.id)
            manage.onConfirmRemove()
            manage.onCancelDate(MON_28_SEP_2026)
            manage.onConfirmCancelDate()
            manage.onOpenOptions()
            manage.onCancelWholeClass()
            manage.onConfirmCancelClass()
            val create = world.createClassVm(coach)
            create.onNameChange("Offline class")
            create.onToggleDay(DayOfWeek.MONDAY)
            create.onAddSlot(DayOfWeek.MONDAY, LocalTime(17, 30), LocalTime(18, 30))
            create.onSave()
            idle()

            assertEquals(UiError.Offline, detail.state.value.error)
            assertEquals(UiError.Offline, invites.state.value.error)
            assertEquals(UiError.Offline, manage.state.value.error)
            assertEquals(UiError.Offline, create.state.value.error)
            assertEquals(0, counting.employeeIdLookups)
            assertEquals(classesBefore.map { it.id }, world.classes.allClasses().map { it.id })
            assertEquals(sessionsBefore, world.sessionsOf(trxClass.id))
            assertEquals(membershipsBefore, world.memberships.all())
            assertEquals(MembershipStatus.INVITED, world.statusOf(trxClass.id, student.id))
            assertNull(create.state.value.savedClassId)
        }
    }

    @Test
    fun AC22_cached_lists_scans_and_daily_qr_keep_working_offline() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            world.giveKeys(coach.id)
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
            val account = student.toAccount()
            world.network.set(NetworkStatus.OFFLINE)
            idle()

            val guest = world.guestClasses()
            assertEquals(listOf(trxClass.id), guest.state.value.items.map { it.classId })
            assertEquals("No internet connection", guest.state.value.connection.banner)

            val attend = world.attend(account)
            attend.onOpened()
            val choice = attend.state.value.choices.single()
            attend.onChoose(choice.sessionId)
            attend.onScanCoachCode()
            attend.onCodeScanned(world.todaysCode(coach.id))
            idle()
            assertEquals(1, world.attendance.all().size)
            assertFalse(attend.state.value.choices.isEmpty())

            val qr = world.dailyQr(coach)
            idle()
            assertTrue(qr.state.value.code.isNotBlank())
            assertEquals("No internet connection", qr.state.value.connection.banner)
            assertEquals(listOf(trxClass.id), world.coachClasses(coach).state.value.classes.map { it.classId })
        }
    }
}
