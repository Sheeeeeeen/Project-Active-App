package ph.project.active.ui.coachhome

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.calendar_check
import projectactive.composeapp.generated.resources.dumbbell
import projectactive.composeapp.generated.resources.qr_code

@Composable
fun CoachTabBar(selected: CoachTab, onSelect: (CoachTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Column(modifier) {
        HorizontalDivider(color = colors.borderDefault)
        NavigationBar(containerColor = colors.surface) {
            CoachTab.entries.forEach { tab ->
                val isSelected = tab == selected
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelect(tab) },
                    icon = { Icon(painterResource(tab.icon()), contentDescription = null) },
                    label = {
                        Text(
                            text = tab.label(),
                            style = TrxTypography.label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else null,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.textOnAccent,
                        selectedTextColor = colors.textPrimary,
                        indicatorColor = colors.accentDefault,
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary,
                    ),
                    modifier = Modifier.testTag(tab.tag()),
                )
            }
        }
    }
}

private fun CoachTab.label(): String = when (this) {
    CoachTab.Classes -> UiCopy.tabClasses()
    CoachTab.Qr -> UiCopy.tabQr()
    CoachTab.Attendance -> UiCopy.tabAttendance()
}

private fun CoachTab.tag(): String = when (this) {
    CoachTab.Classes -> CoachTabBarTestTags.classes
    CoachTab.Qr -> CoachTabBarTestTags.qr
    CoachTab.Attendance -> CoachTabBarTestTags.attendance
}

private fun CoachTab.icon(): DrawableResource = when (this) {
    CoachTab.Classes -> Res.drawable.dumbbell
    CoachTab.Qr -> Res.drawable.qr_code
    CoachTab.Attendance -> Res.drawable.calendar_check
}
