package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

/** A 40dp circle with up to two initials. Decorative: the name beside it is the accessible text. */
@Composable
fun InitialsAvatar(name: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Box(
        modifier
            .size(TrxSpacing.avatar)
            .background(colors.accentSubtle, CircleShape)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
            color = colors.accentText,
            style = TrxTypography.headline,
        )
    }
}
