package ph.project.active.ui.coachhome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.components.LargeTitle
import ph.project.active.ui.components.LargeTitleTopBar
import ph.project.active.ui.components.PlaceholderTag
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing

/** The coach Attendance tab: large-title chrome, the tab bar, and only the placeholder tag. */
@Composable
fun CoachTabPlaceholder(
    title: String,
    rootTag: String,
    titleTag: String,
    accountTag: String,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Placeholder: coach attendance (D21–D22) replaces the Attendance tab in flow 6.
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
                .padding(horizontal = TrxSpacing.screenHorizontal),
        ) {
            LargeTitleTopBar(accountTag = accountTag)
            // The root tag holds the title and the body's own tag, not the account button's.
            Column(
                Modifier
                    .fillMaxWidth()
                    .testTag(rootTag),
            ) {
                LargeTitle(title = title, tag = titleTag)
                PlaceholderTag(Modifier.padding(top = TrxSpacing.s16))
            }
        }
    }
}
