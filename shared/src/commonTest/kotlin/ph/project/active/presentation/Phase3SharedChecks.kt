package ph.project.active.presentation

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.data.model.RegisterResult
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.crypto.PasswordHasher
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.data.model.SignInResult
import ph.project.active.presentation.common.Field
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.registration.AuthCompletion
import ph.project.active.presentation.registration.RegistrationUiState
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.ScriptedAuthRepository
import ph.project.active.testkit.idle
import ph.project.active.testkit.runVm

internal object Phase3SharedChecks {
    fun studentRegistrationFromClassList() = runVm {
        Phase2World().use { world ->
            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")
            val auth = ScriptedAuthRepository().apply {
                studentResult = RegisterResult.Success(account)
            }
            val viewModel = filled(world.registration(RegistrationOrigin.FromClassList, auth))
            viewModel.onSubmit()
            idle()
            assertSignedInNavigation(viewModel, Role.STUDENT, null)
        }
    }

    fun coachRegistrationFromClassList() = runVm {
        Phase2World().use { world ->
            val account = SignedInAccount("c", "Ada", "ada@trx.test", Role.COACH, null)
            val auth = ScriptedAuthRepository().apply {
                coachResult = RegisterResult.Success(account)
            }
            val viewModel = filled(world.registration(RegistrationOrigin.FromClassList, auth))
            viewModel.onShake()
            viewModel.onSubmit()
            idle()
            assertSignedInNavigation(viewModel, Role.COACH, null)
        }
    }

