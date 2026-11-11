package ph.project.active.presentation.studentclasses

import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.Navigation

data class StudentClassesUiState(
    val studentName: String,
    val employeeId: String?,
    val myClasses: List<StudentClassItem>,
    val publicClasses: List<StudentClassItem>,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
