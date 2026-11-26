# TRX Class Attendance — Phase 3, Flow 7 Spec (student Attend tab with the scanner, plus Sessions)

## 1. Status
**Approved by Sheen, Thu 26 Nov 2026, 1:50 PM (Asia/Manila), with suggested answers to O1 (Upcoming = next 7 days), Upcoming soonest first, and O2 (demo data on iOS debug builds only).** Planner, Thu 26 Nov 2026 (reworked 1:45 PM Asia/Manila). Sheen accepted the defaults in `/workspace/flow7-questions.md` at 1:29 PM, then **overrode questions 1 and 2** in a DM: "1. The student dont need to know the coach key in order to scan the qr code 2. I have 2 physical device that can test student and coach in android". After approval, Tester writes the failing tests on a branch from `main`, with this spec as the first commit. Builder never pushes to `main` and never merges; Sheen merges.

- **Baseline:** `main` at `edde156cabcd4336dd5ddc88b3f9ffe34c323843` (`edde156`), the merge of PR #15 (flow 6). The student Attend and Sessions tabs are still `StudentTabPlaceholder`.
- **Design:** Figma `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", Section C `25:4903`: C5 `22:4712`, **C5b `133:6991`** (Sessions with a cancelled session), C6 `22:4926`, C7 `22:5338`, C8 `22:5483`, C9 `22:5628`, C10 `22:5806`, C11 `22:5869`, C12 `22:5945`, C13 `22:6016`, C14 `22:6096`, C15 `22:6159`, **C18 `133:7035`** (camera denied for good). Flow 7 replaces the placeholders C16 `97:5768` and C17 `97:5780`. Pictures are in `/workspace/flow7-figma/`; background is in `/workspace/flow7-context.md`.
- **Package:** `ph.project.active`. **Code rules:** one public type per file; no nested classes (sealed result types keep their existing style, as in `AttendanceResult` and `RejectReason`); Koin only in `composeApp`; tests never start Koin; one NavHost, and the student tabs stay inside the student root; new constructor parameters go last, with defaults; any placeholder screen wears `PlaceholderTag`.
- **Open questions:** O1 and O2 in section 13. One item there also needs Sheen's OK, because it changes his earlier Sessions-order decision.

## 2. Decisions

**Sheen's answers (26 Nov 2026):**

1. **The student phone does not check the signature** (Sheen's DM, overriding the suggested answer). The phone checks only:
   - the link format;
   - that the code's date is today;
   - that the code's coach is the chosen class's coach;
   - that the chosen class has its session today;
   - and the existing roster and cancellation rules.
   The signature (`sig`) is **stored with the record** so the server can verify it after flow 8. The student phone does no public-key lookup. This edits Phase 1 AC32 and AC35 (section 12).
2. **No demo key** (Sheen's DM, overriding the suggested answer). Sheen tests on two physical Android phones. The coach phone keeps signing with its own key, as today.
3. **Scanner.** A small `QrScanner` seam that hands back the scanned text. Android uses CameraX plus ML Kit barcode scanning with the **bundled** model. iOS uses AVFoundation `AVCaptureMetadataOutput`. No third-party KMP camera library. Tests use a fake scanner. The preview sits inside Compose (`AndroidView` / `UIKitView`). There is a torch button, shown only when the phone has a torch. The scanner stops after the first code it hands over. (Builder + Tester.)
4. **Camera permission.**
   - The system prompt appears only after a tap on **Allow camera** (C9).
   - If Android refuses but can still ask, C9 stays, with Allow camera and Open Settings.
   - If access is denied for good (iOS after one no; Android once the system stops asking), **C18** shows on the class sheet: a warning callout in place of the Scan button, with a primary **Open Settings** button.
   - Coming back from Settings re-checks.
   - The iOS usage text is "The camera is only used to read your coach’s QR code."
   - (Tester + Designer.)
5. **No time window.** A scan is accepted at any time on the session's date. The phone clock decides the day.
6. **Other QRs and errors.** Text that does not start with `trxatt://` is ignored and the scanner keeps looking. Every rejection shows C15 "Scan not accepted" with one body line for that reason (copy table), ending " Nothing was saved." The "You chose" and "Today" rows always show. "Code date" shows only for the wrong-date case. The buttons are Scan again and Close.
7. **Offline screens C11 and C13** are built now and driven by the fake offline switch (`MutableNetworkMonitor`). (Designer.)
8. **Success feedback.** One short vibration plus the result screen, and no sound. It fires for Present and Saved on this phone, and not for C14 or C15. Android uses `HapticFeedbackType.Confirm`; iOS uses `UINotificationFeedbackGenerator` success. (Designer.)
9. **Empty states.** Attend with no class today: the sheet shows "No classes today." and "Your scheduled classes for today show up here.", and Scan stays disabled. Sessions with no rows: "No sessions yet" / "When you join a class, its dates show up here." Offline: the usual banner, and local data still shows.
10. **Sessions rows.** While a Present row waits to upload, an **Upload pending** chip shows under the Present chip. Rows are not tappable, and there is no detail screen.
11. **Cancelled dates on Sessions (C5b).**
    - A single cancelled date of a class the student is on is listed, dimmed, with a **Cancelled** chip, and is never Absent.
    - A cancelled date today goes at the end of the Today section.
    - It keeps its session number.
    - A fully cancelled class lists none of its cancelled dates.
    - The class sheet still leaves cancelled dates out.
    - This edits Phase 1 AC37 and Phase 2 AC58. (Designer.)
12. **No profile icon** on Attend or Sessions until sign-out exists.
13. **Key-save failure (flow 6 carry-over).** Log one debug line on the coach phone; nothing visible changes.

**Designer's layout rules for Sessions (C5b), 26 Nov 2026:** Upcoming is sorted by date, then by start time (soonest first; see the note in section 13). The TODAY tag sits on its own line under the class name, so it never overlaps the status chip.

**Already decided, not reopened:**

| Rule | Source |
|---|---|
| The student picks the class; the scan time never decides it; nothing marks Present without a scan | Phase 1 AC33; C6 / C7 notes |
| One record per student per session. A second scan of the same session shows C14 **"You already have attendance"** and keeps one record. Another slot or class the same day records separately. | Phase 1 AC34; Phase 2 AC53 |
| Other sessions that day stay unmarked; "Record another class" goes back to the sheet | Phase 1 AC36; Phase 2 AC55 |
| Class sheet: today's scheduled sessions of classes the student is on; cancelled ones are left out; already-recorded ones stay | Phase 1 AC38; Phase 2 AC48 |
| Opening Attend shows the sheet; × or swipe down closes it with nothing saved; the selected row has an outline plus a check | C6–C8 notes; Phase 2 AC49 |
| Offline: a valid scan is kept as pending if the membership and session are on the phone; it uploads later with its scan time | Phase 1 AC40–41; Phase 2 AC52, AC57 |
| Session numbers count every date of the class in date, then start-time order. Cancelling a date keeps every number, so a cancelled session keeps its number and later ones keep counting from it. **Checked: already true.** | Phase 1 AC7, AC11, AC13; `CreateClass`, `CancelDate` |
| Absent only after the local day ends; the phone time zone decides | Phase 1 AC37; Phase 2 AC60 |
| Refresh on tab shown and on foreground, live updates for local records, no polling, no pull-to-refresh, no spinners | Flow 6 decisions 10–11 |
| Sign-out pop and dropping `onNavController` stay later | Flow 6 decision 21 |

## 3. Scope

**In:**
- The real Attend tab (C6–C15, C18) and Sessions tab (C5, C5b), bound to `AttendViewModel` and `StudentSessionsViewModel`.
- The new student-side code check (no signature check), with the code's `coach`, `date` and `sig` stored on the record and sent through the outbox.
- `QrScanner`, camera permission and haptics seams with Android, iOS and desktop implementations.
- Cancelled rows and the new row order on Sessions.
- Camera manifest and Info.plist entries.
- Koin wiring, new copy, and the key-save log line.

