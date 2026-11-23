package ph.project.active.ui.manageclass

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban

/** D17: the only class option is cancelling the whole class. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClassOptionsSheet(onCancelWholeClass: () -> Unit, onDismiss: () -> Unit) {
    val colors = LocalTrxColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
            Text(UiCopy.classOptions(), color = colors.textPrimary, style = TrxTypography.sectionTitle)
            Row(
                Modifier
                    .padding(top = TrxSpacing.s16)
                    .fillMaxWidth()
                    .heightIn(min = TrxSpacing.touchTarget)
                    .clickable(role = Role.Button) {
                        onDismiss()
                        onCancelWholeClass()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ban),
                    contentDescription = null,
                    tint = colors.dangerFg,
                    modifier = Modifier
                        .padding(end = TrxSpacing.s12)
                        .size(TrxSpacing.iconMedium),
                )
                Text(UiCopy.cancelWholeClass(), color = colors.dangerFg, style = TrxTypography.body)
            }
        }
    }
}
