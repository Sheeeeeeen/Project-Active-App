package ph.project.active.ui

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.theme.TrxColors
import ph.project.active.ui.theme.TrxTheme

/** Flow 6 decision 19 and F6-AC22: text selection uses accentText, not yellow on white. */
@OptIn(ExperimentalTestApi::class)
class F6TextSelectionColorsTest {

    // F6-AC22: "handleColor = the theme’s accentText and backgroundColor = accentText at 40% alpha, in light mode (TrxColors.Light.accentText)"
    @Test
    fun F6_AC22_lightModeSelectionUsesTheLightAccentText() = runFlow5UiTest {
        val accent = TrxColors.Light.accentText
        assertEquals(TextSelectionColors(handleColor = accent, backgroundColor = accent.copy(alpha = 0.4f)), selectionColors(dark = false))
    }

    // F6-AC22: "… and in dark mode (TrxColors.Dark.accentText)"
    @Test
    fun F6_AC22_darkModeSelectionUsesTheDarkAccentText() = runFlow5UiTest {
        val accent = TrxColors.Dark.accentText
        assertEquals(TextSelectionColors(handleColor = accent, backgroundColor = accent.copy(alpha = 0.4f)), selectionColors(dark = true))
    }

    private fun androidx.compose.ui.test.ComposeUiTest.selectionColors(dark: Boolean): TextSelectionColors {
        var seen: TextSelectionColors? = null
        setContent {
            TrxTheme(dark = dark) {
                seen = LocalTextSelectionColors.current
            }
        }
        waitForIdle()
        return checkNotNull(seen)
    }
}
