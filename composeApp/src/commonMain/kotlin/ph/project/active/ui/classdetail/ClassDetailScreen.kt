package ph.project.active.ui.classdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.classdetail.ClassContent
import ph.project.active.presentation.classdetail.ClassDetailUiState
import ph.project.active.presentation.classdetail.daysLong
import ph.project.active.presentation.classdetail.endedLine
import ph.project.active.presentation.classdetail.runsLine
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.manageclass.RequestAction
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusCard
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.calendar_check
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.check
import projectactive.composeapp.generated.resources.chevron_left
import projectactive.composeapp.generated.resources.clock
import projectactive.composeapp.generated.resources.globe
import projectactive.composeapp.generated.resources.hourglass
import projectactive.composeapp.generated.resources.info

object ClassDetailTestTags {
    const val back = "class_detail_back"
    const val chip = "class_detail_chip"
    const val name = "class_detail_name"
    const val days = "class_detail_days"
    const val time = "class_detail_time"
    const val runs = "class_detail_runs"
    const val requestButton = "class_detail_request_button"
    const val requestProgress = "class_detail_request_progress"
    const val footnote = "class_detail_footnote"
    const val statusTitle = "class_detail_status_title"
    const val statusBody = "class_detail_status_body"
    const val callout = "class_detail_callout"
    const val formError = "class_detail_form_error"
    const val notice = "class_detail_notice"
    const val unavailable = "class_detail_unavailable"
}

@Composable
fun ClassDetailScreen(
    state: ClassDetailUiState,
    onRequestToJoin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    val content = state.content?.takeUnless { state.unavailable }
    val showsBar = content != null && showsRequestButton(state, content)
    LightStatusBarIcons(lightIcons = content != null)
    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            if (content == null) {
                BackRow(onBack, colors.textPrimary, Modifier.windowInsetsPadding(WindowInsets.statusBars))
            } else {
                Hero(content, state.requestAction, onBack)
            }
            state.connection.banner?.let { banner -> OfflineBanner(banner) }
            state.notice?.let { outcome ->
                FormRow(UiCopy.joinNotice(outcome), ClassDetailTestTags.notice)
            }
            if (content == null) {
                Unavailable()
            } else {
                Status(state, content)
                Details(content)
            }
            state.error?.let { error -> FormRow(UiCopy.uiError(error), ClassDetailTestTags.formError) }
            if (!showsBar) Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
        if (showsBar && content != null) {
            BottomBar(state, content, onRequestToJoin)
        }
    }
}

@Composable
private fun BackRow(onBack: () -> Unit, tint: Color, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .testTag(ClassDetailTestTags.back)
            .clickable(role = Role.Button, onClick = onBack)
            .semantics {
                this[SemanticsProperties.Text] = listOf(AnnotatedString(UiCopy.guestListTitle()))
            }
            .padding(horizontal = TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.chevron_left),
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .padding(end = TrxSpacing.s4)
                .size(24.dp),
        )
        Text(text = UiCopy.guestListTitle(), color = tint, style = TrxTypography.body)
    }
}

@Composable
private fun Hero(content: ClassContent, action: RequestAction, onBack: () -> Unit) {
    val colors = LocalTrxColors.current
    val visibility = visibilityLabel(content)
    val status = chipLabel(content, action)
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceInverse)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = TrxSpacing.s24),
    ) {
        BackRow(onBack, colors.textInverse)
        Row(
            Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s8),
            horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s8),
        ) {
            if (status == visibility) {
                VisibilityChip(visibility, Modifier.testTag(ClassDetailTestTags.chip))
            } else {
                VisibilityChip(visibility)
                StateChip(content, action, Modifier.testTag(ClassDetailTestTags.chip))
            }
        }
        Text(
            text = content.name,
            color = colors.textInverse,
            style = TrxTypography.title,
            modifier = Modifier
                .padding(horizontal = TrxSpacing.screenHorizontal)
                .padding(top = TrxSpacing.s8)
                .testTag(ClassDetailTestTags.name)
                .semantics { heading() },
        )
    }
}

@Composable
private fun VisibilityChip(label: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    StatusChip(label, Res.drawable.globe, colors.surfaceAlt, colors.textPrimary, modifier)
}

@Composable
private fun StateChip(content: ClassContent, action: RequestAction, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    val label = chipLabel(content, action)
    when {
        content.isCancelled -> StatusChip(label, Res.drawable.ban, colors.surfaceAlt, colors.textSecondary, modifier)
        action is RequestAction.Pending -> StatusChip(label, Res.drawable.hourglass, colors.warningBg, colors.warningFg, modifier)
        action is RequestAction.Joined -> StatusChip(label, Res.drawable.check, colors.successBg, colors.successFg, modifier)
        else -> StatusChip(label, Res.drawable.info, colors.infoBg, colors.infoFg, modifier)
    }
}

