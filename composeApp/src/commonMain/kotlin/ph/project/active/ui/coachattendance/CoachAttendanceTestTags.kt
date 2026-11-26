package ph.project.active.ui.coachattendance

object CoachAttendanceTestTags {
    const val overline = "coach_attendance_overline"
    const val prevDay = "coach_attendance_prev_day"
    const val nextDay = "coach_attendance_next_day"
    const val date = "coach_attendance_date"
    const val dayState = "coach_attendance_day_state"
    const val presentCount = "coach_attendance_present_count"
    const val absentCount = "coach_attendance_absent_count"
    const val footnote = "coach_attendance_footnote"
    const val empty = "coach_attendance_empty"
    const val sectionPrefix = "coach_attendance_section_"
    const val rowPrefix = "coach_attendance_row_"

    fun section(sessionId: String): String = "$sectionPrefix$sessionId"

    fun row(sessionId: String, studentId: String): String = "$rowPrefix${sessionId}_$studentId"
}
