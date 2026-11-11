package ph.project.active.presentation.registration

import ph.project.active.domain.model.Role

data class AuthCompletion(val role: Role, val openClassId: String?)
