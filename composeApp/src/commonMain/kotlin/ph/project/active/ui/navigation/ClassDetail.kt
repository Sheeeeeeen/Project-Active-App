package ph.project.active.ui.navigation

import kotlinx.serialization.Serializable
import ph.project.active.presentation.classdetail.JoinOutcome

@Serializable
data class ClassDetail(
    val classId: String,
    val joinOutcome: JoinOutcome? = null,
)
