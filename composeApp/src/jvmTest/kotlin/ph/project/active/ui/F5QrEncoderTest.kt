package ph.project.active.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import ph.project.active.domain.model.DailyCodeCheck
import ph.project.active.ui.dailyqr.QrEncoder
import ph.project.active.ui.dailyqr.QrMatrix
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.decodeQr

/**
 * Flow 5 F5-AC3 and Planner ruling 2: `QrEncoder.encode(text): QrMatrix` holds `DailyQrUiState.code`
 * exactly, with dark modules on light and a blank quiet zone. zxing (test-only) reads the matrix back.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class F5QrEncoderTest {

    // F5-AC3: "The QR encodes DailyQrUiState.code exactly … A test passes state.code into the encoder … and gets that same string."
    @Test
    fun F5_AC3_theMatrixDecodesToTheViewModelsCodeExactly() = withMain {
        Flow4Harness().use { world ->
            val code = world.dailyQr(world.maraAccount()).state.value.code

            assertEquals(code, decodeQr(QrEncoder.encode(code)))
        }
    }

    // F5-AC3: "the GenerateDailyCode URI for this coach and today" (decision 12: trxatt://v1/daily?coach={id}&date=YYYY-MM-DD&sig={base64url}; ruling: no class id)
    @Test
    fun F5_AC3_theCodeIsTheDailyUriForThisCoachAndToday() = withMain {
        Flow4Harness().use { world ->
            val mara = world.mara()
            val code = world.dailyQr(world.maraAccount()).state.value.code

            val prefix = "trxatt://v1/daily?coach=${mara.id}&date=2026-09-28&sig="
            assertTrue(code.startsWith(prefix), "expected $prefix…, got $code")
            assertTrue(Regex("[A-Za-z0-9_-]+=*").matches(code.removePrefix(prefix)), "sig is base64url: $code")
            assertFalse(code.contains("class", ignoreCase = true), "one code for all classes, no class id: $code")
        }
    }

    // F5-AC3: "VerifyDailyCode on that string for this coach and today is VALID."
    @Test
    fun F5_AC3_theDecodedStringVerifiesForThisCoachToday() = withMain {
        Flow4Harness().use { world ->
            val mara = world.mara()
            val code = world.dailyQr(world.maraAccount()).state.value.code

            val decoded = decodeQr(QrEncoder.encode(code))
            assertEquals(DailyCodeCheck.VALID, world.verifyDailyCode().check(decoded, mara.id))
        }
    }

    // F5-AC3 / ruling 2: "black on white with a blank quiet-zone border" (read as the standard 4-module quiet zone)
    @Test
    fun F5_AC3_theMatrixHasDarkModulesInsideABlankQuietZone() = withMain {
        Flow4Harness().use { world ->
            val matrix = QrEncoder.encode(world.dailyQr(world.maraAccount()).state.value.code)

            assertTrue(matrix.size >= 21 + 2 * QUIET_ZONE, "a version 1+ symbol plus the quiet zone, got ${matrix.size}")
            val border = (0 until matrix.size).flatMap { a -> (0 until matrix.size).map { b -> a to b } }
                .filter { (x, y) -> x < QUIET_ZONE || y < QUIET_ZONE || x >= matrix.size - QUIET_ZONE || y >= matrix.size - QUIET_ZONE }
            assertTrue(border.none { (x, y) -> matrix.isDark(x, y) }, "the quiet zone is blank (light)")
            assertTrue(darkCount(matrix) > 0, "the symbol has dark modules")
        }
    }

    // F5-AC3 / ruling 2: "same input string" — the encoder is a pure function of its text
    @Test
    fun F5_AC3_theSameTextGivesTheSameMatrixAndOtherTextDecodesToo() {
        val uri = "trxatt://v1/daily?coach=coach-1&date=2026-09-28&sig=abc_DEF-123"
        val first = QrEncoder.encode(uri)
        val second = QrEncoder.encode(uri)
        assertEquals(first.size, second.size)
        assertTrue((0 until first.size).all { x -> (0 until first.size).all { y -> first.isDark(x, y) == second.isDark(x, y) } })
        assertEquals(uri, decodeQr(first))
        assertEquals("Valid Mon 28 Sep 2026 only", decodeQr(QrEncoder.encode("Valid Mon 28 Sep 2026 only")))
    }

    private fun darkCount(matrix: QrMatrix): Int =
        (0 until matrix.size).sumOf { x -> (0 until matrix.size).count { y -> matrix.isDark(x, y) } }

    /** DailyQrViewModel watches the network on viewModelScope, so a Main dispatcher is needed outside Compose. */
    private fun withMain(block: () -> Unit) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }
}

/** The standard QR quiet zone, in modules. */
private const val QUIET_ZONE = 4
