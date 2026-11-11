package ph.project.active.presentation.root

import ph.project.active.data.model.SignedInAccount

sealed interface Root {
    data object Guest : Root
    data class Student(val account: SignedInAccount) : Root
    data class Coach(val account: SignedInAccount) : Root
}
