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

    fun registrationOverline(role: Role): String = when (role) {
        Role.STUDENT -> "TRX ATTENDANCE"
        Role.COACH -> "COACH ACCOUNT"
    }

    fun registrationSubtitle(role: Role, origin: RegistrationOrigin): String = when {
        origin is RegistrationOrigin.FromRequestToJoin && role == Role.STUDENT ->
            "Create your student account to send this request."
        role == Role.COACH -> "Create classes, invite students and show the daily code."
        else -> "Join classes and record your attendance."
    }

    fun fieldLabel(field: Field): String = when (field) {
        Field.NAME -> "Full name"
        Field.EMAIL, Field.SIGN_IN_EMAIL -> "Email"
        Field.PASSWORD, Field.SIGN_IN_PASSWORD -> "Password"
        Field.EMPLOYEE_ID -> "Employee ID"
    }

    fun fieldPlaceholder(field: Field): String = when (field) {
        Field.NAME -> "Your name"
        Field.EMAIL, Field.SIGN_IN_EMAIL -> "name@company.com"
        Field.PASSWORD -> "At least 8 characters"
        Field.EMPLOYEE_ID -> "e.g. EMP-20417"
        Field.SIGN_IN_PASSWORD -> "Your password"
    }

    fun passwordHelper(): String = passwordTooShort()

    fun employeeIdHelper(): String = "Your company employee ID. Each ID can be registered once."

    fun alreadyHaveAccount(): String = "Already have an account?"

    fun signInHeading(): String = "Sign in"

    fun requestingToJoinKicker(): String = "REQUESTING TO JOIN"

    fun requestingClassSubtitle(coachName: String, days: String, slots: String): String =
        "Coach $coachName \u00B7 $days \u00B7 $slots"

    fun signInAlsoSends(className: String): String =
        "Signing in also sends your request for $className."

    fun registrationBackLabel(origin: RegistrationOrigin): String = when (origin) {
        RegistrationOrigin.FromClassList -> "Classes"
        is RegistrationOrigin.FromRequestToJoin -> "Back"
    }

    fun requiredField(): String = "Required."

    fun emailTaken(): String = "This email is already registered."

    fun authFailed(): String = "Email or password is incorrect."

    fun offlineNothingSaved(): String = "You\u2019re offline. Nothing was saved."

    fun registerOrSignIn(): String = "Register or sign in"

    fun showPassword(): String = "Show password"

    fun hidePassword(): String = "Hide password"

    fun fieldError(error: FieldError): String = when (error) {
        FieldError.Required -> requiredField()
        FieldError.InvalidEmail -> invalidEmail()
        FieldError.PasswordTooShort -> passwordTooShort()
        FieldError.EmailTaken -> emailTaken()
        FieldError.EmployeeIdTaken -> employeeIdTaken()
    }

    fun uiError(error: UiError): String = when (error) {
        UiError.AuthFailed -> authFailed()
        UiError.Offline -> offlineNothingSaved()
        UiError.ClassCancelled -> sessionCancelled()
        UiError.PrivateClass -> "This class is private."
        UiError.EndDateInPast -> "End date cannot be in the past."
        UiError.PastDate -> "Cannot cancel a past date."
        UiError.InvalidState -> "That action is not available."
    }

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
