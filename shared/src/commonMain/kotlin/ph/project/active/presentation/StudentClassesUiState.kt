package ph.project.active.presentation

data class StudentClassesUiState(
    val studentName: String,
    val employeeId: String?,
    val myClasses: List<StudentClassItem>,
    val publicClasses: List<StudentClassItem>,
    val connection: ConnectionUi,
    val navigation: Navigation?,
)
