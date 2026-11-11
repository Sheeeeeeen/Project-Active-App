package ph.com.sheen.projectactive.feature.classes

import kotlinx.datetime.LocalDate

data class Class(
    val name: String,
    val coachName: String,
    val days: List<ClassDay>,
    val visibility: ClassVisibility,
    val end: LocalDate? = null,
    val everyDay: Boolean = false,
)
