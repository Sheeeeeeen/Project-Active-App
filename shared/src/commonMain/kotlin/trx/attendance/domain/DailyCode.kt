package trx.attendance.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val URI_PREFIX = "trxatt://v1/daily"
private val DAILY_URI = Regex("""^trxatt://v1/daily\?coach=([A-Za-z0-9_.~-]+)&date=(\d{4}-\d{2}-\d{2})&sig=([A-Za-z0-9_-]+)$""")

/** Bytes that get signed: binds the signature to the coach and the date. */
private fun signedPayload(coachId: String, date: LocalDate): ByteArray = "$URI_PREFIX|$coachId|$date".encodeToByteArray()

@OptIn(ExperimentalEncodingApi::class)
private fun ByteArray.toBase64Url(): String = Base64.UrlSafe.encode(this).trimEnd('=')

@OptIn(ExperimentalEncodingApi::class)
private fun String.fromBase64UrlOrNull(): ByteArray? = try {
    val padded = this + "=".repeat((4 - length % 4) % 4)
    Base64.UrlSafe.decode(padded)
} catch (e: IllegalArgumentException) {
    null
}

/**
 * AC27, AC31: `trxatt://v1/daily?coach={id}&date=YYYY-MM-DD&sig={base64url}`.
 * Pure function of the coach's private key, id, and date: no clock, no network.
 */
class GenerateDailyCode(private val keys: CoachKeyRepository) {
    operator fun invoke(coachId: String, date: LocalDate): String {
        val keyPair = checkNotNull(keys.find(coachId)) { "Coach $coachId has no signing key" }
        val sig = keyPair.sign(signedPayload(coachId, date)).toBase64Url()
        return "$URI_PREFIX?coach=$coachId&date=$date&sig=$sig"
    }
}

/** Outcome of checking a scanned daily code against an expected coach and today's local date. */
enum class DailyCodeCheck { VALID, INVALID, WRONG_COACH, WRONG_DATE }

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
