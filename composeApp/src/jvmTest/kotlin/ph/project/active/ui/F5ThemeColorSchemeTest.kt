package ph.project.active.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.theme.TrxColors
import ph.project.active.ui.theme.TrxTheme

/** Flow 5 decision 17 and F5-AC15: TrxTheme hands Material a colour scheme built from the TRX tokens. */
@OptIn(ExperimentalTestApi::class)
class F5ThemeColorSchemeTest {

    // F5-AC15: "In light mode, scheme surface / background / primary / onPrimary / onSurface / error come from the light tokens"
    @Test
    fun F5_AC15_lightModeSchemeComesFromTheLightTokens() = runFlow5UiTest {
        val scheme = schemeOf(dark = false)
        assertSchemeMatches(scheme, TrxColors.Light)
    }

    // F5-AC15: "In dark mode they come from the dark tokens."
    @Test
    fun F5_AC15_darkModeSchemeComesFromTheDarkTokens() = runFlow5UiTest {
        val scheme = schemeOf(dark = true)
        assertSchemeMatches(scheme, TrxColors.Dark)
    }

    private fun ComposeUiTest.schemeOf(dark: Boolean): ColorScheme {
        var scheme: ColorScheme? = null
        setContent {
            TrxTheme(dark = dark) {
                scheme = MaterialTheme.colorScheme
            }
        }
        waitForIdle()
        return checkNotNull(scheme)
    }

    // "canvas or surface for backgrounds, accentDefault for primary, textOnAccent for onPrimary, textPrimary for onSurface, dangerFg for error"
    private fun assertSchemeMatches(scheme: ColorScheme, tokens: TrxColors) {
        val backgrounds = listOf(tokens.canvas, tokens.surface)
        assertTrue(scheme.surface in backgrounds, "surface ${scheme.surface} is canvas or surface")
        assertTrue(scheme.background in backgrounds, "background ${scheme.background} is canvas or surface")
        assertEquals(tokens.accentDefault, scheme.primary, "primary")
        assertEquals(tokens.textOnAccent, scheme.onPrimary, "onPrimary")
        assertEquals(tokens.textPrimary, scheme.onSurface, "onSurface")
        assertEquals(tokens.dangerFg, scheme.error, "error")
    }
}
