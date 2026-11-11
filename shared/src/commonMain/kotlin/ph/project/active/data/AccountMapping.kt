package ph.project.active.data

import ph.project.active.domain.User

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
