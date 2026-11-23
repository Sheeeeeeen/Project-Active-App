package ph.project.active.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxTypography

/** Eyebrow "TRX ATTENDANCE" with the account button on the right, above a large title. */
@Composable
fun LargeTitleHeader(
    title: String,
    titleTag: String,
    accountTag: String?,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    LargeTitleTopBar(accountTag = accountTag, modifier = modifier, action = action)
    LargeTitle(title = title, tag = titleTag)
}

@Composable
fun LargeTitle(title: String, tag: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = LocalTrxColors.current.textPrimary,
        style = TrxTypography.largeTitle,
        modifier = modifier.testTag(tag),
    )
}
