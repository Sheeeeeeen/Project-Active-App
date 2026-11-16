package ph.project.active.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography

@Composable
fun GuestHomePlaceholder(onRegisterOrSignIn: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Scaffold(
        modifier = modifier,
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { innerPadding ->
        Text(
            text = UiCopy.registerOrSignIn(),
            color = colors.textPrimary,
            style = TrxTypography.button,
            modifier = Modifier
                .padding(innerPadding)
                .padding(TrxSpacing.screenHorizontal)
                .fillMaxWidth()
                .heightIn(min = TrxSpacing.touchTarget)
                .testTag(HomeTestTags.guestRegister)
                .clickable(role = Role.Button, onClick = onRegisterOrSignIn)
                .padding(vertical = TrxSpacing.s12),
        )
    }
}
