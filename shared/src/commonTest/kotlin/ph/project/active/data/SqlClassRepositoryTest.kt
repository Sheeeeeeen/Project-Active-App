package ph.project.active.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.Session
import ph.project.active.domain.SessionRef
import ph.project.active.domain.SessionStatus
import ph.project.active.domain.TimeSlot
import ph.project.active.domain.TrxClass
import ph.project.active.domain.Visibility
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.FRI_25_SEP_2026
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MON_WED_EVENING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.SAT_31_OCT_2026
import ph.project.active.testkit.WED_30_SEP_2026

class SqlClassRepositoryTest {

    @Test
    fun AC3_class_round_trips_updates_in_place_and_lists_creation_order() {
        inMemoryDb().use { db ->
            val repository = SqlClassRepository(db.database)
            val morning = TimeSlot(LocalTime(7, 0), LocalTime(8, 0))
            val later = TimeSlot(LocalTime(18, 0), LocalTime(19, 0))
            val first = sampleClass(
                id = "class-1",
                slots = mapOf(DayOfWeek.MONDAY to listOf(later, morning)),
                endDate = null,
                cancelledAt = null,
            )
            val second = sampleClass(
                id = "class-2",
                name = "Second",
                slots = mapOf(DayOfWeek.FRIDAY to listOf(EVENING)),
                endDate = SAT_31_OCT_2026,
                cancelledAt = Instant.parse("2026-10-01T00:00:00Z"),
            )

            repository.saveClass(first)
            repository.saveClass(second)
            assertEquals(first, repository.findClass(first.id))
            assertEquals(second, repository.findClass(second.id))
            assertEquals(listOf(later, morning), repository.findClass(first.id)!!.slotsByWeekday.getValue(DayOfWeek.MONDAY))

            val renamed = first.copy(name = "Renamed", cancelledAt = Instant.parse("2026-11-01T00:00:00Z"))
            repository.saveClass(renamed)
            assertEquals(listOf("class-1", "class-2"), repository.allClasses().map { it.id })
            assertEquals(renamed, repository.findClass(first.id))
        }
    }

    @Test
    fun AC4_sessions_upsert_by_id_and_find_by_ref() {
        inMemoryDb().use { db ->
            val repository = SqlClassRepository(db.database)
            val later = session(id = "s-2", number = 2, status = SessionStatus.CANCELLED)
            val earlier = session(id = "s-1", number = 1, status = SessionStatus.SCHEDULED)
            repository.saveSessions(listOf(later, earlier))

            assertEquals(listOf(1, 2), repository.sessionsOf("class-1").map { it.sessionNumber })
            assertEquals(SessionStatus.CANCELLED, repository.sessionsOf("class-1").single { it.id == "s-2" }.status)
            assertEquals(earlier, repository.findSession(SessionRef("class-1", MON_28_SEP_2026, EVENING)))
            assertEquals(later, repository.findSession(SessionRef("class-1", WED_30_SEP_2026, EVENING)))

            val updated = earlier.copy(status = SessionStatus.CANCELLED, sessionNumber = 1)
            repository.saveSessions(listOf(updated))
            assertEquals(listOf(updated, later), repository.sessionsOf("class-1").sortedBy { it.sessionNumber })
        }
    }

    @Test
    fun AC5_create_class_on_sql_persists_the_ten_ac7_sessions() {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createClass(
                coachId = coach.id,
                name = "TRX Evening",
                visibility = Visibility.PUBLIC,
                slotsByWeekday = MON_WED_EVENING,
                endDate = SAT_31_OCT_2026,
            )
            val sessions = world.sessionsOf(trxClass.id)
            assertEquals(
                listOf(
                    LocalDate(2026, 9, 28), LocalDate(2026, 9, 30),
                    LocalDate(2026, 10, 5), LocalDate(2026, 10, 7),
                    LocalDate(2026, 10, 12), LocalDate(2026, 10, 14),
                    LocalDate(2026, 10, 19), LocalDate(2026, 10, 21),
                    LocalDate(2026, 10, 26), LocalDate(2026, 10, 28),
                ),
                sessions.map { it.date },
            )
            assertEquals((1..10).toList(), sessions.map { it.sessionNumber })
            assertEquals(List(10) { SessionStatus.SCHEDULED }, sessions.map { it.status })
            assertEquals(List(10) { EVENING }, sessions.map { it.slot })
        }
    }
}

private fun sampleClass(
    id: String,
    name: String = "TRX",
    slots: Map<DayOfWeek, List<TimeSlot>>,
    endDate: LocalDate?,
    cancelledAt: Instant?,
): TrxClass = TrxClass(
    id = id,
    coachId = "coach-1",
    name = name,
    visibility = Visibility.PUBLIC,
    slotsByWeekday = slots,
    endDate = endDate,
    createdOn = FRI_25_SEP_2026,
    cancelledAt = cancelledAt,
)

private fun session(id: String, number: Int, status: SessionStatus): Session = Session(
    id = id,
    classId = "class-1",
    date = if (number == 1) MON_28_SEP_2026 else WED_30_SEP_2026,
    slot = EVENING,
    sessionNumber = number,
    status = status,
)

private inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R = try {
    block(this)
} finally {
    close()
}
