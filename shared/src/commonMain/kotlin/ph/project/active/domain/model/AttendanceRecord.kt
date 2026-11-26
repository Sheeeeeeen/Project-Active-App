package ph.project.active.domain.model

import kotlinx.datetime.Instant

data class AttendanceRecord(
    val id: String,
    val sessionId: String,
    val classId: String,
    val studentId: String,
    /** The scan time on the phone, never the later upload time. */
    val scannedAt: Instant,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val uploadState: UploadState = UploadState.PENDING,
    /** The link's coach value, stored for the server to check. Empty on rows from before this column. */
    val codeCoach: String = "",
    /** The link's date value, stored for the server to check. Empty on rows from before this column. */
    val codeDate: String = "",
    /** The link's sig value, stored for the server to check. Empty on rows from before this column. */
    val codeSig: String = "",
)
