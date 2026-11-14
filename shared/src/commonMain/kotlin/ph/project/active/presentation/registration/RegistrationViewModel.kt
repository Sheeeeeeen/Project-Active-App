package ph.project.active.presentation.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.project.active.data.model.PersonProfile
import ph.project.active.data.model.RegisterResult
import ph.project.active.data.model.SignInResult
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.AuthRepository
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.repository.PersonRepository
import ph.project.active.data.repository.SessionStore
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.Role
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.usecase.RequestToJoin
import ph.project.active.presentation.classdetail.JoinOutcome
import ph.project.active.presentation.classdetail.RequestingClassHeader
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.FieldError
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.common.asUiError
import ph.project.active.presentation.common.isOffline
import ph.project.active.presentation.common.toConnection
import ph.project.active.presentation.common.watchConnection

class RegistrationViewModel(
    origin: RegistrationOrigin,
    private val auth: AuthRepository,
    private val session: SessionStore,
    private val people: PersonRepository,
    private val classes: ClassRepository,
    private val requestToJoin: RequestToJoin,
    private val network: NetworkMonitor,
) : ViewModel() {
    private val _state = MutableStateFlow(
        RegistrationUiState(
            origin = origin,
            requestingClass = requestingClass(origin),
            connection = network.status.value.toConnection(),
        ),
    )
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onNameChange(value: String) = edit { copy(name = value, fieldErrors = fieldErrors - Field.NAME) }

    fun onEmailChange(value: String) = edit { copy(email = value, fieldErrors = fieldErrors - Field.EMAIL) }

    fun onPasswordChange(value: String) = edit { copy(password = value, fieldErrors = fieldErrors - Field.PASSWORD) }

    fun onEmployeeIdChange(value: String) = edit {
        copy(employeeId = value, fieldErrors = fieldErrors - Field.EMPLOYEE_ID)
    }

    fun onSignInEmailChange(value: String) = edit {
        copy(signInEmail = value, fieldErrors = fieldErrors - Field.SIGN_IN_EMAIL)
    }

    fun onSignInPasswordChange(value: String) = edit {
        copy(signInPassword = value, fieldErrors = fieldErrors - Field.SIGN_IN_PASSWORD)
    }

    fun onShake() {
        val current = _state.value
        if (current.isSubmitting) return
        if (current.origin is RegistrationOrigin.FromRequestToJoin) return
        val mode = if (current.mode == Role.STUDENT) Role.COACH else Role.STUDENT
        _state.update { it.copy(mode = mode, showEmployeeId = mode == Role.STUDENT) }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isSubmitting) return
        val errors = registrationErrors(current)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        _state.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), error = null) }
        viewModelScope.launch {
            try {
                when (val result = register(current)) {
                    is RegisterResult.Success -> onAccountReady(result.account)
                    RegisterResult.EmailTaken -> showFieldError(Field.EMAIL, FieldError.EmailTaken)
                    RegisterResult.EmployeeIdTaken -> showFieldError(Field.EMPLOYEE_ID, FieldError.EmployeeIdTaken)
                    RegisterResult.Offline -> _state.update { it.copy(error = UiError.Offline) }
                }
            } catch (error: IllegalArgumentException) {
                _state.update { it.copy(error = error.asUiError() ?: UiError.AuthFailed) }
            } catch (error: IllegalStateException) {
                _state.update { it.copy(error = error.asUiError()) }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun onSignIn() {
        val current = _state.value
        if (current.isSubmitting) return
        val errors = buildMap {
            if (current.signInEmail.isBlank()) put(Field.SIGN_IN_EMAIL, FieldError.Required)
            if (current.signInPassword.isBlank()) put(Field.SIGN_IN_PASSWORD, FieldError.Required)
        }
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = it.fieldErrors + errors) }
            return
        }
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        _state.update { it.copy(isSubmitting = true, error = null, signInError = null) }
        viewModelScope.launch {
            try {
                when (val result = auth.signIn(current.signInEmail.trim(), current.signInPassword)) {
                    is SignInResult.Success -> onAccountReady(result.account)
                    SignInResult.Failed -> _state.update { it.copy(signInError = UiError.AuthFailed) }
                    SignInResult.Offline -> _state.update { it.copy(error = UiError.Offline) }
                }
            } catch (error: IllegalArgumentException) {
                _state.update { it.copy(signInError = UiError.AuthFailed) }
            } catch (error: IllegalStateException) {
                _state.update { it.copy(signInError = UiError.AuthFailed, error = error.asUiError()) }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private suspend fun register(current: RegistrationUiState): RegisterResult {
        val name = current.name.trim()
        val email = current.email.trim()
        return if (current.mode == Role.STUDENT) {
            auth.registerStudent(name, email, current.password, current.employeeId.trim())
        } else {
            auth.registerCoach(name, email, current.password)
        }
    }

    private fun onAccountReady(account: SignedInAccount) {
        session.save(account)
        people.upsert(PersonProfile(account.id, account.name, account.role, account.employeeId))
        val origin = _state.value.origin
        val openClassId = (origin as? RegistrationOrigin.FromRequestToJoin)
            ?.takeIf { account.role == Role.STUDENT }
            ?.classId
        val outcome = if (openClassId == null) {
            null
        } else {
            val join = requestToJoin(classId = openClassId, studentId = account.id)
            if (join is JoinResult.ClassCancelled) JoinOutcome.ClassCancelled else null
        }
        _state.update {
            it.copy(
                completion = AuthCompletion(account.role, openClassId),
                joinOutcome = outcome,
                signInError = null,
                error = null,
                navigation = Navigation.OpenSignedInRoot(account.role, openClassId),
            )
        }
    }

    private fun requestingClass(origin: RegistrationOrigin): RequestingClassHeader? {
        val classId = (origin as? RegistrationOrigin.FromRequestToJoin)?.classId ?: return null
        val trxClass = classes.findClass(classId) ?: return null
        return RequestingClassHeader(
            name = trxClass.name,
            coachName = people.find(trxClass.coachId)?.name.orEmpty(),
            slotsByWeekday = trxClass.slotsByWeekday,
        )
    }

    private fun showFieldError(field: Field, error: FieldError) {
        _state.update { it.copy(fieldErrors = mapOf(field to error)) }
    }

    private fun edit(block: RegistrationUiState.() -> RegistrationUiState) {
        _state.update(block)
    }
}

private const val MinimumPasswordLength = 8
private val EmailPattern = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

private fun registrationErrors(state: RegistrationUiState): Map<Field, FieldError> = buildMap {
    if (state.name.isBlank()) put(Field.NAME, FieldError.Required)
    val email = state.email.trim()
    when {
        email.isEmpty() -> put(Field.EMAIL, FieldError.Required)
        !EmailPattern.matches(email) -> put(Field.EMAIL, FieldError.InvalidEmail)
    }
    when {
        state.password.isBlank() -> put(Field.PASSWORD, FieldError.Required)
        state.password.length < MinimumPasswordLength -> put(Field.PASSWORD, FieldError.PasswordTooShort)
    }
    if (state.mode == Role.STUDENT && state.employeeId.isBlank()) {
        put(Field.EMPLOYEE_ID, FieldError.Required)
    }
}
