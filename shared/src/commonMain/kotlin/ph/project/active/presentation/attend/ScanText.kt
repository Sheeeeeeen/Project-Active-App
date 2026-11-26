package ph.project.active.presentation.attend

/** True when [text] looks like an attendance link. Other QR text is ignored and the scanner keeps looking. */
fun looksLikeAttendanceCode(text: String): Boolean = text.trim().startsWith("trxatt://")
