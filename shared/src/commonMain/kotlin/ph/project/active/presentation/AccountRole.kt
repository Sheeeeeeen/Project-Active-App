package ph.project.active.presentation

import ph.project.active.data.SignedInAccount
import ph.project.active.domain.Role

internal fun requireStudent(account: SignedInAccount) {
    require(account.role == Role.STUDENT) { "Student screens require a student account" }
}

internal fun requireCoach(account: SignedInAccount) {
    require(account.role == Role.COACH) { "Coach screens require a coach account" }
}