**Out:**
- Signature checks anywhere on the phone, and any student-side key lookup. The server checks after flow 8.
- Key publishing, a real `AttendanceUploader` / Firebase, and the coach phone seeing a scan made on another phone.
- Sign-out and account deletion.
- A scan time window, sound, a session detail screen, pull-to-refresh and spinners.
- The flow 8 Designer pass.
- Coach screen changes, apart from the key-save log line.

## 4. User stories

- As a student, I open Attend, pick my class, scan my coach’s QR, and see that I’m marked Present.
- As a student with no internet, I can still scan; the phone keeps it and uploads later.
- As a student who scans twice, I’m told I already have attendance, and nothing is doubled.
- As a student who scans the wrong code (old date, another coach, a broken code), I’m told why. A random QR is just ignored. Nothing is saved.
- As a student who said no to the camera, I get a clear way to Settings.
- As a student, Sessions shows my dates: what’s next, today, and what I attended or missed, including dates the coach cancelled.

## 5. Screens and state

Everything lives inside the student root. **No new navigation routes.** The scanner and result screens are full-screen states of the Attend tab with the tab bar hidden. System Back on them acts as ×. Switching tabs never changes the back stack.

| Screen | Shown when | Content |
|---|---|---|
| C6 Attend home | Attend tab, no result, scanner closed | Eyebrow "TRX ATTENDANCE", title "Attend", subtitle `formatDate(today)`. No profile icon. Dark card: icon, "Record attendance", body, **Choose class** (`onOpened()`). "Recorded today" and one row per `recordedToday` item: class name, "Session {n} · scanned {time}", Present chip, plus an Upload pending chip while pending. Header and rows are hidden when there are no rows. Offline: banner under the title. |
| C7 / C8 Class sheet | `sheetOpen` | `ModalBottomSheet` (the same Compose sheet on iOS). "Which class are you attending?", "Today · {formatDate} · choose one", ×. One row per choice: class name, "{formatSlot} · Coach {coachName}", radio. The selected row has a 2dp outline, a filled check and `selected` semantics. **Scan coach’s code** is enabled only when `canScan`. Footnote. Empty: the two empty lines and a disabled Scan. |
| **C18** Camera off (on the sheet) | sheet open, a class selected, camera access `DENIED_FOR_GOOD` | Same as C8, but in place of the Scan button and footnote: a warning callout (triangle icon, "Camera access is off", "Allow camera access in Settings to scan your coach’s code.") and a primary **Open Settings** button. |
| C9 Camera permission | scanner open, access `NOT_ASKED` or `DENIED_CAN_ASK` | Dark chrome: ×, "Scan coach’s code", no torch. "RECORDING FOR" pill, "{class} · {start}", **Change**. Camera icon, "Allow camera to scan", body, **Allow camera**, plus an **Open Settings** text button only for `DENIED_CAN_ASK`. Figma's grey hint line is a design note and does not ship. If the system answers `DENIED_FOR_GOOD`, the scanner closes back to the sheet with the selection kept (`onChangeSession()`), which then shows C18. |
| C10 Scanner | scanner open, access `GRANTED` | Camera preview with corner frame; ×, title, torch (only if `hasTorch`); pill + Change; "Point at the QR code on your coach’s phone" / "It scans automatically." |
| C11 Scanner offline | as C10, offline | Banner "No internet connection" with subtitle `scannerOfflineSubtitle()`. The second hint line becomes "No connection needed. The scan is kept on this phone." |
| C12 Present | `result is Present` | ×. "You’re marked Present" / "Your coach will see you on this session." Present chip. Rows: Class, Session ("Session {n} · {formatDate}"), Time, Scanned. **Done**, **Record another class**. |
| C13 Saved offline | `result is SavedOffline` | Banner (title only). "Saved on this phone" / body. Present + Upload pending chips. Rows: Class, Session, Scanned ("{time} (recorded time)"), Upload ("Waiting for connection"). **Done**. |
| C14 Already recorded | `result is AlreadyRecorded` | "You already have attendance" / `scanAlreadyRecordedBody`. Present chip. Rows: Class, Session, First scan. **Done**, **Choose a different class**. |
| C15 Not accepted | `result is Rejected` | "Scan not accepted" / "{reason line} Nothing was saved." Rows: You chose, Today, Code date (wrong date only). **Scan again**, **Close**. |
| C5 / C5b Sessions | Sessions tab | Eyebrow, "Sessions", "Your dates for joined classes". No profile icon. Groups **Upcoming**, **Today · {formatShortDate}**, **Earlier**; an empty group is hidden. Row: date tile (e.g. "WED" / "30" / "SEP"), class name, then for today's rows the **TODAY** tag on its own line under the name, then "Session {n} · {start}", and a status chip on the right (Upcoming / Present / Absent / Cancelled; Upload pending under Present while pending). Cancelled rows are dimmed (content at 50% alpha, chip still readable). Rows have no click action. Empty: title + body. Offline: banner under the title. |

**Buttons:**
- Choose class → `onOpened()`.
- Sheet × or swipe → `onCloseSheet()`.
- Row → `onChoose(id)`.
- Scan coach’s code → `onScanCoachCode()`.
- Change → `onChangeSession()`.
- Scanner × or Back → `onCloseScanner()` (new).
- Done, C15 Close, or result × → `onDone()`.
- Record another class → `onRecordAnother()`.
- Choose a different class → `onChooseDifferentClass()`.
- Scan again → `onScanAgain()`.
- Open Settings → `permission.openSettings()`.

**Refresh:** `StudentHomeScreen`'s existing `LifecycleStartEffect(selected)` also refreshes Attend and Sessions.

## 6. Shared and app changes (new constructor parameters go last, with defaults)

**Shared:**

- **S1. Read the link, don't verify it.**
  - New `data class DailyCodeLink(val coachId: String, val date: LocalDate, val rawCoach: String, val rawDate: String, val sig: String)` in `domain/model/DailyCodeLink.kt`.
  - New `internal fun parseDailyCodeLink(code: String): DailyCodeLink?` in `domain/crypto/DailyCodePayload.kt`.
  - It returns a link only when `code.trim()` matches `DAILY_URI` (scheme `trxatt://v1/daily`, with `coach`, `date` and `sig` all present in that order), the coach value decodes (`decodeCoachIdOrNull`) to a non-empty id, the date parses, and `sig` is non-empty base64url (`fromBase64UrlOrNull` gives at least one byte).
  - `rawCoach`, `rawDate` and `sig` are the three values exactly as they appear in the link.
  - **No signature check.**
- **S2. `RecordAttendance` uses the link, not `VerifyDailyCode`.** The checks run in this order, one result each:
  1. `NoSessionChosen`
  2. `SessionUnknown`
  3. `ClassCancelled`
  4. `SessionCancelled`
  5. `NotOnRoster`
  6. link unreadable → `InvalidCode`
  7. `link.coachId != class.coachId` → `WrongCoach`
  8. `link.date != today` → `WrongDate`
  9. chosen session's date `!= today` → **`NoSessionToday`** (new; before, this returned `WrongDate`)
  10. existing record → `AlreadyRecorded`
  11. otherwise the scan is recorded.

  The constructor stays the same, so existing tests compile. The `verifyDailyCode` parameter is no longer called and is marked `@Suppress("unused")`, with a comment pointing to flow 7 decision 1. Removing it is a later cleanup.
