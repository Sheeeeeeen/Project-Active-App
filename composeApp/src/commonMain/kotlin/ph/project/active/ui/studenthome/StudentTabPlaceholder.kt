package ph.project.active.ui.studenthome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.components.LargeTitleHeader
import ph.project.active.ui.components.PlaceholderTag
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing

/** The Attend and Sessions tabs: large-title chrome, the tab bar, and only the placeholder tag. */
@Composable
fun StudentTabPlaceholder(
    title: String,
    rootTag: String,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Placeholder: the real Attend (C6–C11) and Sessions (C5, C12–C15) screens replace this in flow 5.
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    Scaffold(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom),
        bottomBar = tabBar,
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = TrxSpacing.screenHorizontal)
                .testTag(rootTag),
        ) {
            LargeTitleHeader(title = title, titleTag = "${rootTag}_title", accountTag = null)
            PlaceholderTag(Modifier.padding(top = TrxSpacing.s16))
        }
    }
}
