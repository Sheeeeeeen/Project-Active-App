package ph.project.active.data.db

/** One membership row as raw text, read straight from SQLite for the schema v2 migration checks. */
data class MembershipRowText(
    val id: String,
    val classId: String,
    val status: String,
    val sortOrder: String,
    val joinedBy: String?,
)
