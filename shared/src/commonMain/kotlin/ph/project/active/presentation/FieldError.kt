package ph.project.active.presentation

sealed interface FieldError {
    data object Required : FieldError
    data object InvalidEmail : FieldError
    data object PasswordTooShort : FieldError
    data object EmailTaken : FieldError
    data object EmployeeIdTaken : FieldError
}
