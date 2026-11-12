package ph.project.active.ui.registration

object RegistrationTestTags {
    const val nameField = "registration_name_field"
    const val emailField = "registration_email_field"
    const val passwordField = "registration_password_field"
    const val employeeIdField = "registration_employee_id_field"
    const val signInEmailField = "registration_signin_email_field"
    const val signInPasswordField = "registration_signin_password_field"
    const val passwordHelper = "registration_password_helper"
    const val employeeIdHelper = "registration_employee_id_helper"
    const val passwordToggle = "registration_password_toggle"
    const val signInPasswordToggle = "registration_signin_password_toggle"
    const val submitButton = "registration_submit_button"
    const val submitProgress = "registration_submit_progress"
    const val signInButton = "registration_signin_button"
    const val signInProgress = "registration_signin_progress"
    const val back = "registration_back"
    const val title = "registration_title"
    const val requestingCard = "registration_requesting_card"
    const val signInNote = "registration_signin_note"
    const val coachBand = "registration_coach_band"
    const val formError = "registration_form_error"
    const val signInError = "registration_signin_error"

    fun field(field: ph.project.active.presentation.common.Field): String = when (field) {
        ph.project.active.presentation.common.Field.NAME -> nameField
        ph.project.active.presentation.common.Field.EMAIL -> emailField
        ph.project.active.presentation.common.Field.PASSWORD -> passwordField
        ph.project.active.presentation.common.Field.EMPLOYEE_ID -> employeeIdField
        ph.project.active.presentation.common.Field.SIGN_IN_EMAIL -> signInEmailField
        ph.project.active.presentation.common.Field.SIGN_IN_PASSWORD -> signInPasswordField
    }

    fun error(field: ph.project.active.presentation.common.Field): String = "${field(field).removeSuffix("_field")}_error"

    fun errorIcon(field: ph.project.active.presentation.common.Field): String =
        "${field(field).removeSuffix("_field")}_error_icon"
}
