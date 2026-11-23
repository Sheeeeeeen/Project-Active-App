package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.presentation.common.UiCopy

class F3P7Flow3CopyTest {
    @Test
    fun F3_P7_flow3CopyMatchesTheSpec() {
        assertEquals("Classes", UiCopy.classesTitle())
        assertEquals("Hi Paolo \u00B7 EMP-20417", UiCopy.classesGreeting("Paolo Reyes", "EMP-20417"))
        assertEquals("MY CLASSES", UiCopy.myClassesHeader())
        assertEquals("Public classes", UiCopy.studentPublicClassesHeader())
        assertEquals("1 joined", UiCopy.joinedCount(1))
        assertEquals("3 joined", UiCopy.joinedCount(3))
        assertEquals("No classes yet", UiCopy.classesEmptyTitle())
        assertEquals(
            "When you join a class, or when a coach creates a public class, it shows up here.",
            UiCopy.classesEmptyBody(),
        )
        assertEquals("Invites", UiCopy.invitesTitle())
        assertEquals("Accept to join a class roster.", UiCopy.invitesSubtitle())
        assertEquals("Coach Leo Ramirez invited you.", UiCopy.inviteInvitedYou("Leo Ramirez"))
        assertEquals("Invite declined", UiCopy.inviteDeclined())
        assertEquals("Classes", UiCopy.tabClasses())
        assertEquals("Attend", UiCopy.tabAttend())
        assertEquals("Sessions", UiCopy.tabSessions())
        assertEquals("Invites", UiCopy.tabInvites())

        assertEquals("You joined TRX Mobility Flow. It\u2019s now in My classes.", UiCopy.inviteAccepted("TRX Mobility Flow"))
        assertEquals("No invites yet", UiCopy.invitesEmptyTitle())
        assertEquals(
            "When a coach invites you to a class, it shows up here. You can accept or decline it.",
            UiCopy.invitesEmptyBody(),
        )
    }

    // F3-AC1 / section 5: the greeting uses the first word of the name.
    // Section 9 item 7: with no employee ID it is "Hi {first name}" with no middle dot.
    @Test
    fun F3_P7_greetingWithoutEmployeeIdIsFirstNameOnlyWithNoDot() {
        assertEquals("Hi Paolo", UiCopy.classesGreeting("Paolo Reyes", null))
    }
}
