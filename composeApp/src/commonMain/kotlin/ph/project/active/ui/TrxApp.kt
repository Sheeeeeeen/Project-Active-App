package ph.project.active.ui

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.coachclasses.CoachClassesViewModel
import ph.project.active.presentation.createclass.CreateClassViewModel
import ph.project.active.presentation.manageclass.ManageClassViewModel
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.ui.navigation.AppNavHost
import ph.project.active.ui.platform.ShakeSource
import ph.project.active.ui.theme.TrxTheme

@Composable
fun TrxApp(
    rootViewModel: RootViewModel = koinViewModel(),
    guestClassListViewModel: GuestClassListViewModel = koinViewModel(),
    registrationViewModel: ((RegistrationOrigin) -> RegistrationViewModel)? = null,
    shakeSource: ShakeSource = koinInject(),
    classDetailViewModel: ((classId: String, viewer: SignedInAccount?, joinOutcome: JoinOutcome?) -> ClassDetailViewModel)? = null,
    studentClassesViewModel: ((SignedInAccount) -> StudentClassesViewModel)? = null,
    invitesViewModel: ((SignedInAccount) -> InvitesViewModel)? = null,
    coachClassesViewModel: ((SignedInAccount) -> CoachClassesViewModel)? = null,
    createClassViewModel: ((SignedInAccount) -> CreateClassViewModel)? = null,
    manageClassViewModel: ((SignedInAccount, String) -> ManageClassViewModel)? = null,
) {
    TrxTheme {
        AppNavHost(
            rootViewModel = rootViewModel,
            guestClassListViewModel = guestClassListViewModel,
            registrationViewModel = registrationViewModel,
            shakeSource = shakeSource,
            classDetailViewModel = classDetailViewModel,
            studentClassesViewModel = studentClassesViewModel,
            invitesViewModel = invitesViewModel,
            coachClassesViewModel = coachClassesViewModel,
            createClassViewModel = createClassViewModel,
            manageClassViewModel = manageClassViewModel,
        )
    }
}
