package ph.project.active.ui.coachclasses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.coachclasses.CoachClassItem
import ph.project.active.presentation.coachclasses.CoachClassesUiState
import ph.project.active.presentation.coachclasses.chipLabel
import ph.project.active.presentation.coachclasses.dayLine
import ph.project.active.presentation.coachclasses.metaLine
import ph.project.active.presentation.coachclasses.spokenCard
import ph.project.active.presentation.coachclasses.timeLine
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.components.EmptyState
import ph.project.active.ui.components.InfoCallout
import ph.project.active.ui.components.InfoCalloutStyle
import ph.project.active.ui.components.LargeTitleHeader
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.chevron_right
import projectactive.composeapp.generated.resources.clock
import projectactive.composeapp.generated.resources.globe
import projectactive.composeapp.generated.resources.inbox
import projectactive.composeapp.generated.resources.lock
import projectactive.composeapp.generated.resources.plus
import projectactive.composeapp.generated.resources.user

object CoachClassesTestTags {
    const val home = "coach_home"
    const val title = "coach_classes_title"
    const val subtitle = "coach_classes_subtitle"
    const val account = "coach_classes_account"
    const val add = "coach_classes_add"
    const val header = "coach_classes_header"
    const val count = "coach_classes_count"
    const val empty = "coach_classes_empty"
    const val emptyCreate = "coach_classes_empty_create"
    const val callout = "coach_classes_callout"
    fun card(classId: String) = "coach_class_card_$classId"
}

/** D1 / D2: the coach's own classes in creation order, or the empty state. */
@Composable
fun CoachClassesScreen(
    coachName: String,
    state: CoachClassesUiState,
    onCreateClass: () -> Unit,
    onClassTapped: (String) -> Unit,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    val subtitle = UiCopy.coachSubtitle(coachName)
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
                .testTag(CoachClassesTestTags.home)
                .semantics { text = AnnotatedString(subtitle) },
        ) {
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
                LargeTitleHeader(
                    title = UiCopy.classesTitle(),
                    titleTag = CoachClassesTestTags.title,
                    accountTag = CoachClassesTestTags.account,
                    action = { AddButton(onCreateClass) },
                )
                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    style = TrxTypography.subhead,
                    modifier = Modifier.testTag(CoachClassesTestTags.subtitle),
                )
            }
            state.connection.banner?.let { banner -> OfflineBanner(banner, Modifier.padding(top = TrxSpacing.s16)) }
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
                if (state.isEmpty) {
                    EmptyState(
                        icon = Res.drawable.inbox,
                        title = UiCopy.coachClassesEmptyTitle(),
                        body = UiCopy.coachClassesEmptyBody(),
                        tag = CoachClassesTestTags.empty,
                    )
                    TrxLargeButton(
                        label = UiCopy.createClass(),
                        kind = TrxButtonKind.Primary,
                        tag = CoachClassesTestTags.emptyCreate,
                        onClick = onCreateClass,
                        enabled = true,
                        icon = Res.drawable.plus,
                        modifier = Modifier.padding(top = TrxSpacing.s24),
                    )
                } else {
                    SectionHeader(count = UiCopy.classCount(state.classes.size))
                    state.classes.forEach { item -> ClassCard(item, onClassTapped) }
                    if (state.classes.any { it.visibility == Visibility.PRIVATE }) {
                        InfoCallout(
                            title = UiCopy.privateCalloutTitle(),
                            body = UiCopy.privateCalloutBody(),
                            tag = CoachClassesTestTags.callout,
                            style = InfoCalloutStyle.Neutral,
                            modifier = Modifier.padding(top = TrxSpacing.s16),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    val colors = LocalTrxColors.current
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .padding(end = TrxSpacing.s8)
            .background(colors.accentDefault, CircleShape)
            .testTag(CoachClassesTestTags.add),
    ) {
        Icon(
            painter = painterResource(Res.drawable.plus),
            contentDescription = UiCopy.createClass(),
            tint = colors.textOnAccent,
            modifier = Modifier.size(TrxSpacing.iconMedium),
        )
    }
}

@Composable
private fun SectionHeader(count: String) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = TrxSpacing.s8, bottom = TrxSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = UiCopy.yourClassesHeader(),
            color = colors.textSecondary,
            style = TrxTypography.overline,
            modifier = Modifier
                .weight(1f)
                .testTag(CoachClassesTestTags.header),
        )
        Text(
            text = count,
            color = colors.textTertiary,
            style = TrxTypography.footnote,
            modifier = Modifier.testTag(CoachClassesTestTags.count),
        )
    }
}

@Composable
private fun ClassCard(item: CoachClassItem, onClassTapped: (String) -> Unit) {
    val colors = LocalTrxColors.current
    val chip = item.chipLabel()
    val days = item.dayLine()
    val time = item.timeLine()
    val meta = item.metaLine()
    val spoken = item.spokenCard()
    val shape = RoundedCornerShape(TrxSpacing.radiusLg)
    Row(
        Modifier
            .padding(top = TrxSpacing.s12)
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.borderDefault, shape)
            .clickable(role = Role.Button) { onClassTapped(item.classId) }
            .testTag(CoachClassesTestTags.card(item.classId))
            .semantics(mergeDescendants = true) {
                contentDescription = spoken
                this[SemanticsProperties.Text] = listOf(item.name, chip, days, time, meta).map { AnnotatedString(it) }
            }
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    color = colors.textPrimary,
                    style = TrxTypography.headline,
                    modifier = Modifier
                        .weight(1f)
                        .clearAndSetSemantics {},
                )
                CardChip(item, chip, Modifier.padding(start = TrxSpacing.s8).clearAndSetSemantics {})
            }
            CaptionRow(Res.drawable.calendar_days, days, Modifier.padding(top = TrxSpacing.s8))
            CaptionRow(Res.drawable.clock, time, Modifier.padding(top = TrxSpacing.s4))
            CaptionRow(Res.drawable.user, meta, Modifier.padding(top = TrxSpacing.s4))
        }
        Icon(
            painter = painterResource(Res.drawable.chevron_right),
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CardChip(item: CoachClassItem, label: String, modifier: Modifier) {
    val colors = LocalTrxColors.current
    val icon = when {
        item.isCancelled -> Res.drawable.ban
        item.visibility == Visibility.PRIVATE -> Res.drawable.lock
        else -> Res.drawable.globe
    }
    val content = if (item.isCancelled) colors.textSecondary else colors.textPrimary
    StatusChip(label, icon, colors.surfaceAlt, content, modifier)
}

@Composable
private fun CaptionRow(icon: DrawableResource, text: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier
                .padding(end = TrxSpacing.s8)
                .size(TrxSpacing.iconSmall),
        )
        Text(text, color = colors.textSecondary, style = TrxTypography.subhead, modifier = Modifier.clearAndSetSemantics {})
    }
}
