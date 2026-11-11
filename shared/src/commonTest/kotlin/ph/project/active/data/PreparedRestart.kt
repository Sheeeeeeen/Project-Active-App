package ph.project.active.data

import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import ph.project.active.domain.SessionStatus

data class PreparedRestart(
    val student: SignedInAccount,
    val coach: SignedInAccount,
    val classId: String,
    val publicKey: ByteArray,
    val sessions: List<Pair<LocalDate, SessionStatus>>,
    val scannedAt: Instant,
    val studentRows: List<List<String>>,
    val choices: List<Pair<String, String>>,
    val recordedToday: List<Pair<Int, Boolean>>,
    val coachRows: List<Triple<String, Int, Boolean>>,
)
