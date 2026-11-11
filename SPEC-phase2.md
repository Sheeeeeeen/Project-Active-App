# TRX Class Attendance — Phase 2 Spec (shared ViewModels + SQLDelight)

Status: **approved by Sheen, 11 Nov 2026.** The draft decisions below are locked. Tester writes failing tests next; Builder holds until those tests are on a feature branch.

Tested on Linux in `shared/commonTest`, run as `:shared:jvmTest`, with a JVM SQLDelight driver, fakes, an injectable `Clock` and `TimeZone`, and a switchable network status. No Compose screens, no Firebase SDK, no security rules, no store signing.

Baseline: GitHub `main` at `9b553df` (PR #1). The Phase 1 domain (AC1–42 in `SPEC-phase1.md`) is used as-is: Phase 2 calls its use cases and implements its repository interfaces. In this phase the sources move from `trx.attendance.*` to `ph.project.active.*`; behavior does not change.

## Packages
Root package `ph.project.active` (Kotlin sources, Android `namespace`/`applicationId`, iOS `PRODUCT_BUNDLE_IDENTIFIER` in `Config.xcconfig`). Sources moved from `trx.attendance.*`; behavior does not change. Packages are split by role, one public type per file:

- `domain.model`: Phase 1 entities, enums, and result types.
- `domain.repository`: Phase 1 repository interfaces and helpers (`activeRoster`, `activeClassIds`, `current`, `currentStatus`); in-memory versions in `domain.repository.inmemory`.
- `domain.usecase`: Phase 1 use cases.
- `domain.crypto`: Ed25519 key pair and daily-code signing.
- `data.model`: Phase 2 data contracts (`SignedInAccount`, `PersonProfile`, `RegisterResult`, `SignInResult`, and the like).
- `data.repository`: SQLDelight implementations (`SqlClassRepository`, `SqlMembershipRepository`, `SqlAttendanceRepository`, `SqlCoachKeyRepository`, `SqlSessionStore`, `SqlPersonRepository`).
- `data.db`: generated SQLDelight database `TrxDatabase` (AC86). `.sq` files go in `shared/src/commonMain/sqldelight/ph/project/active/data/db/`.
- `data.fake`: fakes for Firebase Auth, Firestore, and upload (`FakeAuthRepository`, `FakeAttendanceUploader`, `InMemorySecretKeyStore`, `MutableNetworkMonitor`). They live in `commonMain` so Phase 3 can run the app on Android before Firebase exists.
- `data.sync`: `OutboxSync`.
- `presentation.common`: shared UI types, `UiCopy`, and formatters.
- `presentation.<feature>`, one per ViewModel: `root`, `registration`, `guestclasses`, `classdetail`, `studentclasses`, `attend`, `studentsessions`, `invites`, `coachclasses`, `createclass`, `manageclass`, `dailyqr`, `coachattendance`.
- Android and iOS host and template sources (`MainActivity`, `App`, `Greeting`, `GreetingUtil`, `Platform*`, `MainViewController`) also use `ph.project.active`. The Android manifest uses a relative `.MainActivity`.
- All of this is in `shared`: `commonMain`/`commonTest`, plus a JVM driver in `jvmMain`/`jvmTest`. Android and iOS drivers are declared but not tested. No `composeApp` work is needed in this phase.

## Under test
**Data:** `SqlClassRepository`, `SqlMembershipRepository`, `SqlAttendanceRepository`, `SqlCoachKeyRepository` (these implement the Phase 1 `ClassRepository`, `MembershipRepository`, `AttendanceRepository`, and `CoachKeyRepository`), `SqlSessionStore`, `SqlPersonRepository`, `OutboxSync`, `FakeAuthRepository`, `FakeAttendanceUploader`, `InMemorySecretKeyStore`, and `MutableNetworkMonitor`.

**ViewModels:** `RootViewModel`, `RegistrationViewModel`, `GuestClassListViewModel`, `ClassDetailViewModel`, `StudentClassesViewModel`, `AttendViewModel`, `StudentSessionsViewModel`, `InvitesViewModel`, `CoachClassesViewModel`, `CreateClassViewModel`, `ManageClassViewModel`, `DailyQrViewModel`, and `CoachAttendanceViewModel`.

## New contracts (exact names)
```kotlin
// ph.project.active.data.model (contracts) and data.repository / data.fake (implementations)
data class SignedInAccount(val id: String, val name: String, val email: String, val role: Role, val employeeId: String?)
data class PersonProfile(val id: String, val name: String, val role: Role, val employeeId: String?)
interface SessionStore { val account: StateFlow<SignedInAccount?>; fun save(account: SignedInAccount) }
interface PersonRepository { fun find(id: String): PersonProfile?; fun upsert(profile: PersonProfile) }
interface NetworkMonitor { val status: StateFlow<NetworkStatus> }            // Phase 1 NetworkStatus
interface SecretKeyStore { fun putPrivateKey(coachId: String, key: ByteArray); fun privateKey(coachId: String): ByteArray? }
interface AttendanceUploader { suspend fun upload(record: AttendanceRecord): Boolean }
sealed interface RegisterResult { data class Success(val account: SignedInAccount); data object EmailTaken; data object EmployeeIdTaken; data object Offline }
sealed interface SignInResult { data class Success(val account: SignedInAccount); data object Failed; data object Offline }
interface AuthRepository {
  suspend fun registerStudent(name: String, email: String, password: String, employeeId: String): RegisterResult
  suspend fun registerCoach(name: String, email: String, password: String): RegisterResult
  suspend fun signIn(email: String, password: String): SignInResult
}
class OutboxSync(attendance: AttendanceRepository, uploader: AttendanceUploader, network: NetworkMonitor) { suspend fun sync(): Int }
```
- `FakeAuthRepository(directory: UserRepository, hasher: PasswordHasher, network: NetworkMonitor, session: SessionStore, people: PersonRepository)` wraps the Phase 1 `RegisterStudent`, `RegisterCoach`, and `SignIn`. The `directory` is a Phase 1 `InMemoryUserRepository`. It stands in for Firebase Auth and a Firestore users collection, so credentials never reach SQLDelight. `InviteByEmployeeId` and the coach's invite search use the same `directory`.
- In tests, one `TrxDatabase` is shared by the coach and student ViewModels. It stands in for Firestore replication between phones. Upload latency to other phones is not modelled; that is Phase 5.

## Conventions (apply to every ViewModel)
- Each ViewModel extends `androidx.lifecycle.ViewModel` (JetBrains KMP lifecycle, already in the main catalog) and exposes `val state: StateFlow<XUiState>`. User actions are public `onX(...)` functions. One-shot navigation is a nullable `navigation` field, cleared by `onNavigationHandled()`.
- Dependencies come through the constructor: Phase 1 use cases and repositories, `Clock`, `TimeZone` (the phone's zone), `NetworkMonitor`, and the contracts above. New code uses `kotlin.time.Clock` and `kotlin.time.Instant`. These are the same types as the deprecated `kotlinx.datetime` aliases on main, which pins kotlinx-datetime 0.8.0.
- No exception escapes a ViewModel action. `IllegalArgumentException`, `IllegalStateException`, and `AuthFailed` become typed UiState errors.
- Display names come from `PersonRepository`. Coach names in class rows and student names in coach rows use it.

## Acceptance criteria

### Persistence (SQLDelight)
1. `TrxDatabase.Schema` (version 1) creates these tables on an empty JVM driver: `signed_in_account`, `person`, `trx_class`, `class_slot`, `session`, `membership`, `attendance`, `coach_public_key`.
2. No column in any table is named like `password`, `password_hash`, or `private_key`. Check with `PRAGMA table_info`. Passwords and private keys never reach SQLite.
3. `SqlClassRepository.saveClass`/`findClass` round-trip a `TrxClass` exactly: `slotsByWeekday` with several slots on one weekday, `endDate = null`, `createdOn`, and `cancelledAt`. Saving an existing id updates it; it does not create a duplicate. `allClasses` returns classes in creation order.
4. `saveSessions` upserts by id. `sessionsOf(classId)` is ordered by `sessionNumber`. `findSession(SessionRef)` matches class + date + slot. `status` and `sessionNumber` round-trip.
5. Phase 1 `CreateClass` run on `SqlClassRepository` with the AC7 example (Fri 25 Sep 2026, Mon/Wed 17:30–18:30, end Sat 31 Oct 2026) persists the same 10 sessions as the in-memory repository: numbers 1–10, Mon 28 Sep to Wed 28 Oct.
6. `SqlMembershipRepository.forClass` returns every record for the class, oldest first, history included. `update` replaces by id. `update` with an unknown id throws `IllegalArgumentException`. The Phase 1 helpers `current`, `currentStatus`, `activeRoster`, and `activeClassIds` give the same answers as on the in-memory repository.
7. `SqlAttendanceRepository` supports `add`/`find`/`update`/`all`. `scannedAt` round-trips to an equal `Instant`, and `uploadState` round-trips. A second row for the same `(sessionId, studentId)` is rejected by a unique constraint.
8. Every `nextId()`/`nextClassId()`/`nextSessionId()` value is unique, including after reopening the same database file, and matches `[A-Za-z0-9_.~-]+`.
9. Parity: the Phase 1 scenarios for AC13–16, AC19–24, and AC32–41 produce the same results when the Phase 1 use cases are wired to the SQL repositories (`SqlTestWorld`).
10. Restart: write data through one driver on a temp-file database, close it, then open a new driver and new repositories on the same file. Classes, sessions with their status, memberships, `PENDING` attendance rows, coach public keys, and the signed-in account are all present. A new `StudentSessionsViewModel`, `AttendViewModel`, and `CoachClassesViewModel` built on the reopened database show the same rows as before the restart.
11. `SqlSessionStore.save` keeps at most one account; saving replaces the previous one. `account` emits the saved value and survives a restart.
12. `SqlPersonRepository.upsert`/`find` round-trip `PersonProfile`. Upsert replaces by id.
13. `SqlCoachKeyRepository.save(coachId, pair)` writes `publicKey` to `coach_public_key` and the private key only to `SecretKeyStore`. When both are present, `find` returns a pair, so `GenerateDailyCode` → `VerifyDailyCode` is `VALID`.
14. If only the public key is cached (a student's phone with no private key for that coach), `find` returns a verify-only pair: codes made by the coach's phone verify as `VALID`. `find` returns `null` when neither key is present.
15. Saving a new pair for the same coach replaces both keys, and codes signed with the old key fail (AC30 parity). After a restart that keeps the same `SecretKeyStore`, today's code still verifies.

### Network and outbox
16. `MutableNetworkMonitor(initial)` exposes `status` and `set(NetworkStatus)`. Every ViewModel state has `connection: ConnectionUi(isOffline: Boolean, banner: String?)`. `banner` is exactly `"No internet connection"` while `OFFLINE` and `null` while `ONLINE`, and it updates live without recreating the ViewModel.
17. A valid scan while `OFFLINE` stores one SQL `attendance` row with `uploadState = PENDING` and `scannedAt` = clock now.
18. `OutboxSync.sync()` while `ONLINE` uploads every `PENDING` row in ascending `scannedAt` order (ties broken by id) and marks each one `SYNCED` after `upload` returns `true`. It never changes `scannedAt`, and it returns the number synced.
19. If `upload` returns `false` or throws, that row stays `PENDING` and the sync stops, so later rows keep their order for the next attempt. While `OFFLINE`, `sync()` uploads nothing, returns 0, and the outbox is unchanged. `SYNCED` rows are never uploaded again.
20. `RootViewModel` runs `OutboxSync.sync()` once on every `OFFLINE` → `ONLINE` transition. `AttendViewModel` runs it right after an online `Recorded`. `PENDING` rows that survive a restart sync on the first `ONLINE` status.
21. Phase 1 `FlushOutbox` is unchanged and still passes AC41.
22. Online-only actions do nothing while `OFFLINE`. They store nothing, make no `AuthRepository` or use-case write, and set `error = UiError.Offline`. These actions are register, sign in, request to join, approve, decline, invite search, send invite, accept or decline an invite, remove, create class, cancel date, and cancel class. Cached lists, Attend scans, and the Daily QR keep working offline.

### Root, registration and sign in
23. `RootViewModel` shows `Root.Guest` when `SessionStore.account` is `null`, `Root.Student(account)` for a `STUDENT`, and `Root.Coach(account)` for a `COACH`. After a restart with a cached account, the role's root shows without signing in again.
24. When `SessionStore` saves an account, the root switches from `Guest` to that role's root. `RootViewModel` has no action that switches between student and coach.
25. Student-root ViewModels (`StudentClasses`, `Attend`, `StudentSessions`, `Invites`) throw `IllegalArgumentException` at construction if the account is not a `STUDENT`. Coach-root ViewModels (`CoachClasses`, `CreateClass`, `ManageClass`, `DailyQr`, `CoachAttendance`) throw it if the account is not a `COACH`. `ManageClassViewModel` also throws it for a class the coach does not own.
26. `RegistrationViewModel(origin)` has `origin = FromClassList | FromRequestToJoin(classId)`. It starts with `mode = STUDENT`, `showEmployeeId = true`, empty fields, no errors, and `isSubmitting = false`. `FromRequestToJoin` also exposes `requestingClass` (name, coach name, `slotsByWeekday`) for the B5 header.
27. With `FromClassList`, `onShake()` toggles `STUDENT` ↔ `COACH`. In `COACH` mode, `showEmployeeId = false`. Typed name, email, and password are kept across shakes. Shaking again restores `STUDENT` and `showEmployeeId = true`.
28. With `FromRequestToJoin`, `onShake()` changes nothing: the mode stays `STUDENT`. `onShake()` is the only way to change `mode`; there is no `setMode`.
29. Submit validates first and makes no repository call if any field has an error. Errors go in `fieldErrors: Map<Field, FieldError>`. A blank name, email, or password, or a blank employee ID in `STUDENT` mode, gives `Required`. An email that, trimmed, does not match `^[^\s@]+@[^\s@]+\.[^\s@]+$` gives `InvalidEmail`. A password shorter than 8 characters gives `PasswordTooShort`. Editing a field clears its error.
30. A valid `STUDENT` submit calls `registerStudent` with the trimmed name, email, and employee ID. A `COACH` submit calls `registerCoach`, with no employee ID even if one was typed before the shake. While `isSubmitting` is true, a second submit is ignored, so the repository is called once.
31. `EmailTaken` sets `fieldErrors[EMAIL] = EmailTaken`, and `EmployeeIdTaken` sets `fieldErrors[EMPLOYEE_ID] = EmployeeIdTaken`. In both cases no account is created and no session is saved.
32. On `Success`, `SessionStore` holds the account and `completion = AuthCompletion(role, openClassId)`. With `FromClassList`, `openClassId` is `null`.
33. On a `FromRequestToJoin` registration `Success`, the ViewModel calls Phase 1 `RequestToJoin` exactly once, which creates one `PENDING_REQUEST`. `openClassId = classId`, and `ClassDetailViewModel` for that class then shows `Pending`. If the class was cancelled in the meantime, the account still exists, no membership is created, and `joinOutcome = JoinOutcome.ClassCancelled` (Phase 1 AC16).
34. Sign in uses its own `signInEmail`/`signInPassword` fields; blank gives `Required`. `Failed` sets `signInError = UiError.AuthFailed` and saves no session. `Success` saves the session and opens the root for the account's role, whatever registration `mode` is showing.
35. A sign-in from `FromRequestToJoin` by a `STUDENT` calls `RequestToJoin` once; an already pending or `ACTIVE` membership creates nothing new. A sign-in there by a `COACH` sends no request, and `completion = AuthCompletion(COACH, null)`.
36. `FakeAuthRepository`: a duplicate email (case-insensitive) or a duplicate employee ID returns `EmailTaken` or `EmployeeIdTaken` and creates no user. A new account can sign in immediately, with no confirmation step. An unknown email and a wrong password both return `Failed`. While `OFFLINE`, it returns `Offline` and creates nothing. On success it saves `SessionStore` and upserts `PersonProfile`, storing neither the password nor its hash.

### Guest
37. `GuestClassListViewModel.items` holds public classes that are not cancelled, in creation order. Each item has `classId`, `name`, `coachName`, and `slotsByWeekday`. Private classes never appear. A cancelled class leaves the list (D18: "It leaves the public class list."). An empty list gives `isEmpty = true`.
38. `onRegisterOrSignIn()` sets `navigation = OpenRegistration(FromClassList)`. `onClassTapped(id)` sets `OpenClassDetail(id)`. While `OFFLINE`, the list loads from the cache and the banner shows.
39. `ClassDetailViewModel(classId, viewer: SignedInAccount?)` shows `content` with name, visibility, coach name, `slotsByWeekday`, and `endDate`. The UiState type has no roster, attendance, or QR field. A class the viewer may not see (private and not the viewer's coach or an `ACTIVE` membership), or an unknown id, gives `content = null` and `unavailable = true`.
40. For a guest, `requestAction = RequestToJoin(opensRegistration = true)`. `onRequestToJoin()` sets `navigation = OpenRegistration(FromRequestToJoin(classId))` and creates no membership.
41. For a signed-in student, `requestAction` follows the current membership. None, `DECLINED`, or `REMOVED` gives `RequestToJoin(opensRegistration = false)`; `DECLINED` and `REMOVED` also set `requestAgain = true` (A5). `PENDING_REQUEST` gives `Pending`, with no button. `ACTIVE` gives `Joined`. `INVITED` gives `Invited`, with no button.
42. A student's `onRequestToJoin()` calls `RequestToJoin` and shows `Pending`. A second tap while the request is sending, or after it is pending, creates no second `PENDING_REQUEST`. When the coach approves and the ViewModel refreshes, it shows `Joined`. After a decline or removal it shows `RequestToJoin` again.
43. A coach viewer gets `requestAction = None` on any class.
44. A `JoinResult.ClassCancelled` sets `error = UiError.ClassCancelled`, and `PrivateClass` sets `UiError.PrivateClass`. In both cases nothing is created.

### Student
45. `StudentClassesViewModel` exposes `studentName`, `employeeId`, and `myClasses`, which are classes the student is `ACTIVE` on, public and private, each with `chip = Joined`. It also exposes `publicClasses`: public classes that are not cancelled and that the student is not `ACTIVE` on. Their chip is `Invited` for `INVITED`, `Pending` for `PENDING_REQUEST`, and `null` otherwise. A private class the student is not `ACTIVE` on is never listed. `onClassTapped(id)` sets `OpenClassDetail(id)`.
46. `InvitesViewModel.invites` has one row per class where the student's current membership is `INVITED`: class name, coach name, visibility, and `slotsByWeekday`. `pendingInviteCount` equals the row count and feeds the tab badge. No rows gives `isEmpty = true`.
47. `onAccept(classId)` calls `AcceptInvite`: the membership becomes `ACTIVE`, the row leaves, `confirmation = InviteAccepted(className)`, and the class appears in `StudentClassesViewModel.myClasses`. `onDecline(classId)` calls `DeclineInvite`: the membership becomes `DECLINED`, the row leaves, the student is not on the roster, and there is no confirmation.
48. `AttendViewModel` exposes `today` as `clock.todayIn(timeZone)`. `onOpened()` sets `sheetOpen = true`. `choices` has one row per Phase 1 `TodaysChoosableSessions` session: `sessionId`, `className`, `slot`, `coachName`. Only `SCHEDULED` sessions of classes the student is `ACTIVE` on appear; cancelled sessions and cancelled classes do not. Already-recorded sessions stay listed (C7). No choices gives `noSessionsToday = true`.
49. `onChoose(sessionId)` sets `selectedSessionId`, and `canScan = selectedSessionId != null`. `onCloseSheet()` closes the sheet, clears the selection, does not open the scanner, and writes no attendance row.
50. `onScanCoachCode()` with a selection sets `scannerOpen = true`, closes the sheet, and sets `recordingFor` (class name and slot). Without a selection it does nothing. `onChangeSession()` closes the scanner and reopens the sheet with the selection kept.
51. `onCodeScanned(code)` calls `RecordAttendance(studentId, SessionRef(classId, date, slot) of the selection, code)`. The session is the one chosen, not inferred from the time (AC33). With no selection it passes `null`, gets `NoSessionChosen`, stores nothing, closes the scanner, and reopens the sheet.
52. `Recorded` while `ONLINE` sets `result = ScanResult.Present(className, sessionNumber, date, slot, scannedAt)`, and after `OutboxSync` the row is `SYNCED`. `Recorded` while `OFFLINE` sets `result = ScanResult.SavedOffline(... same fields, uploadPending = true)`, and the row stays `PENDING`.
53. `AlreadyRecorded` sets `result = ScanResult.AlreadyRecorded(className, sessionNumber, date, firstScannedAt)` and writes no second row.
54. Every other result sets `result = ScanResult.Rejected(reason)` and stores nothing. `reason` is one of `WrongDate`, `WrongCoach`, `NotOnRoster`, `SessionCancelled`, `ClassCancelled`, `InvalidCode`, or `SessionUnknown`. `WrongDate` also carries `codeDate: LocalDate?`, parsed from the code's `date=` value, or `null` if it can't be parsed. Phase 1 precedence holds: after `CancelClass` it is `ClassCancelled`.
55. `onRecordAnother()` (C12) and `onChooseDifferentClass()` (C14) clear `result`, clear the selection, and reopen the sheet. A different session that day can then be recorded as a second row. `onScanAgain()` (C15) reopens the scanner with the same selection. `onDone()` clears `result`.
56. `recordedToday` lists this student's rows for `today`: class name, `sessionNumber`, `scannedAt`, `uploadPending` (true while `PENDING`). Other sessions that day stay unrecorded (AC36).
57. While `OFFLINE`, validation uses only the SQL cache (AC40). A session missing from the cache gives `Rejected(SessionUnknown)`, and a membership missing from the cache gives `Rejected(NotOnRoster)`. Calling `refresh()` after the clock passes local midnight replaces `today` and `choices` with the new day's.
58. `StudentSessionsViewModel` has one row per Phase 1 `StudentSessions` row: `className`, `sessionNumber`, `date`, `slot`, `status` (`UPCOMING`/`PRESENT`/`ABSENT`), and `uploadPending`. Cancelled sessions are omitted, and only `ACTIVE` classes appear. No rows gives `isEmpty = true`.
59. Rows are grouped as `upcoming` (date after today), `today`, and `earlier` (date before today). Each group is in reverse chronological order: date descending, then start time descending.
60. The day boundary follows the injected `TimeZone`. At 23:59 local Monday, an unscanned Monday session is `UPCOMING`; at 00:00 local Tuesday it is `ABSENT`. The same `Instant` in two zones can give different statuses.

### Coach
61. `CoachClassesViewModel.classes` lists the coach's own classes, public and private, in creation order: `name`, `visibility`, `slotsByWeekday`, `rosterCount` (`activeRoster` size), `pendingRequestCount`, and `isCancelled`. No classes gives `isEmpty = true`. `onCreateClass()` sets `OpenCreateClass`, and `onClassTapped(id)` sets `OpenManage(id)`.
62. `CreateClassViewModel` starts with empty `name`, `repeat = SPECIFIC_DAYS`, no days, `visibility = PUBLIC`, and `endDate = null` (no end date). `onToggleDay(day)` adds or removes a day; removing a day drops its slots. `onEveryDay()` selects all seven days and sets `daysLocked = true`. `onSpecificDays()` unlocks them.
63. `onAddSlot(day, TimeSlot)` appends a slot to that day. Each day's slots are kept sorted by start time. `onRemoveSlot(day, slot)` removes it. A slot with `start >= end` is not added and sets `slotError = InvalidSlot`.
64. `canSave` is true only when the trimmed name is not blank, at least one day is selected, and every selected day has at least one slot (D3).
65. When `endDate` is set, `previewDateCount` equals the number of distinct dates Phase 1 `CreateClass` would create: 10 for the D4 example (saved Fri 25 Sep 2026, Mon + Wed, end Sat 31 Oct 2026), and 98 for every day from Fri 25 Sep through Thu 31 Dec 2026. With no end date, `previewDateCount = null`.
66. `onSave()` calls `CreateClass(coachId, trimmed name, visibility, slots of selected days only, endDate)`. It sets `savedClassId`, the sessions are in SQL, and `CoachClassesViewModel` lists the class. An end date before today sets `error = EndDateInPast` and creates nothing. `onCancel()` creates nothing.
67. `ManageClassViewModel(classId)` header: `name`, `visibility`, `endDate`, `sessionCount`, `slotsByWeekday`, and `pendingRequestCount`. `selectedTab` starts at `SCHEDULE`, and `onSelectTab(SCHEDULE | REQUESTS | ROSTER | INVITE)` changes it.
68. Schedule: one row per session, scheduled and cancelled, in `sessionNumber` order. Each row has `sessionNumber`, `date`, `slot`, `status`, `isToday`, `isPast`, and `canCancelDate`, which is true when the status is `SCHEDULED`, the date is today or later, and the class is not cancelled.
69. `onCancelDate(date)` sets `confirmCancelDate = date` (D8). `onKeepDate()` clears it and changes nothing. `onConfirmCancelDate()` calls `CancelDate`: every session on that date becomes `CANCELLED` and keeps its number, other dates stay `SCHEDULED`, and `confirmation = DateCancelled(date)`. A past date sets `error = PastDate` and changes nothing.
70. Requests: one row per `PENDING_REQUEST`, with `studentId`, name, and employee ID; several students can appear. `onApprove(studentId)` makes the membership `ACTIVE`, so the row moves to Roster. `onDecline(studentId)` makes it `DECLINED`. No requests gives `requestsEmpty = true`.
71. Roster: one row per student in `activeRoster`, in join order, with name and employee ID. `onRemove(studentId)` sets `confirmRemove` (D13). `onKeep()` clears it. `onConfirmRemove()` calls `RemoveFromRoster`: the membership becomes `REMOVED`, the row leaves, and past attendance rows are unchanged.
72. Invite search: `onSearch()` trims the employee ID and looks it up in the directory. Only a `STUDENT` with that exact employee ID matches, giving `match(name, employeeId, currentMembershipStatus)` and `canSendInvite`, which is true when the status is `null`, `DECLINED`, or `REMOVED`. Anything else, including a coach's id, email, or name, gives `noMatch = true` with `searchedId`. A search never creates a membership.
73. `onSendInvite()` calls `InviteByEmployeeId`. `Invited` sets the match status to `INVITED`, `canSendInvite = false`, and `confirmation = InviteSent(name)`, and `InvitesViewModel` for that student lists the class. `AlreadyInvited`, `AlreadyRequested`, and `AlreadyMember` update the match status and create nothing. `NoMatch` gives `noMatch = true`, and `ClassCancelled` gives `error = UiError.ClassCancelled`.
74. Class options: `onOpenOptions()` sets `optionsOpen`. `onCancelWholeClass()` sets `confirmCancelClass`. `onKeepClass()` clears it. `onConfirmCancelClass()` calls `CancelClass`: today's and later sessions become `CANCELLED`, past sessions and their attendance stay, every row has `canCancelDate = false`, and `navigation = Back`. Afterwards, requests, invites, and scans for the class return `ClassCancelled`.
75. `DailyQrViewModel` shows `today`, `coachName`, `validFor = today`, and one `code` from `GenerateDailyCode(coachId, today)`. The code covers all of the coach's classes; there is no code per class. `VerifyDailyCode` on the same day is `VALID`.
76. If the coach has no private key in `SecretKeyStore`, the ViewModel generates an `Ed25519KeyPair` once and saves it through `SqlCoachKeyRepository`. A second ViewModel on the same day reuses the key, so the code string is identical (AC30).
77. `todaysSessions` lists every session of the coach's classes today, cancelled ones included, by start time, each with `className`, `slot`, and `status` (D19 shows `Cancelled`).
78. While `OFFLINE`, the code is still produced with no network call and the banner shows. `todaysSessions` comes from the cache. `classListUnavailable = true` only when the cache holds none of the coach's classes (D20). Calling `refresh()` after the local date changes gives a new `today` and a new code, and the old code checks as `WRONG_DATE`.
79. `CoachAttendanceViewModel` starts with `date = today`. `onPreviousDay()` and `onNextDay()` move the date, and `canGoNext = date < today`. Sections come from Phase 1 `CoachAttendanceForDate`, one per scheduled session, plus that date's cancelled sessions as sections with `cancelled = true` and no students. They are ordered by start time.
80. Each student row has name, employee ID, and `status` (`PRESENT`/`NOT_SCANNED`/`ABSENT`). A `PRESENT` row also has `chosenClassName` and `scannedAt` from the record. Only `ACTIVE` roster students appear.
81. `dayState` is `DAY_ENDED` when the date is before today and `IN_PROGRESS` when it is today. `presentCount` counts `PRESENT` student-session pairs. `absentCount` counts `ABSENT` pairs and is 0 while `IN_PROGRESS`.

### Copy and formats
82. `formatDate(LocalDate(2026, 9, 28)) == "Mon 28 Sep 2026"`, and `formatShortDate(LocalDate(2026, 10, 5)) == "Mon 5 Oct"`.
83. `formatTime` gives `"5:30 PM"`, `"6:00 AM"`, and `"12:15 PM"`. `formatSlot(17:30–18:30) == "5:30 PM – 6:30 PM"`, with an en dash (U+2013) between spaces. `formatDays({MONDAY, WEDNESDAY}) == "Mon · Wed"`, and all seven days give `"Every day"`.
84. `UiCopy` returns exactly the strings in the copy table below for each typed value. Values marked *open* have no string in Phase 2; tests assert only the typed value.

### Build
85. `JAVA_HOME=/home/box/tools/jdk17 ./gradlew :shared:jvmTest`, run on a checkout of main, passes every existing test (113 on `9b553df`: 112 Phase 1 domain + 1 template test) and every Phase 2 test, SQLDelight JVM tests included. After the package rename, those 112 Phase 1 tests live under `ph.project.active.domain`; there should be no leftover `trx.attendance.domain` test classes. iOS targets stay declared and are not built.
86. Adds to `gradle/libs.versions.toml`: the SQLDelight plugin (`app.cash.sqldelight`, database `TrxDatabase`, package `ph.project.active.data.db`), `sqlite-driver` on JVM, `android-driver`/`native-driver` declared and untested, `kotlinx-coroutines-core` in `commonMain`, and `kotlinx-coroutines-test` in `commonTest`. All versions must be compatible with Kotlin 2.4.21 and Gradle 9.5.1.
87. Phase 1 domain behavior is unchanged, but all Kotlin and Swift sources and tests move to `ph.project.active`, including `domain`, `data`, `presentation`, and the Android and iOS host and template sources (`MainActivity`, `App`, `Greeting`, `GreetingUtil`, `Platform*`, `MainViewController`). The Android manifest uses a relative `.MainActivity`. Android `namespace` and `applicationId`, and the iOS `PRODUCT_BUNDLE_IDENTIFIER` in `Config.xcconfig`, are `ph.project.active`. No Compose screen and no Firebase dependency is added.

## Copy table (exact strings; source frame in brackets)
| Typed value | String |
|---|---|
| `ConnectionUi.banner` | "No internet connection" [REQUIREMENTS, C11, C13, D20] |
| Offline subtitles: scanner / saved result / QR | "Scans are saved on this phone and upload when you are back online." [C11] · "Upload will retry when your connection returns." [C13] · "Showing today’s code saved on this phone." [D20] |
| `classListUnavailable` | "Today’s class list can’t load" + "The code still works. Students on the roster can scan it now; their phones upload later." [D20] |
| Status labels | "Request to join", "Pending", "Awaiting approval", "Joined", "Invited", "Scheduled", "Cancelled", "Upcoming", "Present", "Absent", "Upload pending", "Public", "Private" [REQUIREMENTS, Status Chip] |
| `requestAgain` | "You can request again" [A5] |
| Registration titles / submit | STUDENT "Create account" · COACH "Coach registration" [B1, B4]; submit "Create account" · "Create coach account" · "Create account and send request" (FromRequestToJoin) · loading "Creating account…" [B1, B4, B5, B6] |
| `FieldError.InvalidEmail` / `EmployeeIdTaken` | "Enter a valid email address." / "This employee ID is already registered." [B3] |
| `FieldError.PasswordTooShort` | "At least 8 characters." (B1 helper text; using it as the error text is an assumption) |
| `ScanResult.Present` | "You’re marked Present" [C12] |
| `ScanResult.SavedOffline` | "Saved on this phone" [C13] |
| `ScanResult.AlreadyRecorded` | "You already have attendance" + "{class} on {date} is already recorded. No new record was added." [C14] |
| `ScanResult.Rejected` title | "Scan not accepted" + reason + " Nothing was saved." [C15] |
| `WrongDate` / `WrongCoach` / `NotOnRoster` / `SessionCancelled` | "This code is for a different date." / "This code isn’t from your coach for this class." / "You’re not on this class roster." / "This date is cancelled." [C15 dev notes] |
| `InviteAccepted` | "You joined {class}. It’s now in My classes." [C3] |
| Invites empty | "No invites yet" + "When a coach invites you to a class, it shows up here. You can accept or decline it." [C4] |
| Coach classes empty | "No classes yet" + "Create your first class. Public classes appear in the class list for everyone." [D2] |
| `requestsEmpty` | "No requests" + "Students who request this class will appear here." [D11] |
| `DateCancelled` | "{Mon 5 Oct} cancelled. Other dates stay scheduled." [D9] |
| `InviteSent` / button after send | "Invite sent to {name}. They’ll see it in Invites." / "Invite sent" [D15] |
| `noMatch` | "No student found" + "No account uses {id}. Nothing was sent." [D16] |
| `NOT_SCANNED` / `DAY_ENDED` / `IN_PROGRESS` | "Not scanned yet" / "Day ended" / "Today · in progress" [D21, D22] |
| *open* | `UiError.AuthFailed`, `FieldError.Required`, `EmailTaken`, `InvalidCode`, `ClassCancelled` (scan, request, invite), `SessionUnknown`, `PrivateClass`, `UiError.Offline` beyond the banner, `PastDate`, `EndDateInPast`, `InvalidSlot`, invite `AlreadyInvited`/`AlreadyRequested`/`AlreadyMember`, `noSessionsToday`, guest list empty, Sessions empty, class detail `unavailable`, `joinOutcome = ClassCancelled` |

## Out of scope for Phase 2
Compose Multiplatform screens and navigation; shake, camera, scanner, brightness, and QR-rendering platform bridges; a real `NetworkMonitor`; Firebase Auth/Firestore SDKs and projects; security rules; key backup to the account; publishing a coach's public key to other phones; Android Keystore and iOS Keychain; store publishing and signing; GitLab/GitHub CI; sign-out and account deletion; notifications; chat; payments; capacity; waitlist; NFC; present-without-scan; email confirmation; a visible role picker or shake hint; vendor analytics; class descriptions (A2 shows one, but REQUIREMENTS and the model have none).

## Decisions locked 11 Nov 2026
Sheen approved the Phase 2 criteria as written. These answers are now locked:

1. **Offline writes.** Only attendance scans are queued. The actions listed in AC22 stay blocked while `OFFLINE` (`UiError.Offline`, no write).
2. **Missing copy.** Strings marked *open* in the copy table stay open in this phase: tests assert the typed value only. `FieldError.PasswordTooShort` uses "At least 8 characters."
3. **Cancelled classes.** A cancelled class stays on the coach's Classes list with `isCancelled`, and is dropped from the student's My classes. Outstanding invites sent before `CancelClass` still appear in Invites; accept and decline follow Phase 1.
4. **Invited student on a public class.** Class detail shows `Invited` with no button (AC41).
5. **Membership origin and timestamps.** Not in Phase 2. Do not change the Phase 1 `Membership` model.
6. **Sessions order.** Latest first in every group (AC59).
7. **Create-class defaults.** Starts with Public and no end date (AC62).
8. **Sign-out and account deletion.** Out of scope for Phase 2. Needed before App Store submission, not in this phase.
9. **Package name.** The project package is `ph.project.active`. Kotlin sources live under `ph.project.active.domain`, `.data`, and `.presentation`. Android and iOS host and template sources also use `ph.project.active`. Android `namespace`/`applicationId` and the iOS `PRODUCT_BUNDLE_IDENTIFIER` in `Config.xcconfig` are `ph.project.active`. The Android manifest uses a relative `.MainActivity`. Phase 1 behavior stays the same; Builder finishes any remaining host and template moves on this PR.
