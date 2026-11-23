package ph.project.active.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.circle_user

/** Eyebrow "TRX ATTENDANCE", an optional action, and the account button on the right. */
@Composable
fun LargeTitleTopBar(
    accountTag: String?,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = LocalTrxColors.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(top = TrxSpacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = UiCopy.largeTitleEyebrow(),
            color = colors.accentText,
            style = TrxTypography.overline,
            modifier = Modifier.weight(1f),
        )
        action?.invoke()
        // Placeholder: the account button does nothing yet and wears the PlaceholderTag beside it.
        // An account menu (sign-out, profile) replaces it in a later flow (not yet planned).
        Box(if (accountTag != null) Modifier.testTag(accountTag) else Modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlaceholderTag(Modifier.padding(end = TrxSpacing.s8))
                IconButton(
                    onClick = {},
                    modifier = Modifier.background(colors.surfaceAlt, CircleShape),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.circle_user),
                        contentDescription = UiCopy.account(),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(TrxSpacing.iconMedium),
                    )
                }
            }
        }
    }
}
