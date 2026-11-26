package ph.project.active.ui

import androidx.compose.ui.graphics.PixelMap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.argb
import ph.project.active.ui.testing.argbAt
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.screenPixels
import ph.project.active.ui.testing.seedD19
import ph.project.active.ui.testing.tag
import ph.project.active.ui.theme.TrxColors

/**
 * Flow 5 decision 4 and F5-AC4: with the app in light mode, the QR tab still draws dark.
 * These sample colours only; the QR's content is checked as text in F5QrEncoderTest.
 * (`LightStatusBarIcons(lightIcons = true)` is a no-op on the desktop test host, so it is a source scan in F5StructureTest.)
 */
@OptIn(ExperimentalTestApi::class)
class F5AlwaysDarkQrTest {

    // F5-AC4: "With the app in light mode, the QR tab still uses the dark canvas (TrxColors.Dark.canvas)"
    @Test
    fun F5_AC4_theQrTabUsesTheDarkCanvasInLightMode() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            val pixels = screenPixels()
            val title = tag("coach_qr_title").fetchSemanticsNode().boundsInRoot
            // Right of the title row: D19 has no account icon there, so it is bare canvas.
            assertEquals(TrxColors.Dark.canvas.argb(), pixels.argbAt(pixels.width - 4f, title.center.y), "canvas beside the title")
        }
    }

    // F5-AC4: "light text"
    @Test
    fun F5_AC4_theTitleTextIsLight() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            val pixels = screenPixels()
            val title = tag("coach_qr_title").fetchSemanticsNode().boundsInRoot
            assertTrue(pixels.hasPixelIn(title.left, title.top, title.right, title.bottom) { it.isNear(TrxColors.Dark.textPrimary.argb()) }, "the title is drawn in the dark scheme's light text colour")
        }
    }

    // F5-AC4: "and a dark tab bar"
    @Test
    fun F5_AC4_theTabBarIsDark() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            val pixels = screenPixels()
            val tab = tag("coach_tab_attendance").fetchSemanticsNode().boundsInRoot
            val darkTokens = listOf(TrxColors.Dark.canvas, TrxColors.Dark.surface, TrxColors.Dark.surfaceAlt).map { it.argb() }
            val sample = pixels.argbAt(tab.left + 3f, tab.top + 3f)
            assertTrue(sample in darkTokens, "tab bar background ${Integer.toHexString(sample)} is a dark token")
        }
    }

    // F5-AC4 / decision 4: "The QR modules stay #0B0B0C on #FFFFFF."
    @Test
    fun F5_AC4_theQrIsNearBlackModulesOnAWhiteCard() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            val pixels = screenPixels()
            val card = tag("coach_qr_card").fetchSemanticsNode().boundsInRoot
            val module = 0xFF0B0B0C.toInt()
            val white = 0xFFFFFFFF.toInt()
            assertEquals(white, pixels.argbAt(card.center.x, card.top + 2f), "the card is white")
            assertTrue(pixels.countIn(card.left, card.top, card.right, card.bottom) { it == module } >= 100, "the modules are #0B0B0C")
        }
    }

    private fun PixelMap.hasPixelIn(left: Float, top: Float, right: Float, bottom: Float, test: (Int) -> Boolean): Boolean =
        countIn(left, top, right, bottom, test) > 0

    private fun PixelMap.countIn(left: Float, top: Float, right: Float, bottom: Float, test: (Int) -> Boolean): Int {
        var count = 0
        for (x in left.toInt().coerceAtLeast(0) until right.toInt().coerceAtMost(width)) {
            for (y in top.toInt().coerceAtLeast(0) until bottom.toInt().coerceAtMost(height)) {
                if (test(this[x, y].toArgb())) count++
            }
        }
        return count
    }

    private fun Int.isNear(other: Int): Boolean {
        val channels = listOf(16, 8, 0)
        return channels.all { shift -> abs((this shr shift and 0xFF) - (other shr shift and 0xFF)) <= 8 }
    }
}
