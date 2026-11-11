package ph.project.active.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.atTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DailyCodeTest {

    private val uriPattern = Regex("""^trxatt://v1/daily\?coach=([^&]+)&date=(\d{4}-\d{2}-\d{2})&sig=([A-Za-z0-9_-]+={0,2})$""")

    private fun sigOf(uri: String): String = uriPattern.matchEntire(uri)!!.groupValues[3]

    private fun withSig(uri: String, sig: String): String = uri.substringBefore("&sig=") + "&sig=" + sig

    // ----- AC27 -----

    @Test
    fun AC27_generated_code_has_the_daily_uri_format_and_verifies_with_the_coach_public_key() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)

        val uri = world.generateDailyCode(coachId = coach.id, date = MON_28_SEP_2026)

        val match = assertNotNull(uriPattern.matchEntire(uri), "unexpected URI format: $uri")
        assertEquals(coach.id, match.groupValues[1])
        assertEquals("2026-09-28", match.groupValues[2])
        assertTrue(match.groupValues[3].isNotEmpty())
        assertTrue(world.verifyDailyCode(code = uri, coachId = coach.id))
    }

    @Test
    fun AC27_code_does_not_verify_for_a_different_coach() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coachA = world.coach(1)
        val coachB = world.coach(2)
        world.giveKeys(coachA.id)
        world.giveKeys(coachB.id)

        val uri = world.generateDailyCode(coachId = coachA.id, date = MON_28_SEP_2026)

        assertFalse(world.verifyDailyCode(code = uri, coachId = coachB.id))
    }

    // ----- AC28 -----

    @Test
    fun AC28_code_for_a_date_does_not_verify_on_another_local_date() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val mondayCode = world.generateDailyCode(coachId = coach.id, date = MON_28_SEP_2026)

        world.setLocal(TUE_29_SEP_2026, 9)
        assertFalse(world.verifyDailyCode(code = mondayCode, coachId = coach.id))

        world.setLocal(FRI_25_SEP_2026, 9)
        assertFalse(world.verifyDailyCode(code = mondayCode, coachId = coach.id))

        world.setLocal(MON_28_SEP_2026, 23, 59)
        assertTrue(world.verifyDailyCode(code = mondayCode, coachId = coach.id))
    }

    @Test
    fun AC28_local_date_uses_the_phone_timezone() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val tuesdayCode = world.generateDailyCode(coachId = coach.id, date = TUE_29_SEP_2026)

        // 2026-09-28T16:30Z is Tuesday 29 Sep 00:30 in Manila but still Monday in UTC.
        world.clock.current = Instant.parse("2026-09-28T16:30:00Z")
        assertTrue(world.verifyDailyCode(code = tuesdayCode, coachId = coach.id))

        // 2026-09-28T15:30Z is Monday 28 Sep 23:30 in Manila.
        world.clock.current = Instant.parse("2026-09-28T15:30:00Z")
        assertFalse(world.verifyDailyCode(code = tuesdayCode, coachId = coach.id))
    }

    @Test
    fun AC28_editing_the_date_in_the_uri_breaks_the_signature() {
        val world = TestWorld(start = TUE_29_SEP_2026.atTime(9, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val mondayCode = world.generateDailyCode(coachId = coach.id, date = MON_28_SEP_2026)

        val forged = mondayCode.replace("date=2026-09-28", "date=2026-09-29")

        assertFalse(world.verifyDailyCode(code = forged, coachId = coach.id))
    }

    // ----- AC29 -----

    @Test
    fun AC29_missing_signature_does_not_verify() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val uri = world.todaysCode(coach.id)

        assertFalse(world.verifyDailyCode(code = uri.substringBefore("&sig="), coachId = coach.id))
        assertFalse(world.verifyDailyCode(code = withSig(uri, ""), coachId = coach.id))
    }

    @Test
    fun AC29_truncated_signature_does_not_verify() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val uri = world.todaysCode(coach.id)
        val sig = sigOf(uri).trimEnd('=')

        assertFalse(world.verifyDailyCode(code = withSig(uri, sig.dropLast(4)), coachId = coach.id))
        assertFalse(world.verifyDailyCode(code = withSig(uri, sig.take(10)), coachId = coach.id))
    }

    @Test
    fun AC29_foreign_signature_does_not_verify() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coachA = world.coach(1)
        val coachB = world.coach(2)
        world.giveKeys(coachA.id)
        world.giveKeys(coachB.id)
        val codeA = world.todaysCode(coachA.id)
        val codeB = world.todaysCode(coachB.id)

        // Coach B's signature pasted into coach A's URI.
        assertFalse(world.verifyDailyCode(code = withSig(codeA, sigOf(codeB)), coachId = coachA.id))
        // Coach B's URI relabelled as coach A.
        assertFalse(world.verifyDailyCode(code = codeB.replace("coach=${coachB.id}", "coach=${coachA.id}"), coachId = coachA.id))
    }

    @Test
    fun AC29_malformed_codes_do_not_verify() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)

        for (garbage in listOf("", "hello", "https://example.com", "trxatt://v1/daily", "trxatt://v2/daily?coach=x&date=2026-09-28&sig=AAAA")) {
            assertFalse(world.verifyDailyCode(code = garbage, coachId = coach.id), "should not verify: '$garbage'")
        }
    }

    // ----- AC30 -----

    @Test
    fun AC30_after_rotation_only_the_current_signature_verifies() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)
        val older = world.todaysCode(coach.id)
        assertTrue(world.verifyDailyCode(code = older, coachId = coach.id))

        // The coach's key pair is rotated (e.g. reinstall); the next generate signs with the new key.
        world.giveKeys(coach.id)
        val current = world.todaysCode(coach.id)

        assertTrue(world.verifyDailyCode(code = current, coachId = coach.id))
        assertFalse(world.verifyDailyCode(code = older, coachId = coach.id))
    }

    @Test
    fun AC30_regenerating_without_rotation_still_verifies() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)

        val first = world.todaysCode(coach.id)
        val second = world.todaysCode(coach.id)

        assertTrue(world.verifyDailyCode(code = second, coachId = coach.id))
        if (first == second) assertTrue(world.verifyDailyCode(code = first, coachId = coach.id))
    }

    // ----- AC31 -----

    @Test
    fun AC31_generating_a_code_needs_only_the_key_coach_id_and_date_and_works_offline() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        world.network = NetworkStatus.OFFLINE
        val coach = world.coach()
        world.giveKeys(coach.id)

        // GenerateDailyCode depends only on the key store: no network, clock, or other repository.
        val offlineGenerator = GenerateDailyCode(keys = world.coachKeys)
        val uri = offlineGenerator(coachId = coach.id, date = MON_28_SEP_2026)

        assertTrue(world.verifyDailyCode(code = uri, coachId = coach.id))
    }

    @Test
    fun AC31_codes_can_be_generated_for_any_date_without_the_clock() {
        val world = TestWorld(start = MON_28_SEP_2026.atTime(17, 0))
        val coach = world.coach()
        world.giveKeys(coach.id)

        val wednesdayCode = world.generateDailyCode(coachId = coach.id, date = WED_30_SEP_2026)
        assertEquals("2026-09-30", uriPattern.matchEntire(wednesdayCode)!!.groupValues[2])

        world.setLocal(WED_30_SEP_2026, 8)
        assertTrue(world.verifyDailyCode(code = wednesdayCode, coachId = coach.id))
    }
}
