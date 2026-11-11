package ph.project.active.presentation.common

import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.Role

object UiCopy {
    fun offlineBanner(): String = "No internet connection"

    fun scannerOfflineSubtitle(): String =
        "Scans are saved on this phone and upload when you are back online."

    fun savedOfflineSubtitle(): String = "Upload will retry when your connection returns."

    fun qrOfflineSubtitle(): String = "Showing today\u2019s code saved on this phone."

    fun classListUnavailableTitle(): String = "Today\u2019s class list can\u2019t load"

    fun classListUnavailableBody(): String =
        "The code still works. Students on the roster can scan it now; their phones upload later."

    fun requestToJoin(): String = "Request to join"

    fun pending(): String = "Pending"

    fun awaitingApproval(): String = "Awaiting approval"

    fun joined(): String = "Joined"

    fun invited(): String = "Invited"

    fun scheduled(): String = "Scheduled"

    fun cancelled(): String = "Cancelled"

    fun upcoming(): String = "Upcoming"

    fun present(): String = "Present"

    fun absent(): String = "Absent"

    fun uploadPending(): String = "Upload pending"

    fun visibilityPublic(): String = "Public"

    fun visibilityPrivate(): String = "Private"

    fun requestAgain(): String = "You can request again"

    fun registrationTitle(role: Role): String = when (role) {
        Role.STUDENT -> "Create account"
        Role.COACH -> "Coach registration"
    }

    fun registrationSubmit(role: Role, origin: RegistrationOrigin): String = when {
        origin is RegistrationOrigin.FromRequestToJoin && role == Role.STUDENT -> "Create account and send request"
        role == Role.COACH -> "Create coach account"
        else -> "Create account"
    }

    fun registrationLoading(): String = "Creating account\u2026"

    fun invalidEmail(): String = "Enter a valid email address."

    fun employeeIdTaken(): String = "This employee ID is already registered."

    fun passwordTooShort(): String = "At least 8 characters."

    fun scanPresentTitle(): String = "You\u2019re marked Present"

    fun scanSavedOfflineTitle(): String = "Saved on this phone"

    fun scanAlreadyRecordedTitle(): String = "You already have attendance"

    fun scanAlreadyRecordedBody(className: String, date: LocalDate): String =
        "$className on ${formatDate(date)} is already recorded. No new record was added."

    fun scanRejectedTitle(): String = "Scan not accepted"

    fun scanRejectedTail(): String = " Nothing was saved."

    fun wrongDate(): String = "This code is for a different date."

    fun wrongCoach(): String = "This code isn\u2019t from your coach for this class."

    fun notOnRoster(): String = "You\u2019re not on this class roster."

    fun sessionCancelled(): String = "This date is cancelled."

    fun inviteAccepted(className: String): String = "You joined $className. It\u2019s now in My classes."

    fun invitesEmptyTitle(): String = "No invites yet"

    fun invitesEmptyBody(): String =
        "When a coach invites you to a class, it shows up here. You can accept or decline it."

    fun coachClassesEmptyTitle(): String = "No classes yet"

    fun coachClassesEmptyBody(): String =
        "Create your first class. Public classes appear in the class list for everyone."

    fun requestsEmptyTitle(): String = "No requests"

    fun requestsEmptyBody(): String = "Students who request this class will appear here."

    fun dateCancelled(date: LocalDate): String =
        "${formatShortDate(date)} cancelled. Other dates stay scheduled."

    fun inviteSent(name: String): String = "Invite sent to $name. They\u2019ll see it in Invites."

    fun inviteSentButton(): String = "Invite sent"

    fun noMatchTitle(): String = "No student found"

    fun noMatchBody(employeeId: String): String = "No account uses $employeeId. Nothing was sent."

    fun notScannedYet(): String = "Not scanned yet"

    fun dayEnded(): String = "Day ended"

    fun inProgress(): String = "Today \u00B7 in progress"
}
