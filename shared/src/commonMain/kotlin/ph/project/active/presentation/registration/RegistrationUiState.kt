package ph.project.active.presentation.registration

import ph.project.active.domain.model.Role
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.classdetail.RequestingClassHeader
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.FieldError
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiError

data class RegistrationUiState(
    val origin: RegistrationOrigin,
    val connection: ConnectionUi,
    val mode: Role = Role.STUDENT,
    val showEmployeeId: Boolean = true,
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val employeeId: String = "",
    val signInEmail: String = "",
    val signInPassword: String = "",
    val fieldErrors: Map<Field, FieldError> = emptyMap(),
    val isSubmitting: Boolean = false,
    val requestingClass: RequestingClassHeader? = null,
    val signInError: UiError? = null,
    val completion: AuthCompletion? = null,
    val joinOutcome: JoinOutcome? = null,
    val error: UiError? = null,
    val navigation: Navigation? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            name.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            (mode != Role.STUDENT || employeeId.isNotBlank())
}
