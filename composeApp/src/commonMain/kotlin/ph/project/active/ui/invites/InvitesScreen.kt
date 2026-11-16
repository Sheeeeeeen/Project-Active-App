package ph.project.active.ui.invites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.invites.InviteRow
import ph.project.active.presentation.invites.InvitesUiState
import ph.project.active.presentation.invites.dayLine
import ph.project.active.presentation.invites.timeLine
import ph.project.active.presentation.manageclass.InviteConfirmation
import ph.project.active.ui.components.EmptyState
import ph.project.active.ui.components.FormError
import ph.project.active.ui.components.LargeTitleHeader
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.calendar_days
import projectactive.composeapp.generated.resources.circle_check
import projectactive.composeapp.generated.resources.clock
import projectactive.composeapp.generated.resources.globe
import projectactive.composeapp.generated.resources.inbox
import projectactive.composeapp.generated.resources.info
import projectactive.composeapp.generated.resources.lock

object InvitesTestTags {
    const val title = "invites_title"
    const val subtitle = "invites_subtitle"
    const val account = "invites_account"
    const val empty = "invites_empty"
    const val toast = "invites_toast"
    const val formError = "invites_form_error"
    fun card(classId: String) = "invites_card_$classId"
    fun accept(classId: String) = "invites_accept_$classId"
    fun decline(classId: String) = "invites_decline_$classId"
}

@Composable
fun InvitesScreen(
    state: InvitesUiState,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onConfirmationHandled: () -> Unit,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    val confirmation = state.confirmation
    if (confirmation != null) {
        LaunchedEffect(state.confirmationId) {
            delay(TOAST_MILLIS)
            onConfirmationHandled()
        }
    }
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
                .verticalScroll(rememberScrollState()),
        ) {
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
                LargeTitleHeader(
                    title = UiCopy.invitesTitle(),
                    titleTag = InvitesTestTags.title,
                    accountTag = InvitesTestTags.account,
                )
                if (!state.isEmpty) {
                    Text(
                        text = UiCopy.invitesSubtitle(),
                        color = colors.textSecondary,
                        style = TrxTypography.body,
                        modifier = Modifier
                            .padding(top = TrxSpacing.s4)
                            .testTag(InvitesTestTags.subtitle),
                    )
                }
            }
            state.connection.banner?.let { banner -> OfflineBanner(banner, Modifier.padding(top = TrxSpacing.s16)) }
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
                confirmation?.let { Toast(it) }
                state.error?.let { error -> FormError(UiCopy.uiError(error), InvitesTestTags.formError) }
                if (state.isEmpty) {
                    EmptyState(
                        icon = Res.drawable.inbox,
                        title = UiCopy.invitesEmptyTitle(),
                        body = UiCopy.invitesEmptyBody(),
                        tag = InvitesTestTags.empty,
                    )
                } else {
                    state.invites.forEach { invite -> InviteCard(invite, onAccept, onDecline) }
                }
            }
        }
    }
}

@Composable
private fun InviteCard(invite: InviteRow, onAccept: (String) -> Unit, onDecline: (String) -> Unit) {
    val colors = LocalTrxColors.current
    TrxCard(
        Modifier
            .padding(top = TrxSpacing.s12)
            .testTag(InvitesTestTags.card(invite.classId)),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Initials(invite.coachName)
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = TrxSpacing.s12),
            ) {
                Text(invite.className, color = colors.textPrimary, style = TrxTypography.headline)
                Text(
                    text = UiCopy.inviteInvitedYou(invite.coachName),
                    color = colors.textSecondary,
                    style = TrxTypography.footnote,
                    modifier = Modifier.padding(top = TrxSpacing.s4),
                )
            }
            VisibilityChip(invite.visibility)
        }
        CaptionRow(Res.drawable.calendar_days, invite.dayLine(), Modifier.padding(top = TrxSpacing.s12))
        CaptionRow(Res.drawable.clock, invite.timeLine(), Modifier.padding(top = TrxSpacing.s4))
        Row(
            Modifier.padding(top = TrxSpacing.s12),
            horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s8),
        ) {
            TrxLargeButton(
                label = UiCopy.decline(),
                kind = TrxButtonKind.Secondary,
                tag = InvitesTestTags.decline(invite.classId),
                onClick = { onDecline(invite.classId) },
                enabled = true,
                modifier = Modifier.weight(1f),
                height = TrxSpacing.touchTarget,
                textStyle = TrxTypography.subheadStrong,
            )
            TrxLargeButton(
                label = UiCopy.accept(),
                kind = TrxButtonKind.Primary,
                tag = InvitesTestTags.accept(invite.classId),
                onClick = { onAccept(invite.classId) },
                enabled = true,
                modifier = Modifier.weight(1f),
                height = TrxSpacing.touchTarget,
                textStyle = TrxTypography.subheadStrong,
            )
        }
    }
}

@Composable
private fun Initials(name: String) {
    val colors = LocalTrxColors.current
    Box(
        Modifier
            .size(TrxSpacing.avatar)
            .background(colors.accentSubtle, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
            color = colors.accentText,
            style = TrxTypography.headline,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

@Composable
private fun VisibilityChip(visibility: Visibility) {
    val colors = LocalTrxColors.current
    if (visibility == Visibility.PRIVATE) {
        StatusChip(UiCopy.visibilityPrivate(), Res.drawable.lock, colors.surfaceAlt, colors.textPrimary)
    } else {
        StatusChip(UiCopy.visibilityPublic(), Res.drawable.globe, colors.surfaceAlt, colors.textPrimary)
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
        Text(text, color = colors.textSecondary, style = TrxTypography.subhead)
    }
}

@Composable
private fun Toast(confirmation: InviteConfirmation) {
    val colors = LocalTrxColors.current
    val icon = when (confirmation) {
        is InviteConfirmation.InviteAccepted -> Res.drawable.circle_check
        InviteConfirmation.InviteDeclined -> Res.drawable.info
    }
    Row(
        Modifier
            .padding(bottom = TrxSpacing.s4)
            .fillMaxWidth()
            .background(colors.surfaceInverse, RoundedCornerShape(TrxSpacing.radiusMd))
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = colors.textInverse,
            modifier = Modifier.size(TrxSpacing.s20),
        )
        Text(
            text = confirmation.message(),
            color = colors.textInverse,
            style = TrxTypography.subheadStrong,
            modifier = Modifier
                .padding(start = TrxSpacing.s12)
                .testTag(InvitesTestTags.toast),
        )
    }
}

private fun InviteConfirmation.message(): String = when (this) {
    is InviteConfirmation.InviteAccepted -> UiCopy.inviteAccepted(className)
    InviteConfirmation.InviteDeclined -> UiCopy.inviteDeclined()
}

private const val TOAST_MILLIS = 4_000L
