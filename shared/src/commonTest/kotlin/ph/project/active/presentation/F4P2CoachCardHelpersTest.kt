package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.coachclasses.CoachClassItem
import ph.project.active.presentation.coachclasses.dayLine
import ph.project.active.presentation.coachclasses.metaLine
import ph.project.active.presentation.coachclasses.spokenCard
import ph.project.active.presentation.coachclasses.timeLine
import ph.project.active.testkit.EVENING

class F4P2CoachCardHelpersTest {
    private fun coreBurn(
        students: Int,
        requests: Int,
        visibility: Visibility = Visibility.PUBLIC,
        cancelled: Boolean = false,
    ) = CoachClassItem(
        classId = "class-1",
        name = "TRX Core Burn",
        visibility = visibility,
        slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(EVENING), DayOfWeek.WEDNESDAY to listOf(EVENING)),
        rosterCount = students,
        pendingRequestCount = requests,
        isCancelled = cancelled,
    )

    @Test
    fun F4_P2_cardLinesMatchD1() {
        val item = coreBurn(students = 4, requests = 3)
        assertEquals("Mon \u00B7 Wed", item.dayLine())
        assertEquals("5:30 PM \u2013 6:30 PM", item.timeLine())
        assertEquals("4 students \u00B7 3 requests", item.metaLine())
        assertEquals("12 students", coreBurn(students = 12, requests = 0).metaLine())
    }

    @Test
    fun F4_P2_metaLineUsesSingularAndZeroForms() {
        assertEquals("0 students", coreBurn(students = 0, requests = 0).metaLine())
        assertEquals("1 student", coreBurn(students = 1, requests = 0).metaLine())
        assertEquals("1 student \u00B7 1 request", coreBurn(students = 1, requests = 1).metaLine())
    }

    @Test
    fun F4_P2_spokenCardFollowsTheGuestCardPattern() {
        // Spec P2: "same pattern as GuestClassItem / StudentClassItem": name first, long day names, "to" for the dash.
        val spoken = coreBurn(students = 4, requests = 3).spokenCard()
        assertTrue(spoken.startsWith("TRX Core Burn, "), spoken)
        assertTrue("Monday and Wednesday" in spoken, spoken)
        assertTrue("5:30 PM to 6:30 PM" in spoken, spoken)
        assertTrue("\u2013" !in spoken, spoken)
    }

    @Test
    fun F4_P2_Q8_spokenCardIsExactInTheGuestCardOrder() {
        // Section 10 Q8: name, Public / Private / Cancelled, long days, time with "to", students, requests only when > 0.
        assertEquals(
            "TRX Core Burn, Public, Monday and Wednesday, 5:30 PM to 6:30 PM, 4 students, 3 requests",
            coreBurn(students = 4, requests = 3).spokenCard(),
        )
        assertEquals(
            "TRX Core Burn, Private, Monday and Wednesday, 5:30 PM to 6:30 PM, 1 student",
            coreBurn(students = 1, requests = 0, visibility = Visibility.PRIVATE).spokenCard(),
        )
        assertEquals(
            "TRX Core Burn, Cancelled, Monday and Wednesday, 5:30 PM to 6:30 PM, 0 students, 1 request",
            coreBurn(students = 0, requests = 1, cancelled = true).spokenCard(),
        )
    }
}
