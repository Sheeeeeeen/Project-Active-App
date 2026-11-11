package ph.project.active.domain.usecase

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import ph.project.active.domain.crypto.fromBase64UrlOrNull
import ph.project.active.domain.repository.CoachKeyRepository
import ph.project.active.domain.model.DailyCodeCheck
import ph.project.active.domain.crypto.signedPayload
import ph.project.active.domain.crypto.DAILY_URI
import ph.project.active.domain.repository.current

/** AC27-AC30: verifies a code with the coach's current public key and the phone's local date. */
class VerifyDailyCode(
    private val keys: CoachKeyRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    operator fun invoke(code: String, coachId: String): Boolean = check(code, coachId) == DailyCodeCheck.VALID

    fun check(code: String, expectedCoachId: String): DailyCodeCheck {
        val parsed = parse(code) ?: return DailyCodeCheck.INVALID
        if (parsed.coachId != expectedCoachId) {
            // A genuine code from another coach is a typed "wrong coach"; anything else is just invalid.
            return if (signatureValid(parsed)) DailyCodeCheck.WRONG_COACH else DailyCodeCheck.INVALID
        }
        if (!signatureValid(parsed)) return DailyCodeCheck.INVALID
        if (parsed.date != clock.todayIn(timeZone)) return DailyCodeCheck.WRONG_DATE
        return DailyCodeCheck.VALID
    }

    private fun signatureValid(code: ParsedCode): Boolean {
        val keyPair = keys.find(code.coachId) ?: return false
        return keyPair.verify(signedPayload(code.coachId, code.date), code.signature)
    }

    private class ParsedCode(val coachId: String, val date: LocalDate, val signature: ByteArray)

    private fun parse(code: String): ParsedCode? {
        val match = DAILY_URI.matchEntire(code.trim().trimEnd('=')) ?: return null
        val (coachId, dateText, sigText) = match.destructured
        val date = try {
            LocalDate.parse(dateText)
        } catch (e: IllegalArgumentException) {
            return null
        }
        val signature = sigText.fromBase64UrlOrNull() ?: return null
        return ParsedCode(coachId, date, signature)
    }
}
