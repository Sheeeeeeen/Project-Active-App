package ph.project.active.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.theme.TrxColors

class F1ThemeTest {
    @Test
    fun F1_AC9_lightAndDarkSemanticColorsMatchFigmaNotes() {
        assertEquals(Color(0xFFF8F8F9), TrxColors.Light.canvas)
        assertEquals(Color(0xFFFFFFFF), TrxColors.Light.surface)
        assertEquals(Color(0xFFF1F1F3), TrxColors.Light.surfaceAlt)
        assertEquals(Color(0xFFE3E3E6), TrxColors.Light.borderDefault)
        assertEquals(Color(0xFFC4C4CA), TrxColors.Light.borderStrong)
        assertEquals(Color(0xFF0B0B0C), TrxColors.Light.textPrimary)
        assertEquals(Color(0xFF45454D), TrxColors.Light.textSecondary)
        assertEquals(Color(0xFF63636B), TrxColors.Light.textTertiary)
        assertEquals(Color(0xFFFFC20E), TrxColors.Light.accentDefault)
        assertEquals(Color(0xFF7A5A00), TrxColors.Light.accentText)
        assertEquals(Color(0xFFFFF4CC), TrxColors.Light.accentSubtle)
        assertEquals(Color(0xFFB42318), TrxColors.Light.dangerFg)
        assertEquals(Color(0xFF1F57C3), TrxColors.Light.focusRing)

        assertEquals(Color(0xFF0B0B0C), TrxColors.Dark.canvas)
        assertEquals(Color(0xFF141416), TrxColors.Dark.surface)
        assertEquals(Color(0xFF1F1F23), TrxColors.Dark.surfaceAlt)
        assertEquals(Color(0xFF2C2C31), TrxColors.Dark.borderDefault)
        assertEquals(Color(0xFF45454D), TrxColors.Dark.borderStrong)
        assertEquals(Color(0xFFFFFFFF), TrxColors.Dark.textPrimary)
        assertEquals(Color(0xFFC4C4CA), TrxColors.Dark.textSecondary)
        assertEquals(Color(0xFF8E8E96), TrxColors.Dark.textTertiary)
        assertEquals(Color(0xFFFFC20E), TrxColors.Dark.accentDefault)
        assertEquals(Color(0xFFFFD54A), TrxColors.Dark.accentText)
        assertEquals(Color(0xFF3A2E06), TrxColors.Dark.accentSubtle)
        assertEquals(Color(0xFFFF7A7A), TrxColors.Dark.dangerFg)
        assertEquals(Color(0xFFFFD54A), TrxColors.Dark.focusRing)
    }
}
