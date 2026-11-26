package ph.project.active.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.domain.crypto.parseDailyCodeLink
import ph.project.active.domain.model.DailyCodeLink
import ph.project.active.domain.repository.inmemory.InMemoryCoachKeyRepository
import ph.project.active.domain.usecase.GenerateDailyCode

/** Flow 7 spec S1 and F7-AC1: the student phone reads the daily-code link and never checks its signature. */
class F7Ac1ParseDailyCodeLinkTest {

    /** "Mara's code" for the coach id `coach-mara`, made on the coach side. */
    private val marasCode: String = InMemoryCoachKeyRepository().let { keys ->
        keys.save("coach-mara", Ed25519KeyPair.generate())
        GenerateDailyCode(keys = keys).invoke(coachId = "coach-mara", date = LocalDate(2026, 9, 28))
    }

    // F7-AC1: "For Mara's code it returns coachId = "coach-mara", date = 2026-09-28, and rawCoach, rawDate and sig equal to the link's three values."
    @Test
    fun F7_AC1_marasCodeGivesTheCoachTheDateAndTheThreeRawValues() {
        val link = assertNotNull(parseDailyCodeLink(marasCode), "Mara's code parses")
        assertEquals(
            DailyCodeLink(
                coachId = "coach-mara",
                date = LocalDate(2026, 9, 28),
                rawCoach = "coach-mara",
                rawDate = "2026-09-28",
                sig = marasCode.substringAfter("&sig="),
            ),
            link,
        )
        assertEquals("trxatt://v1/daily?coach=${link.rawCoach}&date=${link.rawDate}&sig=${link.sig}", marasCode)
    }

    // F7-AC1: "It returns null for: text without the trxatt://v1/daily scheme"
    @Test
    fun F7_AC1_textWithoutTheDailySchemeIsNull() {
        val query = marasCode.substringAfter("?")
        assertNull(parseDailyCodeLink("https://example.com/daily?$query"))
        assertNull(parseDailyCodeLink("trxatt://v2/daily?$query"))
        assertNull(parseDailyCodeLink(query))
        assertNull(parseDailyCodeLink(""))
    }

    // F7-AC1: "It returns null for: a link missing coach, date or sig"
    @Test
    fun F7_AC1_aLinkMissingCoachDateOrSigIsNull() {
        val sig = marasCode.substringAfter("&sig=")
        assertNull(parseDailyCodeLink("trxatt://v1/daily?date=2026-09-28&sig=$sig"), "no coach")
        assertNull(parseDailyCodeLink("trxatt://v1/daily?coach=coach-mara&sig=$sig"), "no date")
        assertNull(parseDailyCodeLink("trxatt://v1/daily?coach=coach-mara&date=2026-09-28"), "no sig")
    }

    // F7-AC1: "It returns null for: date=2026-13-45"
    @Test
    fun F7_AC1_anImpossibleDateIsNull() {
        assertNull(parseDailyCodeLink(marasCode.replace("date=2026-09-28", "date=2026-13-45")))
    }

    // F7-AC1: "It returns null for: coach=%ZZ"
    @Test
    fun F7_AC1_aBadCoachEscapeIsNull() {
        assertNull(parseDailyCodeLink(marasCode.replace("coach=coach-mara", "coach=%ZZ")))
    }

    // F7-AC1: "It returns null for: an empty sig"
    @Test
    fun F7_AC1_anEmptySigIsNull() {
        assertNull(parseDailyCodeLink(marasCode.substringBefore("&sig=") + "&sig="))
    }

    // F7-AC1: "It returns null for: sig=!!"
    @Test
    fun F7_AC1_aSigThatIsNotBase64UrlIsNull() {
        assertNull(parseDailyCodeLink(marasCode.substringBefore("&sig=") + "&sig=!!"))
    }

    // F7-AC1: "Mara's code with its last signature character changed still parses (no signature check)."
    @Test
    fun F7_AC1_aChangedSignatureCharacterStillParses() {
        val changed = withChangedLastSigCharacter(marasCode)
        val link = assertNotNull(parseDailyCodeLink(changed), "no signature check on the phone")
        assertEquals("coach-mara", link.coachId)
        assertEquals(LocalDate(2026, 9, 28), link.date)
        assertEquals(changed.substringAfter("&sig="), link.sig)
    }
}

/**
 * [code] with the last signature character swapped for another base64url character whose two low bits
 * are zero ('A' <-> 'Q'), so the text still decodes and only the signature bytes differ.
 */
fun withChangedLastSigCharacter(code: String): String {
    val last = code.last()
    return code.dropLast(1) + (if (last == 'A') 'Q' else 'A')
}
