package ph.project.active.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ph.project.active.ui.theme.LocalTrxColors

internal data class TrxButtonColors(val background: Color, val content: Color, val border: Color)

@Composable
internal fun trxButtonColors(kind: TrxButtonKind, enabled: Boolean): TrxButtonColors {
    val colors = LocalTrxColors.current
    return when {
        !enabled && kind != TrxButtonKind.Secondary -> TrxButtonColors(colors.surfaceAlt, colors.textTertiary, Color.Transparent)
        kind == TrxButtonKind.Primary -> TrxButtonColors(colors.accentDefault, colors.textOnAccent, Color.Transparent)
        kind == TrxButtonKind.Danger -> TrxButtonColors(colors.dangerBg, colors.dangerFg, Color.Transparent)
        else -> TrxButtonColors(colors.surface, colors.textPrimary, colors.borderStrong)
    }
}
