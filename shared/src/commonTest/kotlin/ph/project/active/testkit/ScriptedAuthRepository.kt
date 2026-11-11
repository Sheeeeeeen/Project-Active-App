package ph.project.active.testkit

import kotlinx.coroutines.CompletableDeferred
import ph.project.active.data.AuthRepository
import ph.project.active.data.RegisterResult
import ph.project.active.data.SignInResult

/** Auth double whose calls suspend until [releaseStudents] when [holdStudents] is set. */
class ScriptedAuthRepository : AuthRepository {
    val studentCalls = mutableListOf<StudentRegistrationCall>()
    val coachCalls = mutableListOf<CoachRegistrationCall>()
    val signInCalls = mutableListOf<SignInCall>()

    var studentResult: RegisterResult = RegisterResult.EmailTaken
    var coachResult: RegisterResult = RegisterResult.EmailTaken
    var signInResult: SignInResult = SignInResult.Failed
    var holdStudents: Boolean = false

    private val studentGates = mutableListOf<CompletableDeferred<RegisterResult>>()

    override suspend fun registerStudent(
        name: String,
        email: String,
        password: String,
        employeeId: String,
    ): RegisterResult {
        studentCalls += StudentRegistrationCall(name, email, password, employeeId)
        if (!holdStudents) return studentResult
        val gate = CompletableDeferred<RegisterResult>()
        studentGates += gate
        return gate.await()
    }

    override suspend fun registerCoach(name: String, email: String, password: String): RegisterResult {
        coachCalls += CoachRegistrationCall(name, email, password)
        return coachResult
    }

    override suspend fun signIn(email: String, password: String): SignInResult {
        signInCalls += SignInCall(email, password)
        return signInResult
    }

    fun releaseStudents(result: RegisterResult) {
        studentGates.forEach { it.complete(result) }
    }
}
