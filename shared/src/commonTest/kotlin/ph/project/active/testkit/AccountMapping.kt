package ph.project.active.testkit

import ph.project.active.data.PersonProfile
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.User

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
