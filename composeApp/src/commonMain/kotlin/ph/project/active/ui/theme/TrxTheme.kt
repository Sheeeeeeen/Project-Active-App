package ph.project.active.ui.theme

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TrxTheme(
    dark: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = if (dark) TrxColors.Dark else TrxColors.Light
    val selection = TextSelectionColors(
        handleColor = colors.accentText,
        backgroundColor = colors.accentText.copy(alpha = 0.4f),
    )
    CompositionLocalProvider(LocalTrxColors provides colors) {
        MaterialTheme(colorScheme = colors.toColorScheme(dark)) {
            CompositionLocalProvider(LocalTextSelectionColors provides selection, content = content)
        }
    }
}

// Material components we don't style by hand (the Create date and time pickers) take TRX colours from here.
private fun TrxColors.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accentDefault,
        onPrimary = textOnAccent,
        primaryContainer = accentSubtle,
        onPrimaryContainer = textPrimary,
        secondary = textSecondary,
        onSecondary = surface,
        secondaryContainer = surfaceAlt,
        onSecondaryContainer = textPrimary,
        tertiary = infoFg,
        onTertiary = surface,
        tertiaryContainer = infoBg,
        onTertiaryContainer = infoFg,
        background = canvas,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceAlt,
        onSurfaceVariant = textSecondary,
        surfaceTint = surface,
        inverseSurface = surfaceInverse,
        inverseOnSurface = textInverse,
        inversePrimary = accentText,
        error = dangerFg,
        onError = surface,
        errorContainer = dangerBg,
        onErrorContainer = dangerFg,
        outline = borderStrong,
        outlineVariant = borderDefault,
        surfaceBright = surface,
        surfaceDim = canvas,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = surfaceAlt,
    )
}
