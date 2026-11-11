package ph.project.active.data

import ph.project.active.domain.AuthFailed
import ph.project.active.domain.InMemoryClassRepository
import ph.project.active.domain.InMemoryMembershipRepository
import ph.project.active.domain.NetworkStatus
import ph.project.active.domain.PasswordHasher
import ph.project.active.domain.RegisterCoach
import ph.project.active.domain.RegisterStudent
import ph.project.active.domain.RequestToJoin
import ph.project.active.domain.SignIn
import ph.project.active.domain.User
import ph.project.active.domain.UserRepository

class FakeAuthRepository(
    directory: UserRepository,
    hasher: PasswordHasher,
    private val network: NetworkMonitor,
    private val session: SessionStore,
    private val people: PersonRepository,
) : AuthRepository {
    private val registerStudentUseCase = RegisterStudent(
        users = directory,
        hasher = hasher,
        requestToJoin = RequestToJoin(InMemoryClassRepository(), InMemoryMembershipRepository()),
    )
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
            accept(registerStudentUseCase(name, email, password, employeeId))
        } catch (e: IllegalArgumentException) {
            taken(e)
        }
    }

    override suspend fun registerCoach(name: String, email: String, password: String): RegisterResult {
        if (network.status.value == NetworkStatus.OFFLINE) return RegisterResult.Offline
        return try {
            accept(registerCoachUseCase(name, email, password))
        } catch (e: IllegalArgumentException) {
            taken(e)
        }
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        if (network.status.value == NetworkStatus.OFFLINE) return SignInResult.Offline
        return try {
            acceptSignIn(signInUseCase(email, password))
        } catch (e: AuthFailed) {
            SignInResult.Failed
        }
    }

    private fun acceptSignIn(user: User): SignInResult {
        val account = user.toSignedInAccount()
        session.save(account)
        people.upsert(user.toPersonProfile())
        return SignInResult.Success(account)
    }

    private fun accept(user: User): RegisterResult {
        val account = user.toSignedInAccount()
        session.save(account)
        people.upsert(user.toPersonProfile())
        return RegisterResult.Success(account)
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
