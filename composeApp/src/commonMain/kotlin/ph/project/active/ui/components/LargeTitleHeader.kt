package ph.project.active.ui.components

import androidx.compose.foundation.background
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

/** Eyebrow "TRX ATTENDANCE" with the account button on the right, above a large title. */
@Composable
fun LargeTitleHeader(
    title: String,
    titleTag: String,
    accountTag: String?,
    modifier: Modifier = Modifier,
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
        // Placeholder: the account button does nothing yet. An account menu (sign-out, profile) replaces
        // this no-op in a later flow (not yet planned). No PlaceholderTag here, because the spec keeps
        // Classes and Invites free of the tag (decision 3, F3-AC18).
        IconButton(
            onClick = {},
            modifier = Modifier
                .background(colors.surfaceAlt, CircleShape)
                .let { if (accountTag != null) it.testTag(accountTag) else it },
        ) {
            Icon(
                painter = painterResource(Res.drawable.circle_user),
                contentDescription = UiCopy.account(),
                tint = colors.textPrimary,
                modifier = Modifier.size(TrxSpacing.iconMedium),
            )
        }
    }
    Text(
        text = title,
        color = colors.textPrimary,
        style = TrxTypography.largeTitle,
        modifier = Modifier
            .padding(top = TrxSpacing.s8)
            .testTag(titleTag),
    )
}
