package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.classdetail.RequestingClassHeader
import ph.project.active.presentation.classdetail.scheduleLine

class P5RequestingClassHeaderScheduleLineTest {
    @Test
    fun P5_scheduleLineFormatsCoachDaysAndSlots() {
        val evening = TimeSlot(start = LocalTime(17, 30), end = LocalTime(18, 30))
        val header = RequestingClassHeader(
            name = "TRX Core Burn",
            coachName = "Mara Santos",
            slotsByWeekday = mapOf(
                DayOfWeek.MONDAY to listOf(evening),
                DayOfWeek.WEDNESDAY to listOf(evening),
            ),
        )
        assertEquals(
            "Coach Mara Santos \u00B7 Mon \u00B7 Wed \u00B7 5:30 PM \u2013 6:30 PM",
            header.scheduleLine(),
        )
    }

    @Test
    fun P5_scheduleLineSortsDistinctSlotsByStartTime() {
        val morning = TimeSlot(start = LocalTime(7, 0), end = LocalTime(8, 0))
        val evening = TimeSlot(start = LocalTime(17, 30), end = LocalTime(18, 30))
        val header = RequestingClassHeader(
            name = "TRX Core Burn",
            coachName = "Mara Santos",
            slotsByWeekday = mapOf(
                DayOfWeek.MONDAY to listOf(evening, morning),
            ),
        )
        assertEquals(
            "Coach Mara Santos \u00B7 Mon \u00B7 7:00 AM \u2013 8:00 AM, 5:30 PM \u2013 6:30 PM",
            header.scheduleLine(),
        )
    }
}
