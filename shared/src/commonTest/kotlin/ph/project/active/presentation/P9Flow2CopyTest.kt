package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.Visibility
import ph.project.active.presentation.classdetail.ClassContent
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.UiError
import ph.project.active.presentation.common.formatDaysLong
import ph.project.active.testkit.SAT_31_OCT_2026

class P9Flow2CopyTest {
    @Test
    fun P9_flow2CopyAndDayAndTimeLinesMatchTheSpec() {
        assertEquals("Classes", UiCopy.guestListTitle())
        assertEquals("Public TRX classes. No account needed.", UiCopy.guestListSubtitle())
        assertEquals("PUBLIC CLASSES", UiCopy.publicClassesHeader())
        assertEquals("1 class", UiCopy.classCount(1))
        assertEquals("5 classes", UiCopy.classCount(5))
        assertEquals("No public classes yet", UiCopy.guestListEmptyTitle())
        assertEquals(
            "When a coach creates a public class, it shows up here.",
            UiCopy.guestListEmptyBody(),
        )
        assertEquals("Create an account to request a class.", UiCopy.createAccountToRequest())
        assertEquals("Class details", UiCopy.classDetailsHeading())
        assertEquals("Coach", UiCopy.detailCoachLabel())
        assertEquals("Days", UiCopy.detailDaysLabel())
        assertEquals("Time", UiCopy.detailTimeLabel())
        assertEquals("Runs", UiCopy.detailRunsLabel())
        assertEquals("Until Sat 31 Oct 2026", UiCopy.runsUntil(SAT_31_OCT_2026))
        assertEquals("No end date", UiCopy.noEndDate())
        assertEquals(
            "You\u2019ll create a student account first. Your request is sent after the account exists.",
            UiCopy.requestToJoinGuestNote(),
        )
        assertEquals(
            "Your request goes to Coach Mara Santos for approval.",
            UiCopy.requestToJoinStudentNote("Mara Santos"),
        )
        assertEquals(
            "Coach Mara Santos will approve or decline your request. You don\u2019t need to send it again.",
            UiCopy.pendingApprovalBody("Mara Santos"),
        )
        assertEquals(
            "You can send a new request. The coach will need to approve it again.",
            UiCopy.requestAgainBody(),
        )
        assertEquals("You\u2019re on this class", UiCopy.joinedTitle())
        assertEquals(
            "Record attendance from the Attend tab during each session. Your dates are in Sessions.",
            UiCopy.joinedBody(),
        )
        assertEquals(
            "Coach Mara Santos invited you. Accept or decline it in Invites.",
            UiCopy.invitedBody("Mara Santos"),
        )
        assertEquals("This class has been cancelled.", UiCopy.classCancelledBody())
        assertEquals(
            "This class ended on Sat 31 Oct 2026.",
            UiCopy.classEndedOn(SAT_31_OCT_2026),
        )
        assertEquals("Class not available", UiCopy.classUnavailableTitle())
        assertEquals("It may be private or no longer listed.", UiCopy.classUnavailableBody())
        assertEquals(
            "This class has been cancelled. Nothing was sent.",
            UiCopy.uiError(UiError.ClassCancelled),
        )
        assertEquals(
            "This class is private. Joining is by coach invite only.",
            UiCopy.uiError(UiError.PrivateClass),
        )
        assertEquals("This date is cancelled.", UiCopy.sessionCancelled())
        assertEquals(
            "Your account is ready, but no request was sent because this class was cancelled.",
            UiCopy.accountReadyClassCancelled(),
        )
        assertEquals(
            "Your account is ready, but no request was sent because this class isn\u2019t available.",
            UiCopy.accountReadyClassUnavailable(),
        )
        assertEquals("Monday and Wednesday", formatDaysLong(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)))
        assertEquals(
            "Monday, Tuesday and Thursday",
            formatDaysLong(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)),
        )
        assertEquals("Every day", formatDaysLong(DayOfWeek.entries.toSet()))

        val evening = TimeSlot(LocalTime(17, 30), LocalTime(18, 30))
        val morning = TimeSlot(LocalTime(7, 0), LocalTime(8, 0))
        val sameTime = ClassContent(
            name = "TRX Core Burn",
            visibility = Visibility.PUBLIC,
            coachName = "Mara Santos",
            slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(evening), DayOfWeek.WEDNESDAY to listOf(evening)),
            endDate = LocalDate(2026, 10, 31),
        )
        assertEquals(listOf("5:30 PM \u2013 6:30 PM"), sameTime.timeLines())
        val differentTimes = sameTime.copy(
            slotsByWeekday = mapOf(DayOfWeek.MONDAY to listOf(evening), DayOfWeek.WEDNESDAY to listOf(morning)),
        )
        assertEquals(
            listOf("Mon 5:30 PM \u2013 6:30 PM", "Wed 7:00 AM \u2013 8:00 AM"),
            differentTimes.timeLines(),
        )
    }
}
