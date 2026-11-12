package ph.project.active.ui.theme

import androidx.compose.ui.graphics.Color

data class TrxColors(
    val canvas: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentDefault: Color,
    val accentText: Color,
    val accentSubtle: Color,
    val dangerFg: Color,
    val focusRing: Color,
) {
    companion object {
        val Light = TrxColors(
            canvas = Color(0xFFF8F8F9),
            surface = Color(0xFFFFFFFF),
            surfaceAlt = Color(0xFFF1F1F3),
            borderDefault = Color(0xFFE3E3E6),
            borderStrong = Color(0xFFC4C4CA),
            textPrimary = Color(0xFF0B0B0C),
            textSecondary = Color(0xFF45454D),
            textTertiary = Color(0xFF63636B),
            accentDefault = Color(0xFFFFC20E),
            accentText = Color(0xFF7A5A00),
            accentSubtle = Color(0xFFFFF4CC),
            dangerFg = Color(0xFFB42318),
            focusRing = Color(0xFF1F57C3),
        )

        val Dark = TrxColors(
            canvas = Color(0xFF0B0B0C),
            surface = Color(0xFF141416),
            surfaceAlt = Color(0xFF1F1F23),
            borderDefault = Color(0xFF2C2C31),
            borderStrong = Color(0xFF45454D),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFFC4C4CA),
            textTertiary = Color(0xFF8E8E96),
            accentDefault = Color(0xFFFFC20E),
            accentText = Color(0xFFFFD54A),
            accentSubtle = Color(0xFF3A2E06),
            dangerFg = Color(0xFFFF7A7A),
            focusRing = Color(0xFFFFD54A),
        )
    }
}
