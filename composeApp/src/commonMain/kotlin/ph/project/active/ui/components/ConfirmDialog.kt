package ph.project.active.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxTypography

/** A destructive confirm: a safe "keep" button and a red confirm button. Dismissing keeps. */
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
    AlertDialog(
        onDismissRequest = onKeep,
        modifier = Modifier.testTag(tag),
        containerColor = colors.surface,
        title = { Text(title, color = colors.textPrimary, style = TrxTypography.sectionTitle) },
        text = { Text(body, color = colors.textSecondary, style = TrxTypography.body) },
        dismissButton = {
            TextButton(onClick = onKeep, modifier = Modifier.testTag(keepTag)) {
                Text(keepLabel, color = colors.textPrimary, style = TrxTypography.button)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.testTag(confirmTag)) {
                Text(confirmLabel, color = colors.dangerFg, style = TrxTypography.button)
            }
        },
    )
}
