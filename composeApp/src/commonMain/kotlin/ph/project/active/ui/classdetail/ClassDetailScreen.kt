package ph.project.active.ui.classdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
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
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.chevron_left
import projectactive.composeapp.generated.resources.clock
import projectactive.composeapp.generated.resources.user

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
    LightStatusBarIcons(lightIcons = true)
    val colors = LocalTrxColors.current
    val content = state.content
    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState()),
    ) {
        state.connection.banner?.let { banner -> OfflineBanner(banner) }
        BackRow(onBack)
        state.notice?.let { outcome ->
            FormRow(UiCopy.joinNotice(outcome), ClassDetailTestTags.notice)
        }
        if (content == null || state.unavailable) {
            Unavailable()
            state.error?.let { error -> FormRow(UiCopy.uiError(error), ClassDetailTestTags.formError) }
        } else {
            Hero(content, state.requestAction)
            Details(content)
            state.error?.let { error -> FormRow(UiCopy.uiError(error), ClassDetailTestTags.formError) }
            Bottom(state, content, onRequestToJoin)
        }
    }
}

@Composable
private fun BackRow(onBack: () -> Unit) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .testTag(ClassDetailTestTags.back)
            .clickable(onClick = onBack)
            .semantics {
                role = Role.Button
                this[SemanticsProperties.Text] = listOf(AnnotatedString(UiCopy.guestListTitle()))
            }
            .padding(horizontal = TrxSpacing.screenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.chevron_left),
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier
                .padding(end = TrxSpacing.s8)
                .size(20.dp),
        )
        Text(
            text = UiCopy.guestListTitle(),
            color = colors.textPrimary,
            style = TrxTypography.button,
        )
    }
}

@Composable
private fun Hero(content: ClassContent, action: RequestAction) {
    val colors = LocalTrxColors.current
    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceInverse)
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s24),
    ) {
        Text(
            text = chipLabel(content, action),
            color = colors.textInverse,
            style = TrxTypography.label,
            modifier = Modifier.testTag(ClassDetailTestTags.chip),
        )
        Text(
            text = content.name,
            color = colors.textInverse,
            style = TrxTypography.title,
            modifier = Modifier
                .padding(top = TrxSpacing.s12)
                .testTag(ClassDetailTestTags.name)
                .semantics { heading() },
        )
    }
}

@Composable
private fun Details(content: ClassContent) {
    val colors = LocalTrxColors.current
    val runs = content.runsLine()
    Column(
        Modifier
            .padding(TrxSpacing.screenHorizontal)
            .padding(top = TrxSpacing.s16)
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16),
    ) {
        Text(UiCopy.classDetailsHeading(), color = colors.textPrimary, style = TrxTypography.button)
        DetailRow(Res.drawable.user, UiCopy.detailCoachLabel(), "Coach ${content.coachName}")
        DetailRow(Res.drawable.calendar_days, UiCopy.detailDaysLabel(), content.daysLong(), ClassDetailTestTags.days)
        DetailLines(Res.drawable.clock, UiCopy.detailTimeLabel(), content.timeLines(), ClassDetailTestTags.time)
        DetailRow(Res.drawable.calendar_days, UiCopy.detailRunsLabel(), runs, ClassDetailTestTags.runs)
    }
}

@Composable
private fun DetailRow(
    icon: DrawableResource,
    label: String,
    value: String,
    valueTag: String? = null,
) {
    val colors = LocalTrxColors.current
    Row(Modifier.padding(top = TrxSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier
                .padding(end = TrxSpacing.s12)
                .size(20.dp),
        )
        Column {
            Text(label, color = colors.textSecondary, style = TrxTypography.label)
            Text(
                text = value,
                color = colors.textPrimary,
                style = TrxTypography.body,
                modifier = if (valueTag == null) Modifier else Modifier.testTag(valueTag),
            )
        }
    }
}

