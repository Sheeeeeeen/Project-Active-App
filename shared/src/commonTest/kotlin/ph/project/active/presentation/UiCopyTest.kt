package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.Role
import ph.project.active.presentation.common.RegistrationOrigin
import ph.project.active.presentation.common.UiCopy

class UiCopyTest {

    @Test
    fun AC84_connection_offline_and_unavailable_copy() {
        assertEquals("No internet connection", UiCopy.offlineBanner())
        assertEquals(
            "Scans are saved on this phone and upload when you are back online.",
            UiCopy.scannerOfflineSubtitle(),
        )
        assertEquals("Upload will retry when your connection returns.", UiCopy.savedOfflineSubtitle())
        assertEquals("Showing today’s code saved on this phone.", UiCopy.qrOfflineSubtitle())
        assertEquals("Today’s class list can’t load", UiCopy.classListUnavailableTitle())
        assertEquals(
            "The code still works. Students on the roster can scan it now; their phones upload later.",
            UiCopy.classListUnavailableBody(),
        )
    }

    @Test
    fun AC84_status_labels_and_request_again() {
        assertEquals("Request to join", UiCopy.requestToJoin())
        assertEquals("Pending", UiCopy.pending())
        assertEquals("Awaiting approval", UiCopy.awaitingApproval())
        assertEquals("Joined", UiCopy.joined())
        assertEquals("Invited", UiCopy.invited())
        assertEquals("Scheduled", UiCopy.scheduled())
        assertEquals("Cancelled", UiCopy.cancelled())
        assertEquals("Upcoming", UiCopy.upcoming())
        assertEquals("Present", UiCopy.present())
        assertEquals("Absent", UiCopy.absent())
        assertEquals("Upload pending", UiCopy.uploadPending())
        assertEquals("Public", UiCopy.visibilityPublic())
        assertEquals("Private", UiCopy.visibilityPrivate())
        assertEquals("You can request again", UiCopy.requestAgain())
    }

    @Test
    fun AC84_registration_field_and_scan_copy() {
        assertEquals("Create account", UiCopy.registrationTitle(Role.STUDENT))
        assertEquals("Coach registration", UiCopy.registrationTitle(Role.COACH))
        assertEquals("Create account", UiCopy.registrationSubmit(Role.STUDENT, RegistrationOrigin.FromClassList))
        assertEquals("Create coach account", UiCopy.registrationSubmit(Role.COACH, RegistrationOrigin.FromClassList))
        assertEquals(
            "Create account and send request",
            UiCopy.registrationSubmit(Role.STUDENT, RegistrationOrigin.FromRequestToJoin("class-1")),
        )
        assertEquals("Creating account…", UiCopy.registrationLoading())
        assertEquals("Enter a valid email address.", UiCopy.invalidEmail())
        assertEquals("This employee ID is already registered.", UiCopy.employeeIdTaken())
        assertEquals("At least 8 characters.", UiCopy.passwordTooShort())
        assertEquals("You’re marked Present", UiCopy.scanPresentTitle())
        assertEquals("Saved on this phone", UiCopy.scanSavedOfflineTitle())
        assertEquals("You already have attendance", UiCopy.scanAlreadyRecordedTitle())
        assertEquals(
            "TRX Evening on Mon 28 Sep 2026 is already recorded. No new record was added.",
            UiCopy.scanAlreadyRecordedBody("TRX Evening", LocalDate(2026, 9, 28)),
        )
        assertEquals("Scan not accepted", UiCopy.scanRejectedTitle())
        assertEquals(" Nothing was saved.", UiCopy.scanRejectedTail())
        assertEquals("This code is for a different date.", UiCopy.wrongDate())
        assertEquals("This code isn’t from your coach for this class.", UiCopy.wrongCoach())
        assertEquals("You’re not on this class roster.", UiCopy.notOnRoster())
        assertEquals("This date is cancelled.", UiCopy.sessionCancelled())
    }

    @Test
    fun AC84_invites_classes_and_attendance_copy() {
        assertEquals("You joined TRX Evening. It’s now in My classes.", UiCopy.inviteAccepted("TRX Evening"))
        assertEquals("No invites yet", UiCopy.invitesEmptyTitle())
        assertEquals(
            "When a coach invites you to a class, it shows up here. You can accept or decline it.",
            UiCopy.invitesEmptyBody(),
        )
        assertEquals("No classes yet", UiCopy.coachClassesEmptyTitle())
        assertEquals(
            "Create your first class. Public classes appear in the class list for everyone.",
            UiCopy.coachClassesEmptyBody(),
        )
        assertEquals("No requests", UiCopy.requestsEmptyTitle())
        assertEquals("Students who request this class will appear here.", UiCopy.requestsEmptyBody())
        assertEquals("Mon 5 Oct cancelled. Other dates stay scheduled.", UiCopy.dateCancelled(LocalDate(2026, 10, 5)))
        assertEquals("Invite sent to Ada. They’ll see it in Invites.", UiCopy.inviteSent("Ada"))
        assertEquals("Invite sent", UiCopy.inviteSentButton())
        assertEquals("No student found", UiCopy.noMatchTitle())
        assertEquals("No account uses EMP-9. Nothing was sent.", UiCopy.noMatchBody("EMP-9"))
        assertEquals("Not scanned yet", UiCopy.notScannedYet())
        assertEquals("Day ended", UiCopy.dayEnded())
        assertEquals("Today · in progress", UiCopy.inProgress())
    }
}
