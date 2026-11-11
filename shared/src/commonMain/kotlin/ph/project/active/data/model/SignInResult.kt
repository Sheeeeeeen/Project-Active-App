package ph.project.active.data.model

sealed interface SignInResult {
    data class Success(val account: SignedInAccount) : SignInResult
    data object Failed : SignInResult
    data object Offline : SignInResult
}