@Composable
private fun DetailLines(
    icon: DrawableResource,
    label: String,
    lines: List<String>,
    valueTag: String,
) {
    val colors = LocalTrxColors.current
    Row(Modifier.padding(top = TrxSpacing.s12), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier
                .padding(end = TrxSpacing.s12)
                .size(20.dp),
        )
        Column(
            Modifier
                .testTag(valueTag)
                .semantics {
                    this[SemanticsProperties.Text] = lines.map { AnnotatedString(it) }
                },
        ) {
            Text(label, color = colors.textSecondary, style = TrxTypography.label, modifier = Modifier.clearAndSetSemantics {})
            lines.forEach { line ->
                Text(
                    text = line,
                    color = colors.textPrimary,
                    style = TrxTypography.body,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
}

@Composable
private fun Bottom(
    state: ClassDetailUiState,
    content: ClassContent,
    onRequestToJoin: () -> Unit,
) {
    Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s24)) {
        when {
            content.isCancelled -> StatusBody(UiCopy.classCancelledBody())
            content.isEnded -> StatusBody(content.endedLine())
            state.requestAction is RequestAction.Pending -> {
                StatusTitle(UiCopy.awaitingApproval())
                StatusBody(UiCopy.pendingApprovalBody(content.coachName))
            }
            state.requestAction is RequestAction.Joined -> {
                StatusTitle(UiCopy.joinedTitle())
                StatusBody(UiCopy.joinedBody())
            }
            state.requestAction is RequestAction.Invited -> StatusBody(UiCopy.invitedBody(content.coachName))
            state.requestAgain -> {
                Callout()
                Spacer(Modifier.height(TrxSpacing.s16))
                RequestButton(state, onRequestToJoin)
            }
            state.requestAction is RequestAction.RequestToJoin -> {
                RequestButton(state, onRequestToJoin)
                val action = state.requestAction as RequestAction.RequestToJoin
                val note = if (action.opensRegistration) {
                    UiCopy.requestToJoinGuestNote()
                } else {
                    UiCopy.requestToJoinStudentNote(content.coachName)
                }
                Text(
                    text = note,
                    color = LocalTrxColors.current.textSecondary,
                    style = TrxTypography.label,
                    modifier = Modifier
                        .padding(top = TrxSpacing.s12)
                        .testTag(ClassDetailTestTags.footnote),
                )
            }
        }
    }
}

@Composable
private fun RequestButton(state: ClassDetailUiState, onRequestToJoin: () -> Unit) {
    TrxLargeButton(
        label = UiCopy.requestToJoin(),
        kind = TrxButtonKind.Primary,
        tag = ClassDetailTestTags.requestButton,
        onClick = onRequestToJoin,
        enabled = !state.isSending,
        loading = state.isSending,
        progressTag = ClassDetailTestTags.requestProgress,
    )
}

@Composable
private fun StatusTitle(title: String) {
    Text(
        text = title,
        color = LocalTrxColors.current.textPrimary,
        style = TrxTypography.button,
        modifier = Modifier
            .testTag(ClassDetailTestTags.statusTitle)
            .semantics { heading() },
    )
}

@Composable
private fun StatusBody(body: String) {
    Text(
        text = body,
        color = LocalTrxColors.current.textSecondary,
        style = TrxTypography.body,
        modifier = Modifier
            .padding(top = TrxSpacing.s8)
            .testTag(ClassDetailTestTags.statusBody),
    )
}

@Composable
private fun Callout() {
    val colors = LocalTrxColors.current
    val title = UiCopy.requestAgain()
    val body = UiCopy.requestAgainBody()
    Column(
        Modifier
            .fillMaxWidth()
            .testTag(ClassDetailTestTags.callout)
            .semantics {
                this[SemanticsProperties.Text] = listOf(AnnotatedString(title), AnnotatedString(body))
            }
            .background(colors.surfaceAlt, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16),
    ) {
        Text(title, color = colors.textPrimary, style = TrxTypography.button, modifier = Modifier.clearAndSetSemantics {})
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

private fun chipLabel(content: ClassContent, action: RequestAction): String = when {
    content.isCancelled -> UiCopy.cancelled()
    action is RequestAction.Pending -> UiCopy.pending()
    action is RequestAction.Joined -> UiCopy.joined()
    action is RequestAction.Invited -> UiCopy.invited()
    content.visibility == Visibility.PRIVATE -> UiCopy.visibilityPrivate()
    else -> UiCopy.visibilityPublic()
}
