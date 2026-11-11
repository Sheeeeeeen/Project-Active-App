package ph.project.active.presentation.common

sealed interface RegistrationOrigin {
    data object FromClassList : RegistrationOrigin
    data class FromRequestToJoin(val classId: String) : RegistrationOrigin
}
