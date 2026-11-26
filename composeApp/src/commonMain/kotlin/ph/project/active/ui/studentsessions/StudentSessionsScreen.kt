package ph.project.active.ui.studentsessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.studentsessions.StudentSessionUiRow
import ph.project.active.presentation.studentsessions.StudentSessionsUiState
import ph.project.active.ui.components.LargeTitle
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.circle_check
import projectactive.composeapp.generated.resources.circle_x
import projectactive.composeapp.generated.resources.hourglass

@Composable
fun StudentSessionsScreen(
    state: StudentSessionsUiState,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    Scaffold(
        modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom),
        bottomBar = tabBar,
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .testTag(StudentTabBarTestTags.sessionsRoot),
        ) {
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
                Text(
                    text = UiCopy.largeTitleEyebrow(),
                    color = colors.accentText,
                    style = TrxTypography.overline,
                    modifier = Modifier.padding(top = TrxSpacing.s8).testTag(StudentSessionsTestTags.overline),
                )
                LargeTitle(title = UiCopy.tabSessions(), tag = StudentTabBarTestTags.sessionsTitle)
                Text(
                    text = UiCopy.sessionsSubtitle(),
                    color = colors.textSecondary,
                    style = TrxTypography.subhead,
                    modifier = Modifier.testTag(StudentSessionsTestTags.subtitle),
                )
            }
            state.connection.banner?.let { banner ->
                OfflineBanner(banner, Modifier.padding(top = TrxSpacing.s16))
            }
            if (state.isEmpty) {
                Column(Modifier.padding(TrxSpacing.screenHorizontal).padding(top = TrxSpacing.s32).testTag(StudentSessionsTestTags.empty)) {
                    Text(UiCopy.noSessionsYetTitle(), color = colors.textPrimary, style = TrxTypography.sectionTitle)
                    Text(
                        text = UiCopy.noSessionsYetBody(),
                        color = colors.textSecondary,
                        style = TrxTypography.subhead,
                        modifier = Modifier.padding(top = TrxSpacing.s8),
                    )
                }
            } else {
                SessionGroup(UiCopy.upcoming(), StudentSessionsTestTags.groupUpcoming, state.upcoming, showToday = false)
                val todayHeader = state.today.firstOrNull()?.let { UiCopy.sessionsTodayHeader(it.date) }
                if (todayHeader != null) {
                    SessionGroup(todayHeader, StudentSessionsTestTags.groupToday, state.today, showToday = true)
                }
                SessionGroup(UiCopy.earlier(), StudentSessionsTestTags.groupEarlier, state.earlier, showToday = false)
            }
        }
    }
}

@Composable
private fun SessionGroup(title: String, tag: String, rows: List<StudentSessionUiRow>, showToday: Boolean) {
    if (rows.isEmpty()) return
    val colors = LocalTrxColors.current
    Text(
        text = title,
        color = colors.textPrimary,
        style = TrxTypography.sectionTitle,
        modifier = Modifier
            .padding(horizontal = TrxSpacing.screenHorizontal)
            .padding(top = TrxSpacing.s24, bottom = TrxSpacing.s8)
            .testTag(tag),
    )
    rows.forEach { row -> SessionRow(row, showToday) }
}

@Composable
private fun SessionRow(row: StudentSessionUiRow, showToday: Boolean) {
    val colors = LocalTrxColors.current
    val cancelled = row.status == StudentSessionStatus.CANCELLED
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s8)
            .testTag(StudentSessionsTestTags.row(row.sessionId))
            .semantics { if (cancelled) set(SessionsRowCancelled, true) },
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            Modifier
                .width(52.dp)
                .background(colors.surfaceAlt, RoundedCornerShape(TrxSpacing.radiusSm))
                .padding(vertical = TrxSpacing.s8)
                .alpha(if (cancelled) 0.5f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(UiCopy.dateTileWeekday(row.date), color = colors.textTertiary, style = TrxTypography.overline)
            Text(row.date.day.toString(), color = colors.textPrimary, style = TrxTypography.sectionTitle, textAlign = TextAlign.Center)
            Text(UiCopy.dateTileMonth(row.date), color = colors.textTertiary, style = TrxTypography.overline)
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = TrxSpacing.s12)
                .alpha(if (cancelled) 0.5f else 1f),
        ) {
            Text(row.className, color = colors.textPrimary, style = TrxTypography.headline)
            if (showToday) {
                Text(
                    text = UiCopy.today(),
                    color = colors.accentText,
                    style = TrxTypography.overline,
                    modifier = Modifier.padding(top = TrxSpacing.s4).testTag(StudentSessionsTestTags.todayTag),
                )
            }
            Text(
                text = UiCopy.sessionRowLine(row.sessionNumber, row.slot.start),
                color = colors.textSecondary,
                style = TrxTypography.subhead,
                modifier = Modifier.padding(top = TrxSpacing.s4),
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            StatusChip(statusLabel(row.status), statusIcon(row.status), statusBackground(row.status), statusContent(row.status))
            if (row.uploadPending && row.status == StudentSessionStatus.PRESENT) {
                StatusChip(
                    UiCopy.uploadPending(),
                    Res.drawable.hourglass,
                    colors.warningBg,
                    colors.warningFg,
                    Modifier.padding(top = TrxSpacing.s4),
                )
            }
        }
    }
}

@Composable
private fun statusLabel(status: StudentSessionStatus): String = when (status) {
    StudentSessionStatus.UPCOMING -> UiCopy.upcoming()
    StudentSessionStatus.PRESENT -> UiCopy.present()
    StudentSessionStatus.ABSENT -> UiCopy.absent()
    StudentSessionStatus.CANCELLED -> UiCopy.cancelled()
}

@Composable
private fun statusIcon(status: StudentSessionStatus): DrawableResource = when (status) {
    StudentSessionStatus.UPCOMING -> Res.drawable.calendar_days
    StudentSessionStatus.PRESENT -> Res.drawable.circle_check
    StudentSessionStatus.ABSENT -> Res.drawable.circle_x
    StudentSessionStatus.CANCELLED -> Res.drawable.ban
}

@Composable
private fun statusBackground(status: StudentSessionStatus) = when (status) {
    StudentSessionStatus.UPCOMING -> LocalTrxColors.current.infoBg
    StudentSessionStatus.PRESENT -> LocalTrxColors.current.successBg
    StudentSessionStatus.ABSENT -> LocalTrxColors.current.dangerBg
    StudentSessionStatus.CANCELLED -> LocalTrxColors.current.surfaceAlt
}

@Composable
private fun statusContent(status: StudentSessionStatus) = when (status) {
    StudentSessionStatus.UPCOMING -> LocalTrxColors.current.infoFg
    StudentSessionStatus.PRESENT -> LocalTrxColors.current.successFg
    StudentSessionStatus.ABSENT -> LocalTrxColors.current.dangerFg
    StudentSessionStatus.CANCELLED -> LocalTrxColors.current.textSecondary
}
