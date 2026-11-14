package ph.project.active.data.fake

import ph.project.active.data.model.RegisterResult
import ph.project.active.data.model.SignInResult
import ph.project.active.data.model.SignedInAccount
import ph.project.active.data.repository.AuthRepository
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.repository.PersonRepository
import ph.project.active.data.repository.SessionStore
import ph.project.active.data.repository.toPersonProfile
import ph.project.active.data.repository.toSignedInAccount
import ph.project.active.domain.crypto.PasswordHasher
import ph.project.active.domain.model.AuthFailed
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.User
import ph.project.active.domain.repository.UserRepository
import ph.project.active.domain.usecase.RegisterCoach
import ph.project.active.domain.usecase.RegisterStudent
import ph.project.active.domain.usecase.SignIn

class FakeAuthRepository(
    directory: UserRepository,
    hasher: PasswordHasher,
    private val network: NetworkMonitor,
    private val session: SessionStore,
    private val people: PersonRepository,
) : AuthRepository {
    private val registerStudentUseCase = RegisterStudent(users = directory, hasher = hasher)
    private val registerCoachUseCase = RegisterCoach(users = directory, hasher = hasher)
    private val signInUseCase = SignIn(users = directory, hasher = hasher)

    override suspend fun registerStudent(
        name: String,
        email: String,
        password: String,
        employeeId: String,
    ): RegisterResult {
        if (network.status.value == NetworkStatus.OFFLINE) return RegisterResult.Offline
        return try {
            stored(registerStudentUseCase(name, email, password, employeeId))
        } catch (e: IllegalArgumentException) {
            taken(e)
        }
    }

    override suspend fun registerCoach(name: String, email: String, password: String): RegisterResult {
        if (network.status.value == NetworkStatus.OFFLINE) return RegisterResult.Offline
        return try {
            stored(registerCoachUseCase(name, email, password))
        } catch (e: IllegalArgumentException) {
            taken(e)
        }
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        if (network.status.value == NetworkStatus.OFFLINE) return SignInResult.Offline
        return try {
            SignInResult.Success(remember(signInUseCase(email, password)))
        } catch (e: AuthFailed) {
            SignInResult.Failed
        }
    }

    private fun stored(user: User): RegisterResult = RegisterResult.Success(remember(user))

    private fun remember(user: User): SignedInAccount {
        val account = user.toSignedInAccount()
        session.save(account)
        people.upsert(user.toPersonProfile())
        return account
    }

    private fun taken(error: IllegalArgumentException): RegisterResult {
        val message = error.message.orEmpty()
        return when {
            "Employee ID is already registered" in message -> RegisterResult.EmployeeIdTaken
            "Email is already registered" in message -> RegisterResult.EmailTaken
            else -> throw error
        }
    }
}
