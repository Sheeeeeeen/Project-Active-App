# TRX Class Attendance — Phase 1 Spec (shared domain)

Tested on Linux in `shared/commonTest` with in-memory fakes and an injectable `Clock`.
No UI, no Firebase, no SQLDelight in this phase. Status: draft until Sheen approves `ARCHITECTURE.md`.

Package: originally `trx.attendance.domain`. Since Phase 2 this domain lives under `ph.project.active.domain` (`model`, `repository`, `repository.inmemory`, `usecase`, `crypto`); see `SPEC-phase2.md` Packages. Behavior is unchanged.

## Use cases under test
`RegisterStudent`, `RegisterCoach`, `SignIn`, `CreateClass` (session generation), `CancelDate`, `CancelClass`, `RequestToJoin`, `ApproveRequest`, `DeclineRequest`, `InviteByEmployeeId`, `AcceptInvite`, `DeclineInvite`, `RemoveFromRoster`, `GenerateDailyCode`, `RecordAttendance` (includes offline/outbox policy), and queries for public classes, student sessions, today's choosable classes, and coach attendance for a date.

## Acceptance criteria

### Accounts
1. `RegisterStudent(name, email, password, employeeId)` creates a `STUDENT` who can sign in immediately (no email confirmation).
2. A blank name, email, password, or employee ID throws `IllegalArgumentException`. Email and employee ID are trimmed. Password is stored only as a hash, never in plain text in the domain model.
3. A duplicate email or a duplicate employee ID throws `IllegalArgumentException` and creates no user.
4. `RegisterCoach(name, email, password)` creates a `COACH` with `employeeId = null`. A coach cannot be registered with an employee ID.
5. `SignIn(email, password)` returns the user on a match and throws `AuthFailed` on a wrong password or unknown email.
6. A user's role cannot be changed after creation.

### Session generation
7. Saving a class on Friday 25 September 2026, weekdays Monday and Wednesday, one slot 17:30–18:30, end date Saturday 31 October 2026, creates ten `SCHEDULED` sessions, Monday 28 September through Wednesday 28 October, numbered 1..10 in date then start-time order.
8. The save date is included if it matches a selected weekday; dates before the save date are not created.
9. `endDate = null` creates sessions from the save date through a configurable open-ended horizon used only in tests (e.g. 12 months); the class stays open until cancelled.
10. An every-day class with one slot per day creates one session for every remaining date in the range (save date through end date).
11. Two slots on the same weekday create two sessions on each matching date, each with one `TimeSlot`, numbered in start-time order. Attendance is recorded per session, so a student can mark each slot separately.
12. Past dates are never created, even if they match the weekday pattern.

### Cancel
13. `CancelDate` on today or a future date sets every session on that date to `CANCELLED` and keeps each `sessionNumber`. Sessions on other dates stay `SCHEDULED`.
14. `CancelDate` on a past date throws `IllegalArgumentException` and leaves all sessions unchanged.
15. `CancelClass` sets `cancelledAt`, marks today and future sessions `CANCELLED`, and leaves past sessions and their attendance records unchanged.
16. After `CancelClass`, new requests, invites, and scans for remaining dates are rejected.

### Visibility and membership
17. Public classes are listed with no account. Private classes are visible only to their coach and to students with `ACTIVE` membership.
18. Guest `RequestToJoin` is not a domain call (the UI opens registration first). After `RegisterStudent` from that path, exactly one `PENDING_REQUEST` is created for that class.
19. A signed-in student `RequestToJoin` on a public class creates one `PENDING_REQUEST`. A second call while a request is still pending does not create another.
20. `ApproveRequest` sets membership to `ACTIVE`. `DeclineRequest` sets `DECLINED` and leaves the student off the roster.
21. After `DECLINED` or `REMOVED`, `RequestToJoin` may be called again and creates a new `PENDING_REQUEST`.
22. `InviteByEmployeeId` with exactly one matching student creates an `INVITED` membership. An unknown employee ID creates nothing and returns `NoMatch`. A coach cannot invite another coach.
23. `AcceptInvite` sets `ACTIVE`. `DeclineInvite` does not put the student on the roster.
24. `RemoveFromRoster` sets `REMOVED`. That student can request again (AC21).
25. A student is "joined" only when membership is `ACTIVE`. Pending, invited, declined, and removed are not joined.
26. `RequestToJoin` on a private class is rejected. Private classes are joinable only by invite.

### Daily code
27. `GenerateDailyCode(coachId, date)` returns a URI `trxatt://v1/daily?coach={id}&date=YYYY-MM-DD&sig={sig}` that verifies with that coach's public key.
28. A code produced for date D does not verify on a clock whose local date is not D.
29. A code with a missing, truncated, or foreign signature does not verify.
30. A second generate for the same coach and date may rotate the signature; only a currently valid signature verifies. Older signatures for that date fail.
31. Generating a code does not require a network (pure function of the coach's private key, id, and date).

### Attendance
32. `RecordAttendance` requires a chosen session (class + date + slot), a daily code that verifies for that session's coach and date, and `ACTIVE` membership on that class. On success it writes one `PRESENT` record with `scannedAt` equal to the clock's now (not a later upload time) and `uploadState = PENDING`.
33. The chosen session is the one passed in, not inferred from scan time.
34. A second `RecordAttendance` for the same student and the same session returns `AlreadyRecorded` and does not write a second record. A `RecordAttendance` for a different session (another slot or class) on the same date succeeds and writes a new record.
35. Store nothing and return a typed error when: membership is not `ACTIVE`; the code is invalid or for another date or coach; no session was chosen; the session is `CANCELLED`; the class was cancelled.
36. Other scheduled sessions that day for the same student stay unmarked.
37. Student session query is one row per session (slot). A future `SCHEDULED` session is `UPCOMING`. Today with no record is `UPCOMING`. Today or past with a record is `PRESENT`. After the local day is over, a `SCHEDULED` session with no record is `ABSENT`. `CANCELLED` sessions are omitted from this list.
38. Today's choosable list is one row per `SCHEDULED` session today for classes the student is `ACTIVE` on. `CANCELLED` sessions are omitted.
39. Coach attendance for a date is listed per session (slot). For each session, each `ACTIVE` roster student is `PRESENT` if a record exists for that session; `NOT_SCANNED` if the date is today and no record exists; `ABSENT` if the local day is over and no record exists.
40. Offline policy (still in-memory in this phase): if `ACTIVE` membership and the session schedule are available in the local store, a valid scan is kept with `PENDING` even when `NetworkStatus.OFFLINE`. If membership or schedule is not in the local store, store nothing (`NotOnRoster` or `SessionUnknown`).
41. `FlushOutbox` in `NetworkStatus.ONLINE` marks pending records `SYNCED` and does not change `scannedAt`. While `OFFLINE`, the outbox is unchanged.

### Build
42. `./gradlew :shared:jvmTest` passes on Linux. iOS targets may be declared but are not built in this phase.

## Out of scope for Phase 1
Compose Multiplatform screens, shake, camera, Firebase, SQLDelight schema, security rules, store publishing, GitLab CI.
