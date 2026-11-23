package ph.project.active.ui.manageclass

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.manageclass.InviteMatch
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.ui.components.EmptyState
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.components.TrxTextField
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.id_card
import projectactive.composeapp.generated.resources.mail
import projectactive.composeapp.generated.resources.user

@Composable
internal fun InviteTab(
    state: ManageClassUiState,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSendInvite: () -> Unit,
) {
    val colors = LocalTrxColors.current
    Text(UiCopy.inviteIntro(), color = colors.textSecondary, style = TrxTypography.subhead)
    TrxTextField(
        value = state.searchText,
        onValueChange = onSearchTextChange,
        label = UiCopy.fieldLabel(Field.EMPLOYEE_ID),
        placeholder = UiCopy.fieldPlaceholder(Field.EMPLOYEE_ID),
        fieldTag = ManageClassTestTags.inviteSearch,
        enabled = true,
        keyboardType = KeyboardType.Text,
        modifier = Modifier.padding(top = TrxSpacing.s16),
        leadingIcon = Res.drawable.id_card,
        imeAction = ImeAction.Search,
        onImeAction = onSearch,
    )
    val match = state.match
    when {
        match != null -> MatchCard(match, state.inviteSent, onSendInvite)
        state.noMatch -> EmptyState(
            icon = Res.drawable.user,
            title = UiCopy.noMatchTitle(),
            body = UiCopy.noMatchBody(state.searchedId.orEmpty()),
            tag = ManageClassTestTags.inviteEmpty,
        )
    }
}

@Composable
private fun MatchCard(match: InviteMatch, inviteSent: Boolean, onSendInvite: () -> Unit) {
    val colors = LocalTrxColors.current
    TrxCard(Modifier.padding(top = TrxSpacing.s16)) {
        Text(UiCopy.matchCount(1), color = colors.textSecondary, style = TrxTypography.overline)
        PersonLine(match.name, Modifier.padding(top = TrxSpacing.s8)) {
            Text(
                text = UiCopy.matchLine(match.employeeId, match.currentMembershipStatus.matchStatus()),
                color = colors.textSecondary,
                style = TrxTypography.subhead,
            )
        }
        // An invited, requested or rostered student still shows Send invite; the send is refused with its line.
        TrxLargeButton(
            label = if (inviteSent) UiCopy.inviteSentButton() else UiCopy.sendInvite(),
            kind = TrxButtonKind.Primary,
            tag = ManageClassTestTags.inviteSend,
            onClick = onSendInvite,
            enabled = !inviteSent,
            modifier = Modifier.padding(top = TrxSpacing.s12),
            icon = Res.drawable.mail,
            height = TrxSpacing.touchTarget,
            textStyle = TrxTypography.subheadStrong,
        )
    }
}

private fun MembershipStatus?.matchStatus(): String = when (this) {
    MembershipStatus.INVITED -> UiCopy.invited()
    MembershipStatus.PENDING_REQUEST -> UiCopy.requested()
    MembershipStatus.ACTIVE -> UiCopy.onThisClass()
    else -> UiCopy.notOnThisClass()
}
