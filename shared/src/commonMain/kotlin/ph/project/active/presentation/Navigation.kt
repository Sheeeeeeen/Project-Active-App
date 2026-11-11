package ph.project.active.presentation

sealed interface Navigation {
    data class OpenRegistration(val origin: RegistrationOrigin) : Navigation
    data class OpenClassDetail(val classId: String) : Navigation
    data object OpenCreateClass : Navigation
    data class OpenManage(val classId: String) : Navigation
    data object Back : Navigation
}
