package ph.project.active.presentation

sealed interface RegistrationOrigin {
    data object FromClassList : RegistrationOrigin
    data class FromRequestToJoin(val classId: String) : RegistrationOrigin
}
