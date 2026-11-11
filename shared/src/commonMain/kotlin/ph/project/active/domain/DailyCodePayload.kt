package ph.project.active.domain

import kotlinx.datetime.LocalDate
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal const val DAILY_URI_PREFIX = "trxatt://v1/daily"
internal val DAILY_URI = Regex("""^trxatt://v1/daily\?coach=([A-Za-z0-9_.~-]+)&date=(\d{4}-\d{2}-\d{2})&sig=([A-Za-z0-9_-]+)$""")

/** Bytes that get signed: binds the signature to the coach and the date. */
internal fun signedPayload(coachId: String, date: LocalDate): ByteArray =
    "$DAILY_URI_PREFIX|$coachId|$date".encodeToByteArray()

@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.toBase64Url(): String = Base64.UrlSafe.encode(this).trimEnd('=')

@OptIn(ExperimentalEncodingApi::class)
internal fun String.fromBase64UrlOrNull(): ByteArray? = try {
    val padded = this + "=".repeat((4 - length % 4) % 4)
    Base64.UrlSafe.decode(padded)
} catch (e: IllegalArgumentException) {
    null
}
