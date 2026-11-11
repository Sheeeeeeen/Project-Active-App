package ph.project.active.data

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.data.db.TrxDatabase
import ph.project.active.domain.ClassRepository
import ph.project.active.domain.Session
import ph.project.active.domain.SessionRef
import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.TrxClass
import ph.project.active.domain.Visibility

class SqlClassRepository(private val database: TrxDatabase) : ClassRepository {
    private val ids = SqlIdSource(database)
    private val queries get() = database.trxDatabaseQueries

    override fun nextClassId(): String = ids.next("class")

    override fun nextSessionId(): String = ids.next("session")

    override fun allClasses(): List<TrxClass> = queries.selectAllClasses().executeAsList().map { it.toClass() }

    override fun findClass(id: String): TrxClass? = queries.selectClassById(id).executeAsOneOrNull()?.toClass()

    override fun saveClass(trxClass: TrxClass) {
        database.transaction {
            val existing = queries.selectClassById(trxClass.id).executeAsOneOrNull()
            if (existing == null) {
                val order = queries.maxClassSortOrder().executeAsOne() + 1
                queries.insertClass(
                    trxClass.id,
                    trxClass.coachId,
                    trxClass.name,
                    trxClass.visibility.name,
                    trxClass.endDate?.toString(),
                    trxClass.createdOn.toString(),
                    trxClass.cancelledAt?.toString(),
                    order,
                )
            } else {
                queries.updateClass(
                    trxClass.coachId,
                    trxClass.name,
                    trxClass.visibility.name,
                    trxClass.endDate?.toString(),
                    trxClass.createdOn.toString(),
                    trxClass.cancelledAt?.toString(),
                    trxClass.id,
                )
            }
            queries.deleteSlots(trxClass.id)
            trxClass.slotsByWeekday.forEach { (day, slots) ->
                slots.forEachIndexed { index, slot ->
                    queries.insertSlot(
                        trxClass.id,
                        day.name,
                        slot.start.toString(),
                        slot.end.toString(),
                        index.toLong(),
                    )
                }
            }
        }
    }

    override fun sessionsOf(classId: String): List<Session> =
        queries.selectSessions(classId).executeAsList().map { it.toSession() }

    override fun findSession(ref: SessionRef): Session? =
        queries.selectSessionByRef(
            ref.classId,
            ref.date.toString(),
            ref.slot.start.toString(),
            ref.slot.end.toString(),
        ).executeAsList().firstOrNull()?.toSession()

    override fun saveSessions(sessions: List<Session>) {
        database.transaction {
            sessions.forEach { session ->
                queries.upsertSession(
                    session.id,
                    session.classId,
                    session.date.toString(),
                    session.slot.start.toString(),
                    session.slot.end.toString(),
                    session.sessionNumber.toLong(),
                    session.status.name,
                )
            }
        }
    }

    private fun ph.project.active.data.db.SelectAllClasses.toClass(): TrxClass = toTrxClass(
        id = id,
        coachId = coachId,
        name = name,
        visibility = visibility,
        endDate = endDate,
        createdOn = createdOn,
        cancelledAt = cancelledAt,
    )

    private fun ph.project.active.data.db.SelectClassById.toClass(): TrxClass = toTrxClass(
        id = id,
        coachId = coachId,
        name = name,
        visibility = visibility,
        endDate = endDate,
        createdOn = createdOn,
        cancelledAt = cancelledAt,
    )

    private fun toTrxClass(
        id: String,
        coachId: String,
        name: String,
        visibility: String,
        endDate: String?,
        createdOn: String,
        cancelledAt: String?,
    ): TrxClass {
        val slots = queries.selectSlots(id).executeAsList()
            .groupBy { DayOfWeek.valueOf(it.weekday) }
            .mapValues { (_, rows) ->
                rows.sortedBy { it.position }.map { row ->
                    TimeSlot(LocalTime.parse(row.startTime), LocalTime.parse(row.endTime))
                }
            }
        return TrxClass(
            id = id,
            coachId = coachId,
            name = name,
            visibility = Visibility.valueOf(visibility),
            slotsByWeekday = slots,
            endDate = endDate?.let(LocalDate::parse),
            createdOn = LocalDate.parse(createdOn),
            cancelledAt = cancelledAt?.let(Instant::parse),
        )
    }
}

private fun ph.project.active.data.db.SelectSessions.toSession(): Session = Session(
    id = id,
    classId = classId,
    date = LocalDate.parse(date),
    slot = TimeSlot(LocalTime.parse(startTime), LocalTime.parse(endTime)),
    sessionNumber = sessionNumber.toInt(),
    status = SessionStatus.valueOf(status),
)

private fun ph.project.active.data.db.SelectSessionByRef.toSession(): Session = Session(
    id = id,
    classId = classId,
    date = LocalDate.parse(date),
    slot = TimeSlot(LocalTime.parse(startTime), LocalTime.parse(endTime)),
    sessionNumber = sessionNumber.toInt(),
    status = SessionStatus.valueOf(status),
)
