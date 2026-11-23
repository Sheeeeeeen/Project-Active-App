package ph.project.active.presentation.common

import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.Role
import ph.project.active.presentation.classdetail.JoinOutcome

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
        UiError.ClassCancelled -> "This class has been cancelled. Nothing was sent."
        UiError.PrivateClass -> "This class is private. Joining is by coach invite only."
        UiError.EndDateInPast -> "End date cannot be in the past."
        UiError.PastDate -> "Cannot cancel a past date."
        UiError.InvalidState -> "That action is not available."
        UiError.InvalidSlot -> "The end time must be after the start time."
        UiError.AlreadyInvited -> "This student already has an invite."
        UiError.AlreadyRequested -> "This student already requested this class."
        UiError.AlreadyMember -> "This student is already on the roster."
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

    fun guestListTitle(): String = "Classes"

    fun guestListSubtitle(): String = "Public TRX classes. No account needed."

    fun publicClassesHeader(): String = "PUBLIC CLASSES"

    fun classCount(count: Int): String = if (count == 1) "1 class" else "$count classes"

    fun guestListEmptyTitle(): String = "No public classes yet"

    fun guestListEmptyBody(): String = "When a coach creates a public class, it shows up here."

    fun createAccountToRequest(): String = "Create an account to request a class."

    fun classDetailsHeading(): String = "Class details"

    fun detailCoachLabel(): String = "Coach"

    fun detailDaysLabel(): String = "Days"

    fun detailTimeLabel(): String = "Time"

    fun detailRunsLabel(): String = "Runs"

    fun runsUntil(date: LocalDate): String = "Until ${formatDate(date)}"

    fun noEndDate(): String = "No end date"

    fun requestToJoinGuestNote(): String =
        "You\u2019ll create a student account first. Your request is sent after the account exists."

    fun requestToJoinStudentNote(coachName: String): String =
        "Your request goes to Coach $coachName for approval."

    fun pendingApprovalBody(coachName: String): String =
        "Coach $coachName will approve or decline your request. You don\u2019t need to send it again."

    fun requestAgainBody(): String =
        "You can send a new request. The coach will need to approve it again."

    fun joinedTitle(): String = "You\u2019re on this class"

    fun joinedBody(): String =
        "Record attendance from the Attend tab during each session. Your dates are in Sessions."

    fun invitedBody(coachName: String): String =
        "Coach $coachName invited you. Accept or decline it in Invites."

    fun classCancelledBody(): String = "This class has been cancelled."

    fun classEndedOn(date: LocalDate): String = "This class ended on ${formatDate(date)}."

    fun classUnavailableTitle(): String = "Class not available"

    fun classUnavailableBody(): String = "It may be private or no longer listed."

    fun accountReadyClassCancelled(): String =
        "Your account is ready, but no request was sent because this class was cancelled."

    fun accountReadyClassUnavailable(): String =
        "Your account is ready, but no request was sent because this class isn\u2019t available."

    fun joinNotice(outcome: JoinOutcome): String = when (outcome) {
        JoinOutcome.ClassCancelled -> accountReadyClassCancelled()
        JoinOutcome.ClassUnavailable -> accountReadyClassUnavailable()
    }

    fun largeTitleEyebrow(): String = "TRX ATTENDANCE"

    fun classesTitle(): String = "Classes"

    fun classesGreeting(name: String, employeeId: String?): String {
        val firstName = name.trim().substringBefore(' ')
        return if (employeeId.isNullOrBlank()) "Hi $firstName" else "Hi $firstName \u00B7 $employeeId"
    }

    fun myClassesHeader(): String = "MY CLASSES"

    fun studentPublicClassesHeader(): String = "Public classes"

    fun joinedCount(count: Int): String = "$count joined"

    fun classesEmptyTitle(): String = "No classes yet"

    fun classesEmptyBody(): String =
        "When you join a class, or when a coach creates a public class, it shows up here."

    fun invitesTitle(): String = "Invites"

    fun invitesSubtitle(): String = "Accept to join a class roster."

    fun inviteInvitedYou(coachName: String): String = "Coach $coachName invited you."

    fun inviteDeclined(): String = "Invite declined"

    fun decline(): String = "Decline"

    fun accept(): String = "Accept"

    fun account(): String = "Account"

    fun tabClasses(): String = "Classes"

    fun tabAttend(): String = "Attend"

    fun tabSessions(): String = "Sessions"

    fun tabInvites(): String = "Invites"

    fun coachSubtitle(name: String): String = "Coach $name"

    fun yourClassesHeader(): String = "YOUR CLASSES"

    fun createClass(): String = "Create class"

    fun privateCalloutTitle(): String = "Private classes are invite-only"

    fun privateCalloutBody(): String = "Guests and students only see your public classes."

    fun studentCount(count: Int): String = if (count == 1) "1 student" else "$count students"

    fun requestCount(count: Int): String = if (count == 1) "1 request" else "$count requests"

    fun slotCount(count: Int): String = if (count == 1) "1 slot" else "$count slots"

    fun dateCount(count: Int): String = if (count == 1) "1 date" else "$count dates"

    fun tabQr(): String = "QR"

    fun tabAttendance(): String = "Attendance"

    fun newClassTitle(): String = "New class"

    fun cancel(): String = "Cancel"

    fun classNameLabel(): String = "Class name"

    fun classNamePlaceholder(): String = "e.g. TRX Core Burn"

    fun repeatsLabel(): String = "Repeats"

    fun specificDays(): String = "Specific days"

    fun everyDay(): String = "Every day"

    fun timeSlotsLabel(): String = "Time slots"

    fun chooseDayFirst(): String = "Choose at least one day. Each day gets its own time slots."

    fun startLabel(): String = "Start"

    fun endLabel(): String = "End"

    fun addTimeSlot(): String = "Add time slot"

    fun visibilityLabel(): String = "Visibility"

    fun publicHelper(): String = "Public: anyone can see it and request to join."

    fun privateHelper(): String = "Private: only you and students on the roster see it. Students join by your invite."

    fun endsLabel(): String = "Ends"

    fun endDateLabel(): String = "End date"

    fun chooseDate(): String = "Choose"

    fun saveClass(): String = "Save class"

    fun previewTitle(count: Int): String = "${dateCount(count)} will be created"

    fun previewBody(today: LocalDate, dayNames: String, first: LocalDate, last: LocalDate): String =
        "Saving today, ${formatDate(today)}: every $dayNames from ${formatShortDate(first)} to ${formatDate(last)}. " +
            "Past dates are not created. The end date is included."

    fun noEndDateBody(): String =
        "This class keeps repeating until you cancel the whole class. Dates are created from today."

    fun classesBack(): String = "Classes"

    fun classOptions(): String = "Class options"

    fun manageEndsLine(endDate: LocalDate?, count: Int): String =
        if (endDate == null) "${noEndDate()} \u00B7 ${dateCount(count)}" else "Ends ${formatDate(endDate)} \u00B7 ${dateCount(count)}"

    fun tabSchedule(): String = "Schedule"

    fun tabRequests(): String = "Requests"

    fun tabRoster(): String = "Roster"

    fun tabInvite(): String = "Invite"

    fun cancelDateCalloutTitle(): String = "Cancel a single date"

    fun cancelDateCalloutBody(): String = "Only today or a future date can be cancelled. The class keeps repeating."

    fun datesHeader(): String = "DATES"

    fun sessionLabel(number: Int): String = "SESSION $number"

    fun today(): String = "TODAY"

    fun pastDate(): String = "Past date"

    fun cancelDate(): String = "Cancel date"

    fun keepDate(): String = "Keep date"

    fun cancelDateTitle(date: LocalDate): String = "Cancel ${formatShortDate(date)}?"

    fun cancelDateBody(sessionNumber: Int, className: String, nextDate: LocalDate?): String {
        val first = "Session $sessionNumber of $className will show as Cancelled and won\u2019t accept attendance."
        return if (nextDate == null) first else "$first ${formatShortDate(nextDate)} and every other date stay scheduled."
    }

    fun waitingForYou(): String = "WAITING FOR YOU"

    fun approve(): String = "Approve"

    fun requestsFootnote(): String =
        "Approve adds the student to the roster. Decline leaves them off; they can request again."

    fun onTheRoster(): String = "ON THE ROSTER"

    fun joinedByRequest(): String = "Joined by request"

    fun joinedByInvite(): String = "Joined by invite"

    fun remove(): String = "Remove"

    fun keep(): String = "Keep"

    fun rosterFootnote(): String = "A removed student is taken off this class only and can request to join again."

    fun removeTitle(name: String): String = "Remove $name?"

    fun removeBody(name: String, className: String): String {
        val firstName = name.trim().substringBefore(' ')
        return "$firstName will be taken off $className and can\u2019t record attendance for it. " +
            "They can request to join again. Past attendance stays as recorded."
    }

    fun inviteIntro(): String = "Search a student by employee ID to invite them to this class."

    fun matchCount(count: Int): String = if (count == 1) "1 match" else "$count matches"

    fun matchLine(employeeId: String, status: String): String = "$employeeId \u00B7 Student \u00B7 $status"

    fun notOnThisClass(): String = "Not on this class"

    fun requested(): String = "Requested"

    fun onThisClass(): String = "On this class"

    fun sendInvite(): String = "Send invite"

    fun cancelWholeClass(): String = "Cancel whole class"

    fun keepClass(): String = "Keep class"

    fun cancelClass(): String = "Cancel class"

    fun cancelClassTitle(className: String): String = "Cancel $className?"

    fun cancelClassBody(): String =
        "All remaining dates are cancelled and the class stops repeating. It leaves the public class list. " +
            "Past sessions and their attendance stay as recorded."
}