- **S3. `VerifyDailyCode` stays exactly as it is**, with its Phase 1 AC27–30 tests, as the reference for the server check after flow 8. Nothing in the app calls it any more. The coach side only signs (`GenerateDailyCode`), as today. The student path never reads `CoachKeyRepository`.
- **S4. Store the code on the record.**
  - `AttendanceRecord` gains `codeCoach: String = ""`, `codeDate: String = ""` and `codeSig: String = ""` (last, with defaults). `RecordAttendance` fills them with `rawCoach`, `rawDate` and `sig`.
  - `SqlAttendanceRepository` and `InMemoryAttendanceRepository` keep them. The SQL table gets three `TEXT NOT NULL DEFAULT ''` columns, `code_coach`, `code_date` and `code_sig`, added in the next schema version through the existing SQLDelight migration setup (`TrxDatabase.Schema` plus `TrxSchema`). Old rows read as empty strings.
  - `AttendanceUploader.upload(record)` already receives the whole record, so the outbox carries the three values. `OutboxSync` keeps them when it marks a row `SYNCED`.
- **S5.** `AttendanceResult` gains `NoSessionToday`, and `RejectReason` gains `NoSessionToday` (data objects). `AttendViewModel` maps one to the other. Nothing else in the result mapping changes.
- **S6.** `AttendViewModel.onCloseScanner()` sets `scannerOpen = false`, `sheetOpen = false`, `recordingFor = null`, `selectedSessionId = null` and `canScan = false`, and clears the chosen session. Stores nothing.
- **S7.** `AttendViewModel.refresh()` handles a new day. If `today` changed and no result is showing, it clears the selection, closes the scanner and opens the sheet with the new day's choices. A result already showing stays until `onDone()`.
- **S8.** `AttendViewModel` (`recordedToday`) and `StudentSessionsViewModel` reload whenever `attendance.changes` moves.
- **S9. Cancelled rows.** `StudentSessionStatus` gains `CANCELLED`. `StudentSessions` also returns CANCELLED sessions of classes that are not cancelled, with status `CANCELLED`, which also wins over a record. It does not return CANCELLED sessions of a cancelled class. Past SCHEDULED sessions of a cancelled class keep Present / Absent. `sessionNumber` is the session's own number.
- **S10. Row order and id.** `StudentSessionUiRow` gains `sessionId: String = ""` (last).
  - `upcoming`: date ascending, then start time ascending, with cancelled rows mixed in by the same order.
  - `today`: non-cancelled rows newest first (start time descending, as now), then cancelled rows (start time ascending) at the end.
  - `earlier`: unchanged (date descending, then start time descending), with cancelled rows mixed in by the same order.
- **S11. Upcoming length (provisional, O1).** `upcoming` keeps only rows dated up to 7 days after today.
- **S12.** Top-level `fun looksLikeAttendanceCode(text: String): Boolean` in `presentation/attend/ScanText.kt`. It is true when `text.trim()` starts with `trxatt://` (case-sensitive). The UI uses it before `onCodeScanned`. `onCodeScanned` itself still turns any other text into `InvalidCode`, so existing Phase 2 AC54 tests hold.
- **S13.** New `UiCopy` lines are in the copy table. Existing functions don't change.

**App / platform:**

- **A1. Screens.** `AttendScreen`, `ClassSheet` (C7, C8, C18), `ScannerScreen` (C9–C11) and `ScanResultScreen` (C12–C15) in `ui.attend`; `StudentSessionsScreen` in `ui.studentsessions`; one file each. Reuse `LargeTitleHeader`, `OfflineBanner`, `StatusChip`, `TrxCard` and the theme tokens. Delete `StudentTabPlaceholder` and its stale "flow 5" comment.
- **A2. Seams:** `QrScanner`, `CameraPermission` / `CameraAccess` and `ScanHaptics` (section 9), each with `expect fun remember…()` in `ui/platform` and Android, iOS and desktop `actual`s.
- **A3. Dependencies** (`composeApp` `androidMain`): `androidx.camera:camera-camera2`, `camera-lifecycle` and `camera-view`; `com.google.mlkit:barcode-scanning` (bundled, not `play-services-mlkit-barcode-scanning`); `androidx.activity:activity-compose` (already in the catalog). Builder picks current stable versions and lists them in the PR.
- **A4. Android manifest** (`androidApp/src/main/AndroidManifest.xml`): add `<uses-permission android:name="android.permission.CAMERA" />` and `<uses-feature android:name="android.hardware.camera.any" android:required="false" />`.
- **A5. iOS** (`iosApp/iosApp/Info.plist`): add `NSCameraUsageDescription` = "The camera is only used to read your coach’s QR code."
- **A6. Koin.**
  - Bind `AttendViewModel` and `StudentSessionsViewModel` in `ViewModelModule`, with `Clock.System` and `TimeZone.currentSystemDefault()`.
  - `RecordAttendance` is built from the bound repositories. Its unused `verifyDailyCode` argument gets `VerifyDailyCode(keys, clock, timeZone)` only to satisfy the constructor.
  - Factories: `rememberAttend(account, factory)` in `di/AttendFactory.kt` and `rememberStudentSessions(account, factory)` in `di/StudentSessionsFactory.kt`, keyed `"attend:${account.id}"` and `"student-sessions:${account.id}"`.
- **A7. Wiring.** `StudentHomeScreen` and `AppNavHost` gain last optional parameters: `attendViewModel`, `studentSessionsViewModel` (factories), and `qrScanner`, `cameraPermission`, `scanHaptics` (seams), all `= null`. A null factory resolves from Koin only when its tab first opens. A null seam uses the platform `remember…()`.
- **A8. One-shot scan.** `ScannerScreen` passes on only the first text that passes `looksLikeAttendanceCode`, then drops everything until it is opened again. The platform scanners stop analysing after that.
- **A9. Haptics.** When `result` changes to `Present` or `SavedOffline`, call `scanHaptics.success()` once.
- **A10. Never log the link.** No log or print call anywhere in the scan path takes the scanned text, the link or the record's `code*` values. That covers `ui.attend`, the scanner and permission `actual`s, `presentation.attend` and `RecordAttendance`.
- **A11. Key-save log line (decision 13).** Where `AndroidSecretKeyStore.putPrivateKey` and `IosSecretKeyStore.putPrivateKey` catch a failure, log one line: `Log.w("TrxKeys", "Could not save signing key for coach")` on Android, `NSLog("TrxKeys: Could not save signing key for coach")` on iOS. No key bytes, no coach id.

## 7. Acceptance criteria

**Example data (tests build it themselves).**
- **People:** coaches **Mara Santos** (`coach-mara`) and **Leo Ramirez** (`coach-leo`); student **Paolo Reyes** (`student-paolo`, EMP-20417).
- **Classes, all created on Mon 28 Sep 2026, Paolo ACTIVE on all four.** Session numbers start at 1 on Mon 28 Sep:

| Class | Coach | Time | Days | Sessions |
|---|---|---|---|---|
| Sunrise TRX Strength | Mara | 6:00 AM – 6:45 AM | Mon/Wed/Fri | Mon 28 = 1, Wed 30 = 2, Fri 2 Oct = 3, Mon 5 Oct = 4 |
| Lunch Express TRX | Mara | 12:15 PM – 12:45 PM | Mon/Tue/Thu | Mon 28 = 1, **cancelled**; Tue 29 = 2; Thu 1 Oct = 3; Mon 5 Oct = 4 |
| TRX Core Burn | Mara | 5:30 PM – 6:30 PM | Mon/Wed | Mon 28 = 1, Wed 30 = 2, Mon 5 Oct = 3 |
| TRX Mobility Flow | Leo | 7:00 PM – 8:00 PM | Mon/Thu | Mon 28 = 1, Thu 1 Oct = 2, Mon 5 Oct = 3 |

