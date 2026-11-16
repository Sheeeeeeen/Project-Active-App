package ph.project.active.ui.studenthome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ph.project.active.data.model.SignedInAccount
import ph.project.active.di.rememberInvites
import ph.project.active.di.rememberStudentClasses
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.ui.invites.InvitesScreen
import ph.project.active.ui.studentclasses.StudentClassesScreen

/** The signed-in student root: four tabs inside one back-stack entry, so tab switches never touch the back stack. */
@Composable
fun StudentHomeScreen(
    account: SignedInAccount,
    studentClassesViewModel: ((SignedInAccount) -> StudentClassesViewModel)?,
    invitesViewModel: ((SignedInAccount) -> InvitesViewModel)?,
    onOpenClass: (String) -> Unit,
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
        StudentTab.Attend -> StudentTabPlaceholder(
            title = UiCopy.tabAttend(),
            rootTag = "student_attend",
            tabBar = tabBar,
        )
        StudentTab.Sessions -> StudentTabPlaceholder(
            title = UiCopy.tabSessions(),
            rootTag = "student_sessions",
            tabBar = tabBar,
        )
        StudentTab.Invites -> InvitesScreen(
            state = invitesState,
            onAccept = invites::onAccept,
            onDecline = invites::onDecline,
            onConfirmationHandled = invites::onConfirmationHandled,
            tabBar = tabBar,
        )
    }
}
