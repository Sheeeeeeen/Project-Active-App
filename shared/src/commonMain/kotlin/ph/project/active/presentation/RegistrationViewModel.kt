package ph.project.active.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ph.project.active.data.AuthRepository
import ph.project.active.data.NetworkMonitor
import ph.project.active.data.PersonProfile
import ph.project.active.data.PersonRepository
import ph.project.active.data.RegisterResult
import ph.project.active.data.SessionStore
import ph.project.active.data.SignInResult
import ph.project.active.data.SignedInAccount
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.JoinResult
import ph.project.active.domain.RequestToJoin
import ph.project.active.domain.Role

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
            mode = Role.STUDENT,
            showEmployeeId = true,
            name = "",
            email = "",
            password = "",
            employeeId = "",
            signInEmail = "",
            signInPassword = "",
            fieldErrors = emptyMap(),
            isSubmitting = false,
            requestingClass = header(origin),
            signInError = null,
            completion = null,
            joinOutcome = null,
            error = null,
            connection = network.status.value.toConnection(),
            navigation = null,
        ),
    )
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    init {
        watchConnection(network, _state) { copy(connection = it) }
    }

    fun onNameChange(value: String) = edit { copy(name = value, fieldErrors = fieldErrors - Field.NAME) }

    fun onEmailChange(value: String) = edit { copy(email = value, fieldErrors = fieldErrors - Field.EMAIL) }

    fun onPasswordChange(value: String) = edit { copy(password = value, fieldErrors = fieldErrors - Field.PASSWORD) }

    fun onEmployeeIdChange(value: String) = edit { copy(employeeId = value, fieldErrors = fieldErrors - Field.EMPLOYEE_ID) }

    fun onSignInEmailChange(value: String) = edit { copy(signInEmail = value, fieldErrors = fieldErrors - Field.SIGN_IN_EMAIL) }

    fun onSignInPasswordChange(value: String) = edit { copy(signInPassword = value, fieldErrors = fieldErrors - Field.SIGN_IN_PASSWORD) }

    fun onShake() {
        val current = _state.value
        if (current.origin is RegistrationOrigin.FromRequestToJoin) return
        val mode = if (current.mode == Role.STUDENT) Role.COACH else Role.STUDENT
        _state.update { it.copy(mode = mode, showEmployeeId = mode == Role.STUDENT) }
    }

    fun onSubmit() {
        val current = _state.value
        if (current.isSubmitting) return
        val errors = validateRegistration(current)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }
        if (network.isOffline()) {
            _state.update { it.copy(error = UiError.Offline) }
            return
        }
        _state.update { it.copy(isSubmitting = true, fieldErrors = emptyMap(), error = null) }
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val result = if (current.mode == Role.STUDENT) {
                    auth.registerStudent(
                        name = current.name.trim(),
                        email = current.email.trim(),
                        password = current.password,
                        employeeId = current.employeeId.trim(),
                    )
                } else {
                    auth.registerCoach(
                        name = current.name.trim(),
                        email = current.email.trim(),
                        password = current.password,
                    )
                }
                when (result) {
                    is RegisterResult.Success -> onAccountReady(result.account)
                    RegisterResult.EmailTaken -> _state.update {
                        it.copy(fieldErrors = mapOf(Field.EMAIL to FieldError.EmailTaken))
                    }
                    RegisterResult.EmployeeIdTaken -> _state.update {
                        it.copy(fieldErrors = mapOf(Field.EMPLOYEE_ID to FieldError.EmployeeIdTaken))
                    }
                    RegisterResult.Offline -> _state.update { it.copy(error = UiError.Offline) }
                }
            } catch (e: IllegalArgumentException) {
                _state.update { it.copy(error = e.asUiError() ?: UiError.AuthFailed) }
            } catch (e: IllegalStateException) {
                _state.update { it.copy(error = e.asUiError()) }
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
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                when (val result = auth.signIn(current.signInEmail.trim(), current.signInPassword)) {
                    is SignInResult.Success -> onAccountReady(result.account)
                    SignInResult.Failed -> _state.update { it.copy(signInError = UiError.AuthFailed) }
                    SignInResult.Offline -> _state.update { it.copy(error = UiError.Offline) }
                }
            } catch (e: IllegalArgumentException) {
                _state.update { it.copy(signInError = UiError.AuthFailed) }
            } catch (e: IllegalStateException) {
                _state.update { it.copy(signInError = UiError.AuthFailed, error = e.asUiError()) }
            } finally {
                _state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun onNavigationHandled() {
        _state.update { it.copy(navigation = null) }
    }

    private fun onAccountReady(account: SignedInAccount) {
        session.save(account)
        people.upsert(PersonProfile(account.id, account.name, account.role, account.employeeId))
        val origin = _state.value.origin
        var outcome: JoinOutcome? = null
        val openClassId = if (origin is RegistrationOrigin.FromRequestToJoin && account.role == Role.STUDENT) {
            val join = requestToJoin(classId = origin.classId, studentId = account.id)
            if (join is JoinResult.ClassCancelled) outcome = JoinOutcome.ClassCancelled
            origin.classId
        } else {
            null
        }
        _state.update {
            it.copy(
                completion = AuthCompletion(account.role, openClassId),
                joinOutcome = outcome,
                signInError = null,
                error = null,
            )
        }
    }

    private fun header(origin: RegistrationOrigin): RequestingClassHeader? {
        val classId = (origin as? RegistrationOrigin.FromRequestToJoin)?.classId ?: return null
        val trxClass = classes.findClass(classId) ?: return null
        return RequestingClassHeader(
            name = trxClass.name,
            coachName = people.find(trxClass.coachId)?.name.orEmpty(),
            slotsByWeekday = trxClass.slotsByWeekday,
        )
    }

    private fun edit(block: RegistrationUiState.() -> RegistrationUiState) {
        _state.update(block)
    }
}

private val EMAIL = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")

private fun validateRegistration(state: RegistrationUiState): Map<Field, FieldError> = buildMap {
    if (state.name.isBlank()) put(Field.NAME, FieldError.Required)
    val email = state.email.trim()
    when {
        email.isEmpty() -> put(Field.EMAIL, FieldError.Required)
        !EMAIL.matches(email) -> put(Field.EMAIL, FieldError.InvalidEmail)
    }
    when {
        state.password.isBlank() -> put(Field.PASSWORD, FieldError.Required)
        state.password.length < 8 -> put(Field.PASSWORD, FieldError.PasswordTooShort)
    }
    if (state.mode == Role.STUDENT && state.employeeId.isBlank()) {
        put(Field.EMPLOYEE_ID, FieldError.Required)
    }
}
