package ph.project.active.ui.studentsessions

object StudentSessionsTestTags {
    const val overline = "sessions_overline"
    const val subtitle = "sessions_subtitle"
    const val groupUpcoming = "sessions_group_upcoming"
    const val groupToday = "sessions_group_today"
    const val groupEarlier = "sessions_group_earlier"
    const val todayTag = "sessions_today_tag"
    const val empty = "sessions_empty"

    fun row(sessionId: String): String = "sessions_row_$sessionId"
}
