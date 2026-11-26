package ph.project.active.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ph.project.active.domain.model.StudentSessionRow
import ph.project.active.domain.model.StudentSessionStatus
import ph.project.active.domain.model.TrxClass

/** Flow 7 spec S9 and decision 11: StudentSessions lists single cancelled dates as CANCELLED. F7-AC11. */
class F7CancelledSessionsTest {

    // F7-AC11: "StudentSessions includes Lunch Express Mon 28 with status CANCELLED and session number 1; Lunch Express Tue 29 is UPCOMING with number 2."
    @Test
    fun F7_AC11_aCancelledDateIsListedAsCancelledAndKeepsItsNumber() {
        val example = Flow7Example()

        val monday = example.rowOf(example.lunchExpress, MON_28_SEP_2026)
        val tuesday = example.rowOf(example.lunchExpress, TUE_29_SEP_2026)

        assertEquals(StudentSessionStatus.CANCELLED, monday.status)
        assertEquals(1, monday.session.sessionNumber)
        assertEquals(StudentSessionStatus.UPCOMING, tuesday.status)
        assertEquals(2, tuesday.session.sessionNumber)
    }

    // F7-AC11: "After the clock moves to Tue 29, Mon 28 Lunch Express is still CANCELLED, never ABSENT."
    @Test
    fun F7_AC11_aPastCancelledDateStaysCancelledNeverAbsent() {
        val example = Flow7Example()
        example.world.setLocal(TUE_29_SEP_2026, 9, 0)

        assertEquals(StudentSessionStatus.CANCELLED, example.rowOf(example.lunchExpress, MON_28_SEP_2026).status)
    }

    // F7-AC11: "After CancelClass on Core Burn, no CANCELLED Core Burn session is listed, and the past Mon 28 Core Burn session keeps ABSENT or PRESENT."
    @Test
    fun F7_AC11_aCancelledClassListsNoCancelledDatesAndKeepsItsPast() {
        val example = Flow7Example()
        example.world.setLocal(TUE_29_SEP_2026, 9, 0)
        example.world.cancelClass(example.coreBurn.id)

        val coreBurnRows = example.rowsOf(example.coreBurn)
        assertTrue(coreBurnRows.none { it.status == StudentSessionStatus.CANCELLED }, "no CANCELLED Core Burn rows: $coreBurnRows")
        assertEquals(listOf(MON_28_SEP_2026), coreBurnRows.map { it.session.date }, "only the past Mon 28 date stays")
        assertEquals(StudentSessionStatus.ABSENT, coreBurnRows.single().status)
    }

    // F7-AC11: "… the past Mon 28 Core Burn session keeps … PRESENT."
    @Test
    fun F7_AC11_aCancelledClassKeepsAPastPresent() {
        val example = Flow7Example()
        example.record(example.coreBurn, example.marasCode)
        example.world.setLocal(TUE_29_SEP_2026, 9, 0)
        example.world.cancelClass(example.coreBurn.id)

        val coreBurnRows = example.rowsOf(example.coreBurn)
        assertTrue(coreBurnRows.none { it.status == StudentSessionStatus.CANCELLED })
        assertEquals(StudentSessionStatus.PRESENT, coreBurnRows.single().status)
    }

    // F7-AC11: "A CANCELLED session with a record is CANCELLED."
    @Test
    fun F7_AC11_cancelledWinsOverARecord() {
        val example = Flow7Example()
        example.record(example.coreBurn, example.marasCode)
        example.world.cancelDate(example.coreBurn.id, MON_28_SEP_2026)

        val row = example.rowOf(example.coreBurn, MON_28_SEP_2026)
        assertEquals(StudentSessionStatus.CANCELLED, row.status)
        assertEquals(1, row.session.sessionNumber)
    }

    private fun Flow7Example.rowsOf(trxClass: TrxClass): List<StudentSessionRow> =
        world.studentSessions(studentId = paolo.id).filter { it.session.classId == trxClass.id }

    private fun Flow7Example.rowOf(trxClass: TrxClass, date: kotlinx.datetime.LocalDate): StudentSessionRow =
        rowsOf(trxClass).single { it.session.date == date }
}
