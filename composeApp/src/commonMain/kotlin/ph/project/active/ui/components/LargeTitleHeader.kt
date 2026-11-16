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
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.user

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
            text = UiCopy.registrationOverline(Role.STUDENT),
            color = colors.accentText,
            style = TrxTypography.overline,
            modifier = Modifier.weight(1f),
        )
        // The account button has no action until sign-out and the account screen arrive.
        IconButton(
            onClick = {},
            modifier = Modifier
                .background(colors.surfaceAlt, CircleShape)
                .let { if (accountTag != null) it.testTag(accountTag) else it },
        ) {
            Icon(
                painter = painterResource(Res.drawable.user),
                contentDescription = UiCopy.account(),
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
    Text(
        text = title,
        color = colors.textPrimary,
        style = TrxTypography.title,
        modifier = Modifier.testTag(titleTag),
    )
}
