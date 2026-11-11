package ph.project.active.presentation.registration

import ph.project.active.presentation.classdetail.RequestingClassHeader
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.common.FieldError
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.common.Field
import ph.project.active.domain.model.Role

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
