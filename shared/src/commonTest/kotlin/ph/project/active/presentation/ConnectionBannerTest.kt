package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull
import ph.project.active.testkit.idle
import ph.project.active.domain.NetworkStatus
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.assertConnection
import ph.project.active.testkit.runVm

class ConnectionBannerTest {

    @Test
    fun AC16_every_view_model_banner_tracks_the_monitor_without_recreation() = runVm {
        Phase2World().use { world ->
            val coach = world.coachAccount()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.studentAccount()
            val root = world.root()
            val registration = world.registration(RegistrationOrigin.FromClassList)
            val guest = world.guestClasses()
            val detail = world.classDetail(trxClass.id, null)
            val studentClasses = world.studentClasses(student)
            val attend = world.attend(student)
            val sessions = world.studentSessionsVm(student)
            val invites = world.invites(student)
            val coachClasses = world.coachClasses(coach)
            val create = world.createClassVm(coach)
            val manage = world.manage(coach, trxClass.id)
            val qr = world.dailyQr(coach)
            val attendance = world.coachAttendance(coach)
            listOf(
                root, registration, guest, detail, studentClasses, attend, sessions,
                invites, coachClasses, create, manage, qr, attendance,
            ).forEach { assertIs<ViewModel>(it) }

            idle()
            fun banners(offline: Boolean) {
                assertConnection(root.state.value.connection, offline)
                assertConnection(registration.state.value.connection, offline)
                assertConnection(guest.state.value.connection, offline)
                assertConnection(detail.state.value.connection, offline)
                assertConnection(studentClasses.state.value.connection, offline)
                assertConnection(attend.state.value.connection, offline)
                assertConnection(sessions.state.value.connection, offline)
                assertConnection(invites.state.value.connection, offline)
                assertConnection(coachClasses.state.value.connection, offline)
                assertConnection(create.state.value.connection, offline)
                assertConnection(manage.state.value.connection, offline)
                assertConnection(qr.state.value.connection, offline)
                assertConnection(attendance.state.value.connection, offline)
            }
            banners(offline = false)
            world.network.set(NetworkStatus.OFFLINE)
            idle()
            banners(offline = true)
            world.network.set(NetworkStatus.ONLINE)
            idle()
            banners(offline = false)

            root.onNavigationHandled()
            registration.onNavigationHandled()
            guest.onNavigationHandled()
            detail.onNavigationHandled()
            studentClasses.onNavigationHandled()
            attend.onNavigationHandled()
            sessions.onNavigationHandled()
            invites.onNavigationHandled()
            coachClasses.onNavigationHandled()
            create.onNavigationHandled()
            manage.onNavigationHandled()
            qr.onNavigationHandled()
            attendance.onNavigationHandled()
            assertNull(root.state.value.navigation)
            assertNull(guest.state.value.navigation)
            assertNull(coachClasses.state.value.navigation)
        }
    }
}
