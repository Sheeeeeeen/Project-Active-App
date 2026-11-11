package ph.project.active.data.repository

import ph.project.active.data.model.RegisterResult
import ph.project.active.data.model.SignInResult

interface AuthRepository {
    suspend fun registerStudent(name: String, email: String, password: String, employeeId: String): RegisterResult
    suspend fun registerCoach(name: String, email: String, password: String): RegisterResult
    suspend fun signIn(email: String, password: String): SignInResult
}
