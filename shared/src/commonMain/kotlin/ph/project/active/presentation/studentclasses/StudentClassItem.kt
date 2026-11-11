package ph.project.active.presentation.studentclasses

import ph.project.active.presentation.classdetail.ClassChip

data class StudentClassItem(
    val classId: String,
    val name: String,
    val chip: ClassChip?,
)
