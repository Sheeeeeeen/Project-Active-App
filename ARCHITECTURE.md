# TRX Class Attendance — Technical layout

Sources: `REQUIREMENTS.md` (product source of truth), `FIGMA-NOTES.md` (screens, IA, tokens).
Status: Phase 1 approved. UI stack updated 11 Nov 2026: shared Compose Multiplatform, targeting Play Store and App Store.

## Goal
One Android and iOS app. Coaches run recurring TRX classes. Students join, then record attendance by choosing today's class and scanning a daily QR code on the coach's phone. After sign-in, a coach sees only coach screens and a student sees only student screens. One account has one role, set at registration.

## Stack (recommended)
- **Build system:** Gradle with the Kotlin DSL, generated from the official KMP wizard with shared Compose Multiplatform UI for Android and iOS. Stay on Gradle, not Kotlin Toolchain: Toolchain is still Alpha, which is a poor fit for a Play Store and App Store release. Set `kotlin { jvmToolchain(17) }` in the Gradle build so JVM compilation uses JDK 17 consistently.
- **Kotlin Multiplatform** for shared domain, data contracts, and ViewModels. Shared ViewModels are tested in `commonTest` on Linux.
- **UI:** Compose Multiplatform in `composeApp`, one set of screens for Android and iOS, mapped to the Figma frames and tokens. Android can be built and run from Linux. iOS still needs Xcode on Sheen's Mac to compile, run, and submit to the App Store.
- **Typography:** ship a shared font (Inter, already used in the Figma mock-ups) so both stores look the same, with system font scaling enabled. Do not split SF Pro / Roboto across platforms.
- **Local database:** SQLDelight in `commonMain` (cache + offline attendance outbox).
- **Backend (decided 11 Nov 2026):** Firebase Authentication (email and password, no email confirmation) and Cloud Firestore. Repository interfaces still hide Firebase so it can be replaced later.
- **Time:** `kotlinx-datetime`, with an injectable `Clock`. "Today" and "the day is over" use the phone's timezone.
- **DI:** constructor injection in Phase 1; Koin only if wiring gets noisy.
- **Tests:** `kotlin-test` + `kotlinx-coroutines-test` in `commonTest`. No mocking library required; use fakes.

## Repository layout
```
trx-attendance/
  REQUIREMENTS.md
  FIGMA-NOTES.md
  ARCHITECTURE.md          ← this file
  SPEC-phase1.md           ← domain acceptance criteria
  settings.gradle.kts
  shared/                  KMP library
    src/commonMain/kotlin/trx/attendance/
      domain/              models, rules, use cases (pure Kotlin)
      data/                repository interfaces, DTOs
      presentation/        ViewModels and UiState
    src/commonTest/        Phase 1 and 2 tests (Linux)
    src/androidMain/       Firebase, SQLDelight Android drivers
    src/iosMain/           Firebase, SQLDelight iOS drivers
  composeApp/              Compose Multiplatform UI (commonMain screens + androidMain/iosMain bridges)
  androidApp/              Android application entry; depends on composeApp
  iosApp/                  Thin Xcode host that embeds the Compose iOS app
```

## What is shared vs native

**In `shared` (KMP)**
- Domain models and rules: accounts, class schedules, session generation, join requests, invites, roster, cancel date / cancel class, attendance status, QR payload generate and verify, offline queue policy.
- Repository interfaces and the SQLDelight cache/outbox.
- Shared ViewModels and UiState for every screen that is more than a platform widget.

**In `composeApp` (shared UI)**
- Screen layout and navigation, following Figma IA, written once in Compose Multiplatform.
- Design tokens (colors, spacing, 24-pt side margins, 52-pt primary buttons, minimum 48 dp / 44 pt touch targets).
- Offline banner ("No internet connection") and all feedback UI (dialogs, toasts, empty states, QR card).
- QR art is always `#0B0B0C` on `#FFFFFF`.

**Platform-specific (expect/actual or androidMain/iosMain in composeApp)**
- Shake detection on the registration screen (no on-screen hint).
- Camera permission, QR scanner, and displaying the coach QR.
- Store-required permission strings and privacy disclosures.

## Navigation (three roots)
Sign-in replaces the root. There is no role switch afterwards.

