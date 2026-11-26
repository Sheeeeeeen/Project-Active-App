package ph.project.active.ui.studenthome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ph.project.active.data.model.SignedInAccount
import ph.project.active.di.rememberAttend
import ph.project.active.di.rememberInvites
import ph.project.active.di.rememberStudentClasses
import ph.project.active.di.rememberStudentSessions
import ph.project.active.presentation.attend.AttendViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.presentation.studentsessions.StudentSessionsViewModel
import ph.project.active.ui.attend.AttendScreen
import ph.project.active.ui.invites.InvitesScreen
import ph.project.active.ui.platform.CameraPermission
import ph.project.active.ui.platform.QrScanner
import ph.project.active.ui.platform.ScanHaptics
import ph.project.active.ui.platform.rememberCameraPermission
import ph.project.active.ui.platform.rememberQrScanner
import ph.project.active.ui.platform.rememberScanHaptics
import ph.project.active.ui.studentclasses.StudentClassesScreen
import ph.project.active.ui.studentsessions.StudentSessionsScreen

/** The signed-in student root: four tabs inside one back-stack entry, so tab switches never touch the back stack. */
@Composable
fun StudentHomeScreen(
    account: SignedInAccount,
    studentClassesViewModel: ((SignedInAccount) -> StudentClassesViewModel)?,
    invitesViewModel: ((SignedInAccount) -> InvitesViewModel)?,
    onOpenClass: (String) -> Unit,
    attendViewModel: ((SignedInAccount) -> AttendViewModel)? = null,
    studentSessionsViewModel: ((SignedInAccount) -> StudentSessionsViewModel)? = null,
    qrScanner: QrScanner? = null,
    cameraPermission: CameraPermission? = null,
    scanHaptics: ScanHaptics? = null,
) {
    val classes = rememberStudentClasses(account, studentClassesViewModel)
    val invites = rememberInvites(account, invitesViewModel)
    var selected by rememberSaveable { mutableStateOf(StudentTab.Classes) }
    LifecycleStartEffect(selected) {
        classes.refresh()
        invites.refresh()
        onStopOrDispose {}
    }
    val classesState by classes.state.collectAsStateWithLifecycle()
    val invitesState by invites.state.collectAsStateWithLifecycle()
    val tabBar = @Composable {
        StudentTabBar(
            selected = selected,
            pendingInviteCount = invitesState.pendingInviteCount,
            onSelect = { selected = it },
        )
    }
    when (selected) {
        StudentTab.Classes -> StudentClassesScreen(
            state = classesState,
            onClassTapped = { id ->
                classes.onClassTapped(id)
                classes.state.value.navigation?.let {
                    classes.onNavigationHandled()
                    onOpenClass(id)
                }
            },
            tabBar = tabBar,
        )
        StudentTab.Attend -> {
            val attend = rememberAttend(account, attendViewModel)
            val scanner = qrScanner ?: rememberQrScanner()
            val permission = cameraPermission ?: rememberCameraPermission()
            val haptics = scanHaptics ?: rememberScanHaptics()
            val attendState by attend.state.collectAsStateWithLifecycle()
            LifecycleStartEffect(attend) {
                attend.refresh()
                onStopOrDispose {}
            }
            AttendScreen(
                state = attendState,
                timeZone = attend.timeZone,
                scanner = scanner,
                permission = permission,
                haptics = haptics,
                onOpened = attend::onOpened,
                onChoose = attend::onChoose,
                onCloseSheet = attend::onCloseSheet,
                onScanCoachCode = attend::onScanCoachCode,
                onChangeSession = attend::onChangeSession,
                onCloseScanner = attend::onCloseScanner,
                onCodeScanned = attend::onCodeScanned,
                onRecordAnother = attend::onRecordAnother,
                onChooseDifferentClass = attend::onChooseDifferentClass,
                onScanAgain = attend::onScanAgain,
                onDone = attend::onDone,
                tabBar = tabBar,
            )
        }
        StudentTab.Sessions -> {
            val sessions = rememberStudentSessions(account, studentSessionsViewModel)
            val sessionsState by sessions.state.collectAsStateWithLifecycle()
            LifecycleStartEffect(sessions) {
                sessions.refresh()
                onStopOrDispose {}
            }
            StudentSessionsScreen(
                state = sessionsState,
                tabBar = tabBar,
            )
        }
        StudentTab.Invites -> InvitesScreen(
            state = invitesState,
            onAccept = invites::onAccept,
            onDecline = invites::onDecline,
            onConfirmationHandled = invites::onConfirmationHandled,
            tabBar = tabBar,
        )
    }
}
