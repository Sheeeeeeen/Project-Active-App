package ph.project.active.presentation

import ph.project.active.domain.Role

data class RegistrationUiState(
    val origin: RegistrationOrigin,
    val mode: Role,
    val showEmployeeId: Boolean,
    val name: String,
    val email: String,
    val password: String,
    val employeeId: String,
    val signInEmail: String,
    val signInPassword: String,
    val fieldErrors: Map<Field, FieldError>,
    val isSubmitting: Boolean,
    val requestingClass: RequestingClassHeader?,
    val signInError: UiError?,
    val completion: AuthCompletion?,
    val joinOutcome: JoinOutcome?,
    val error: UiError?,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
