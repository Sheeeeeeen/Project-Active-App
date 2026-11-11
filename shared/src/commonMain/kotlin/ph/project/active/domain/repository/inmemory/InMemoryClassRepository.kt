package ph.project.active.domain.repository.inmemory

import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.usecase.SequentialIds
import ph.project.active.domain.model.SessionRef
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.Session

class InMemoryClassRepository : ClassRepository {
    private val classes = mutableListOf<TrxClass>()
    private val sessions = mutableListOf<Session>()
    private val classIds = SequentialIds("class")
    private val sessionIds = SequentialIds("session")
    override fun nextClassId(): String = classIds.next()
    override fun nextSessionId(): String = sessionIds.next()

    override fun allClasses(): List<TrxClass> = classes.toList()
    override fun findClass(id: String): TrxClass? = classes.firstOrNull { it.id == id }
    override fun saveClass(trxClass: TrxClass) {
        val index = classes.indexOfFirst { it.id == trxClass.id }
        if (index >= 0) classes[index] = trxClass else classes += trxClass
    }

    override fun sessionsOf(classId: String): List<Session> = sessions.filter { it.classId == classId }
    override fun findSession(ref: SessionRef): Session? =
        sessions.firstOrNull { it.classId == ref.classId && it.date == ref.date && it.slot == ref.slot }

    override fun saveSessions(sessions: List<Session>) {
        for (session in sessions) {
            val index = this.sessions.indexOfFirst { it.id == session.id }
            if (index >= 0) this.sessions[index] = session else this.sessions += session
        }
    }
}
