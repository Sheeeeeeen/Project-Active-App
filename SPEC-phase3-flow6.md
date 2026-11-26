# TRX Class Attendance — Phase 3, Flow 6 Spec (coach Attendance tab)

## 1. Status
**Draft for Sheen’s approval — Thu 26 Nov 2026, Asia/Manila.** Sheen accepted all 8 default answers in `/workspace/flow6-questions.md`, and its section 2 fold-ins, on 26 Nov 2026. This spec turns those answers, Figma, and the code on `main` into a build plan. After approval, Tester writes the failing tests on a branch from `main`, with this spec as the first commit. Builder never pushes to `main` and never merges; Sheen merges.

- Baseline: `main` at `ba71d1a` (PR #14, flow 5, merged 26 Nov 2026). The QR tab is the real Daily QR screen. The Attendance tab is still `CoachTabPlaceholder` (D24).
- Design: Figma `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", section D `25:4904`: D21 `22:9992` (notes `22:10221`), D22 `22:10428` (notes `22:10610`). Flow 6 replaces the D24 placeholder `108:6478`. Screenshots re-checked 26 Nov 2026 (saved in `/workspace/flow6-figma/`).
- Naming: Figma labels D21 and D22 only as "Results". In our Phase 3 plan, flow 6 is the coach’s real Attendance tab.
- Package: `ph.project.active`. Code rules: one public type per file, no nested classes. Koin only in `composeApp`; tests never start Koin. One NavHost; tabs stay inside the coach root. New constructor parameters go last, with defaults. Any placeholder screen wears `PlaceholderTag`.

## 2. Decisions we made for you (veto any line)

**What’s in and what’s out**

1. Flow 6 builds the signed-in coach’s real **Attendance** tab (D21 a finished date, D22 today in progress). The behaviour mostly exists already in `CoachAttendanceViewModel` (Phase 2 AC79–81). This flow is the Compose screen, the Koin wiring, a few small shared additions (section header data, live updates, `refresh()`), and the leftovers in decisions 15–20.
2. Out of this flow: the student scanner (the student Attend tab stays a placeholder); publishing the coach’s public key to other phones; a real attendance uploader / Firebase; sign-out and account deletion; editing a class after it is saved; pull-to-refresh; loading spinners.

**The Attendance screen**

3. Follow Figma D21–D22. Light screen (it follows the app theme, unlike the always-dark QR tab). Eyebrow "TRX ATTENDANCE", title "Attendance". **No account icon and no +.** `PlaceholderTag` leaves this tab, so `coach_attendance_account` goes away.
4. Under the title: a date card with a left chevron, the date ("Mon 28 Sep 2026") and one line under it: "Today · in progress" for today, "Day ended" for any earlier date. The left chevron always goes back one day (no limit). The right chevron goes forward one day and is disabled on today; you can never page into the future.
5. Summary row: a green **Present** chip with the count next to it. When the day has ended, a red **Absent** chip with its count follows. While today is in progress there is no Absent chip; instead the footnote "· Absent is set after the day ends" follows the Present count.
6. Then one section per session, in start-time order. Heading: the class name. Line under it: "{start} – {end} · Session {n}". Under that, a card with one row per student on the class roster, in the order they joined (not A to Z).
   - **Present** row: initials avatar, name, employee ID, a check icon with "Chose {class} · scanned {time}", and a Present chip.
   - **Absent** row (day ended only): avatar, name, employee ID, and an Absent chip. No "Chose" line.
   - **Not scanned yet** row (today only): avatar, name, employee ID, and "Not scanned yet" as grey text on the right. No chip.
7. A **cancelled** session stays in its start-time place, on today as well as past dates. It shows as a grey card with the class name, "{start} – {end} · no attendance", and a Cancelled chip. No student rows.
8. A date with no sessions, or a coach with no classes: keep the date card and the summary row (counts 0; Absent chip only if the day has ended; footnote only on today) and show one line, "No sessions on this date." No other empty-state copy.
9. Offline: show the usual "No internet connection" banner under the title (title only, no subtitle) and keep showing whatever is in the phone’s store. No callout. The screen makes no writes.
10. **Live updates.** When an attendance record is added or changed in this phone’s store, the screen updates while it is open, with no tab switch. The screen also reloads each time the Attendance tab is shown and each time it comes back to the foreground. No polling timer and no pull-to-refresh. A scan from another phone only appears after that phone uploads, which needs a later real uploader.
11. **Overnight.** If the coach was looking at today and the date changes, the next reload (tab shown, or foreground) moves to the new today. If the coach had paged back to an earlier date, that date stays, and only its data reloads.

**Already locked, not reopened**

12. A student is Absent only after the day is over: the date is before today on the app clock and time zone (midnight Asia/Manila in tests). Until then they are "Not scanned yet" and the Absent count is 0 (Phase 1 AC39, Phase 2 AC81).
13. Each session (time slot) is its own section. A class with two slots that day shows two sections (Phase 2 AC79–80). Student order is roster join order (Phase 2 AC80).
14. Existing strings stay as they are: "Present", "Absent", "Cancelled", "Not scanned yet", "Day ended", "Today · in progress", "TRX ATTENDANCE", "Attendance", "No internet connection".

**Leftovers that ship in this flow**

15. **Keep the signing key across restarts.** Today `SecretKeyStore` is `InMemorySecretKeyStore`, so a new key and a new daily code appear after every restart. Store the coach’s private key with Android Keystore and the iOS Keychain instead, so the code for a date stays the same after a restart. Desktop (dev only) keeps the in-memory store.
16. **URL-encode the coach id** in `trxatt://v1/daily?coach=…`, and decode it when checking a code. Ids that are already plain letters, digits, `-`, `.`, `_`, `~` give exactly the same link as today.
17. **QR screen-reader label.** TalkBack / VoiceOver reads the QR image as "Today’s attendance code, valid {date} only", for example "Today’s attendance code, valid Mon 28 Sep 2026 only".
18. **iOS idle timer.** When the QR tab is left, set `idleTimerDisabled` back to what it was before the tab opened, not always `false`.
19. **Text selection in light mode.** Selection handles and highlight use `accentText` (`#7A5A00` in light), not yellow on white.
20. **Flow 4 leftover: student greeting read twice.** TalkBack reads the student home greeting ("Hi Paolo · EMP-20417") twice because the screen root also carries it as text. Expose it once.
21. Still later, not in this flow: sign-out must pop to `graph.id` inclusive (sign-out does not exist yet), and dropping `AppNavHost`’s `onNavController` (every UI test harness uses it to read the back stack, so removing it is a test refactor, not a small fix).

## 3. Scope

**In:** the real Attendance tab (D21–D22) bound to `CoachAttendanceViewModel`; section header data; live updates and `refresh()`; Koin wiring; new copy; decisions 15–20.

**Out:** the student scanner; key publishing; a real `AttendanceUploader`; sign-out; account deletion; edit-class; pull-to-refresh; spinners; `onNavController` removal; any change to `DemoSeed`.

## 4. User stories

- As a coach, I open the Attendance tab and see today: who has scanned into each session so far, and who has not scanned yet.
- As a coach, the next morning I go back one day and see that day’s final result: who was Present (and which class they chose, and when) and who was Absent.
- As a coach, I can page back through earlier dates, but not into the future.
- As a coach, I see cancelled sessions marked "no attendance" so I don’t wonder why nobody scanned.
- As a coach with the tab open, I see a new scan recorded on this phone appear without leaving the tab.
- As a coach who is offline, I still see what is already on my phone, with the usual offline banner.
- As a coach, my daily code stays the same after I close and reopen the app.

## 5. Screens and navigation

| Screen | Shows |
|---|---|
| D22 Attendance, today | Eyebrow "TRX ATTENDANCE"; title "Attendance"; no account icon, no +. Date card: ‹ "Mon 28 Sep 2026" / "Today · in progress" › (› disabled). Summary: Present chip + count, then "· Absent is set after the day ends". Sections in start-time order. Present rows with "Chose … · scanned …" and a Present chip; unscanned rows with "Not scanned yet". Cancelled sessions as grey "no attendance" cards. Tab bar with Attendance selected. No `PlaceholderTag`. |
| D21 Attendance, earlier date | Same, but the line under the date is "Day ended", › is enabled, the summary shows Present and Absent chips with counts and no footnote, and unscanned students show an Absent chip. |
| Empty date | Date card and summary (counts 0), then "No sessions on this date." |
| Offline | Same content, plus the "No internet connection" banner under the title. |

- The three tabs still live inside `CoachHome`. Switching tabs does not change the back stack. The starting tab is still Classes.
- `CoachAttendanceViewModel` is bound in `ViewModelModule` and kept per signed-in coach, like `rememberDailyQr`, so it survives tab switches. `refresh()` runs each time the Attendance tab is shown and when it returns to the foreground (same `LifecycleStartEffect` pattern as the other tabs).
- The Attendance tab no longer uses `CoachTabPlaceholder`. If nothing else uses `CoachTabPlaceholder` after this flow, delete it.

**Debug demo seed:** no change. The seed has no attendance records, and its Sunrise roster is not the Figma picture. Tests build their own data (section 7).

## 6. Shared and app changes (new constructor parameters go last, with defaults)

Each shared item gets new `:shared:phase2JvmTest` tests (they live under `shared/src/commonTest`).

- **C1. Section header data.** `CoachAttendanceSection` gains `sessionId: String = ""`, `className: String = ""`, `sessionNumber: Int = 0` (last, with defaults). The ViewModel fills them from the `Session` and its class, for scheduled and cancelled sections. Sort sections by start time, then scheduled before cancelled, then class name. Test: with the example data, sections are Sunrise, Lunch Express, Core Burn, each with its own session id, class name and session number; Lunch Express has `cancelled = true` and no students.
- **C2. Scan time in local time.** `CoachAttendanceStudentRow` gains `scannedTime: LocalTime? = null` (last). The ViewModel sets it from `scannedAt` in its time zone for Present rows; null otherwise. Test: a record scanned at 6:02 AM Asia/Manila gives `scannedTime = 06:02`.
- **C3. Store changes.** `AttendanceRepository` gains `val changes: StateFlow<Int>`, a counter that goes up by one after each successful `add()` or `update()`. Both `SqlAttendanceRepository` and `InMemoryAttendanceRepository` implement it (no new library needed; the app binds one store instance, so every write goes through it). Test, for both: the value starts at 0; after `add()` it is 1; after `update()` it is 2; a failed `update()` of an unknown record does not change it.
- **C4. Live reload and `refresh()`.** `CoachAttendanceViewModel` reloads the shown date whenever `attendance.changes` moves. It gains `fun refresh()`, which follows decision 11: if the last load showed today and today has changed, show the new today; otherwise reload the shown date. Test: with the screen state on Mon 28 Sep, adding a record for Dana on Core Burn makes her row Present and `presentCount` goes up by one, with no other call. Moving the clock to Tue 29 Sep and calling `refresh()` shows Tue 29 with `dayState = IN_PROGRESS`. Paging back to Sun 27 Sep, moving the clock a day, and calling `refresh()` keeps Sun 27.
- **C5. New `UiCopy` lines.** See the copy table. Existing functions do not change.
- **C6. URL-encoded coach id.** `GenerateDailyCode` percent-encodes the coach id (UTF-8; letters, digits, `-`, `.`, `_`, `~` stay as they are; everything else becomes `%HH` with upper-case hex). `VerifyDailyCode` accepts `%HH` in the coach value and decodes it before comparing and before checking the signature. A bad escape (for example `%ZZ` or a lone `%`) is `INVALID`. The signed payload still uses the plain, decoded id. Put the two helpers in `domain/crypto/DailyCodePayload.kt` as `internal` functions. Test: coach id `coach 1/ä` gives `coach=coach%201%2F%C3%A4` in the link, and checking that link for that coach today is `VALID`; for the current kind of id the link is unchanged from today; `coach=%ZZ` is `INVALID`.
- **C7. Signing key store contract.** A shared test for `InMemorySecretKeyStore` pins the contract the platform stores must also meet: `privateKey` of an unknown coach is null; after `putPrivateKey` it returns equal bytes; a second `putPrivateKey` replaces the first; changing the returned array does not change what is stored.

App / UI work (`composeApp`, covered by the ACs below):

- `CoachAttendanceScreen` (package `ph.project.active.ui.coachattendance`), wired from `CoachHomeScreen` when `selected == CoachTab.Attendance`. Reuse `LargeTitle`, `OfflineBanner`, `StatusChip`, `InitialsAvatar`, `TrxCard`. Icons: `circle_check` for Present, a new `circle_x` drawable (Lucide "circle-x") for Absent, `check` before the "Chose" line, `ban` for Cancelled, `chevron_left` / `chevron_right` for the date card.
- `CoachAttendanceTestTags` (one object, own file, same package).
- `rememberCoachAttendance(account, factory)` in `ph.project.active.di` (`CoachAttendanceFactory.kt`), key `"coach-attendance:${account.id}"`, same shape as `rememberDailyQr`.
- Bind `CoachAttendanceViewModel` in `ViewModelModule` with `Clock.System` and `TimeZone.currentSystemDefault()`.
- `CoachHomeScreen` and `AppNavHost` each gain a last optional parameter `coachAttendanceViewModel: ((SignedInAccount) -> CoachAttendanceViewModel)? = null`. The default resolves from Koin only when the Attendance tab opens.
- Platform signing-key stores (decision 15), the QR label (17), the iOS idle timer (18), text-selection colours (19), and the greeting fix (20).

## 7. Acceptance criteria

**Example data (tests build it themselves, on top of the flow 5 D19 data):** coach Mara Santos. Classes created on Mon 28 Sep 2026: Sunrise TRX Strength (`6:00 AM – 6:45 AM`, Mon/Wed/Fri), Lunch Express TRX (`12:15 PM – 12:45 PM`, cancelled on Mon 28 Sep only), TRX Core Burn (`5:30 PM – 6:30 PM`, Mon/Wed). Rosters in join order: Sunrise = Paolo Reyes (EMP-20417), Dana Uy (EMP-20155), Hana Cho (EMP-20877). Core Burn = Paolo, Dana, Gia Torres (EMP-20249). Lunch Express roster is empty. Mon 28 Sep records: Paolo on Sunrise at 6:02 AM, Dana on Sunrise at 6:05 AM, Paolo on Core Burn at 5:31 PM. Because the classes are created that day, each Mon 28 Sep session is "Session 1" (Figma’s "Session 10" is illustration).

- **D22 clock:** Mon 28 Sep 2026, 5:40 PM, Asia/Manila.
- **D21 clock:** Tue 29 Sep 2026, 9:41 AM, Asia/Manila, plus one more record: Dana on Core Burn at 5:33 PM on Mon 28 Sep.
- Any other test uses the usual Mon 28 Sep 2026, 9:00 AM, Asia/Manila, unless the AC says otherwise.

**The Attendance screen (D21–D22)**

- **F6-AC1.** Signed in as Mara, selecting the Attendance tab shows the real screen: `coach_attendance` exists; `coach_attendance_title` reads "Attendance"; `coach_attendance_overline` reads "TRX ATTENDANCE"; `coach_tab_attendance` is selected. There is no `placeholder_tag` anywhere on the tab, no `coach_attendance_account`, no add button, and no loading spinner.
- **F6-AC2.** On the D22 clock, the date card shows `coach_attendance_date` = "Mon 28 Sep 2026" and `coach_attendance_day_state` = "Today · in progress". `coach_attendance_next_day` is not enabled. `coach_attendance_prev_day` is enabled.
- **F6-AC3.** On the D22 clock, the summary shows a "Present" chip with `coach_attendance_present_count` = "3", and `coach_attendance_footnote` = "· Absent is set after the day ends". There is no `coach_attendance_absent_count` and no "Absent" chip anywhere on the screen.
- **F6-AC4.** On the D22 clock, the sections appear top to bottom as Sunrise TRX Strength, Lunch Express TRX, TRX Core Burn (each tagged `coach_attendance_section_{sessionId}`). Sunrise’s line reads "6:00 AM – 6:45 AM · Session 1" and Core Burn’s reads "5:30 PM – 6:30 PM · Session 1".
- **F6-AC5.** On the D22 clock, Sunrise rows appear in join order Paolo, Dana, Hana. Each row (tag `coach_attendance_row_{sessionId}_{studentId}`) shows initials, name and employee ID. Paolo: "Chose Sunrise TRX Strength · scanned 6:02 AM" and a "Present" chip. Dana: "Chose Sunrise TRX Strength · scanned 6:05 AM" and "Present". Hana: "Not scanned yet", with no chip and no "Chose" line. Core Burn: Paolo "Chose TRX Core Burn · scanned 5:31 PM" and "Present"; Dana and Gia "Not scanned yet".
- **F6-AC6.** Lunch Express (cancelled on that date) shows the line "12:15 PM – 12:45 PM · no attendance" and a "Cancelled" chip. It has no student rows. This holds on today (D22 clock) and on a past date (D21 clock).
- **F6-AC7.** On the D21 clock the tab opens on Tue 29 Sep ("Today · in progress"). Tapping `coach_attendance_prev_day` shows "Mon 28 Sep 2026" / "Day ended". The summary shows Present "4" and an "Absent" chip with `coach_attendance_absent_count` = "2". There is no footnote. Hana (Sunrise) and Gia (Core Burn) show an "Absent" chip and no "Chose" line. Dana on Core Burn shows "Chose TRX Core Burn · scanned 5:33 PM". `coach_attendance_next_day` is enabled; tapping it returns to Tue 29 Sep, where it is disabled again.
- **F6-AC8.** The left chevron has no limit: from Mon 28 Sep, tapping it twice shows "Sat 26 Sep 2026" / "Day ended". The right chevron never shows a date after today. Both chevrons have spoken labels ("Previous day", "Next day") and a touch target of at least 48dp (`TrxSpacing.touchTarget`).
- **F6-AC9.** On a date with no sessions (Sun 27 Sep), and separately for a coach with no classes, `coach_attendance_empty` reads "No sessions on this date.", there are no section tags, and the Present count is "0". On a past date the Absent chip shows "0"; on today the footnote shows instead.
- **F6-AC10.** Offline (fake network monitor offline) with the D22 data: `offline_banner` shows under the title with "No internet connection" and no subtitle, and all three sections and their rows still show. Going back online hides the banner without leaving the tab.

**Live updates and refresh**

- **F6-AC11.** With the tab open on the D22 clock, adding a record for Dana on Core Burn at 5:33 PM to the store (no tab switch, no other call) changes her row to "Chose TRX Core Burn · scanned 5:33 PM" with a "Present" chip, and the Present count becomes "4".
- **F6-AC12.** Leaving the Attendance tab for Classes, adding a record for Gia on Core Burn, then selecting Attendance again shows Gia as Present. Sending the app to the background and back (`HostLifecycle`) also reloads.
- **F6-AC13.** With the tab showing today (Mon 28 Sep), move the clock to Tue 29 Sep and bring the app back to the foreground: the date card shows "Tue 29 Sep 2026" / "Today · in progress". If the coach had paged back to Sun 27 Sep first, the same steps leave it on "Sun 27 Sep 2026".
- **F6-AC14.** Shared tests for C1–C4 pass in `:shared:phase2JvmTest`: section `sessionId` / `className` / `sessionNumber`, `scannedTime` in the app time zone, `AttendanceRepository.changes` for both stores, the live reload, and `refresh()`.

**Tabs, wiring and copy**

- **F6-AC15.** From Attendance, selecting Classes returns to the class list and selecting QR shows the real Daily QR screen, without changing the back stack. Selecting Attendance again shows the real Attendance screen, still with no `placeholder_tag`. Leaving QR for Attendance still restores brightness (F5-AC5 holds).
- **F6-AC16.** `CoachAttendanceViewModel` is bound in `ViewModelModule`, and a signed-in coach can open the Attendance tab in the production graph without a custom factory (checked by the Koin graph test and a source scan, like F5-AC12). UI tests pass a factory and never start Koin.
- **F6-AC17.** `UiCopy` returns exactly the strings in the copy table for each new function. Existing strings are unchanged.

**Leftovers**

- **F6-AC18.** Signing key store (decision 15): `FakeDataModule` no longer binds `InMemorySecretKeyStore`, and its "Placeholder: the private key stays in memory" comment is gone. The Android `platformModule()` binds `SecretKeyStore` to `AndroidSecretKeyStore`, the iOS one to `IosSecretKeyStore`, and the desktop (`jvmMain`) one to `InMemorySecretKeyStore` (checked by a source scan). The C7 contract test passes. A real restart is manual check P1.
- **F6-AC19.** URL-encoded coach id (C6) tests pass. Existing Phase 2 daily-code tests (AC27–31, AC75–78) and F5-AC3 stay green.
- **F6-AC20.** On the QR tab, the QR image (under `coach_qr_card`) has the spoken label "Today’s attendance code, valid Mon 28 Sep 2026 only" (from `UiCopy.qrCodeDescription(state.validFor)`). After the F5-AC11 date change it reads "…valid Tue 29 Sep 2026 only".
- **F6-AC21.** iOS idle timer (decision 18): `IosScreenBrightness` saves `idleTimerDisabled` on the first `raise()` and puts that saved value back on `restore()`. A source scan finds no `idleTimerDisabled = false` in `IosScreenBrightness.kt`. A real device is manual check P2.
- **F6-AC22.** Inside `TrxTheme`, `LocalTextSelectionColors.current` has `handleColor` = the theme’s `accentText` and `backgroundColor` = `accentText` at 40% alpha, in light mode (`TrxColors.Light.accentText`) and in dark mode (`TrxColors.Dark.accentText`).
- **F6-AC23.** On the student home (Classes tab), the greeting text appears exactly once in the merged semantics tree under `student_home` (on `student_classes_greeting`). The screen root no longer carries the greeting as its own text.

**Structure and regression**

- **F6-AC24.** Source scans still pass: one public type per file, no nested classes, Koin only in `composeApp`, no Firebase. New files are included (add `F6StructureTest` beside `F5StructureTest`). All existing suites still pass (`:shared:jvmTest`, `:shared:phase2JvmTest`, `:composeApp:jvmTest`, `:composeApp:structureJvmTest`, `androidHostTest`). The only test edits allowed are:
  - assertions that the Attendance tab is a placeholder (`placeholder_tag` under `coach_attendance` or `coach_attendance_account`, in the F4 and F5 tab tests);
  - Phase 2 AC79–81 tests that compare whole `CoachAttendanceSection` or `CoachAttendanceStudentRow` objects, which may add the new fields (behaviour asserts unchanged);
  - test doubles that implement `AttendanceRepository`, which may add `changes`;
  - flow 3 tests that read the greeting from the `student_home` node itself, which move to `student_classes_greeting`.

## 8. Test plan

| What | Suite | ACs or items |
|---|---|---|
| Section data, scan time, store `changes`, live reload, `refresh()`, URL encoding, key-store contract, new `UiCopy` | `:shared:phase2JvmTest` (new tests) | C1–C7; F6-AC14, F6-AC17 (shared part), F6-AC19 |
| Existing shared ViewModel / use-case / crypto tests | `:shared:jvmTest`, `:shared:phase2JvmTest` | regression; Phase 2 AC27–31, AC75–81 |
| D21–D22 screen tests (screen + ViewModel with fakes, `v2.runComposeUiTest`), tabs, live update, refresh, QR label, selection colours, greeting | `:composeApp:jvmTest` | F6-AC1–13, F6-AC15, F6-AC17 (UI part), F6-AC20, F6-AC22, F6-AC23 |
| Koin bindings and source scans | `:composeApp:structureJvmTest` | F6-AC16, F6-AC18, F6-AC21, F6-AC24 |
| Regression | all of the above, including `androidHostTest` | F6-AC24 |
| Restart keeps the code; iOS idle timer; screen readers; device look | manual (Sheen / Reviewer) | P1–P5 |

**Manual device checks**

- **P1.** On Android and on iOS: sign in as Mara, open QR, note the code (or scan it). Force-quit the app, reopen, open QR again: the code is the same. Sign in on a fresh install: a new key is created and the QR still shows.
- **P2.** On iOS: open QR, then go to Attendance, and wait. The phone auto-locks again on its normal timer. Brightness returns to what it was.
- **P3.** With TalkBack (Android) and VoiceOver (iOS): the QR image is read as "Today’s attendance code, valid {date} only". On the student home, the greeting is read once. The date chevrons are read as "Previous day" / "Next day".
- **P4.** The Attendance tab matches D21 and D22 in light mode and looks right in dark mode. A long "Chose … · scanned …" line wraps cleanly (design QA; Designer may ask for a layout tweak, not a copy change).
- **P5.** In light mode, selecting text in a field (for example Create class → Class name) shows dark-gold handles and highlight, not yellow on white.

**Test tags** (snake_case; new tags live in `CoachAttendanceTestTags`; existing tags keep their current objects):

- Existing, still used: `coach_attendance`, `coach_attendance_title`, `coach_tab_classes`, `coach_tab_qr`, `coach_tab_attendance`, `coach_qr`, `coach_qr_card`, `offline_banner`, `placeholder_tag`, `student_home`, `student_classes_greeting`
- Existing, removed from the Attendance tab: `coach_attendance_account` (delete the constant if nothing else uses it)
- New: `coach_attendance_overline`, `coach_attendance_prev_day`, `coach_attendance_next_day`, `coach_attendance_date`, `coach_attendance_day_state`, `coach_attendance_present_count`, `coach_attendance_absent_count`, `coach_attendance_footnote`, `coach_attendance_empty`, `coach_attendance_section_{sessionId}`, `coach_attendance_row_{sessionId}_{studentId}`

## 9. Pinned names and seams

- `ph.project.active.ui.coachattendance.CoachAttendanceScreen(state: CoachAttendanceUiState, onPreviousDay: () -> Unit, onNextDay: () -> Unit, tabBar: @Composable () -> Unit, modifier: Modifier = Modifier)`. Stateless; `CoachHomeScreen` connects it to the ViewModel.
- `ph.project.active.ui.coachattendance.CoachAttendanceTestTags` (object, own file).
- `ph.project.active.di.rememberCoachAttendance(account, factory)` in `CoachAttendanceFactory.kt`.
- `CoachHomeScreen(…, coachAttendanceViewModel: ((SignedInAccount) -> CoachAttendanceViewModel)? = null)` and `AppNavHost(…, coachAttendanceViewModel = null)`: new last parameters. The default must not resolve from Koin eagerly.
- `CoachAttendanceViewModel` keeps its constructor: `(account, classes, memberships, attendance, people, clock, timeZone, network)`. New: `fun refresh()`.
- `CoachAttendanceSection(slot, cancelled, students, sessionId: String = "", className: String = "", sessionNumber: Int = 0)`.
- `CoachAttendanceStudentRow(…, scannedTime: LocalTime? = null)`.
- `AttendanceRepository.changes: StateFlow<Int>`.
- `ph.project.active.data.secure.AndroidSecretKeyStore(context)` in `composeApp/src/androidMain`, and `ph.project.active.data.secure.IosSecretKeyStore()` in `composeApp/src/iosMain`. Both implement the shared `SecretKeyStore`. Suggested approach (Builder may adjust within the C7 contract and record it in the PR): Android wraps the private key bytes with an AES-GCM key held in Android Keystore and keeps the wrapped bytes in app-private preferences, one entry per coach id. iOS saves a Keychain generic-password item (service `ph.project.active.signing-key`, account = coach id, accessible after first unlock, this device only). If a stored key can’t be read, return null; `DailyQrViewModel` then makes a new key, as it does today.
- `internal fun encodeCoachId(id: String): String` and `internal fun decodeCoachIdOrNull(text: String): String?` in `domain/crypto/DailyCodePayload.kt`. `DAILY_URI`’s coach group also accepts `%`.
- `UiCopy.qrCodeDescription(date)` is set as the QR `Canvas`’s `contentDescription` in `DailyQrScreen`.

## 10. Assumptions

These are the only points not fixed by the approved answers, Figma, or existing code:

1. **Session ids on sections** (`sessionId`) are added beyond the agreed class name and session number. They give each section and row a stable test tag and list key. No visible change.
2. **"Chose {class}"** uses the section’s own class name. A record belongs to one session, so the class the student chose is that session’s class (this is what the ViewModel already does).
3. **The Absent chip on an empty past date** shows "0" (decision 8 keeps the summary row).
4. **Chevron spoken labels** "Previous day" / "Next day" are new copy for screen readers only. Figma shows icons with no text.
5. **Production clock and time zone** are `Clock.System` and `TimeZone.currentSystemDefault()`, as for `DailyQrViewModel`. Tests inject the pinned clock and Asia/Manila through the factory.
6. **Live updates** cover attendance records only. Roster or schedule changes show on the next tab show or foreground.
7. **An existing install** that already has a public key but lost its private key in memory gets one new key the first time QR opens after this update. After that, the key stays.

## 11. Open notes for later

- Student scanner / Attend tab (needed for a hand-run, same-phone live-update demo).
- Publishing the coach’s public key; a real uploader / Firebase; cross-device scans showing on the coach phone.
- Sign-out (must pop to `graph.id` inclusive), account deletion, edit-class-after-save.
- Drop `AppNavHost.onNavController` once the test harnesses pass `navController` instead.

## 12. Known gaps (on purpose)

- A real Android Keystore / iOS Keychain round trip, the iOS idle timer, screen-reader speech, and the device look are manual checks (P1–P5). The automated tests cover the contract, the bindings and the labels.
- Cross-device attendance waits for a real uploader.
- The "Chose …" line wrap is design QA, not a copy change.

## Copy table (exact strings; typographic apostrophe U+2019, en dash U+2013, middle dot U+00B7)

Existing functions, do not change:

| Function | String |
|---|---|
| `largeTitleEyebrow()` | "TRX ATTENDANCE" |
| `tabAttendance()` | "Attendance" |
| `present()` / `absent()` / `cancelled()` | "Present" / "Absent" / "Cancelled" |
| `notScannedYet()` | "Not scanned yet" |
| `dayEnded()` | "Day ended" |
| `inProgress()` | "Today · in progress" |
| `offlineBanner()` | "No internet connection" |
| `formatDate(LocalDate(2026, 9, 28))` | "Mon 28 Sep 2026" |
| `formatSlot` for 6:00–6:45 | "6:00 AM – 6:45 AM" |
| `sessionLabel(10)` (Schedule tab only; not used here) | "SESSION 10" |

New `UiCopy` functions:

| Function | String |
|---|---|
| `attendanceSlotLine(slot, sessionNumber)` | "{formatSlot(slot)} · Session {n}" (example: "6:00 AM – 6:45 AM · Session 10") |
| `cancelledSlotLine(slot)` | "{formatSlot(slot)} · no attendance" (example: "12:15 PM – 12:45 PM · no attendance") |
| `choseAndScanned(className, time: LocalTime)` | "Chose {className} · scanned {formatTime(time)}" (example: "Chose Sunrise TRX Strength · scanned 6:02 AM") |
| `absentSetAfterDayEnds()` | "· Absent is set after the day ends" |
| `noSessionsOnDate()` | "No sessions on this date." |
| `previousDay()` / `nextDay()` | "Previous day" / "Next day" |
| `qrCodeDescription(date)` | "Today’s attendance code, valid {formatDate(date)} only" (example: "Today’s attendance code, valid Mon 28 Sep 2026 only") |
