package ph.project.active.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

/** A destructive confirm: two equal buttons, an outlined "keep" and a Danger confirm. Dismissing keeps. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    title: String,
    body: String,
    keepLabel: String,
    confirmLabel: String,
    onKeep: () -> Unit,
    onConfirm: () -> Unit,
    tag: String,
    keepTag: String,
    confirmTag: String,
) {
    val colors = LocalTrxColors.current
    BasicAlertDialog(onDismissRequest = onKeep) {
        Surface(
            modifier = Modifier
                .testTag(tag)
                .semantics { paneTitle = title },
            shape = RoundedCornerShape(TrxSpacing.radiusLg),
            color = colors.surface,
        ) {
            Column(Modifier.padding(TrxSpacing.s24)) {
                Text(title, color = colors.textPrimary, style = TrxTypography.sectionTitle)
                Text(
                    text = body,
                    color = colors.textSecondary,
                    style = TrxTypography.subhead,
                    modifier = Modifier.padding(top = TrxSpacing.s8),
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = TrxSpacing.s20),
                    horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s12),
                ) {
                    TrxLargeButton(
                        label = keepLabel,
                        kind = TrxButtonKind.Secondary,
                        tag = keepTag,
                        onClick = onKeep,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        height = TrxSpacing.touchTarget,
                        textStyle = TrxTypography.subheadStrong,
                    )
                    TrxLargeButton(
                        label = confirmLabel,
                        kind = TrxButtonKind.Danger,
                        tag = confirmTag,
                        onClick = onConfirm,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        height = TrxSpacing.touchTarget,
                        textStyle = TrxTypography.subheadStrong,
                    )
                }
            }
        }
    }
}
