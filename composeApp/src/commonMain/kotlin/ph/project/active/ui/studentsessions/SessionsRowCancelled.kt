package ph.project.active.ui.studentsessions

import androidx.compose.ui.semantics.SemanticsPropertyKey

/** Set on a Sessions row whose date was cancelled. The row is never tappable. */
val SessionsRowCancelled = SemanticsPropertyKey<Boolean>("sessions_row_cancelled")
