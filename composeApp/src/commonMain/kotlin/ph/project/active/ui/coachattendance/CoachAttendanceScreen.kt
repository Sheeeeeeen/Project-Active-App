package ph.project.active.ui.coachattendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.CoachAttendanceStatus
import ph.project.active.presentation.coachattendance.CoachAttendanceSection
import ph.project.active.presentation.coachattendance.CoachAttendanceStudentRow
import ph.project.active.presentation.coachattendance.CoachAttendanceUiState
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.createclass.DayState
import ph.project.active.ui.coachhome.CoachTabBarTestTags
import ph.project.active.ui.components.InitialsAvatar
import ph.project.active.ui.components.LargeTitle
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.check
import projectactive.composeapp.generated.resources.chevron_left
import projectactive.composeapp.generated.resources.chevron_right
import projectactive.composeapp.generated.resources.circle_check
import projectactive.composeapp.generated.resources.circle_x

/** D21–D22: one day of the coach's sessions, who scanned, and who did not. */
@Composable
fun CoachAttendanceScreen(
    state: CoachAttendanceUiState,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                .verticalScroll(rememberScrollState())
                .padding(bottom = TrxSpacing.s16)
                .testTag(CoachTabBarTestTags.attendanceRoot),
        ) {
            Header()
            state.connection.banner?.let { banner ->
                OfflineBanner(banner, Modifier.padding(top = TrxSpacing.s12))
            }
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
                DateCard(state, onPreviousDay, onNextDay)
                Summary(state, Modifier.padding(top = TrxSpacing.s16))
                if (state.sections.isEmpty()) {
                    EmptyLine(Modifier.padding(top = TrxSpacing.s20))
                } else {
                    state.sections.forEach { section ->
                        SectionBlock(section, Modifier.padding(top = TrxSpacing.s12))
                    }
                }
            }
        }
    }
}

@Composable
private fun Header() {
    val colors = LocalTrxColors.current
    Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = TrxSpacing.s8)
                .heightIn(min = TrxSpacing.touchTarget),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = UiCopy.largeTitleEyebrow(),
                color = colors.accentText,
                style = TrxTypography.overline,
                modifier = Modifier.testTag(CoachAttendanceTestTags.overline),
            )
        }
        LargeTitle(title = UiCopy.tabAttendance(), tag = CoachTabBarTestTags.attendanceTitle)
    }
}

@Composable
private fun DateCard(state: CoachAttendanceUiState, onPreviousDay: () -> Unit, onNextDay: () -> Unit) {
    val colors = LocalTrxColors.current
    val dayLine = if (state.dayState == DayState.IN_PROGRESS) UiCopy.inProgress() else UiCopy.dayEnded()
    TrxCard(Modifier.padding(top = 14.dp), contentPadding = TrxSpacing.s0) {
        Row(Modifier.padding(horizontal = TrxSpacing.s4, vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
            DayChevron(Res.drawable.chevron_left, UiCopy.previousDay(), CoachAttendanceTestTags.prevDay, true, onPreviousDay)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDate(state.date),
                    color = colors.textPrimary,
                    style = TrxTypography.headline,
                    modifier = Modifier.testTag(CoachAttendanceTestTags.date),
                )
                Text(
                    text = dayLine,
                    color = colors.textSecondary,
                    style = TrxTypography.footnote,
                    modifier = Modifier.testTag(CoachAttendanceTestTags.dayState),
                )
            }
            DayChevron(
                Res.drawable.chevron_right,
                UiCopy.nextDay(),
                CoachAttendanceTestTags.nextDay,
                state.canGoNext,
                onNextDay,
            )
        }
    }
}

@Composable
private fun DayChevron(icon: DrawableResource, label: String, tag: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = LocalTrxColors.current
    Box(
        Modifier
            .size(TrxSpacing.touchTarget)
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button)
            .semantics { contentDescription = label }
            .testTag(tag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (enabled) colors.textPrimary else colors.textTertiary,
            modifier = Modifier.size(TrxSpacing.iconMedium),
        )
    }
}

@Composable
private fun Summary(state: CoachAttendanceUiState, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    val inProgress = state.dayState == DayState.IN_PROGRESS
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Count(UiCopy.present(), Res.drawable.circle_check, colors.successBg, colors.successFg, state.presentCount, CoachAttendanceTestTags.presentCount)
        if (inProgress) {
            val footnote = UiCopy.absentSetAfterDayEnds()
            Box(
                Modifier
                    .padding(start = TrxSpacing.s8)
                    .semantics {
                        this.text = AnnotatedString(footnote)
                        this.testTag = CoachAttendanceTestTags.footnote
                    },
            ) {
                Text(
                    text = footnote.keepLastWordTogether(),
                    color = colors.textTertiary,
                    style = TrxTypography.footnote,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        } else {
            Count(
                UiCopy.absent(),
                Res.drawable.circle_x,
                colors.dangerBg,
                colors.dangerFg,
                state.absentCount,
                CoachAttendanceTestTags.absentCount,
                Modifier.padding(start = TrxSpacing.s8),
            )
        }
    }
}

@Composable
private fun Count(
    label: String,
    icon: DrawableResource,
    background: Color,
    content: Color,
    count: Int,
    tag: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        StatusChip(label, icon, background, content)
        Text(
            text = count.toString(),
            color = colors.textSecondary,
            style = TrxTypography.footnote,
            modifier = Modifier
                .padding(start = TrxSpacing.s8)
                .testTag(tag),
        )
    }
}

