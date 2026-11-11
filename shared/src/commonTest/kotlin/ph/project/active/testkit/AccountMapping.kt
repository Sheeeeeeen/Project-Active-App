package ph.project.active.testkit

import ph.project.active.data.model.PersonProfile
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.User

fun User.toAccount(): SignedInAccount = SignedInAccount(
    id = id,
    name = name,
    email = email,
    role = role,
    employeeId = employeeId,
)

fun User.toProfile(): PersonProfile = PersonProfile(
    id = id,
    name = name,
    role = role,
    employeeId = employeeId,
)
