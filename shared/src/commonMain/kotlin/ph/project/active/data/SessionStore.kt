package ph.project.active.data

import kotlinx.coroutines.flow.StateFlow

interface SessionStore {
    val account: StateFlow<SignedInAccount?>
    fun save(account: SignedInAccount)
}
