package ph.project.active.domain.crypto

import kotlinx.datetime.LocalDate
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

internal const val DAILY_URI_PREFIX = "trxatt://v1/daily"
internal val DAILY_URI = Regex("""^trxatt://v1/daily\?coach=([A-Za-z0-9_.~%-]+)&date=(\d{4}-\d{2}-\d{2})&sig=([A-Za-z0-9_-]+)$""")

/** Bytes that get signed: binds the signature to the coach and the date. */
internal fun signedPayload(coachId: String, date: LocalDate): ByteArray =
    "$DAILY_URI_PREFIX|$coachId|$date".encodeToByteArray()

@OptIn(ExperimentalEncodingApi::class)
internal fun ByteArray.toBase64Url(): String = Base64.UrlSafe.encode(this).trimEnd('=')

/** Percent-encoding for the coach id. Letters, digits, and `-._~` stay as they are. */
internal fun encodeCoachId(id: String): String = buildString {
    for (byte in id.encodeToByteArray()) {
        val value = byte.toInt() and 0xFF
        if (value.isUnreserved()) {
            append(value.toChar())
        } else {
            append('%')
            append(HEX[value ushr 4])
            append(HEX[value and 0x0F])
        }
    }
}

/** Reverses [encodeCoachId]. A bad escape (`%ZZ`, a lone `%`, `%4`) is null. */
internal fun decodeCoachIdOrNull(text: String): String? {
    val bytes = ArrayList<Byte>(text.length)
    var index = 0
    while (index < text.length) {
        if (text[index] == '%') {
            if (index + 2 >= text.length) return null
            val value = text.substring(index + 1, index + 3).toIntOrNull(16) ?: return null
            bytes.add(value.toByte())
            index += 3
        } else {
            val code = text[index].code
            if (code > 0x7F) return null
            bytes.add(code.toByte())
            index += 1
        }
    }
    return bytes.toByteArray().decodeToString()
}

private fun Int.isUnreserved(): Boolean = when (this) {
    in 'A'.code..'Z'.code, in 'a'.code..'z'.code, in '0'.code..'9'.code -> true
    '-'.code, '.'.code, '_'.code, '~'.code -> true
    else -> false
}

private const val HEX = "0123456789ABCDEF"

@OptIn(ExperimentalEncodingApi::class)
internal fun String.fromBase64UrlOrNull(): ByteArray? = try {
    val padded = this + "=".repeat((4 - length % 4) % 4)
    Base64.UrlSafe.decode(padded)
} catch (e: IllegalArgumentException) {
    null
}
