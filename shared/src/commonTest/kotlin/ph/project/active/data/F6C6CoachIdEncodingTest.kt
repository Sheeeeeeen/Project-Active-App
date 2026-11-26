package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.crypto.decodeCoachIdOrNull
import ph.project.active.domain.crypto.encodeCoachId
import ph.project.active.domain.crypto.fromBase64UrlOrNull
import ph.project.active.domain.crypto.signedPayload
import ph.project.active.domain.model.DailyCodeCheck
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World

/** Flow 6 spec C6 (decision 16, F6-AC19): the coach id is percent-encoded in the daily link. */
class F6C6CoachIdEncodingTest {

    private val oddId = "coach 1/\u00E4"

    // C6: "coach id `coach 1/ä` gives `coach=coach%201%2F%C3%A4` in the link"
    @Test
    fun F6_C6_anIdWithSpacesSlashesAndNonAsciiIsPercentEncodedInTheLink() {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            world.giveKeys(oddId)

            val link = world.generateDailyCode(coachId = oddId, date = MON_28_SEP_2026)

            assertTrue(link.startsWith("trxatt://v1/daily?coach=coach%201%2F%C3%A4&date=2026-09-28&sig="), link)
        }
    }

    // C6: "checking that link for that coach today is VALID"
    @Test
    fun F6_C6_theEncodedLinkChecksAsValidForThatCoachToday() {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            world.giveKeys(oddId)

            val link = world.generateDailyCode(coachId = oddId, date = MON_28_SEP_2026)

            assertEquals(DailyCodeCheck.VALID, world.verifyDailyCode.check(link, oddId))
        }
    }

    // C6: "The signed payload still uses the plain, decoded id"
    @Test
    fun F6_C6_theSignatureCoversThePlainDecodedId() {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val keys = world.giveKeys(oddId)

            val link = world.generateDailyCode(coachId = oddId, date = MON_28_SEP_2026)
            val signature = link.substringAfter("&sig=").fromBase64UrlOrNull()!!

            assertTrue(keys.verify(signedPayload(oddId, MON_28_SEP_2026), signature))
        }
    }

    // C6: "for the current kind of id the link is unchanged from today"
    @Test
    fun F6_C6_aPlainIdGivesTheSameLinkAsBefore() {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val coach = world.coach()
            world.giveKeys(coach.id)

            val link = world.generateDailyCode(coachId = coach.id, date = MON_28_SEP_2026)

            assertTrue(link.startsWith("trxatt://v1/daily?coach=${coach.id}&date=2026-09-28&sig="), link)
            assertEquals(DailyCodeCheck.VALID, world.verifyDailyCode.check(link, coach.id))
        }
    }

    // C6: "`coach=%ZZ` is INVALID" (a bad escape)
    @Test
    fun F6_C6_aBadEscapeInTheCoachValueIsInvalid() {
        Phase2World().use { world ->
            world.setLocal(MON_28_SEP_2026, 9)
            val coach = world.coach()
            world.giveKeys(coach.id)
            val link = world.generateDailyCode(coachId = coach.id, date = MON_28_SEP_2026)

            val badEscape = link.replace("coach=${coach.id}&", "coach=%ZZ&")
            val lonepercent = link.replace("coach=${coach.id}&", "coach=${coach.id}%&")

            assertEquals(DailyCodeCheck.INVALID, world.verifyDailyCode.check(badEscape, coach.id))
            assertEquals(DailyCodeCheck.INVALID, world.verifyDailyCode.check(lonepercent, coach.id))
        }
    }

    // C6: "letters, digits, -, ., _, ~ stay as they are; everything else becomes %HH with upper-case hex" (UTF-8)
    @Test
    fun F6_C6_encodeCoachIdKeepsUnreservedCharactersAndUsesUpperCaseHex() {
        assertEquals("Abc-1.2_3~z", encodeCoachId("Abc-1.2_3~z"))
        assertEquals("coach%201%2F%C3%A4", encodeCoachId(oddId))
        assertEquals("a%2Bb%3Dc%26d%3Fe%25", encodeCoachId("a+b=c&d?e%"))
    }

    // C6: "VerifyDailyCode accepts %HH in the coach value and decodes it"; "A bad escape (for example %ZZ or a lone %) is INVALID"
    @Test
    fun F6_C6_decodeCoachIdOrNullReversesTheEncodingAndRejectsBadEscapes() {
        assertEquals(oddId, decodeCoachIdOrNull("coach%201%2F%C3%A4"))
        assertEquals("Abc-1.2_3~z", decodeCoachIdOrNull("Abc-1.2_3~z"))
        assertEquals("a%2Fb", decodeCoachIdOrNull(encodeCoachId("a%2Fb")), "a literal % round-trips")
        assertNull(decodeCoachIdOrNull("%ZZ"))
        assertNull(decodeCoachIdOrNull("%"))
        assertNull(decodeCoachIdOrNull("abc%4"))
    }
}
