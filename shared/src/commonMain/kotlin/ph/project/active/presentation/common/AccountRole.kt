package ph.project.active.presentation.common

import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.Role

internal fun requireStudent(account: SignedInAccount) {
    require(account.role == Role.STUDENT) { "Student screens require a student account" }
}

internal fun requireCoach(account: SignedInAccount) {
    require(account.role == Role.COACH) { "Coach screens require a coach account" }
}

internal fun Throwable.asUiError(): UiError? = when (this) {
    is IllegalArgumentException -> when {
        message?.contains("End date cannot be in the past") == true -> UiError.EndDateInPast
        message?.contains("Cannot cancel a past date") == true -> UiError.PastDate
        else -> UiError.InvalidState
    }
    is IllegalStateException -> UiError.InvalidState
    else -> null
}
