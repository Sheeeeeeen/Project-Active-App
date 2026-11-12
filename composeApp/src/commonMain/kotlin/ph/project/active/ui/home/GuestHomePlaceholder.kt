package ph.project.active.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun GuestHomePlaceholder(onRegisterOrSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Text(
        text = UiCopy.registerOrSignIn(),
        color = colors.textPrimary,
        style = TrxTypography.button,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(TrxSpacing.screenHorizontal)
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .testTag(HomeTestTags.guestRegister)
            .clickable(onClick = onRegisterOrSignIn)
            .semantics { role = Role.Button }
            .padding(vertical = TrxSpacing.s12),
    )
}
