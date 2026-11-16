package ph.project.active.ui.guestclasses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.guestclasses.GuestClassItem
import ph.project.active.presentation.guestclasses.GuestClassListUiState
import ph.project.active.presentation.guestclasses.dayLine
import ph.project.active.presentation.guestclasses.spokenCard
import ph.project.active.presentation.guestclasses.timeLine
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.chevron_right
import projectactive.composeapp.generated.resources.log_in

object GuestClassListTestTags {
    const val title = "guest_list_title"
    const val count = "guest_list_count"
    const val empty = "guest_list_empty"
    fun card(classId: String) = "guest_list_card_$classId"
}

@Composable
fun GuestClassListScreen(
    state: GuestClassListUiState,
    onClassTapped: (String) -> Unit,
    onRegisterOrSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        state.connection.banner?.let { banner -> OfflineBanner(banner) }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
        ) {
            Column(
                Modifier
                    .testTag(GuestClassListTestTags.title)
                    .semantics {
                        this[SemanticsProperties.Text] = listOf(
                            AnnotatedString(UiCopy.guestListTitle()),
                            AnnotatedString(UiCopy.guestListSubtitle()),
                        )
                    },
            ) {
                Text(
                    text = UiCopy.guestListTitle(),
                    color = colors.textPrimary,
                    style = TrxTypography.title,
                    modifier = Modifier.clearAndSetSemantics {},
                )
                Text(
                    text = UiCopy.guestListSubtitle(),
                    color = colors.textSecondary,
                    style = TrxTypography.body,
                    modifier = Modifier
                        .padding(top = TrxSpacing.s8)
                        .clearAndSetSemantics {},
                )
            }
            Text(
                text = UiCopy.publicClassesHeader(),
                color = colors.textSecondary,
                style = TrxTypography.overline,
                modifier = Modifier.padding(top = TrxSpacing.s24),
            )
            if (!state.isEmpty) {
                Text(
                    text = UiCopy.classCount(state.items.size),
                    color = colors.textSecondary,
                    style = TrxTypography.label,
                    modifier = Modifier
                        .padding(top = TrxSpacing.s4)
                        .testTag(GuestClassListTestTags.count),
                )
            }
            if (state.isEmpty) {
                Column(
                    Modifier
                        .padding(top = TrxSpacing.s16)
                        .testTag(GuestClassListTestTags.empty)
                        .semantics {
                            this[SemanticsProperties.Text] = listOf(
                                AnnotatedString(UiCopy.guestListEmptyTitle()),
                                AnnotatedString(UiCopy.guestListEmptyBody()),
                            )
                        },
                ) {
                    Text(
                        text = UiCopy.guestListEmptyTitle(),
                        color = colors.textPrimary,
                        style = TrxTypography.button,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                    Text(
                        text = UiCopy.guestListEmptyBody(),
                        color = colors.textSecondary,
                        style = TrxTypography.body,
                        modifier = Modifier
                            .padding(top = TrxSpacing.s8)
                            .clearAndSetSemantics {},
                    )
                }
            } else {
                state.items.forEach { item ->
                    ClassCard(item, onClassTapped, Modifier.padding(top = TrxSpacing.s12))
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = TrxSpacing.touchTarget)
                    .testTag("guest_home_register_button")
                    .clickable(onClick = onRegisterOrSignIn)
                    .semantics {
                        role = Role.Button
                        this[SemanticsProperties.Text] = listOf(AnnotatedString(UiCopy.registerOrSignIn()))
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.log_in),
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier
                        .padding(end = TrxSpacing.s8)
                        .size(20.dp),
                )
                Text(
                    text = UiCopy.registerOrSignIn(),
                    color = colors.textPrimary,
                    style = TrxTypography.button,
                )
            }
            Text(
                text = UiCopy.createAccountToRequest(),
                color = colors.textSecondary,
                style = TrxTypography.label,
                modifier = Modifier.padding(top = TrxSpacing.s4),
            )
        }
    }
}

@Composable
private fun ClassCard(
    item: GuestClassItem,
    onClassTapped: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTrxColors.current
    val days = item.dayLine()
    val time = item.timeLine()
    val coach = "Coach ${item.coachName}"
    val spoken = item.spokenCard()
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .background(colors.surface, RoundedCornerShape(TrxSpacing.radiusMd))
            .clickable { onClassTapped(item.classId) }
            .testTag(GuestClassListTestTags.card(item.classId))
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = spoken
                this[SemanticsProperties.Text] = listOf(item.name, coach, days, time).map { AnnotatedString(it) }
            }
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, color = colors.textPrimary, style = TrxTypography.button, modifier = Modifier.clearAndSetSemantics {})
            Text(coach, color = colors.textSecondary, style = TrxTypography.body, modifier = Modifier.clearAndSetSemantics {})
            Text(days, color = colors.textSecondary, style = TrxTypography.body, modifier = Modifier.clearAndSetSemantics {})
            Text(time, color = colors.textPrimary, style = TrxTypography.body, modifier = Modifier.clearAndSetSemantics {})
        }
        Icon(
            painter = painterResource(Res.drawable.chevron_right),
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}