    fun studentRegistrationFromRequestToJoin() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")
            val auth = ScriptedAuthRepository().apply {
                studentResult = RegisterResult.Success(account)
            }
            val viewModel = filled(
                world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth),
            )
            viewModel.onSubmit()
            idle()
            assertSignedInNavigation(viewModel, Role.STUDENT, trxClass.id)
        }
    }

    fun signInFromClassList() = runVm {
        Phase2World().use { world ->
            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-1")
            val auth = ScriptedAuthRepository().apply {
                signInResult = SignInResult.Success(account)
            }
            val viewModel = world.registration(RegistrationOrigin.FromClassList, auth)
            viewModel.onShake()
            viewModel.onSignInEmailChange(account.email)
            viewModel.onSignInPasswordChange("password1")
            viewModel.onSignIn()
            idle()
            assertSignedInNavigation(viewModel, Role.STUDENT, null)
        }
    }

    fun coachSignInFromRequestDoesNotOpenAClass() = runVm {
        Phase2World().use { world ->
            val trxClass = world.createMonWedEveningClass(world.coach().id)
            val account = world.coachAccount(2)
            val auth = ScriptedAuthRepository().apply {
                signInResult = SignInResult.Success(account)
            }
            val viewModel = world.registration(RegistrationOrigin.FromRequestToJoin(trxClass.id), auth)
            viewModel.onSignInEmailChange(account.email)
            viewModel.onSignInPasswordChange("password1")
            viewModel.onSignIn()
            idle()
            assertSignedInNavigation(viewModel, Role.COACH, null)
        }
    }

    fun navigationClearsAfterSuccess() = runVm {
        Phase2World().use { world ->
            val account = SignedInAccount("s", "Ada", "ada@trx.test", Role.STUDENT, "EMP-9")
            val auth = ScriptedAuthRepository().apply {
                studentResult = RegisterResult.Success(account)
            }
            val viewModel = filled(world.registration(RegistrationOrigin.FromClassList, auth))
            viewModel.onSubmit()
            idle()
            assertSignedInNavigation(viewModel, Role.STUDENT, null)
            viewModel.onNavigationHandled()
            assertNull(viewModel.state.value.navigation)
            assertEquals(AuthCompletion(Role.STUDENT, null), viewModel.state.value.completion)
        }
    }

    fun fakePasswordHasherRoundTrip() {
        val hasher = fakePasswordHasher()
        val password = "p@ss-w0rd"
        val hash = hasher.hash(password)
        assertEquals(hash, hasher.hash(password))
        assertTrue(hasher.verify(password, hash))
        assertFalse(hasher.verify("other", hash))
    }

    fun canSubmitFollowsVisibleFields() {
        val getter = state().javaClass.methods.firstOrNull { method ->
            method.name == "getCanSubmit" && method.parameterCount == 0
        } ?: throw AssertionError("RegistrationUiState.canSubmit is not defined")
        fun can(value: RegistrationUiState): Boolean = getter.invoke(value) as Boolean

        assertFalse(can(state()))
        assertTrue(
            can(
                state(
                    name = "Ella Navarro",
                    email = "ella.navarro@acme.test",
                    password = "password1",
                    employeeId = "EMP-20417",
                ),
            ),
        )
        assertFalse(
            can(
                state(
                    name = " ",
                    email = "ella.navarro@acme.test",
                    password = "password1",
                    employeeId = "EMP-20417",
                ),
            ),
        )
        assertFalse(
            can(
                state(
                    name = "Ella Navarro",
                    email = " ",
                    password = "password1",
                    employeeId = "EMP-20417",
                ),
            ),
        )
        assertFalse(
            can(
                state(
                    name = "Ella Navarro",
                    email = "ella.navarro@acme.test",
                    password = " ",
                    employeeId = "EMP-20417",
                ),
            ),
        )
        assertFalse(
            can(
                state(
                    name = "Ella Navarro",
                    email = "ella.navarro@acme.test",
                    password = "password1",
                    employeeId = " ",
                ),
            ),
        )
        assertFalse(
            can(
                state(
                    name = "Ella Navarro",
                    email = "ella.navarro@acme.test",
                    password = "password1",
                    employeeId = "EMP-20417",
                    isSubmitting = true,
                ),
            ),
        )
        assertTrue(
            can(
                state(
                    mode = Role.COACH,
                    name = "Mara Santos",
                    email = "mara.santos@acme.test",
                    password = "password1",
                    employeeId = "",
                ),
            ),
        )
        assertTrue(
            can(
                state(
                    mode = Role.COACH,
                    name = "Mara Santos",
                    email = "mara.santos@acme.test",
                    password = "password1",
                    employeeId = "EMP-20417",
                ),
            ),
        )
        assertFalse(
            can(
                state(
                    mode = Role.COACH,
                    name = "Mara Santos",
                    email = "mara.santos@acme.test",
                    password = "",
                    employeeId = "",
                ),
            ),
        )
    }

    fun flow1CopyMatchesUiCopy() {
        val returned = uiCopyReturns()
        val missing = FLOW1_COPY.filter { it !in returned }
        assertTrue(
            missing.isEmpty(),
            "UiCopy is missing flow 1 strings:\n" + missing.joinToString("\n") { "  $it" },
        )
    }

    private fun assertSignedInNavigation(
        viewModel: RegistrationViewModel,
        role: Role,
        openClassId: String?,
    ) {
        val completion = assertNotNull(viewModel.state.value.completion)
        assertEquals(AuthCompletion(role, openClassId), completion)
        val navigation = assertNotNull(
            viewModel.state.value.navigation,
            "expected OpenSignedInRoot($role, $openClassId) but navigation was null",
        )
        assertEquals("OpenSignedInRoot", navigation::class.simpleName)
        assertEquals(role, navigation.javaClass.getMethod("getRole").invoke(navigation))
        assertEquals(openClassId, navigation.javaClass.getMethod("getOpenClassId").invoke(navigation))
        assertEquals(completion.role, navigation.javaClass.getMethod("getRole").invoke(navigation))
        assertEquals(
            completion.openClassId,
            navigation.javaClass.getMethod("getOpenClassId").invoke(navigation),
        )
    }

    private fun fakePasswordHasher(): PasswordHasher {
        val clazz = try {
            Class.forName("ph.project.active.data.fake.FakePasswordHasher")
        } catch (missing: ClassNotFoundException) {
            throw AssertionError(
                "FakePasswordHasher is missing from ph.project.active.data.fake",
                missing,
            )
        }
        val instance = clazz.getDeclaredConstructor().newInstance()
        check(instance is PasswordHasher) { "FakePasswordHasher must implement PasswordHasher" }
        return instance
    }

    private fun filled(viewModel: RegistrationViewModel): RegistrationViewModel {
        viewModel.onNameChange("Ada")
        viewModel.onEmailChange("ada@trx.test")
        viewModel.onPasswordChange("password1")
        viewModel.onEmployeeIdChange("EMP-9")
        return viewModel
    }

    private fun state(
        mode: Role = Role.STUDENT,
        name: String = "",
        email: String = "",
        password: String = "",
        employeeId: String = "",
        isSubmitting: Boolean = false,
    ): RegistrationUiState = RegistrationUiState(
        origin = RegistrationOrigin.FromClassList,
        mode = mode,
        showEmployeeId = mode == Role.STUDENT,
        name = name,
        email = email,
        password = password,
        employeeId = employeeId,
        signInEmail = "",
        signInPassword = "",
        fieldErrors = emptyMap(),
        isSubmitting = isSubmitting,
        requestingClass = null,
        signInError = null,
        completion = null,
        joinOutcome = null,
        error = null,
        connection = ConnectionUi(isOffline = false, banner = null),
        navigation = null,
    )

    private fun uiCopyReturns(): Set<String> {
        val returned = linkedSetOf<String>()
        val roles = listOf(Role.STUDENT, Role.COACH)
        val origins = listOf(
            RegistrationOrigin.FromClassList,
            RegistrationOrigin.FromRequestToJoin("class-1"),
        )
        val names = listOf(
            "TRX Core Burn",
            "Mara Santos",
            "Mon \u00B7 Wed",
            "5:30 PM \u2013 6:30 PM",
        )
        val fields = Field.entries
        for (method in UiCopy::class.java.declaredMethods) {
            if (method.returnType != String::class.java) continue
            if (method.name.contains("$")) continue
            if (method.parameterCount > 4) continue
            val choices = method.parameterTypes.map { type ->
                choicesFor(type, roles, origins, names, fields)
            }
            if (choices.any { it.isEmpty() }) continue
            for (args in product(choices)) {
                try {
                    returned += method.invoke(UiCopy, *args.toTypedArray()) as String
                } catch (_: Throwable) {
                }
            }
        }
        return returned
    }

    private fun choicesFor(
        type: Class<*>,
        roles: List<Role>,
        origins: List<RegistrationOrigin>,
        names: List<String>,
        fields: List<Field>,
    ): List<Any> = when {
        type == String::class.java -> names
        type == Role::class.java -> roles
        type == RegistrationOrigin::class.java -> origins
        type == Field::class.java -> fields
        type == java.lang.Boolean.TYPE || type == java.lang.Boolean::class.java -> listOf(true, false)
        else -> emptyList()
    }

    private fun product(choices: List<List<Any>>): List<List<Any>> {
        var acc = listOf(emptyList<Any>())
        for (choice in choices) {
            if (acc.size * choice.size > 256) return emptyList()
            acc = acc.flatMap { prefix -> choice.map { prefix + it } }
        }
        return acc
    }
}

private val FLOW1_COPY = listOf(
    "TRX ATTENDANCE",
    "COACH ACCOUNT",
    "Join classes and record your attendance.",
    "Create classes, invite students and show the daily code.",
    "Create your student account to send this request.",
    "Full name",
    "Email",
    "Password",
    "Employee ID",
    "Your name",
    "name@company.com",
    "At least 8 characters",
    "e.g. EMP-20417",
    "Your password",
    "At least 8 characters.",
    "Your company employee ID. Each ID can be registered once.",
    "Already have an account?",
    "Sign in",
    "REQUESTING TO JOIN",
    "Coach Mara Santos \u00B7 Mon \u00B7 Wed \u00B7 5:30 PM \u2013 6:30 PM",
    "Signing in also sends your request for TRX Core Burn.",
    "Classes",
    "Back",
    "Create account",
    "Coach registration",
    "Create coach account",
    "Create account and send request",
    "Creating account\u2026",
    "Enter a valid email address.",
    "This employee ID is already registered.",
    "No internet connection",
    "Required.",
    "This email is already registered.",
    "Email or password is incorrect.",
    "You\u2019re offline. Nothing was saved.",
    "Register or sign in",
)
