package ph.project.active.data.repository

import kotlinx.coroutines.flow.StateFlow
import ph.project.active.data.model.SignedInAccount

interface SessionStore {
    val account: StateFlow<SignedInAccount?>
    fun save(account: SignedInAccount)
}
