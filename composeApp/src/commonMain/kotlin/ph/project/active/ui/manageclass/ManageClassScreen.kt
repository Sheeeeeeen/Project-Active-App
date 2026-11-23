package ph.project.active.ui.manageclass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import ph.project.active.domain.model.SessionStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDays
import ph.project.active.presentation.common.formatSlot
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.presentation.manageclass.ManageConfirmation
import ph.project.active.presentation.manageclass.ManageTab
import ph.project.active.ui.components.ConfirmDialog
import ph.project.active.ui.components.FormError
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxToast
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.chevron_left
import projectactive.composeapp.generated.resources.ellipsis
import projectactive.composeapp.generated.resources.globe
import projectactive.composeapp.generated.resources.lock

object ManageClassTestTags {
    const val back = "manage_class_back"
    const val options = "manage_class_options"
    const val tabSchedule = "manage_tab_schedule"
    const val tabRequests = "manage_tab_requests"
    const val tabRoster = "manage_tab_roster"
    const val tabInvite = "manage_tab_invite"
    const val requestsBadge = "manage_tab_requests_badge"
    const val requestsEmpty = "manage_requests_empty"
    const val inviteSearch = "manage_invite_search"
    const val inviteSearchError = "manage_invite_search_error"
    const val inviteSearchErrorIcon = "manage_invite_search_error_icon"
    const val inviteSend = "manage_invite_send"
    const val inviteEmpty = "manage_invite_empty"
    const val toast = "manage_toast"
    const val formError = "manage_form_error"
    const val dialog = "manage_dialog"
    const val dialogKeep = "manage_dialog_keep"
    const val dialogConfirm = "manage_dialog_confirm"
    fun dateRow(isoDate: String) = "manage_date_row_$isoDate"
    fun cancelDate(isoDate: String) = "manage_cancel_date_$isoDate"
    fun approve(studentId: String) = "manage_approve_$studentId"
    fun decline(studentId: String) = "manage_decline_$studentId"
    fun remove(studentId: String) = "manage_remove_$studentId"
}

/** D7–D18: one class's dates, requests, roster and invite. There is no control that edits the class. */
@Composable
fun ManageClassScreen(
    state: ManageClassUiState,
    actions: ManageClassActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = false)
    val colors = LocalTrxColors.current
    LaunchedEffect(state.navigation) {
        if (state.navigation == Navigation.Back) {
            actions.onNavigationHandled()
            onBack()
        }
    }
    state.confirmation?.let { confirmation ->
        LaunchedEffect(confirmation) {
            delay(TOAST_MILLIS)
            actions.onConfirmationHandled()
        }
    }
    Scaffold(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom),
        topBar = { TopBar(showOptions = !state.isCancelled, onBack = onBack, onOptions = actions.onOpenOptions) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            state.connection.banner?.let { banner -> OfflineBanner(banner) }
            Header(state)
            Tabs(state, actions.onSelectTab)
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
                state.confirmation?.let { TrxToast(it.message(), ManageClassTestTags.toast, Modifier.padding(bottom = TrxSpacing.s12)) }
                state.error?.let { error ->
                    FormError(UiCopy.uiError(error), ManageClassTestTags.formError, Modifier.padding(bottom = TrxSpacing.s12))
                }
                when (state.selectedTab) {
                    ManageTab.SCHEDULE -> ScheduleTab(state, actions.onCancelDate)
                    ManageTab.REQUESTS -> RequestsTab(state, actions.onApprove, actions.onDecline)
                    ManageTab.ROSTER -> RosterTab(state, actions.onRemove)
                    ManageTab.INVITE -> InviteTab(state, actions.onSearchTextChange, actions.onSearch, actions.onSendInvite)
                }
            }
        }
    }
    ManageDialogs(state, actions)
}

@Composable
private fun TopBar(showOptions: Boolean, onBack: () -> Unit, onOptions: () -> Unit) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = TrxSpacing.s8, vertical = TrxSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack, modifier = Modifier.testTag(ManageClassTestTags.back)) {
            Icon(
                painter = painterResource(Res.drawable.chevron_left),
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier.size(TrxSpacing.iconMedium),
            )
            Text(UiCopy.classesBack(), color = colors.accentText, style = TrxTypography.body)
        }
        Spacer(Modifier.weight(1f))
        if (showOptions) {
            IconButton(onClick = onOptions, modifier = Modifier.testTag(ManageClassTestTags.options)) {
                Icon(
                    painter = painterResource(Res.drawable.ellipsis),
                    contentDescription = UiCopy.classOptions(),
                    tint = colors.textPrimary,
                    modifier = Modifier.size(TrxSpacing.iconMedium),
                )
            }
        }
    }
}

@Composable
private fun Header(state: ManageClassUiState) {
    val colors = LocalTrxColors.current
    val slots = state.slotsByWeekday.values.flatten().sortedBy { it.start }.distinct().joinToString(", ") { formatSlot(it) }
    Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            when {
                state.isCancelled -> StatusChip(UiCopy.cancelled(), Res.drawable.ban, colors.surfaceAlt, colors.textSecondary)
                state.visibility == Visibility.PRIVATE ->
                    StatusChip(UiCopy.visibilityPrivate(), Res.drawable.lock, colors.surfaceAlt, colors.textPrimary)
                else -> StatusChip(UiCopy.visibilityPublic(), Res.drawable.globe, colors.surfaceAlt, colors.textPrimary)
            }
            Text(
                text = UiCopy.manageEndsLine(state.endDate, state.sessionCount),
                color = colors.textSecondary,
                style = TrxTypography.footnote,
                modifier = Modifier.padding(start = TrxSpacing.s8),
            )
        }
        Text(
            text = state.name,
            color = colors.textPrimary,
            style = TrxTypography.title,
            modifier = Modifier.padding(top = TrxSpacing.s8),
        )
        Text(
            text = "${formatDays(state.slotsByWeekday.keys)} \u00B7 $slots",
            color = colors.textSecondary,
            style = TrxTypography.subhead,
            modifier = Modifier.padding(top = TrxSpacing.s4),
        )
    }
}

