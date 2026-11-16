package ph.project.active.ui.studentclasses

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import ph.project.active.presentation.classdetail.ClassChip
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.studentclasses.StudentClassItem
import ph.project.active.presentation.studentclasses.StudentClassesUiState
import ph.project.active.presentation.studentclasses.dayLine
import ph.project.active.presentation.studentclasses.spokenCard
import ph.project.active.presentation.studentclasses.timeLine
import ph.project.active.ui.components.EmptyState
import ph.project.active.ui.components.LargeTitleHeader
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.check
import projectactive.composeapp.generated.resources.chevron_right
import projectactive.composeapp.generated.resources.clock
import projectactive.composeapp.generated.resources.hourglass
import projectactive.composeapp.generated.resources.inbox
import projectactive.composeapp.generated.resources.mail
import projectactive.composeapp.generated.resources.user

object StudentClassesTestTags {
    const val home = "student_home"
    const val title = "student_classes_title"
    const val greeting = "student_classes_greeting"
    const val account = "student_classes_account"
    const val myClassesHeader = "student_my_classes_header"
    const val myClassesCount = "student_my_classes_count"
    const val publicClassesHeader = "student_public_classes_header"
    const val empty = "student_classes_empty"
    fun card(classId: String) = "student_class_card_$classId"
}

@Composable
fun StudentClassesScreen(
    state: StudentClassesUiState,
    onClassTapped: (String) -> Unit,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    val greeting = UiCopy.classesGreeting(state.studentName, state.employeeId)
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
                .testTag(StudentClassesTestTags.home)
                .semantics { text = AnnotatedString(greeting) },
        ) {
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
                LargeTitleHeader(
                    title = UiCopy.classesTitle(),
                    titleTag = StudentClassesTestTags.title,
                    accountTag = StudentClassesTestTags.account,
                )
                Text(
                    text = greeting,
                    color = colors.textSecondary,
                    style = TrxTypography.body,
                    modifier = Modifier
                        .padding(top = TrxSpacing.s4)
                        .testTag(StudentClassesTestTags.greeting),
                )
            }
            state.connection.banner?.let { banner -> OfflineBanner(banner, Modifier.padding(top = TrxSpacing.s16)) }
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
                if (state.myClasses.isEmpty() && state.publicClasses.isEmpty()) {
                    EmptyState(
                        icon = Res.drawable.inbox,
                        title = UiCopy.classesEmptyTitle(),
                        body = UiCopy.classesEmptyBody(),
                        tag = StudentClassesTestTags.empty,
                    )
                }
                if (state.myClasses.isNotEmpty()) {
                    SectionHeader(
                        title = UiCopy.myClassesHeader(),
                        titleTag = StudentClassesTestTags.myClassesHeader,
                        count = UiCopy.joinedCount(state.myClasses.size),
                    )
                    state.myClasses.forEach { item -> ClassCard(item, onClassTapped) }
                }
                if (state.publicClasses.isNotEmpty()) {
                    SectionHeader(
                        title = UiCopy.studentPublicClassesHeader(),
                        titleTag = StudentClassesTestTags.publicClassesHeader,
                        count = null,
                    )
                    state.publicClasses.forEach { item -> ClassCard(item, onClassTapped) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, titleTag: String, count: String?) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = TrxSpacing.s8, bottom = TrxSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = colors.textSecondary,
            style = TrxTypography.overline,
            modifier = Modifier
                .weight(1f)
                .testTag(titleTag),
        )
        if (count != null) {
            Text(
                text = count,
                color = colors.textTertiary,
                style = TrxTypography.footnote,
                modifier = Modifier.testTag(StudentClassesTestTags.myClassesCount),
            )
        }
    }
}

@Composable
private fun ClassCard(item: StudentClassItem, onClassTapped: (String) -> Unit) {
    val colors = LocalTrxColors.current
    val coach = "Coach ${item.coachName}"
    val days = item.dayLine()
    val time = item.timeLine()
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
            .testTag(StudentClassesTestTags.card(item.classId))
            .semantics(mergeDescendants = true) {
                contentDescription = spoken
                this[SemanticsProperties.Text] = listOf(item.name, coach, days, time).map { AnnotatedString(it) }
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
                item.chip?.let { chip -> CardChip(chip, Modifier.padding(start = TrxSpacing.s8)) }
            }
            CaptionRow(Res.drawable.user, coach, Modifier.padding(top = TrxSpacing.s8))
            CaptionRow(Res.drawable.calendar_days, days, Modifier.padding(top = TrxSpacing.s4))
            CaptionRow(Res.drawable.clock, time, Modifier.padding(top = TrxSpacing.s4))
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
private fun CardChip(chip: ClassChip, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    when (chip) {
        ClassChip.Joined -> StatusChip(UiCopy.joined(), Res.drawable.check, colors.successBg, colors.successFg, modifier)
        ClassChip.Invited -> StatusChip(UiCopy.invited(), Res.drawable.mail, colors.infoBg, colors.infoFg, modifier)
        ClassChip.Pending -> StatusChip(UiCopy.pending(), Res.drawable.hourglass, colors.warningBg, colors.warningFg, modifier)
    }
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