@Composable
private fun EmptyLine(modifier: Modifier = Modifier) {
    Text(
        text = UiCopy.noSessionsOnDate(),
        color = LocalTrxColors.current.textSecondary,
        style = TrxTypography.body,
        modifier = modifier.testTag(CoachAttendanceTestTags.empty),
    )
}

@Composable
private fun SectionBlock(section: CoachAttendanceSection, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().testTag(CoachAttendanceTestTags.section(section.sessionId))) {
        if (section.cancelled) {
            CancelledCard(section)
        } else {
            SectionHeading(section.className, UiCopy.attendanceSlotLine(section.slot, section.sessionNumber))
            StudentCard(section)
        }
    }
}

@Composable
private fun SectionHeading(className: String, line: String) {
    val colors = LocalTrxColors.current
    Text(text = className, color = colors.textPrimary, style = TrxTypography.headline)
    Text(
        text = line,
        color = colors.textSecondary,
        style = TrxTypography.footnote,
        modifier = Modifier.padding(top = TrxSpacing.s0),
    )
}

@Composable
private fun CancelledCard(section: CoachAttendanceSection) {
    val colors = LocalTrxColors.current
    val shape = RoundedCornerShape(TrxSpacing.radiusLg)
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceAlt, shape)
            .border(1.dp, colors.borderDefault, shape)
            .padding(horizontal = TrxSpacing.s16, vertical = TrxSpacing.s12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = section.className, color = colors.textPrimary, style = TrxTypography.headline)
            Text(
                text = UiCopy.cancelledSlotLine(section.slot),
                color = colors.textSecondary,
                style = TrxTypography.footnote,
            )
        }
        StatusChip(
            UiCopy.cancelled(),
            Res.drawable.ban,
            colors.surface,
            colors.textSecondary,
            Modifier.padding(start = TrxSpacing.s12),
        )
    }
}

@Composable
private fun StudentCard(section: CoachAttendanceSection) {
    val colors = LocalTrxColors.current
    TrxCard(Modifier.padding(top = TrxSpacing.s8), contentPadding = TrxSpacing.s0) {
        section.students.forEachIndexed { index, student ->
            if (index > 0) HorizontalDivider(color = colors.borderDefault)
            StudentRow(section, student)
        }
    }
}

@Composable
private fun StudentRow(section: CoachAttendanceSection, student: CoachAttendanceStudentRow) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = TrxSpacing.s16, top = TrxSpacing.s12, end = TrxSpacing.s12, bottom = TrxSpacing.s12)
            .testTag(CoachAttendanceTestTags.row(section.sessionId, student.studentId)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s12),
    ) {
        InitialsAvatar(student.name)
        Column(Modifier.weight(1f)) {
            Text(text = student.name, color = colors.textPrimary, style = TrxTypography.headline)
            Text(text = student.employeeId, color = colors.textSecondary, style = TrxTypography.footnote)
            if (student.status == CoachAttendanceStatus.PRESENT) ChoseLine(student, section.className)
        }
        RowEnd(student)
    }
}

@Composable
private fun ChoseLine(student: CoachAttendanceStudentRow, className: String) {
    val colors = LocalTrxColors.current
    val time = student.scannedTime ?: return
    Row(Modifier.padding(top = TrxSpacing.s4), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(Res.drawable.check),
            contentDescription = null,
            tint = colors.successFg,
            modifier = Modifier
                .padding(end = TrxSpacing.s4)
                .size(TrxSpacing.iconSmall),
        )
        Text(
            text = UiCopy.choseAndScanned(className, time),
            color = colors.textSecondary,
            style = TrxTypography.footnote,
        )
    }
}

@Composable
private fun RowEnd(student: CoachAttendanceStudentRow) {
    val colors = LocalTrxColors.current
    when (student.status) {
        CoachAttendanceStatus.PRESENT ->
            StatusChip(UiCopy.present(), Res.drawable.circle_check, colors.successBg, colors.successFg)
        CoachAttendanceStatus.ABSENT ->
            StatusChip(UiCopy.absent(), Res.drawable.circle_x, colors.dangerBg, colors.dangerFg)
        CoachAttendanceStatus.NOT_SCANNED ->
            Text(text = UiCopy.notScannedYet(), color = colors.textTertiary, style = TrxTypography.footnote)
    }
}

/** A non-breaking space before the last word, so "ends" is not left alone on the next line. */
private fun String.keepLastWordTogether(): String {
    val space = lastIndexOf(' ')
    if (space < 0) return this
    return substring(0, space) + "\u00A0" + substring(space + 1)
}
