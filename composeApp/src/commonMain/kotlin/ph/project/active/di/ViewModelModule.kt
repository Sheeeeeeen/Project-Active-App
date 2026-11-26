package ph.project.active.di

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ph.project.active.data.model.SignedInAccount
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.coachattendance.CoachAttendanceViewModel
import ph.project.active.presentation.coachclasses.CoachClassesViewModel
import ph.project.active.presentation.createclass.CreateClassViewModel
import ph.project.active.presentation.dailyqr.DailyQrViewModel
import ph.project.active.presentation.manageclass.ManageClassViewModel
import ph.project.active.domain.usecase.ApproveRequest
import ph.project.active.domain.usecase.CancelClass
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.domain.usecase.DeclineRequest
import ph.project.active.domain.usecase.InviteByEmployeeId
import ph.project.active.domain.usecase.RemoveFromRoster
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.presentation.invites.InvitesViewModel
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.presentation.root.RootViewModel
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.CoachKeyRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.usecase.RecordAttendance
import ph.project.active.domain.usecase.VerifyDailyCode
import ph.project.active.presentation.attend.AttendViewModel
import ph.project.active.presentation.studentclasses.StudentClassesViewModel
import ph.project.active.presentation.studentsessions.StudentSessionsViewModel

val ViewModelModule = module {
    viewModel { RootViewModel(session = get(), network = get(), outbox = get()) }
    viewModel { GuestClassListViewModel(classes = get(), people = get(), network = get()) }
    viewModel { (classId: String, viewer: SignedInAccount?, joinOutcome: JoinOutcome?) ->
        ClassDetailViewModel(
            classId = classId,
            viewer = viewer,
            classes = get(),
            memberships = get(),
            people = get(),
            requestToJoin = get(),
            network = get(),
            joinOutcome = joinOutcome,
        )
    }
    viewModel { (account: SignedInAccount) ->
        StudentClassesViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            people = get(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        InvitesViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            people = get(),
            acceptInvite = get(),
            declineInvite = get(),
            network = get(),
        )
    }
    viewModel { (origin: RegistrationOrigin) ->
        RegistrationViewModel(
            origin = origin,
            auth = get(),
            session = get(),
            people = get(),
            classes = get(),
            requestToJoin = get(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        CoachClassesViewModel(account = account, classes = get(), memberships = get(), network = get())
    }
    viewModel { (account: SignedInAccount) ->
        CreateClassViewModel(
            account = account,
            classes = get(),
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount, classId: String) ->
        val timeZone = TimeZone.currentSystemDefault()
        ManageClassViewModel(
            account = account,
            classId = classId,
            classes = get(),
            memberships = get(),
            attendance = get(),
            people = get(),
            users = get(),
            approveRequest = ApproveRequest(get()),
            declineRequest = DeclineRequest(get()),
            removeFromRoster = RemoveFromRoster(get()),
            inviteByEmployeeId = InviteByEmployeeId(get(), get(), get()),
            cancelDate = CancelDate(get(), Clock.System, timeZone),
            cancelClass = CancelClass(get(), Clock.System, timeZone),
            clock = Clock.System,
            timeZone = timeZone,
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        DailyQrViewModel(
            account = account,
            classes = get(),
            keys = get(),
            people = get(),
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        CoachAttendanceViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            attendance = get(),
            people = get(),
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        AttendViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            attendance = get(),
            people = get(),
            recordAttendance = recordAttendance(get(), get(), get(), get(), get()),
            outbox = get(),
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            network = get(),
        )
    }
    viewModel { (account: SignedInAccount) ->
        StudentSessionsViewModel(
            account = account,
            classes = get(),
            memberships = get(),
            attendance = get(),
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
            network = get(),
        )
    }
}

private fun recordAttendance(
    classes: ClassRepository,
    memberships: MembershipRepository,
    attendance: AttendanceRepository,
    keys: CoachKeyRepository,
    network: NetworkMonitor,
): RecordAttendance {
    val timeZone = TimeZone.currentSystemDefault()
    return RecordAttendance(
        classes = classes,
        memberships = memberships,
        attendance = attendance,
        verifyDailyCode = VerifyDailyCode(keys = keys, clock = Clock.System, timeZone = timeZone),
        clock = Clock.System,
        timeZone = timeZone,
        networkStatus = { network.status.value },
    )
}
