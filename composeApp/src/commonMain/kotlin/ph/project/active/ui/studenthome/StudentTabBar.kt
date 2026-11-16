package ph.project.active.ui.studenthome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.dumbbell
import projectactive.composeapp.generated.resources.mail

object StudentTabBarTestTags {
    const val classes = "student_tab_classes"
    const val attend = "student_tab_attend"
    const val sessions = "student_tab_sessions"
    const val invites = "student_tab_invites"
    const val invitesBadge = "student_tab_invites_badge"
    const val attendRoot = "student_attend"
    const val attendTitle = "student_attend_title"
    const val sessionsRoot = "student_sessions"
    const val sessionsTitle = "student_sessions_title"
}

@Composable
fun StudentTabBar(
    selected: StudentTab,
    pendingInviteCount: Int,
    onSelect: (StudentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    Column(modifier) {
        HorizontalDivider(color = colors.borderDefault)
        NavigationBar(containerColor = colors.surface) {
            StudentTab.entries.forEach { tab -> TabItem(tab, tab == selected, pendingInviteCount, onSelect) }
        }
    }
}

@Composable
private fun RowScope.TabItem(
    tab: StudentTab,
    selected: Boolean,
    pendingInviteCount: Int,
    onSelect: (StudentTab) -> Unit,
) {
    val colors = LocalTrxColors.current
    NavigationBarItem(
        selected = selected,
        onClick = { onSelect(tab) },
        icon = {
            val icon = @Composable {
                Icon(painterResource(tab.icon()), contentDescription = null)
            }
            if (tab == StudentTab.Invites && pendingInviteCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            modifier = Modifier.testTag(StudentTabBarTestTags.invitesBadge),
                            containerColor = colors.dangerFg,
                            contentColor = colors.textInverse,
                        ) {
                            Text(pendingInviteCount.toString())
                        }
                    },
                ) { icon() }
            } else {
                icon()
            }
        },
        label = {
            Text(
                text = tab.label(),
                style = TrxTypography.label,
                fontWeight = if (selected) FontWeight.SemiBold else null,
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

private fun StudentTab.label(): String = when (this) {
    StudentTab.Classes -> UiCopy.tabClasses()
    StudentTab.Attend -> UiCopy.tabAttend()
    StudentTab.Sessions -> UiCopy.tabSessions()
    StudentTab.Invites -> UiCopy.tabInvites()
}

private fun StudentTab.tag(): String = when (this) {
    StudentTab.Classes -> StudentTabBarTestTags.classes
    StudentTab.Attend -> StudentTabBarTestTags.attend
    StudentTab.Sessions -> StudentTabBarTestTags.sessions
    StudentTab.Invites -> StudentTabBarTestTags.invites
}

private fun StudentTab.icon(): DrawableResource = when (this) {
    StudentTab.Classes -> Res.drawable.dumbbell
    StudentTab.Attend -> Res.drawable.calendar_check
    StudentTab.Sessions -> Res.drawable.calendar_days
    StudentTab.Invites -> Res.drawable.mail
}
