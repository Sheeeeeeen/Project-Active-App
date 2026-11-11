package ph.project.active.data.model

sealed interface RegisterResult {
    data class Success(val account: SignedInAccount) : RegisterResult
    data object EmailTaken : RegisterResult
    data object EmployeeIdTaken : RegisterResult
    data object Offline : RegisterResult
}
