package ph.project.active.domain.repository

import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.SessionRef
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.Session

interface ClassRepository {
    fun nextClassId(): String
    fun nextSessionId(): String
    fun allClasses(): List<TrxClass>
    fun findClass(id: String): TrxClass?
    fun saveClass(trxClass: TrxClass)
    fun sessionsOf(classId: String): List<Session>
    fun findSession(ref: SessionRef): Session?
    fun saveSessions(sessions: List<Session>)
}

fun ClassRepository.sessionsOn(classId: String, date: LocalDate): List<Session> =
    sessionsOf(classId).filter { it.date == date }
