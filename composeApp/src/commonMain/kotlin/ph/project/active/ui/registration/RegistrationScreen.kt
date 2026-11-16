package ph.project.active.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role as ButtonRole
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.DrawableResource
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.registration.RegistrationUiState
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.RequestingClassCard
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.components.TrxTextField
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTheme
import ph.project.active.ui.theme.TrxThemeMode
import ph.project.active.ui.theme.TrxThemeModeKey
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.id_card
import projectactive.composeapp.generated.resources.lock
import projectactive.composeapp.generated.resources.mail
import projectactive.composeapp.generated.resources.user

@Composable
fun RegistrationScreen(
    state: RegistrationUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onEmployeeIdChange: (String) -> Unit,
    onSignInEmailChange: (String) -> Unit,
    onSignInPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = state.mode == Role.COACH
    LightStatusBarIcons(lightIcons = dark)
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var signInPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var signInWasLastAction by rememberSaveable { mutableStateOf(false) }
    val registerLoading = state.isSubmitting && !signInWasLastAction
    val signInLoading = state.isSubmitting && signInWasLastAction
    val formError = state.error?.takeUnless { signInWasLastAction }
    val signInBanner = state.signInError ?: state.error?.takeIf { signInWasLastAction }
    TrxTheme(dark = dark) {
        val colors = LocalTrxColors.current
        Column(
            modifier
                .fillMaxSize()
                .background(colors.canvas)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .semantics {
                    this[TrxThemeModeKey] = if (dark) TrxThemeMode.Dark else TrxThemeMode.Light
                },
        ) {
            if (dark) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(TrxSpacing.coachBand)
                        .background(colors.accentDefault)
                        .testTag(RegistrationTestTags.coachBand),
                )
            }
            state.connection.banner?.let { banner -> OfflineBanner(banner) }
            Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)) {
                Text(
                    text = UiCopy.registrationBackLabel(state.origin),
                    color = colors.textPrimary,
                    style = TrxTypography.button,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = TrxSpacing.touchTarget)
                        .testTag(RegistrationTestTags.back)
                        .clickable(role = ButtonRole.Button, onClick = onBack)
                        .padding(vertical = TrxSpacing.s12),
                )
                state.requestingClass?.let { header ->
                    RequestingClassCard(
                        header = header,
                        modifier = Modifier.padding(top = TrxSpacing.s8),
                    )
                }
                Text(
                    text = UiCopy.registrationOverline(state.mode),
                    color = colors.accentText,
                    style = TrxTypography.overline,
                    modifier = Modifier.padding(top = TrxSpacing.s16),
                )
                Text(
                    text = UiCopy.registrationTitle(state.mode),
                    color = colors.textPrimary,
                    style = TrxTypography.title,
                    modifier = Modifier
                        .padding(top = TrxSpacing.s8)
                        .testTag(RegistrationTestTags.title),
                )
                Text(
                    text = UiCopy.registrationSubtitle(state.mode, state.origin),
                    color = colors.textSecondary,
                    style = TrxTypography.body,
                    modifier = Modifier.padding(top = TrxSpacing.s8),
                )
                AccountField(
                    field = Field.NAME,
                    value = state.name,
                    onValueChange = onNameChange,
                    state = state,
                    keyboardType = KeyboardType.Text,
                    modifier = Modifier.padding(top = TrxSpacing.s24),
                )
                AccountField(
                    field = Field.EMAIL,
                    value = state.email,
                    onValueChange = onEmailChange,
                    state = state,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.padding(top = TrxSpacing.s16),
                )
                AccountField(
                    field = Field.PASSWORD,
                    value = state.password,
                    onValueChange = onPasswordChange,
                    state = state,
                    keyboardType = KeyboardType.Password,
                    password = true,
                    passwordVisible = passwordVisible,
                    passwordToggleTag = RegistrationTestTags.passwordToggle,
                    onTogglePassword = { passwordVisible = !passwordVisible },
                    helper = UiCopy.passwordHelper(),
                    helperTag = RegistrationTestTags.passwordHelper,
                    modifier = Modifier.padding(top = TrxSpacing.s16),
                )
                if (state.showEmployeeId) {
                    AccountField(
                        field = Field.EMPLOYEE_ID,
                        value = state.employeeId,
                        onValueChange = onEmployeeIdChange,
                        state = state,
                        keyboardType = KeyboardType.Text,
                        helper = UiCopy.employeeIdHelper(),
                        helperTag = RegistrationTestTags.employeeIdHelper,
                        modifier = Modifier.padding(top = TrxSpacing.s16),
                    )
                }
                formError?.let { error ->
                    Text(
                        text = UiCopy.uiError(error),
                        color = colors.dangerFg,
                        style = TrxTypography.body,
                        modifier = Modifier
                            .padding(top = TrxSpacing.s16)
                            .testTag(RegistrationTestTags.formError),
                    )
                }
                Spacer(Modifier.height(TrxSpacing.s24))
                TrxLargeButton(
                    label = if (registerLoading) {
                        UiCopy.registrationLoading()
                    } else {
                        UiCopy.registrationSubmit(state.mode, state.origin)
                    },
                    kind = TrxButtonKind.Primary,
                    tag = RegistrationTestTags.submitButton,
                    onClick = {
                        signInWasLastAction = false
                        onSubmit()
                    },
                    enabled = state.canSubmit,
                    loading = registerLoading,
                    progressTag = RegistrationTestTags.submitProgress,
                )
                DividerCaption(
                    text = UiCopy.alreadyHaveAccount(),
                    modifier = Modifier.padding(top = TrxSpacing.s32),
                )
                val requestingClass = state.requestingClass
                if (state.origin is RegistrationOrigin.FromRequestToJoin && requestingClass != null) {
                    Text(
                        text = UiCopy.signInAlsoSends(requestingClass.name),
                        color = colors.textSecondary,
                        style = TrxTypography.footnote,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = TrxSpacing.s16)
                            .testTag(RegistrationTestTags.signInNote),
                    )
                }
                Text(
                    text = UiCopy.signInHeading(),
                    color = colors.textPrimary,
                    style = TrxTypography.sectionTitle,
                    modifier = Modifier.padding(top = TrxSpacing.s16),
                )
                AccountField(
                    field = Field.SIGN_IN_EMAIL,
                    value = state.signInEmail,
                    onValueChange = onSignInEmailChange,
                    state = state,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.padding(top = TrxSpacing.s16),
                )
                AccountField(
                    field = Field.SIGN_IN_PASSWORD,
                    value = state.signInPassword,
                    onValueChange = onSignInPasswordChange,
                    state = state,
                    keyboardType = KeyboardType.Password,
                    password = true,
                    passwordVisible = signInPasswordVisible,
                    passwordToggleTag = RegistrationTestTags.signInPasswordToggle,
                    onTogglePassword = { signInPasswordVisible = !signInPasswordVisible },
                    modifier = Modifier.padding(top = TrxSpacing.s16),
                )
                signInBanner?.let { error ->
                    Text(
                        text = UiCopy.uiError(error),
                        color = colors.dangerFg,
                        style = TrxTypography.body,
                        modifier = Modifier
                            .padding(top = TrxSpacing.s16)
                            .testTag(RegistrationTestTags.signInError),
                    )
                }
                Spacer(Modifier.height(TrxSpacing.s24))
                TrxLargeButton(
                    label = UiCopy.signInHeading(),
                    kind = TrxButtonKind.Secondary,
                    tag = RegistrationTestTags.signInButton,
                    onClick = {
                        signInWasLastAction = true
                        onSignIn()
                    },
                    enabled = !state.isSubmitting,
                    loading = signInLoading,
                    progressTag = RegistrationTestTags.signInProgress,
                )
                Spacer(Modifier.height(TrxSpacing.s32))
            }
        }
    }
}

