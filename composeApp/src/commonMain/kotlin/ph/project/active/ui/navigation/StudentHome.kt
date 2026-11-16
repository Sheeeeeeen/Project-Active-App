package ph.project.active.ui.navigation

import kotlinx.serialization.Serializable
import ph.project.active.presentation.classdetail.JoinOutcome

@Serializable
data class StudentHome(
    val openClassId: String? = null,
    val joinOutcome: JoinOutcome? = null,
)