- **Default clock:** Mon 28 Sep 2026, 5:31 PM, Asia/Manila, online. Record: Paolo on Sunrise Mon 28 at 6:02 AM, `SYNCED`.
- **"Mara’s code"** = `GenerateDailyCode` for Mara on Mon 28 Sep, made with a key that exists only on the test's coach side. Paolo's store holds **no coach keys**. "Old code" = Mara's code for Mon 21 Sep. "Leo’s code" = Leo's code for Mon 28 Sep.

**Reading the code and recording (shared)**

- **F7-AC1.** `parseDailyCodeLink`:
  - For Mara's code it returns `coachId = "coach-mara"`, `date = 2026-09-28`, and `rawCoach`, `rawDate` and `sig` equal to the link's three values.
  - It returns null for:
    - text without the `trxatt://v1/daily` scheme;
    - a link missing `coach`, `date` or `sig`;
    - `date=2026-13-45`;
    - `coach=%ZZ`;
    - an empty `sig`;
    - `sig=!!`.
  - Mara's code with its last signature character changed still parses (no signature check).
- **F7-AC2.** `RecordAttendance` for Paolo:
  - Core Burn + Mara's code → `Recorded`, with no coach keys on the phone.
  - Core Burn + Mara's code with a changed signature character → `Recorded` (the server checks later).
  - Mobility Flow + Mara's code → `WrongCoach`.
  - Core Burn + Leo's code → `WrongCoach`.
  - Core Burn + old code → `WrongDate`.
  - Core Burn + `"trxatt://v1/daily?coach=coach-mara&date=2026-09-28"` → `InvalidCode`.
  - Paolo removed from Core Burn → `NotOnRoster`, even with a broken code (the roster check comes first).
  - Only `Recorded` writes a record.
- **F7-AC3.** `RecordAttendance` built with a `VerifyDailyCode` whose `CoachKeyRepository` throws on every call still records Core Burn + Mara's code. Neither the record path nor the ViewModel path touches the key repository.
- **F7-AC4.** Choose Core Burn for Mon 28. Move the clock to Tue 29 Sep 9:00 AM without `refresh()`, then record with Mara's Tue 29 code → `NoSessionToday` (`Rejected(NoSessionToday)` in the ViewModel). Nothing is stored.
- **F7-AC5.** The Core Burn record from F7-AC2 has `codeCoach = "coach-mara"`, `codeDate = "2026-09-28"` and `codeSig` equal to the link's `sig`. A copy saved and read back through `SqlAttendanceRepository` and through `InMemoryAttendanceRepository` keeps all three.
- **F7-AC6.** Online, after the scan, `FakeAttendanceUploader.uploaded` holds one record with those same three values. The stored row is `SYNCED` and still has them.
- **F7-AC7.** A database created at the previous schema version with one attendance row opens with `TrxSchema` and upgrades to the new version. The old row reads `codeCoach = codeDate = codeSig = ""`, and a new scan saves its values.

**ViewModels (shared)**

- **F7-AC8.** `onCloseScanner()` after `onScanCoachCode()`: `scannerOpen = false`, `sheetOpen = false`, no selection, `canScan = false`, `recordingFor = null`, no record.
- **F7-AC9.** Scanner open for Core Burn; the clock moves to Tue 29 Sep 9:00 AM; `refresh()`. Then `today` = Tue 29, the scanner is closed, the sheet is open, there is no selection, and `choices` = Lunch Express Session 2 only. With a `Present` result showing, the same steps leave the result until `onDone()`.
- **F7-AC10.** Adding a PENDING Core Burn record straight to the store adds a `recordedToday` row (`uploadPending = true`). An already-created `StudentSessionsViewModel` shows that row as `PRESENT` with `uploadPending = true`, without `refresh()`.
- **F7-AC11.** `StudentSessions` includes Lunch Express Mon 28 with status `CANCELLED` and session number 1; Lunch Express Tue 29 is `UPCOMING` with number 2. After the clock moves to Tue 29, Mon 28 Lunch Express is still `CANCELLED`, never `ABSENT`. After `CancelClass` on Core Burn, no CANCELLED Core Burn session is listed, and the past Mon 28 Core Burn session keeps `ABSENT` or `PRESENT`. A CANCELLED session with a record is `CANCELLED`.
- **F7-AC12.** `StudentSessionsViewModel` on the default clock:
  - `today` = Mobility Flow, Core Burn, Sunrise (non-cancelled, start time descending), then Lunch Express (CANCELLED) last.
  - Every row has `sessionId` set.
  - (Provisional, O1) `upcoming` = Tue 29 Lunch 2; Wed 30 Sunrise 2, Core Burn 2; Thu 1 Oct Lunch 3, Mobility 2; Fri 2 Oct Sunrise 3; Mon 5 Oct Sunrise 4, Lunch 4, Core Burn 3, Mobility 3. That is date ascending, then start time ascending, and nothing after Mon 5 Oct.
  - Cancelling Lunch Express Thu 1 Oct moves nothing: that row stays in place with `CANCELLED`.
- **F7-AC13.** `looksLikeAttendanceCode` is true for Mara's code, `"  trxatt://v1/daily?x"` and any `trxatt://` text. It is false for `"https://example.com"`, `""` and `"TRXATT://v1/daily"`.
- **F7-AC14.** `UiCopy` returns exactly the copy-table strings. Existing strings are unchanged.

**Attend tab (C6–C8, C18)**

- **F7-AC15.** Signed in as Paolo, selecting Attend shows the real screen under `StudentTabBarTestTags.attendRoot`:
  - title "Attend", `attend_overline` "TRX ATTENDANCE", `attend_subtitle` "Mon 28 Sep 2026";
  - `attend_card_title` "Record attendance", the card body, and `attend_choose_class` "Choose class";
  - no `placeholder_tag`, no profile icon, no spinner.
- **F7-AC16.** Selecting Attend opens `attend_sheet` with no tap. `attend_sheet_close` closes it. `attend_choose_class` reopens it. Closing writes no record.
- **F7-AC17.** The sheet shows:
  - "Which class are you attending?" and "Today · Mon 28 Sep 2026 · choose one";
  - `attend_choice_{sessionId}` for Sunrise, Core Burn and Mobility Flow in start-time order, and no Lunch Express (cancelled today);
  - row lines like "5:30 PM – 6:30 PM · Coach Mara Santos", with Sunrise still listed;
  - `attend_sheet_scan` "Scan coach’s code", disabled until a row is tapped. Then the tapped row has `selected = true` and the others `false`;
  - the footnote.
- **F7-AC18.** On Sat 3 Oct 9:00 AM, `attend_sheet_empty` shows "No classes today." and "Your scheduled classes for today show up here.". There are no rows, and Scan is disabled.
- **F7-AC19.** `attend_recorded_header` "Recorded today" and `attend_recorded_row_0` show "Sunrise TRX Strength", "Session 1 · scanned 6:02 AM" and "Present", with no "Upload pending". A PENDING Core Burn record adds `attend_recorded_row_1` with "Upload pending". With no records today, neither shows.
- **F7-AC20.** Offline: `offline_banner` "No internet connection", with no subtitle, under the title. Back online, it hides.
- **F7-AC21.** C18:
  - With fake camera access `DENIED_FOR_GOOD` and Core Burn selected, the sheet shows `camera_denied` with a warning icon, "Camera access is off" and "Allow camera access in Settings to scan your coach’s code.", plus `camera_open_settings` "Open Settings".
  - There is no `attend_sheet_scan` and no footnote. Tapping Open Settings calls `openSettings()` once.
  - With no row selected, the Scan button shows (disabled), as in C7.

**Scanner and permission (C9–C11)**

- **F7-AC22.** With access `GRANTED`, choose Core Burn and tap Scan:
  - `scanner` shows and the tab bar is hidden;
  - `scanner_title` reads "Scan coach’s code", `scanner_recording_label` "RECORDING FOR", `scanner_recording_for` "TRX Core Burn · 5:30 PM";
  - `scanner_change` reads "Change" and returns to the sheet with Core Burn selected;
  - `scanner_hint` reads "Point at the QR code on your coach’s phone" and `scanner_hint_detail` "It scans automatically.";
  - `fake_qr_preview` shows.