@Composable
private fun Status(state: ClassDetailUiState, content: ClassContent) {
    val colors = LocalTrxColors.current
    val action = state.requestAction
    val modifier = Modifier
        .padding(horizontal = TrxSpacing.screenHorizontal)
        .padding(top = TrxSpacing.s16)
    when {
        content.isCancelled -> StatusCard(
            label = UiCopy.cancelled(),
            icon = Res.drawable.ban,
            background = colors.surfaceAlt,
            accent = colors.textSecondary,
            title = null,
            body = UiCopy.classCancelledBody(),
            titleTag = ClassDetailTestTags.statusTitle,
            bodyTag = ClassDetailTestTags.statusBody,
            modifier = modifier,
        )
        content.isEnded -> StatusCard(
            label = null,
            icon = Res.drawable.ban,
            background = colors.surfaceAlt,
            accent = colors.textSecondary,
            title = null,
            body = content.endedLine(),
            titleTag = ClassDetailTestTags.statusTitle,
            bodyTag = ClassDetailTestTags.statusBody,
            modifier = modifier,
        )
        action is RequestAction.Pending -> StatusCard(
            label = UiCopy.pending(),
            icon = Res.drawable.hourglass,
            background = colors.warningBg,
            accent = colors.warningFg,
            title = UiCopy.awaitingApproval(),
            body = UiCopy.pendingApprovalBody(content.coachName),
            titleTag = ClassDetailTestTags.statusTitle,
            bodyTag = ClassDetailTestTags.statusBody,
            modifier = modifier,
        )
        action is RequestAction.Joined -> StatusCard(
            label = UiCopy.joined(),
            icon = Res.drawable.check,
            background = colors.successBg,
            accent = colors.successFg,
            title = UiCopy.joinedTitle(),
            body = UiCopy.joinedBody(),
            titleTag = ClassDetailTestTags.statusTitle,
            bodyTag = ClassDetailTestTags.statusBody,
            modifier = modifier,
        )
        action is RequestAction.Invited -> StatusCard(
            label = UiCopy.invited(),
            icon = Res.drawable.info,
            background = colors.infoBg,
            accent = colors.infoFg,
            title = null,
            body = UiCopy.invitedBody(content.coachName),
            titleTag = ClassDetailTestTags.statusTitle,
            bodyTag = ClassDetailTestTags.statusBody,
            modifier = modifier,
        )
        state.requestAgain -> Callout(modifier)
    }
}

@Composable
private fun Details(content: ClassContent) {
    val colors = LocalTrxColors.current
    TrxCard(
        Modifier
            .padding(horizontal = TrxSpacing.screenHorizontal)
            .padding(top = TrxSpacing.s16, bottom = TrxSpacing.s24),
    ) {
        Text(UiCopy.classDetailsHeading(), color = colors.textSecondary, style = TrxTypography.overline)
        CoachRow(content.coachName)
        HorizontalDivider(color = colors.borderDefault)
        DetailRow(Res.drawable.calendar_days, UiCopy.detailDaysLabel(), listOf(content.daysLong()), ClassDetailTestTags.days)
        HorizontalDivider(color = colors.borderDefault)
        DetailRow(Res.drawable.clock, UiCopy.detailTimeLabel(), content.timeLines(), ClassDetailTestTags.time)
        HorizontalDivider(color = colors.borderDefault)
        DetailRow(Res.drawable.calendar_check, UiCopy.detailRunsLabel(), listOf(content.runsLine()), ClassDetailTestTags.runs)
    }
}

@Composable
private fun CoachRow(coachName: String) {
    val colors = LocalTrxColors.current
    Row(Modifier.padding(vertical = TrxSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .padding(end = TrxSpacing.s16)
                .size(TrxSpacing.avatar)
                .background(colors.accentSubtle, CircleShape)
                .clearAndSetSemantics {},
            contentAlignment = Alignment.Center,
        ) {
            Text(initials(coachName), color = colors.accentText, style = TrxTypography.headline)
        }
        Column {
            Text(UiCopy.detailCoachLabel(), color = colors.textSecondary, style = TrxTypography.footnote)
            Text(coachName, color = colors.textPrimary, style = TrxTypography.headline)
        }
    }
}