| Root | Entry | Structure |
|---|---|---|
| Guest | App launch, signed out | Stack: public class list → class detail → registration / sign in |
| Student | After student sign-in | Tabs: Classes · Attend · Sessions · Invites |
| Coach | After coach sign-in | Tabs: Classes · QR · Attendance. Managing a class is a push with top tabs: Schedule · Requests · Roster · Invite. Create class is a full-height sheet. "Cancel whole class" is in the class options menu. |

A coach opening a public class detail sees information only: no request button, no roster, no QR.

## Domain model

```
User            id, role (STUDENT|COACH), name, email,
                employeeId (required and unique for students; null for coaches)

TimeSlot        start: LocalTime, end: LocalTime          // wall time, AM/PM in the UI

Class           id, coachId, name, visibility (PUBLIC|PRIVATE),
                weekdays, slotsByWeekday: Map<DayOfWeek, List<TimeSlot>>,
                endDate: LocalDate? (inclusive; null = open until cancelled),
                createdOn: LocalDate, cancelledAt: Instant?

Session         id, classId, date, slot: TimeSlot,        // one session per time slot
                sessionNumber (stable; assigned in date, then start-time order),
                status (SCHEDULED|CANCELLED)

Membership      id, classId, studentId,
                status (PENDING_REQUEST|INVITED|ACTIVE|DECLINED|REMOVED)

Attendance      id, sessionId, studentId, scannedAt: Instant,
                uploadState (PENDING|SYNCED)
                // stored only for a successful Present; Absent is computed

DailyCode       coachId, date, signature
                // URI: trxatt://v1/daily?coach={id}&date=YYYY-MM-DD&sig={base64url}
```

**Session rule (decided 11 Nov 2026):** a session is one time slot on one date of one class. Attendance is one record per student per session, so a student can mark each slot separately. The choose-class sheet shows one row per slot.

**Other rules adopted from design, consistent with requirements**
- Cancelled dates stay on the coach schedule as Cancelled. Every session (slot) on that date is Cancelled and keeps its session number. Those sessions are hidden from the student's Sessions list and from the choose-class sheet. A scan for a cancelled session is rejected.
- Before the day ends, coach attendance shows "Not scanned yet" for a session with no scan. After the day is over, a roster student with no scan for that session is Absent. The student's own list still shows Upcoming for a session today until they scan it, then Present; Absent only after the day ends. A student can be Present for one slot that day and Upcoming or Absent for another.
- "Cancel whole class" cancels remaining dates (today and future), keeps past sessions and attendance, and stops new joins.

## Session generation
Saving a class creates sessions from the save date through the end date (end date included). The save date is included if it matches a selected weekday. Dates before the save date are not created. An every-day class selects all seven weekdays.

Example from the requirements: saved Friday 25 September 2026, Monday and Wednesday, one slot 17:30–18:30, end date Saturday 31 October 2026 → ten sessions, Monday 28 September through Wednesday 28 October, numbered 1 to 10 in date order. If that Monday also had a second slot, that date would have two sessions, numbered in start-time order.

Cancel one date: today or a future date only. Every session on that date becomes Cancelled and keeps its session number. Sessions on other dates stay Scheduled. The series keeps repeating.

## Membership
- Guest "Request to join" opens student registration (shake is ignored). After the account is created, one pending request is sent for that class.
- A signed-in student "Request to join" sends the request immediately. A second tap must not create a second pending request.
- After decline or removal, "Request to join" returns and a new request may be sent.
- Coach Approve → membership ACTIVE (student sees the class as joined). Decline → not on the roster.
- Invite: search a unique employee ID. One match: the coach can send an invite. No match: send nothing. Student Accept → ACTIVE. Decline → not on the roster.
- Private classes are visible only to the coach and active roster students. Joining a private class is by invite only.
- Public classes are visible with no account.

