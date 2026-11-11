package ph.project.active.presentation.attend

import kotlinx.datetime.LocalDate

sealed interface RejectReason {
    data class WrongDate(val codeDate: LocalDate?) : RejectReason
    data object WrongCoach : RejectReason
    data object NotOnRoster : RejectReason
    data object SessionCancelled : RejectReason
    data object ClassCancelled : RejectReason
    data object InvalidCode : RejectReason
    data object SessionUnknown : RejectReason
}
