# TRX Class Attendance — MVP UI Source of Truth

Use this document as the source of truth for the Figma end-to-end design. If anything here conflicts with an earlier requirements draft, this document wins.

## Button and status labels (use these exact words)

- Request to join
- Pending
- Awaiting approval
- Accept
- Decline
- Approve
- Scheduled
- Cancelled
- Upcoming
- Present
- Absent
- No internet connection

## Screens and rules

### Public class list
The app opens here. No account is required.
Each row: class name, coach name, days, and times.
A button on this screen opens the registration screen (the path where shake CAN switch to coach registration).

### Public class detail
Shows class name, coach, days, and times only.
Do not show roster, attendance, or QR.

Request actions:
- Guest: **Request to join**. This opens student registration. It does not send the request yet.
- Signed-in student who has not requested: **Request to join**. Sends the request immediately.
- Signed-in student with a pending request: **Pending** and **Awaiting approval**. No request button.
- Signed-in student after a decline or removal: **Request to join** is back.
- Signed-in student after accept or approve: the class is shown as joined. No request button.
- Coach: no request button.

A second tap must not create a second pending request.

### Registration and sign in
One screen. It opens as student registration.
Student fields: name, email, password, employee ID. Sign in is on this same screen: email and password.
Shake the device: the screen color and title change to coach registration. Fields become name, email, and password. The employee ID field is gone. Shake again: student registration returns, including the color, title, and employee ID field.
If this screen was opened from **Request to join**, shake does nothing. That path always creates a student. After the account exists, one pending request is sent for that class, and the class detail shows Pending and Awaiting approval.
No email confirmation step. Submit creates an account the person can use immediately.
There is no role picker, switch, or toggle.
Do not put a visible “shake for coach” control or hint on the screen; that would be a role picker. Document the shake behavior only in dev annotations.

### Student invites
A list of invites from coaches. Each row is one class. Actions: Accept, Decline.
Accept puts the student on that class roster. Decline does not.
The student sees a class as joined only after they accept an invite or the coach approves their request.

### Student sessions
For each date of a joined class: Upcoming, Present, or Absent.
A future date stays Upcoming. After the day is over, a roster student who did not scan that class is Absent. The phone’s timezone decides the date and when the day is over.

### Attendance
The student opens attendance. A modal lists the classes scheduled that day, each with its time. Cancelled dates are not in this list.
- The student chooses one class. Then the scanner opens.
- Closing the modal without a choice does not open the scanner and saves nothing.
- After a successful scan, that class on that date is Present.
- They can scan again the same day and choose a different class.
- If they already have attendance for that class on that date, show that they already have attendance. Do not add a second record.
Offline: if they are already on the roster, the scan is kept on the phone and uploaded when the connection returns. Show **No internet connection**. The recorded time is the scan time.

### Coach class list
The coach’s own classes, public and private. From here the coach creates a class and opens one to manage it.

### Create class
Fields:
- Class name
- Days: specific weekdays, or every day. Each weekday has its own time slots. Slots are a start and an end, shown with AM or PM.
- Public or private
- End date, or no end date

No end date means the class stays open until the coach deletes or cancels the whole class. An end date is the last day the class stays open, and that day is included.
Saving creates the matching dates from the save date through the end date. Past dates are not created. An every-day class creates every remaining day in that range.
Example: saved Friday 25 September 2026, Monday and Wednesday, end date Saturday 31 October 2026. Sessions are every Monday and Wednesday from 28 September through 28 October.

Treat “time slots” as: each selected weekday can have one or more start/end pairs, and different weekdays can have different times. The form must support adding a slot to a weekday.

Include a way for the coach to delete or cancel the whole class, because “no end date” stays open until that happens.

### Coach class management
Opened from the coach’s class. No request-to-join button.
Sections:
- Schedule of dates. Each date is **Scheduled** or **Cancelled**.
- Requests. More than one student can request the same class. Each request: Approve or Decline. Approve adds them to the roster. Decline leaves them off.
- Roster. The coach can remove a student. A removed student can request again.
- Invite. Search an employee ID. One match: the coach can send an invite. No match: send nothing.

Cancel one date: today or a future date only. That date stays in the list and its status becomes **Cancelled**. The other dates stay **Scheduled**. The class keeps repeating. A cancelled date does not accept a scan.

### Daily QR
The coach shows one QR code for today. The same code covers every class that coach has today. It is not one code per class.
The coach can show it with no internet, even when the roster cannot load. Show **No internet connection** in that case.

### Coach attendance
For one date, the coach sees who is Present, who is Absent, and which class each present student chose.

## Flows (must be visible as complete screen sequences)

### Guest looks, then joins
1. Guest opens the public class list. No account.
2. Guest opens one class and sees name, coach, days, and times.
3. Guest taps **Request to join**.
4. Student registration opens. Shake is ignored.
5. Guest enters name, email, password, and employee ID.
6. The class shows **Pending** and **Awaiting approval**. The request button is gone.
7. Coach approves.
8. The student sees the class as joined.

A second tap must not create a second pending request.

### Guest creates a coach account
1. Guest opens registration from the button on the public class list. The screen is student registration.
2. Guest shakes the device. Color and title change. Employee ID disappears.
3. Guest enters name, email, and password.
4. Sign-in opens the coach screens.

### Signed-in student requests
1. Student opens a public class.
2. Student taps **Request to join**. The request is pending immediately.
3. If the coach declines, or later removes them, **Request to join** returns. A new request can be sent.

### Coach invites
1. Coach opens a class and searches an employee ID.
2. One student matches. Coach sends the invite.
3. Student opens their invite list and accepts.
4. Student is on that class roster and can scan its sessions.

### Student records attendance
1. Coach shows today’s QR. Offline is allowed.
2. Student opens attendance. The modal lists today’s scheduled classes and times.
3. Student chooses one class. The scanner opens.
4. Student scans the coach’s code.
5. That class on that date is Present. Other classes that day stay unmarked until the student scans again and chooses them.
6. After the day is over, an unmarked class for a roster student is Absent.

### Coach cancels one date
1. Coach opens the class schedule.
2. Coach cancels this Monday, or a future date.
3. That date remains on the schedule with status **Cancelled**.
4. Next Monday stays **Scheduled**.
5. A scan for the cancelled date is rejected. The other dates still accept attendance.

## Do not put on these screens
- A student/coach toggle
- Email confirmation
- A button that marks the student present without a scan
- NFC
- Roster, attendance, or QR on the public class detail
- A request button for a coach
- Payments, capacity, waitlist, notifications, or chat
- A visible shake hint or role control on registration

## Non-conflicting rules from the earlier agreed MVP (keep these; do not override anything above)

- One app for Android and iOS. After sign-in, a coach sees coach screens and a student sees student screens. One account, one role.
- Employee ID is unique and belongs to a student. A coach account has no employee ID. Searching an unknown employee ID creates no invite.
- Attendance always belongs to one session (one date of one class), not to the class as a whole.
- The daily code belongs to one coach and one date. Only a code produced by the coach’s app is valid. A scan with no coach code does not mark the student present.
- Store nothing when: the student is not on that class roster; the code is for another date or was not produced for that coach; the student closes the modal without choosing a class; the session is cancelled.
- A student who is not on the roster cannot gain a saved attendance record for that class.
- Choosing one class marks that class present. Each other class on that date stays unmarked (then Absent after the day ends) unless they scan again and choose it.
- Public classes are visible with no account. Private classes are visible only to the coach and students on the roster. Joining a private class is by coach invite only.
- After sign-in, a student does not see coach class setup, and a coach does not see the student scanner.