- **F7-AC23.** Offline, the scanner shows `offline_banner` with "No internet connection" and "Scans are saved on this phone and upload when you are back online.", and `scanner_hint_detail` reads "No connection needed. The scan is kept on this phone."
- **F7-AC24.** `scanner_torch` exists only when the fake has `hasTorch = true`. It toggles the fake's `torchOn` on and off, and it is absent on C9.
- **F7-AC25.** `scanner_close`, and a system back press, return to the Attend home with the sheet closed, no record, and the tab bar back.
- **F7-AC26.** The fake emits `"https://example.com"`: the scanner stays, with no result and no record. It then emits Mara's code: C12.
- **F7-AC27.** The fake emits Mara's code twice in one frame: one record, one C12, and `success()` called once.
- **F7-AC28.** Access `NOT_ASKED`:
  - Scan shows `camera_permission` with "Allow camera to scan", the body, and `camera_allow` "Allow camera". There is no `camera_open_settings`, no preview and no torch.
  - `camera_allow` calls `request()` once.
  - Answer `GRANTED` → the scanner with preview.
  - Answer `DENIED_FOR_GOOD` → the scanner closes, and the sheet shows C18 with Core Burn still selected.
- **F7-AC29.** Answer `DENIED_CAN_ASK` → `camera_permission` stays, with both `camera_allow` and `camera_open_settings` "Open Settings". Open Settings calls `openSettings()` once.
- **F7-AC30.** On C18, set the fake to `GRANTED` and bring the app to the foreground (`HostLifecycle`): the callout goes away, and `attend_sheet_scan` shows and is enabled.
- **F7-AC31.** Android mapping `cameraAccessOf(granted, showRationale, askedBefore)` (`androidMain`, tested in `androidHostTest`):

| granted | asked before | rationale | result |
|---|---|---|---|
| yes | any | any | `GRANTED` |
| no | no | — | `NOT_ASKED` |
| no | yes | yes | `DENIED_CAN_ASK` |
| no | yes | no | `DENIED_FOR_GOOD` |

  The iOS mapping is manual check P2.

**Results (C12–C15)**

- **F7-AC32.** Choose Core Burn and scan Mara's code online:
  - `result_present` shows "You’re marked Present", "Your coach will see you on this session.", "Present", and rows Class "TRX Core Burn", Session "Session 1 · Mon 28 Sep 2026", Time "5:30 PM – 6:30 PM", Scanned "5:31 PM";
  - `success()` is called once;
  - `result_done` goes home with Core Burn in Recorded today;
  - `result_record_another` "Record another class" opens the sheet with no selection.
- **F7-AC33.** Same, offline:
  - `result_saved_offline` shows the banner (title only), "Saved on this phone", "Your attendance is stored now and uploads automatically when you’re back online. The recorded time is the scan time.", the "Present" and "Upload pending" chips, and rows Class, Session, Scanned "5:31 PM (recorded time)", Upload "Waiting for connection", plus one `result_done`;
  - `success()` is called once, and the record is PENDING;
  - a real ONLINE reading makes `RootViewModel` run `OutboxSync`. The row becomes `SYNCED`, and the Sessions row loses "Upload pending" without leaving the tab.
- **F7-AC34.** Choose Sunrise (already recorded) and scan Mara's code:
  - `result_already` shows "You already have attendance", "Sunrise TRX Strength on Mon 28 Sep 2026 is already recorded. No new record was added.", "Present", and rows Class, Session "Session 1 · Mon 28 Sep 2026", First scan "6:02 AM";
  - `result_done` "Done" and `result_choose_different` "Choose a different class" show;
  - there is still one Sunrise record, and no haptic.
- **F7-AC35.** Rejections all go to `result_rejected`:
  - **Every case:** title "Scan not accepted"; `result_message` = the line below plus " Nothing was saved."; `result_you_chose` "{class} · {start}"; `result_today` "Mon 28 Sep 2026" (Tue 29 Sep in case 8); no new record; no haptic.
  - `result_code_date` shows only in case 1.
  - `result_scan_again` "Scan again" reopens the scanner with the same class. `result_close` "Close" goes home.

| # | Setup | Line |
|---|---|---|
| 1 | Core Burn + old code | "This code is for a different date." (Code date "Mon 21 Sep 2026") |
| 2 | Mobility Flow + Mara's code | "This code isn’t from your coach for this class." |
| 3 | Core Burn + `trxatt://v1/daily?coach=coach-mara&date=2026-09-28` (no `sig`) | "This isn’t a valid attendance code." |
| 4 | Core Burn, Paolo removed from the roster | "You’re not on this class roster." |
| 5 | Core Burn, Mon 28 cancelled while the sheet is open | "This date is cancelled." |
| 6 | Core Burn, `CancelClass` on Core Burn | "This class has been cancelled." |
| 7 | Offline, chosen session missing from the store | "This class date isn’t on this phone yet." |
| 8 | Scanner open for Core Burn across midnight (no refresh), Mara's Tue 29 code | "This class has no session today." |

**Sessions tab (C5, C5b)**

- **F7-AC36.** Selecting Sessions shows the real screen under `StudentTabBarTestTags.sessionsRoot`: title "Sessions", `sessions_overline` "TRX ATTENDANCE", `sessions_subtitle` "Your dates for joined classes". No `placeholder_tag`, no profile icon, no spinner.
- **F7-AC37.** On the default clock:
  - `sessions_group_upcoming` "Upcoming" lists the rows in the F7-AC12 order, each with "Upcoming" and, for example, "Session 2 · 12:15 PM" for Tue 29 Lunch Express.
  - `sessions_group_today` "Today · Mon 28 Sep" lists Mobility Flow ("Upcoming"), Core Burn ("Upcoming"), Sunrise ("Present"), then Lunch Express.
  - There is no `sessions_group_earlier`.
  - The date tiles read "MON" / "28" / "SEP".
- **F7-AC38.** In Today, the Lunch Express row (`sessions_row_{sessionId}`) is last:
  - it shows "Session 1 · 12:15 PM" and a "Cancelled" chip, and carries `sessions_row_cancelled = true`;
  - it has no "Absent" chip, on Mon 28 or after the clock moves to Tue 29 (when it sits in Earlier, still Cancelled);
  - the Tue 29 Lunch Express row in Upcoming shows "Session 2 · 12:15 PM".
- **F7-AC39.** On every today row, the `sessions_today_tag` "TODAY" is entirely below the class name's bounds and does not overlap the status chip's bounds. Check this at 360dp and 412dp wide, with the long name "Sunrise TRX Strength".
- **F7-AC40.** A PENDING Core Burn record shows "Present" and "Upload pending"; once `SYNCED`, the Upload pending chip is gone. After the clock moves to Tue 29, unscanned Mon 28 rows show "Absent" in Earlier.
- **F7-AC41.** A student on no class sees `sessions_empty`: "No sessions yet" / "When you join a class, its dates show up here.", and no groups.
- **F7-AC42.** Offline, `offline_banner` shows under the title, and rows still show.
- **F7-AC43.** Rows have no click action. Recording on Attend and then selecting Sessions shows that row as Present.

**Wiring, platform files, structure**

