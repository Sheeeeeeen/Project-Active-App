package ph.project.active.ui.guestclasses

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.Role as AccountRole
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.guestclasses.GuestClassItem
import ph.project.active.presentation.guestclasses.GuestClassListUiState
import ph.project.active.presentation.guestclasses.dayLine
import ph.project.active.presentation.guestclasses.spokenCard
import ph.project.active.presentation.guestclasses.timeLine
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.chevron_right
import projectactive.composeapp.generated.resources.clock
import projectactive.composeapp.generated.resources.log_in
import projectactive.composeapp.generated.resources.user

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
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)),
    ) {
        state.connection.banner?.let { banner -> OfflineBanner(banner) }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
        ) {
            Text(
                text = UiCopy.registrationOverline(AccountRole.STUDENT),
                color = colors.accentText,
                style = TrxTypography.overline,
                modifier = Modifier.padding(bottom = TrxSpacing.s8),
            )
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = TrxSpacing.s24),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = UiCopy.publicClassesHeader(),
                    color = colors.textSecondary,
                    style = TrxTypography.overline,
                    modifier = Modifier.weight(1f),
                )
                if (!state.isEmpty) {
                    Text(
                        text = UiCopy.classCount(state.items.size),
                        color = colors.textTertiary,
                        style = TrxTypography.footnote,
                        modifier = Modifier.testTag(GuestClassListTestTags.count),
                    )
                }
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
        HorizontalDivider(color = colors.borderDefault)
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TrxLargeButton(
                label = UiCopy.registerOrSignIn(),
                kind = TrxButtonKind.Primary,
                tag = "guest_home_register_button",
                onClick = onRegisterOrSignIn,
                enabled = true,
                icon = Res.drawable.log_in,
            )
            Text(
                text = UiCopy.createAccountToRequest(),
                color = colors.textSecondary,
                style = TrxTypography.footnote,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = TrxSpacing.s12),
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
    val shape = RoundedCornerShape(TrxSpacing.radiusLg)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = TrxSpacing.touchTarget)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.borderDefault, shape)
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
            Text(item.name, color = colors.textPrimary, style = TrxTypography.headline, modifier = Modifier.clearAndSetSemantics {})
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
        Text(text, color = colors.textSecondary, style = TrxTypography.footnote, modifier = Modifier.clearAndSetSemantics {})
    }
}