@Composable
private fun Tabs(state: ManageClassUiState, onSelect: (ManageTab) -> Unit) {
    val colors = LocalTrxColors.current
    val selectedIndex = state.selectedTab.ordinal
    // Scrollable with content-width tabs so all four labels and the count fit at 360dp.
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = colors.canvas,
        contentColor = colors.textPrimary,
        edgePadding = TrxSpacing.s16,
        minTabWidth = TrxSpacing.s0,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedIndex, matchContentSize = false),
                width = Dp.Unspecified,
                color = colors.textPrimary,
            )
        },
        divider = { HorizontalDivider(color = colors.borderDefault) },
    ) {
        ManageTab.entries.forEach { tab ->
            val selected = tab == state.selectedTab
            Tab(
                selected = selected,
                onClick = { onSelect(tab) },
                modifier = Modifier.testTag(tab.tag()),
                selectedContentColor = colors.textPrimary,
                unselectedContentColor = colors.textSecondary,
            ) {
                Row(
                    Modifier
                        .heightIn(min = TrxSpacing.touchTarget)
                        .padding(horizontal = TrxSpacing.s8),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tab.label(),
                        style = if (selected) TrxTypography.subheadStrong else TrxTypography.subhead,
                        maxLines = 1,
                        softWrap = false,
                    )
                    if (tab == ManageTab.REQUESTS && state.pendingRequestCount > 0) {
                        Box(
                            Modifier
                                .padding(start = TrxSpacing.s4)
                                .height(TrxSpacing.s20)
                                .widthIn(min = TrxSpacing.s20)
                                .background(colors.accentDefault, CircleShape)
                                .testTag(ManageClassTestTags.requestsBadge)
                                .padding(horizontal = TrxSpacing.s4),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = UiCopy.countBadge(state.pendingRequestCount),
                                color = colors.textOnAccent,
                                style = TrxTypography.label,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageDialogs(state: ManageClassUiState, actions: ManageClassActions) {
    state.confirmCancelDate?.let { date ->
        val row = state.schedule.firstOrNull { it.date == date }
        val next = state.schedule.firstOrNull { it.date > date && it.status == SessionStatus.SCHEDULED }?.date
        ConfirmDialog(
            title = UiCopy.cancelDateTitle(date),
            body = UiCopy.cancelDateBody(row?.sessionNumber ?: 0, state.name, next),
            keepLabel = UiCopy.keepDate(),
            confirmLabel = UiCopy.cancelDate(),
            onKeep = actions.onKeepDate,
            onConfirm = actions.onConfirmCancelDate,
            tag = ManageClassTestTags.dialog,
            keepTag = ManageClassTestTags.dialogKeep,
            confirmTag = ManageClassTestTags.dialogConfirm,
        )
    }
    state.confirmRemove?.let { remove ->
        ConfirmDialog(
            title = UiCopy.removeTitle(remove.name),
            body = UiCopy.removeBody(remove.name, state.name),
            keepLabel = UiCopy.keep(),
            confirmLabel = UiCopy.remove(),
            onKeep = actions.onKeep,
            onConfirm = actions.onConfirmRemove,
            tag = ManageClassTestTags.dialog,
            keepTag = ManageClassTestTags.dialogKeep,
            confirmTag = ManageClassTestTags.dialogConfirm,
        )
    }
    if (state.confirmCancelClass) {
        ConfirmDialog(
            title = UiCopy.cancelClassTitle(state.name),
            body = UiCopy.cancelClassBody(),
            keepLabel = UiCopy.keepClass(),
            confirmLabel = UiCopy.cancelClass(),
            onKeep = actions.onKeepClass,
            onConfirm = actions.onConfirmCancelClass,
            tag = ManageClassTestTags.dialog,
            keepTag = ManageClassTestTags.dialogKeep,
            confirmTag = ManageClassTestTags.dialogConfirm,
        )
    } else if (state.optionsOpen && !state.isCancelled) {
        ClassOptionsSheet(onCancelWholeClass = actions.onCancelWholeClass, onDismiss = actions.onCloseOptions)
    }
}

private fun ManageTab.label(): String = when (this) {
    ManageTab.SCHEDULE -> UiCopy.tabSchedule()
    ManageTab.REQUESTS -> UiCopy.tabRequests()
    ManageTab.ROSTER -> UiCopy.tabRoster()
    ManageTab.INVITE -> UiCopy.tabInvite()
}

private fun ManageTab.tag(): String = when (this) {
    ManageTab.SCHEDULE -> ManageClassTestTags.tabSchedule
    ManageTab.REQUESTS -> ManageClassTestTags.tabRequests
    ManageTab.ROSTER -> ManageClassTestTags.tabRoster
    ManageTab.INVITE -> ManageClassTestTags.tabInvite
}

private fun ManageConfirmation.message(): String = when (this) {
    is ManageConfirmation.DateCancelled -> UiCopy.dateCancelled(date)
    is ManageConfirmation.InviteSent -> UiCopy.inviteSent(name)
}

private const val TOAST_MILLIS = 4_000L