- **F7-AC44.** `AttendViewModel` and `StudentSessionsViewModel` are bound in `ViewModelModule`. A signed-in student opens both tabs in the production graph without custom factories (Koin graph test and source scan, as F6-AC16). UI tests pass factories and fakes and never start Koin.
- **F7-AC45.** Source scans:
  - the manifest has `CAMERA`, and `android.hardware.camera.any` with `required="false"`;
  - `Info.plist` has `NSCameraUsageDescription` with the exact string;
  - `composeApp` `androidMain` uses `com.google.mlkit:barcode-scanning` and not `play-services-mlkit-barcode-scanning`;
  - nothing under `presentation/attend`, `ui/attend` or `RecordAttendance.kt` references `CoachKeyRepository` or calls `VerifyDailyCode`.
- **F7-AC46.** Logging, by source scan:
  - no `println(`, `print(`, `Log.` or `NSLog(` call in `ui/attend`, `presentation/attend`, `RecordAttendance.kt` or the scanner / permission `actual` files;
  - `AndroidSecretKeyStore.kt` contains `Log.w("TrxKeys"` and `IosSecretKeyStore.kt` contains `NSLog("TrxKeys`, with no key or coach-id argument.
- **F7-AC47.** Structure and regression.
  - **Source scans still pass:** one public type per file, no nested classes, Koin only in `composeApp`, no Firebase, `PlaceholderTag` on any placeholder. Add `F7StructureTest`. `StudentTabPlaceholder` is gone.
  - **All suites pass:** `:shared:jvmTest`, `:shared:phase2JvmTest`, `:composeApp:jvmTest`, `:composeApp:structureJvmTest`, `androidHostTest`. `assembleDebug` builds, and both iOS targets compile.
  - **Allowed test edits only:**
    - assertions that Attend or Sessions is a placeholder;
    - Phase 1 / Phase 2 attendance tests that expect a rejection for a bad or unknown-key signature (now `Recorded`), or `WrongDate` when the chosen session isn't today (now `NoSessionToday`);
    - Phase 1 AC37 and Phase 2 AC58 tests that expect cancelled sessions to be left out;
    - Phase 2 AC59 tests of the `upcoming` and `today` order;
    - exhaustive `when` blocks over `AttendanceResult`, `RejectReason` or `StudentSessionStatus`, which may add the new value;
    - whole-object comparisons of `AttendanceRecord` or `StudentSessionUiRow`, which may add the new fields.
  - Phase 1 AC27–30 `VerifyDailyCode` tests are **not** edited.

## 8. Test plan

| What | Suite | ACs |
|---|---|---|
| Link reading, record checks, no key use, stored code values, outbox, migration, cancelled rows | `:shared:jvmTest` (new) | F7-AC1–7, F7-AC11 |
| ViewModels: close scanner, new day, live lists, row order, code filter, copy | `:shared:phase2JvmTest` (new) | F7-AC8–10, F7-AC12–14 |
| Existing domain, ViewModel and crypto tests | both shared suites | regression, with the allowed edits |
| Attend, sheet, C18, scanner, permission, results, Sessions (`v2.runComposeUiTest`, `FakeQrScanner`, `FakeCameraPermission`, `FakeScanHaptics`, `MutableNetworkMonitor`, `HostLifecycle`) | `:composeApp:jvmTest` | F7-AC15–30, F7-AC32–43 |
| Android permission mapping | `androidHostTest` | F7-AC31 |
| Koin and source scans | `:composeApp:structureJvmTest` | F7-AC44–47 |
| Real camera, permission dialogs, haptics, torch, two phones, look | manual | P1–P7 |

**Fakes** (test sources, one per file, `composeApp/src/commonTest/.../ui/testing`):
- `FakeQrScanner(hasTorch = false)`: `emit(text)`, a readable `torchOn`, and a preview box tagged `fake_qr_preview`.
- `FakeCameraPermission(initial)`: `access`, `nextAnswer`, `requestCount`, `openSettingsCount`.
- `FakeScanHaptics`: `successCount`.

**Manual device checks**

- **P1. Android camera permission.**
  1. On a fresh debug install, sign in as Paolo, choose a class and tap Scan. C9 shows with only Allow camera, and the system dialog appears only after tapping it.
  2. Deny. C9 now shows Allow camera and Open Settings.
  3. Deny again with "Don’t ask again" (or a second time, depending on the Android version). The scanner closes and the sheet shows C18.
  4. Tap Open Settings, allow Camera, and come back. The Scan button is back.
- **P2. iOS camera permission.** Same as P1 on an iPhone. After the first no, the sheet goes straight to C18. Open Settings opens this app's Settings page. The system dialog shows the A5 text. **Needs O2:** without a seeded student on iOS there is no class to choose.
- **P3. Two-phone scan (the main check).** Sheen's two Android phones, both on the same debug build, each freshly installed or cleared (there is no sign-out). Both use only the `DemoSeed` data. Do not create anything new on either phone. Run Monday to Friday.
  1. Coach phone: sign in as **mara@company.com / password1** and open the QR tab.
  2. Student phone: sign in as **paolo@company.com / password1**. Paolo is seeded ACTIVE on three of Mara's classes: **Sunrise TRX Strength** (Mon/Wed/Fri 6:00 AM), **Lunch Express TRX** (Mon/Tue/Thu 12:15 PM) and **TRX Core Burn** (Mon/Wed 5:30 PM).
  3. Student phone: Attend → choose one of those classes → Scan → point at the coach phone's QR.
  - **What this proves:** the student phone reads the coach phone's QR, accepts it (format, today's date, Mara is the chosen class's coach; no key needed), records attendance on the student phone, shows C12 "You’re marked Present" with one vibration, lists it under "Recorded today", and shows it as Present in Sessions. Scanning the same class again shows C14 "You already have attendance".
  - **What it does not show, on purpose:** the coach phone's Attendance tab will **not** show this scan. Each phone keeps its own fake data until flow 8 brings a real uploader. This is expected, not a bug.
- **P4. Expected rejections on two phones.**
  - A class created on the coach phone never appears on the student phone (separate fake data), so Paolo can't choose it. Expected.
  - If the coach phone is signed in as a coach registered on that phone (not Mara), and Paolo chose a Mara class, the student phone shows "This code isn’t from your coach for this class." Expected: the code names a different coach.
  - On a Tuesday or Thursday (the seeded Mobility Flow days), accept Leo's **TRX Mobility Flow** invite on the student phone, choose it, and scan Mara's QR: "This code isn’t from your coach for this class."
- **P5. Offline.** With the student phone in airplane mode, the scanner shows the C11 banner, a scan gives C13, and Sessions shows Upload pending. Network back on: Upload pending goes away.
- **P6. Feel and controls.**
  - One vibration on C12 and C13, and none on C14 or C15.
  - The torch toggles on a phone with one and is hidden otherwise.
  - Holding the QR in view after a result never gives a second result.
  - A random QR (any web link) is ignored.
- **P7. Look and screen readers.**
  - C5, C5b and C6–C15 plus C18 match Figma in light mode, look right in dark mode, and work at 360dp and 412dp.
  - The TODAY tag sits on its own line under the class name.
  - TalkBack and VoiceOver announce the selected class row as selected and read every button.

**Test tags** (snake_case; `AttendTestTags` in `ui.attend`, `StudentSessionsTestTags` in `ui.studentsessions`, one object per file):
- **Existing, still used:** `StudentTabBarTestTags.attendRoot`, `attendTitle`, `sessionsRoot`, `sessionsTitle`, the tab tags, `offline_banner`, `placeholder_tag` (now absent from both tabs).
- **New, Attend:** `attend_overline`, `attend_subtitle`, `attend_card_title`, `attend_card_body`, `attend_choose_class`, `attend_recorded_header`, `attend_recorded_row_{index}`, `attend_sheet`, `attend_sheet_close`, `attend_choice_{sessionId}`, `attend_sheet_scan`, `attend_sheet_empty`, `camera_denied`, `scanner`, `scanner_close`, `scanner_title`, `scanner_torch`, `scanner_recording_label`, `scanner_recording_for`, `scanner_change`, `scanner_hint`, `scanner_hint_detail`, `camera_permission`, `camera_allow`, `camera_open_settings`, `result_present`, `result_saved_offline`, `result_already`, `result_rejected`, `result_message`, `result_you_chose`, `result_today`, `result_code_date`, `result_done`, `result_record_another`, `result_choose_different`, `result_scan_again`, `result_close`.
- **New, Sessions:** `sessions_overline`, `sessions_subtitle`, `sessions_group_upcoming`, `sessions_group_today`, `sessions_group_earlier`, `sessions_row_{sessionId}`, `sessions_today_tag`, `sessions_empty`, plus the semantics key `sessions_row_cancelled` (its own file).

