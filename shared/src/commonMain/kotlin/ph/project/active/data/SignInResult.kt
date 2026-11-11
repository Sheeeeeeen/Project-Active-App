package ph.project.active.data

sealed interface SignInResult {
    data class Success(val account: SignedInAccount) : SignInResult
    data object Failed : SignInResult
    data object Offline : SignInResult
}
