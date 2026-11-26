package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.presentation.attend.looksLikeAttendanceCode
import ph.project.active.testkit.Flow7Example
import ph.project.active.testkit.Phase2World

/** Flow 7 spec S12 and decision 6: other QR codes are ignored before they reach onCodeScanned. F7-AC13. */
class F7ScanTextTest {

    // F7-AC13: "looksLikeAttendanceCode is true for Mara's code, "  trxatt://v1/daily?x" and any trxatt:// text."
    @Test
    fun F7_AC13_trxattTextLooksLikeAnAttendanceCode() {
        Phase2World().use { world ->
            assertTrue(looksLikeAttendanceCode(Flow7Example(world).marasCode))
        }
        assertTrue(looksLikeAttendanceCode("  trxatt://v1/daily?x"))
        assertTrue(looksLikeAttendanceCode("trxatt://"))
        assertTrue(looksLikeAttendanceCode("trxatt://anything else"))
    }

    // F7-AC13: "It is false for "https://example.com", "" and "TRXATT://v1/daily"."
    @Test
    fun F7_AC13_otherTextDoesNotLookLikeAnAttendanceCode() {
        assertFalse(looksLikeAttendanceCode("https://example.com"))
        assertFalse(looksLikeAttendanceCode(""))
        assertFalse(looksLikeAttendanceCode("TRXATT://v1/daily"))
    }
}