## 9. Pinned names and seams

- **No key lookup on the student side.** Builder's proposed `CoachKeyDirectory` is not needed: the phone no longer checks signatures. `CoachKeyRepository` stays coach-side only (the QR tab creates and uses Mara's key).
- `DailyCodeLink` (`domain/model/DailyCodeLink.kt`); `parseDailyCodeLink` (`domain/crypto/DailyCodePayload.kt`, internal).
- `AttendanceResult.NoSessionToday`; `RejectReason.NoSessionToday`; `StudentSessionStatus.CANCELLED`; `AttendanceRecord(…, codeCoach = "", codeDate = "", codeSig = "")`; `StudentSessionUiRow(…, sessionId = "")`.
- `RecordAttendance` and `VerifyDailyCode` keep their constructors. `VerifyDailyCode` is unchanged and unused by the app.
- `AttendViewModel.onCloseScanner()`; `refresh()` handles a new day.
- `looksLikeAttendanceCode(text)` in `presentation/attend/ScanText.kt`.
- **`QrScanner`** (`ui/platform/QrScanner.kt`):
  ```kotlin
  interface QrScanner {
      val hasTorch: Boolean
      @Composable fun Preview(torchOn: Boolean, onText: (String) -> Unit, modifier: Modifier)
  }
  ```
  `@Composable expect fun rememberQrScanner(): QrScanner`. The implementations:
  - `CameraXQrScanner` (Android): `PreviewView` in `AndroidView`, `ImageAnalysis` with ML Kit `BarcodeScanning` set to `FORMAT_QR_CODE`, torch via `CameraControl.enableTorch`.
  - `AvFoundationQrScanner` (iOS): `AVCaptureSession` with `AVCaptureMetadataOutput` for QR, `AVCaptureVideoPreviewLayer` in `UIKitView`, torch via `AVCaptureDevice.torchMode`.
  - `NoQrScanner` (desktop): no torch, an empty dark box.
  
  Each stops when the app goes to the background.
- **`CameraAccess`** (`ui/platform/CameraAccess.kt`): `enum class CameraAccess { NOT_ASKED, GRANTED, DENIED_CAN_ASK, DENIED_FOR_GOOD }`.
- **`CameraPermission`** (`ui/platform/CameraPermission.kt`): `interface CameraPermission { fun current(): CameraAccess; fun request(onResult: (CameraAccess) -> Unit); fun openSettings() }` with `@Composable expect fun rememberCameraPermission()`. The implementations:
  - Android: `rememberLauncherForActivityResult(RequestPermission())`, `shouldShowRequestPermissionRationale` and an "asked before" flag in app-private preferences, mapped by `cameraAccessOf`. Settings opens through `ACTION_APPLICATION_DETAILS_SETTINGS`.
  - iOS: `AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)` maps not determined → `NOT_ASKED`, authorized → `GRANTED`, denied / restricted → `DENIED_FOR_GOOD`. It asks with `requestAccessForMediaType` and opens Settings with `UIApplicationOpenSettingsURLString`.
  - Desktop: always `GRANTED`.
  
  The sheet and scanner read `current()` when shown and on every `ON_START`.
- **`ScanHaptics`** (`ui/platform/ScanHaptics.kt`): `interface ScanHaptics { fun success() }`, `@Composable expect fun rememberScanHaptics()`. Android uses `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.Confirm)`. iOS uses `UINotificationFeedbackGenerator().notificationOccurred(UINotificationFeedbackTypeSuccess)`. Desktop does nothing.
- **Screens:** `AttendScreen(state, scanner, permission, haptics, actions…, tabBar, modifier = Modifier)` and `StudentSessionsScreen(state, tabBar, modifier = Modifier)`. Both are stateless apart from the torch and the permission status.
- **Factories:** `rememberAttend` and `rememberStudentSessions` (A6).

## 10. Copy table (exact strings; typographic apostrophe U+2019, en dash U+2013, middle dot U+00B7)

**Existing, unchanged and used here:**
- Chrome and status words: `largeTitleEyebrow()` "TRX ATTENDANCE"; `tabAttend()` "Attend"; `tabSessions()` "Sessions"; `upcoming()`, `present()`, `absent()`, `cancelled()`, `uploadPending()`; `today()` "TODAY".
- Offline: `offlineBanner()`; `scannerOfflineSubtitle()`.
- Result titles: `scanPresentTitle()` "You’re marked Present"; `scanSavedOfflineTitle()` "Saved on this phone"; `scanAlreadyRecordedTitle()` "You already have attendance"; `scanAlreadyRecordedBody(class, date)`; `scanRejectedTitle()` "Scan not accepted"; `scanRejectedTail()` " Nothing was saved.".
- Rejection lines: `wrongDate()`; `wrongCoach()`; `notOnRoster()`; `sessionCancelled()`; `classCancelledBody()`.
- Formatters: `formatDate`, `formatShortDate`, `formatTime`, `formatSlot`.

**C15 body lines, one per reason** (`rejectLine(reason)` + `scanRejectedTail()`):

| Reason | Line |
|---|---|
| `InvalidCode` (link unreadable: wrong scheme, missing or unreadable `coach`, `date` or `sig`) | "This isn’t a valid attendance code." (new `invalidCode()`) |
| `WrongCoach` | "This code isn’t from your coach for this class." (`wrongCoach()`) |
| `WrongDate` | "This code is for a different date." (`wrongDate()`) |
| `NoSessionToday` | "This class has no session today." (new `noSessionToday()`) |
| `NotOnRoster` | "You’re not on this class roster." (`notOnRoster()`) |
| `SessionCancelled` | "This date is cancelled." (`sessionCancelled()`) |
| `ClassCancelled` | "This class has been cancelled." (`classCancelledBody()`) |
| `SessionUnknown` | "This class date isn’t on this phone yet." (new `sessionNotOnPhone()`) |

**New `UiCopy` functions:**