## Daily QR and attendance
- One QR per coach per date, generated on the coach's phone. It covers every class that coach has that day. It must work offline; show "No internet connection" if the roster cannot load.
- The code is valid only if it was produced by that coach's app for today's date (phone timezone). A scan with no valid coach code does not mark the student present.
- Signing: the coach has an Ed25519 key pair. The private key stays on the coach's device (and is backed up to the account so a reinstall can still show a valid code). The public key is stored on the server and cached on student devices that need to verify.
- Student flow: open Attend → sheet lists today's Scheduled sessions (one row per slot) for classes the student is on (not Cancelled) → choose one session → scanner opens. Closing the sheet stores nothing.
- A valid scan writes Present for that session, at the scan time (not the upload time). Other sessions that day stay unmarked until the student scans again and chooses them. A second scan of the same session shows "already have attendance" and does not write a second record. A scan of a different slot the same day is a new record.
- Offline: the scan is validated against the cached roster, schedule, date, and signature (if the public key is cached). If membership cannot be confirmed from the cache, store nothing. If valid, keep the record on the phone with `uploadState = PENDING`, show "Upload pending" / "No internet connection", and upload when the connection returns.
- Store nothing when: the student is not on that class roster; the code is for another date or was not produced for that coach; the student closes the sheet without choosing; the session is cancelled.

## Offline and sync
- SQLDelight holds a cache of classes, sessions, memberships, and an attendance outbox.
- The outbox uploads in order when the network returns. The recorded time stays the scan time.
- ViewModels expose connection state so the UI can show the exact label "No internet connection".

## Architecture layers
```
androidApp / iosApp     thin host apps (Play / App Store entry points)
        │
composeApp              shared Compose UI, navigation, platform bridges
        │
shared/presentation     ViewModels, UiState, user actions
        │
shared/domain           use cases and rules (no Android/iOS types)
        │
shared/data             AuthRepository, ClassRepository, MembershipRepository,
                        AttendanceRepository, DailyCodeRepository, Outbox
        │
Firebase + SQLDelight   (Phase 2+; Phase 1 uses in-memory fakes)
```

Use cases talk only to repository interfaces. Errors that the UI must show (validation, "already have attendance", rejected scan, no match) are typed results or domain errors, not uncaught exceptions.

## Build and test
- Linux: `./gradlew :shared:jvmTest` and Android Compose previews or an emulator. iOS targets are declared but not built on the shared computer.
- Sheen builds, runs, and archives the iOS app on a Mac with Xcode.
- Feature work stays on a feature branch, test-first: Tester writes failing tests from the numbered criteria, Builder makes them pass, Reviewer checks the branch.

## Phases
1. **Shared domain (in progress).** Pure Kotlin, in-memory fakes, all rules in `SPEC-phase1.md`, tested on Linux. No UI, no Firebase.
2. **Shared ViewModels + SQLDelight + repository fakes** that later wrap Firebase. Still Linux-tested.
3. **Shared Compose Multiplatform UI** mapped to Figma frames A–D, including shake, scanner, and QR, runnable on Android from Linux.
4. **iOS host + device verification** on Sheen's Mac (Xcode build, camera, shake, App Store icons and permission strings).
5. **Production and store release:** real Firebase project, security rules, outbox upload, signing, Play Store and App Store listings.


## Play Store and App Store
The same Compose UI ships to both stores. The store packages are still platform-specific.

- **Accounts.** A Google Play Console account and an Apple Developer Program membership are required before upload. Sheen owns those accounts; the team prepares the binaries and listing copy.
- **Android (Play).** Ship an Android App Bundle (`.aab`) with Play App Signing. Configure the application ID, version code, target API, and a Data safety form that covers email/password auth, Firestore, camera, and data stored on device. Include a privacy policy URL.
- **iOS (App Store).** Archive from Xcode on a Mac with the app's certificates and provisioning profiles. Provide Privacy Nutrition Labels, camera usage text (`NSCameraUsageDescription`), and any photo/network disclosures the scan flow needs. The shake-to-open coach registration path has no on-screen hint; be ready to explain that in review notes so it is not treated as hidden functionality.
- **Versioning.** Use one marketing version (for example `1.0.0`) on both stores. Platform build numbers increment separately (`versionCode` on Android, `CFBundleVersion` on iOS).
- **Icons and screenshots.** Store listing assets are per store; in-app UI is shared. Keep the gold-and-black system from Figma.
- **Signing and CI.** Local signing is enough for the first upload. CI signing can wait until the first store build is working.

## Out of scope (MVP)
Notifications, chat, payments, capacity, waitlist, NFC, a present-without-scan button, email confirmation, a visible role picker or shake hint, vendor analytics.

## Open questions
1. GitLab project URL, when the team should push (not required to start Phase 1 on the shared computer).
