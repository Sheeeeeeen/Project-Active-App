package ph.project.active.data.repository

import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.model.PersonProfile
import ph.project.active.domain.model.User

internal fun User.toSignedInAccount(): SignedInAccount = SignedInAccount(
    id = id,
    name = name,
    email = email,
    role = role,
    employeeId = employeeId,
)

internal fun User.toPersonProfile(): PersonProfile = PersonProfile(
    id = id,
    name = name,
    role = role,
    employeeId = employeeId,
)