@Composable
private fun AccountField(
    field: Field,
    value: String,
    onValueChange: (String) -> Unit,
    state: RegistrationUiState,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    passwordVisible: Boolean = false,
    passwordToggleTag: String? = null,
    onTogglePassword: (() -> Unit)? = null,
    helper: String? = null,
    helperTag: String? = null,
) {
    val error = state.fieldErrors[field]
    TrxTextField(
        value = value,
        onValueChange = onValueChange,
        label = UiCopy.fieldLabel(field),
        placeholder = UiCopy.fieldPlaceholder(field),
        fieldTag = RegistrationTestTags.field(field),
        enabled = !state.isSubmitting,
        keyboardType = keyboardType,
        errorMessage = error?.let(UiCopy::fieldError),
        errorTag = RegistrationTestTags.error(field),
        errorIconTag = RegistrationTestTags.errorIcon(field),
        helper = if (error == null) helper else null,
        helperTag = if (error == null) helperTag else null,
        password = password,
        passwordVisible = passwordVisible,
        passwordToggleTag = passwordToggleTag,
        onTogglePassword = onTogglePassword,
        leadingIcon = fieldIcon(field),
        modifier = modifier,
    )
}

@Composable
private fun DividerCaption(text: String, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = colors.borderDefault)
        Text(
            text = text,
            color = colors.textTertiary,
            style = TrxTypography.footnote,
            modifier = Modifier.padding(horizontal = TrxSpacing.s12),
        )
        HorizontalDivider(Modifier.weight(1f), color = colors.borderDefault)
    }
}

private fun fieldIcon(field: Field): DrawableResource = when (field) {
    Field.NAME -> Res.drawable.user
    Field.EMAIL, Field.SIGN_IN_EMAIL -> Res.drawable.mail
    Field.PASSWORD, Field.SIGN_IN_PASSWORD -> Res.drawable.lock
    Field.EMPLOYEE_ID -> Res.drawable.id_card
}
