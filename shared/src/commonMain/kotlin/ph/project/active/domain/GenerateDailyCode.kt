package ph.project.active.domain

import kotlinx.datetime.LocalDate

/**
 * AC27, AC31: `trxatt://v1/daily?coach={id}&date=YYYY-MM-DD&sig={base64url}`.
 * Pure function of the coach's private key, id, and date: no clock, no network.
 */
class GenerateDailyCode(private val keys: CoachKeyRepository) {
    operator fun invoke(coachId: String, date: LocalDate): String {
        val keyPair = checkNotNull(keys.find(coachId)) { "Coach $coachId has no signing key" }
        val sig = keyPair.sign(signedPayload(coachId, date)).toBase64Url()
        return "$DAILY_URI_PREFIX?coach=$coachId&date=$date&sig=$sig"
    }
}
