package ph.project.active.ui.testing

import kotlinx.coroutines.CompletableDeferred
import ph.project.active.data.model.RegisterResult
import ph.project.active.data.model.SignInResult
import ph.project.active.data.repository.AuthRepository

class GatedAuthRepository(
    private val delegate: AuthRepository,
) : AuthRepository {
    val gate = CompletableDeferred<Unit>()
    var registerStudentCalls: Int = 0
        private set
    var registerCoachCalls: Int = 0
        private set
    var signInCalls: Int = 0
        private set

    override suspend fun registerStudent(
        name: String,
        email: String,
        password: String,
        employeeId: String,
    ): RegisterResult {
        registerStudentCalls += 1
        gate.await()
        return delegate.registerStudent(name, email, password, employeeId)
    }

    override suspend fun registerCoach(name: String, email: String, password: String): RegisterResult {
        registerCoachCalls += 1
        gate.await()
        return delegate.registerCoach(name, email, password)
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        signInCalls += 1
        gate.await()
        return delegate.signIn(email, password)
    }

    fun release() {
        gate.complete(Unit)
    }
}