@Composable
private fun DetailRow(
    icon: DrawableResource,
    label: String,
    lines: List<String>,
    valueTag: String,
) {
    val colors = LocalTrxColors.current
    Row(Modifier.padding(vertical = TrxSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier
                .padding(start = TrxSpacing.s8, end = TrxSpacing.s24)
                .size(24.dp),
        )
        Column {
            Text(label, color = colors.textSecondary, style = TrxTypography.footnote)
            Column(
                Modifier
                    .testTag(valueTag)
                    .semantics { this[SemanticsProperties.Text] = lines.map { AnnotatedString(it) } },
            ) {
                lines.forEach { line ->
                    Text(
                        text = line,
                        color = colors.textPrimary,
                        style = TrxTypography.headline,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    state: ClassDetailUiState,
    content: ClassContent,
    onRequestToJoin: () -> Unit,
) {
    val colors = LocalTrxColors.current
    val action = state.requestAction
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
    ) {
        HorizontalDivider(color = colors.borderDefault)
        Column(
            Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TrxLargeButton(
                label = UiCopy.requestToJoin(),
                kind = TrxButtonKind.Primary,
                tag = ClassDetailTestTags.requestButton,
                onClick = onRequestToJoin,
                enabled = !state.isSending,
                loading = state.isSending,
                progressTag = ClassDetailTestTags.requestProgress,
                height = TrxSpacing.bottomBarButtonHeight,
            )
            if (!state.requestAgain && action is RequestAction.RequestToJoin) {
                val note = if (action.opensRegistration) {
                    UiCopy.requestToJoinGuestNote()
                } else {
                    UiCopy.requestToJoinStudentNote(content.coachName)
                }
                Text(
                    text = note,
                    color = colors.textSecondary,
                    style = TrxTypography.footnote,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = TrxSpacing.s12)
                        .testTag(ClassDetailTestTags.footnote),
                )
            }
        }
    }
}

@Composable
private fun Callout(modifier: Modifier) {
    val colors = LocalTrxColors.current
    val title = UiCopy.requestAgain()
    val body = UiCopy.requestAgainBody()
    Column(
        modifier
            .fillMaxWidth()
            .testTag(ClassDetailTestTags.callout)
            .semantics {
                this[SemanticsProperties.Text] = listOf(AnnotatedString(title), AnnotatedString(body))
            }
            .background(colors.accentSubtle, RoundedCornerShape(TrxSpacing.radiusLg))
            .padding(TrxSpacing.s16),
    ) {
        Text(title, color = colors.textPrimary, style = TrxTypography.headline, modifier = Modifier.clearAndSetSemantics {})
        Text(
            body,
            color = colors.textSecondary,
            style = TrxTypography.body,
            modifier = Modifier
                .padding(top = TrxSpacing.s8)
                .clearAndSetSemantics {},
        )
    }
}

@Composable
private fun Unavailable() {
    val colors = LocalTrxColors.current
    val title = UiCopy.classUnavailableTitle()
    val body = UiCopy.classUnavailableBody()
    Column(
        Modifier
            .padding(TrxSpacing.screenHorizontal)
            .padding(top = TrxSpacing.s32)
            .testTag(ClassDetailTestTags.unavailable)
            .semantics {
                this[SemanticsProperties.Text] = listOf(AnnotatedString(title), AnnotatedString(body))
            },
    ) {
        Text(title, color = colors.textPrimary, style = TrxTypography.title, modifier = Modifier.clearAndSetSemantics {})
        Text(
            body,
            color = colors.textSecondary,
            style = TrxTypography.body,
            modifier = Modifier
                .padding(top = TrxSpacing.s8)
                .clearAndSetSemantics {},
        )
    }
}

@Composable
private fun FormRow(message: String, tag: String) {
    val colors = LocalTrxColors.current
    Text(
        text = message,
        color = colors.textPrimary,
        style = TrxTypography.body,
        modifier = Modifier
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s12)
            .fillMaxWidth()
            .background(colors.dangerBg, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16)
            .testTag(tag),
    )
}

private fun showsRequestButton(state: ClassDetailUiState, content: ClassContent): Boolean {
    val action = state.requestAction
    if (content.isCancelled || content.isEnded) return false
    if (action is RequestAction.Pending || action is RequestAction.Joined || action is RequestAction.Invited) return false
    return state.requestAgain || action is RequestAction.RequestToJoin
}

private fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private fun visibilityLabel(content: ClassContent): String =
    if (content.visibility == Visibility.PRIVATE) UiCopy.visibilityPrivate() else UiCopy.visibilityPublic()

private fun chipLabel(content: ClassContent, action: RequestAction): String = when {
    content.isCancelled -> UiCopy.cancelled()
    action is RequestAction.Pending -> UiCopy.pending()
    action is RequestAction.Joined -> UiCopy.joined()
    action is RequestAction.Invited -> UiCopy.invited()
    else -> visibilityLabel(content)
}