| Function | String |
|---|---|
| `recordAttendanceTitle()` | "Record attendance" |
| `recordAttendanceBody()` | "First choose the class you’re attending. Then scan the QR code on your coach’s phone." |
| `chooseClass()` | "Choose class" |
| `recordedToday()` | "Recorded today" |
| `recordedTodayLine(sessionNumber, time)` | "Session {n} · scanned {formatTime}" |
| `chooseClassTitle()` | "Which class are you attending?" |
| `chooseClassSubtitle(date)` | "Today · {formatDate} · choose one" |
| `choiceLine(slot, coachName)` | "{formatSlot} · Coach {coachName}" |
| `scanCoachCode()` | "Scan coach’s code" |
| `choiceFootnote()` | "Your choice is saved with the scan. The time of the scan doesn’t change it." |
| `noClassesToday()` / `noClassesTodayBody()` | "No classes today." / "Your scheduled classes for today show up here." |
| `cameraOffTitle()` (C18) | "Camera access is off" |
| `cameraOffBody()` (C18) | "Allow camera access in Settings to scan your coach’s code." |
| `openSettings()` | "Open Settings" |
| `recordingFor()` | "RECORDING FOR" |
| `recordingForLine(className, start)` | "{className} · {formatTime}" |
| `change()` | "Change" |
| `scannerHint()` / `scannerHintDetail()` / `scannerHintOffline()` | "Point at the QR code on your coach’s phone" / "It scans automatically." / "No connection needed. The scan is kept on this phone." |
| `allowCameraTitle()` / `allowCameraBody()` / `allowCamera()` (C9) | "Allow camera to scan" / "The camera is only used to read your coach’s QR code. Nothing is recorded until a valid code is scanned." / "Allow camera" |
| `cameraUsageDescription()` (also the Info.plist value) | "The camera is only used to read your coach’s QR code." |
| `scanPresentBody()` | "Your coach will see you on this session." |
| `scanSavedOfflineBody()` | "Your attendance is stored now and uploads automatically when you’re back online. The recorded time is the scan time." |
| `resultClass()` / `resultSession()` / `resultTime()` / `resultScanned()` / `resultUpload()` / `resultFirstScan()` | "Class" / "Session" / "Time" / "Scanned" / "Upload" / "First scan" |
| `resultSessionLine(sessionNumber, date)` | "Session {n} · {formatDate}" |
| `recordedTimeLine(time)` | "{formatTime} (recorded time)" |
| `waitingForConnection()` | "Waiting for connection" |
| `done()` / `recordAnotherClass()` / `chooseDifferentClass()` | "Done" / "Record another class" / "Choose a different class" |
| `resultYouChose()` / `resultToday()` / `resultCodeDate()` | "You chose" / "Today" / "Code date" |
| `scanAgain()` / `close()` | "Scan again" / "Close" |
| `invalidCode()` / `noSessionToday()` / `sessionNotOnPhone()` | see the C15 table |
| `rejectLine(reason)` | the C15 table |
| `sessionsSubtitle()` | "Your dates for joined classes" |
| `sessionsTodayHeader(date)` | "Today · {formatShortDate}" (e.g. "Today · Mon 28 Sep") |
| `earlier()` | "Earlier" |
| `sessionRowLine(sessionNumber, start)` | "Session {n} · {formatTime}" |
| `dateTileWeekday(date)` / `dateTileMonth(date)` | upper case, e.g. "MON" / "SEP" |
| `noSessionsYetTitle()` / `noSessionsYetBody()` | "No sessions yet" / "When you join a class, its dates show up here." |

If a function with the same string already exists, reuse it.

## 11. Assumptions

1. **Cancelled wins over a record** on Sessions. This matches the coach view, where a cancelled session shows no students.
2. **A past cancelled date** stays in Earlier as Cancelled, never Absent.
3. **The C9 grey hint line** is a design note, not app copy.
4. **The sheet opens on every visit** to the Attend tab (C6 note), but not while a result is showing.
5. **Change from C9** goes back to the sheet, the same as from C10.
6. **Production uses** `Clock.System` and `TimeZone.currentSystemDefault()`. Tests inject a pinned clock and Asia/Manila.
7. **Codes are trusted until flow 8.** Anyone who knows the link format could build a code the phone accepts. Sheen accepted this; the stored `sig` lets the server reject such records later.
8. **The two-phone check is Android to Android** (Sheen's devices). The seed runs only in the Android debug app.

## 12. Edits to earlier ACs (apply in this PR's first commit, in `SPEC-phase1.md` and `SPEC-phase2.md` on the branch)

- **Phase 1 AC32.**
  - Before: "…a daily code that verifies for that session's coach and date…".
  - After: "…a daily code whose link is well formed, whose coach is that session's coach, and whose date is today. The phone does not check the signature; it stores the code's `coach`, `date` and `sig` on the record (`codeCoach`, `codeDate`, `codeSig`) for the server to verify after flow 8."
- **Phase 1 AC35.**
  - After: "Store nothing and return a typed error when:
    - membership is not `ACTIVE`;
    - the code's link is not well formed (`InvalidCode`);
    - it is for another coach (`WrongCoach`) or another date (`WrongDate`);
    - the chosen session is not today (`NoSessionToday`);
    - no session was chosen;
    - the session is `CANCELLED`;
    - the class was cancelled.
    
    Signature verification moves to the server later; the phone never rejects for the signature."
  - `VerifyDailyCode` and AC27–30 stay as they are, as the reference for that server check.
- **Phase 1 AC37.** After: "… A `CANCELLED` session of a class that is not cancelled is listed with status `CANCELLED`, keeping its `sessionNumber` (never `ABSENT`; `CANCELLED` also wins over a record). `CANCELLED` sessions of a cancelled class are left out."
- **Phase 2 AC54.** Add `NoSessionToday` to the reasons.
- **Phase 2 AC58.** After: "… `status` (`UPCOMING`/`PRESENT`/`ABSENT`/`CANCELLED`), `uploadPending` and `sessionId`. Cancelled sessions follow Phase 1 AC37 as edited in flow 7; only `ACTIVE` classes appear."
- **Phase 2 AC59** (subject to the section 13 note). After: "`upcoming` (date after today) is in date order, then start-time order, soonest first. `today` lists non-cancelled rows by start time descending, then cancelled rows. `earlier` (date before today) is date descending, then start time descending." If O1 is accepted, add: "`upcoming` holds only dates up to 7 days after today."

## 13. Open questions for Sheen

- **O1. How far ahead does Upcoming go?** The seed and Create class make dates up to 12 months ahead (`openEndedHorizon = DatePeriod(months = 12)`), so Paolo would have about 400 Upcoming rows. **Suggested:** the next 7 days (S11 and F7-AC12 are written that way, marked provisional). Alternatives: 14 days, or no limit.
- **O2. Demo data on iOS?** `DemoSeed` lives only in `androidApp/src/debug`. With no seed on iOS and no sign-out, an iOS student has no class to choose, so P2 can't run. **Suggested:** run the same seed on iOS debug builds only, by moving `DemoSeed` to a shared place that the iOS app calls when `Platform.isDebugBinary` is true. The code would then sit unused inside iOS release builds. Alternative: skip the iOS checks until later.
- **Needs Sheen's OK (not a question, a changed decision):** Designer's C5b rule sorts Upcoming **soonest first**. In flow 2 (Phase 2 AC59) Sheen chose "latest first in every group", and that was in the already-decided list he approved today. This spec follows Designer (S10, F7-AC12, the AC59 edit). If Sheen keeps latest first, only those three change.

## 14. Findings from the code

- **Camera entries:** not there today. The manifest has only `ACCESS_NETWORK_STATE`, and `Info.plist` has no `NSCameraUsageDescription`. A4 and A5 add them.
- **Haptics:** Compose Multiplatform **1.12.2**. `HapticFeedbackType.Confirm` is common API, and Compose Multiplatform has supported the newer haptic types on iOS since 1.8. The iOS `ScanHaptics` still calls `UINotificationFeedbackGenerator` directly, to get exactly the feel Designer asked for.
- **DemoSeed:** `androidApp/src/debug/kotlin/ph/project/active/DemoSeed.kt`, run by `DebugTrxApplication`. It is Android debug only.
  - Mara is `demo-mara`.
  - Paolo is `demo-paolo`: ACTIVE on Sunrise, Lunch Express and Core Burn, invited to Leo's Mobility Flow (Tue/Thu 7:00 PM), and pending on the Weekend Bootcamp.
  - It seeds no keys and needs none now.
- **Outbox on reconnect:** `RootViewModel` runs `OutboxSync.sync()` when a real reading turns ONLINE. A failed upload stays `PENDING` until the next ONLINE. `AttendViewModel` also syncs right after an online scan. The uploader is `FakeAttendanceUploader`, which always succeeds.
- **Session numbers:** `CreateClass` numbers every date 1..n in date, then start-time order, and `CancelDate` keeps the numbers (Phase 1 AC7, AC13). Cancelled sessions already stay in the count. Nothing to fix.
