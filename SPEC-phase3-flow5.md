# TRX Class Attendance — Phase 3, Flow 5 Spec (coach daily QR)

## 1. Status
**Approved by Sheen — Mon 23 Nov 2026, 3:45 PM Asia/Manila.** Sheen approved all 9 default answers in `/workspace/flow5-questions.md`, and the section 2 fold-ins, on 23 Nov 2026 (Asia/Manila). This spec turns those answers, Figma, and the code on `main` into a build plan. Tester writes the failing tests on a branch from `main`, with this spec as the first commit.

- Baseline: `main` at `67f8231` (flow 4 and PR #13 merged). The QR and Attendance tabs are still `CoachTabPlaceholder`.
- Design: Figma `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", section D `25:4904` (D19 `22:9725`, notes `22:9859`, label `22:9722`; D20 `22:9866`, notes `22:9985`, label `22:9863`; D23 placeholder `108:6464`, to be replaced). D21 `22:9992` and D22 `22:10428`, and the D24 placeholder `108:6478`, stay for flow 6. Details are in `/workspace/flow5-questions.md` and `/workspace/flow5-context.md`.
- Naming: Figma labels D19 as "Flow 5 · step 1" and D21 / D22 only as "Results". In our Phase 3 plan, flow 5 is the coach’s real Daily QR tab (D19–D20). Coach attendance stays for flow 6.
- Package: `ph.project.active`. Code rule: one public type per file, no nested classes. Koin only in `composeApp`. One NavHost. Tabs live inside the signed-in coach root, not a nested NavHost. New constructor parameters go last, with defaults. New `AppNavHost` / `CoachHome` ViewModel factory parameters are optional, like `rememberCoachClasses`. Placeholders wear `PlaceholderTag`.

## 2. Decisions we made for you (veto any line)

**What’s in and what’s out**

1. Flow 5 builds the signed-in coach’s real **Daily QR** tab (D19 online, D20 offline). Daily QR behaviour already exists in `DailyQrViewModel` (Phase 2 AC75–78); this flow is the Compose UI, Koin wiring, the `classListUnavailable` tightening, the start-up sync gate, the schema migration, and the leftover styling in decisions 16–18.
2. Coach attendance (D21–D22) stays out. The Attendance tab remains `CoachTabPlaceholder` (D24) for flow 6. The QR tab replaces D23 / `CoachTabPlaceholder` with a real `DailyQrScreen`.
3. Also out of this flow: the student scanner; publishing the coach’s public key to other phones; a real attendance uploader / Firebase; sign-out and account deletion; and editing a class after it is saved. A same-phone demo still works: when Mara first opens the QR tab the public key is saved in the local database, so a later Paolo sign-in on that phone can verify the code.

**The QR screen**

4. The QR tab is always dark, even when the rest of the app is in light mode: dark canvas, light status-bar icons (`LightStatusBarIcons(lightIcons = true)`), dark tab bar. The QR itself is always black modules on a white card (`#0B0B0C` on `#FFFFFF`), in both modes, as the tokens already say.
5. While the QR tab is visible, raise brightness to full and keep the screen awake. Restore the previous brightness, and allow the screen to sleep again, when the coach leaves the tab, backgrounds the app, or the screen is disposed. Do this through a small interface (one public type per file, following `ShakeSource`) with a fake for tests. Android uses `window.screenBrightness`; iOS uses `UIScreen.main.brightness`. There is no automated brightness or keep-awake test against the real system APIs (see manual P1).
6. Follow Figma D19–D20: the real QR screen has no account icon and no **+**. `coach_qr_account` goes away. `PlaceholderTag` leaves the QR tab. The Attendance placeholder keeps its body tag and its account-button tag, as today.
7. Eyebrow "DAILY QR", title "Today’s code", subtitle `{formatDate(today)} · Coach {name}` (so "Mon 28 Sep 2026 · Coach Mara Santos"). The card caption is "Valid {formatDate(today)} only". Under the card: "Brightness is raised while this code is open". Section header "This code covers today" / "One code · all classes". Today’s sessions are listed in one card, in start-time order, with `formatSlot` and a Scheduled or Cancelled chip. Cancelled sessions stay on the list (Phase 2 AC77).

**Offline, empty, and refresh**

8. The code is always generated on the phone and works offline (flow 4 decision 16, Phase 2 AC78). There is no network call to produce it.
9. While offline, `offline_banner` shows under the title, with "No internet connection" and the existing subtitle "Showing today’s code saved on this phone." (`UiCopy.qrOfflineSubtitle()`). If today’s sessions are already in the local store, they still appear; the D20 callout does not.
10. The D20 callout ("Today’s class list can’t load" / "The code still works. Students on the roster can scan it now; their phones upload later.") shows only when the coach is offline **and** the phone holds none of their classes. `DailyQrViewModel` today sets `classListUnavailable` whenever the coach has zero classes, even when online. Change it so `classListUnavailable` is true only when `connection.isOffline` and the coach has no classes. Online with no classes, or with classes but no sessions today, still shows the code and the section header, with an empty list and no callout. No new empty-state copy.
11. Call `refresh()` whenever the QR tab is shown, and whenever that tab returns to the foreground, using the same `LifecycleStartEffect` pattern as Classes. If the local date has changed, replace the code silently; the card reads "Valid {today} only". The old code already checks as `WRONG_DATE`. No dialog and no pull-to-refresh. There is no loading spinner.

**QR encoding, keys, and Koin**

12. The QR encodes `DailyQrUiState.code` exactly, which is the URI from `GenerateDailyCode`: `trxatt://v1/daily?coach={id}&date=YYYY-MM-DD&sig={base64url}`. Prefer a small pure-Kotlin or KMP QR encoder that works on Android and iOS. Builder picks the library and records the choice in the PR. The contract is in F5-AC3: same input string, black on white, with a quiet zone. Do not add an Android-only or iOS-only library as the only path.
13. If the coach has no key yet, the ViewModel already creates an `Ed25519KeyPair` once and reuses it (Phase 2 AC76). Bind `CoachKeyRepository` (`SqlCoachKeyRepository`) and `SecretKeyStore` (`InMemorySecretKeyStore`, until a later Keystore / Keychain pass) in the app Koin module (`FakeDataModule` or the same place the other repositories are bound), so `DailyQrViewModel` can be constructed. Tests keep the fakes they already use.
14. Bind `DailyQrViewModel` in `ViewModelModule` (Koin in `composeApp` only), created with the signed-in coach account, and scope it so it survives tab switches, the same idea as `rememberCoachClasses`. Tests may pass an optional factory.

**Carry-overs that ship in this flow**

15. Gate start-up sync on the first **real** network reading. `IosNetworkMonitor`’s pre-callback `ONLINE` (used so the offline banner does not flash) is not a real reading. `MutableNetworkMonitor` and `AndroidNetworkMonitor` are real from the start. `RootViewModel` runs `OutboxSync` only when a real reading is `ONLINE` and the previous real reading was not `ONLINE` (including "no previous real reading", so a restart that is actually online still syncs). A failed sync is retried on the next real `ONLINE`. Tests keep `MutableNetworkMonitor`. Existing AC20 still holds for a monitor whose first value is already a real reading.
16. Add SQLDelight migration `1.sqm` and bump the schema to v2. The migration must succeed on a v1 database that does not have `membership.joined_by` (pre-PR-12) and on a v1 database that already has it (current `main`). After migrate, `TrxDatabase.Schema.version` is 2, the column is present, and existing membership rows are intact. Update `SchemaTest`, which currently asserts version 1. Pre-PR-12 debug installs should still clear app data if anything looks wrong; the migration is the supported path.
17. Map `TrxColors.Light` and `TrxColors.Dark` into `MaterialTheme` colour schemes in `TrxTheme`, so the Create date and time pickers use TRX colours and follow dark mode instead of staying the default purple (and staying light in dark mode).
18. The Requests count pill (`manage_tab_requests_badge`) already shows when `pendingRequestCount` is greater than 0, and is hidden at 0. Give it a **minimum** width of `TrxSpacing.s20` (20.dp) and a height of 20.dp, so three-digit text is not clipped. Cap the label at "99+": counts 1–99 show the number ("99" at 99); counts of 100 or more show "99+".

**Already locked, not reopened**

19. One code covers every class the coach has today, not one per class (Phase 2 AC75). Same pinned clock as flows 3–4: Mon 28 Sep 2026, 9:00 AM, Asia/Manila, unless an AC says otherwise. The real network monitors already shipped in flow 4; this flow only fixes start-up sync timing (decision 15).

## 3. Scope

**In:** the real Daily QR tab (D19–D20); `DailyQrScreen` bound to `DailyQrViewModel`; the `classListUnavailable` rule change; QR encoding; brightness / keep-awake; start-up sync gate; schema v2 migration; TRX Material colour schemes; the Requests pill cap; the seed / spec wording fix in section 5.

**Out:** D21–D22 and any real Attendance UI; the student scanner; publishing keys to other phones; a real `AttendanceUploader`; sign-out and account deletion; edit-class; pull-to-refresh; loading skeletons; Android Keystore / iOS Keychain (keep `InMemorySecretKeyStore`).

## 4. User stories

- As a signed-in coach, I can open the QR tab and show today’s code, which covers every class I have today.
- As a signed-in coach who is offline, I can still show today’s code from this phone. If my class list is already on the phone, I still see it; if it is not, I see the D20 callout and the code still works.
- As a student who scans that code on the same phone later, I can verify it because the coach’s public key was saved locally when the QR tab first opened. Cross-device scanning waits for a later flow.
- As a coach who leaves the QR tab open overnight, I see a new code the next day with no extra dialog.

## 5. Screens and navigation

| Screen | Shows |
|---|---|
| D19 Daily QR | Always-dark. Eyebrow "DAILY QR"; title "Today’s code"; subtitle "{formatDate(today)} · Coach {full name}". No account icon, no **+**. White QR card with black modules and "Valid {formatDate(today)} only". Caption "Brightness is raised while this code is open". Section "This code covers today" / "One code · all classes". One card of today’s sessions, start-time order: class name, `formatSlot`, Scheduled or Cancelled chip. Tab bar with QR selected. No `PlaceholderTag`. |
| D20 Daily QR, offline | Same as D19, plus the offline banner under the title: "No internet connection" / "Showing today’s code saved on this phone." The code still shows. If classes are cached, the session list still shows. If they are not, the session list is replaced by the callout "Today’s class list can’t load" / "The code still works. Students on the roster can scan it now; their phones upload later." (existing `InfoCallout`; use `Res.drawable.wifi_off` if it fits, otherwise the callout’s default icon is fine until design QA). |
| Attendance tab | Unchanged `CoachTabPlaceholder`: title "Attendance", `placeholder_tag` in the body and on the account button. |

- The three tabs still live inside `CoachHome`, not as their own NavHost destinations. Switching tabs does not change the back stack. The starting tab is still Classes.
- `DailyQrViewModel` is bound in `ViewModelModule` and scoped to the `CoachHome` back-stack entry. Call `refresh()` each time the QR tab is shown, and when it returns to the foreground.
- `placeholder_tag` leaves the QR tab. It stays on the Attendance placeholder only (body + `coach_attendance_account`). The Classes account button is unchanged. This supersedes the QR clause of F4-AC9 and the QR half of F4-AC25; those tests must be updated. The Attendance assertions stay.

**Debug demo seed:** do not change `DemoSeed` in this flow, except as needed so a fresh debug install still signs in the same way. Keep the six public classes and Paolo’s memberships as flow 3 left them (Sunrise stays Public). Mara’s Core Burn already holds the Manage roster and requests (Dana, Gia, and Hana on the roster after Paolo; requests from Ella, Bea, and Carlo; Felix on no class) and still has **no end date**. Tests that need "Ends Sat 31 Oct 2026" and "10 dates" pin those values themselves (the Manage example data in the flow 4 spec, section 7). The Figma D1 snapshot (Sunrise as Private, "3 classes", "12 students") is still not this seed.

Mara’s sign-in stays as documented: `mara@company.com` / `password1`. The app does not sign in as Mara on its own.

## 6. Shared and app changes (new constructor parameters go last, with defaults)

Each shared item gets a `:shared:phase2JvmTest` (those tests live under `shared/src/commonTest` and run in `phase2JvmTest`, same as today).

- **C1. `classListUnavailable`.** Set it true only when `connection.isOffline` and the coach has no classes in the repository. Test: online with no classes → `classListUnavailable` is false, `todaysSessions` is empty, and `code` is not blank; offline with no classes → `classListUnavailable` is true and `code` is not blank; offline after creating a class for this coach → `classListUnavailable` is false and `todaysSessions` is filled. Existing AC78 still holds (offline with no classes is unavailable; adding a class and building a new ViewModel, or calling `refresh()`, makes it available; `refresh()` after the date changes still rolls the code).
- **C2. New `UiCopy` lines.** Add functions for the D19 strings in the copy table below. Existing functions (`offlineBanner`, `qrOfflineSubtitle`, `classListUnavailableTitle`, `classListUnavailableBody`, `scheduled`, `cancelled`, `tabQr`) do not change. Test: each new function returns the exact string in the table; `formatDate(LocalDate(2026, 9, 28))` is still "Mon 28 Sep 2026".
- **C3. Start-up sync gate.** Implement decision 15. Test: `MutableNetworkMonitor` starting `ONLINE` still syncs once on start (a real reading; existing AC20 restart case). A test double that starts as unconfirmed `ONLINE` and then emits a real `OFFLINE` must not have called the uploader; a later real `ONLINE` syncs once. A failed sync is retried on the next real `ONLINE`.
- **C4. Schema v2.** Add `shared/src/commonMain/sqldelight/ph/project/active/data/db/1.sqm`, set the SQLDelight schema version to 2, and update `SchemaTest`. Test: a fresh create is version 2 and still has every required table, including `membership.joined_by`; migrating a v1 database without `joined_by` adds the column and keeps existing rows; migrating a v1 database that already has `joined_by` also succeeds and keeps existing rows.

App / UI work (not `phase2JvmTest`, covered by the ACs below):

- `DailyQrScreen` in `composeApp` (package `ph.project.active.ui.dailyqr`), wired from `CoachHomeScreen` when `selected == CoachTab.Qr`. A `rememberDailyQr` factory beside `rememberCoachClasses`, with an optional test factory.
- Bind `DailyQrViewModel`, `CoachKeyRepository`, and `SecretKeyStore` as in decisions 13–14. Extend `F1Ac8FakeDataModuleBindingsTest` / the Koin graph test if they list bindings.
- A small brightness / keep-awake interface with a fake (decision 5).
- Optional subtitle on `OfflineBanner` (default null so Classes, Create, and Manage stay title-only). The QR screen passes `UiCopy.qrOfflineSubtitle()`.
- `TrxTheme` colour schemes (decision 17). Requests pill (decision 18).

## 7. Acceptance criteria

Unless an AC says otherwise, tests use a clock fixed on **Mon 28 Sep 2026**, 9:00 AM, Asia/Manila.

**D19 example data:** coach Mara Santos; today Mon 28 Sep 2026; today’s sessions in start-time order are Sunrise TRX Strength (`6:00 AM – 6:45 AM`, Scheduled), Lunch Express TRX (`12:15 PM – 12:45 PM`, Cancelled), and TRX Core Burn (`5:30 PM – 6:30 PM`, Scheduled). Tests build this data themselves (cancel Lunch Express on that date). It is not the pretty-picture-only Figma snapshot, and it is not "whatever the debug seed happens to contain".

**Daily QR (D19–D20)**

- **F5-AC1.** After signing in as the coach and opening the QR tab, the real Daily QR screen is showing: `coach_qr` exists; `coach_qr_title` reads "Today’s code"; the eyebrow is "DAILY QR"; the subtitle is "Mon 28 Sep 2026 · Coach Mara Santos"; the tab bar is visible with `coach_tab_qr` selected. There is no `placeholder_tag` under `coach_qr` (or anywhere on the QR tab). There is no `coach_qr_account` and no add button. There is no loading spinner.
- **F5-AC2.** With the D19 example data, the card shows "Valid Mon 28 Sep 2026 only", the brightness caption "Brightness is raised while this code is open", the section "This code covers today" / "One code · all classes", and the three session rows in that order, each with `formatSlot` and the Scheduled or Cancelled chip. Cancelled Lunch Express is present.
- **F5-AC3.** The QR encodes `DailyQrUiState.code` exactly (the `GenerateDailyCode` URI for this coach and today). Modules are black on a white card, with a quiet zone. A test passes `state.code` into the encoder (or decodes the bit matrix) and gets that same string. `VerifyDailyCode` on that string for this coach and today is `VALID`.
- **F5-AC4.** With the app in light mode, the QR tab still uses the dark canvas (`TrxColors.Dark.canvas`), light text, and a dark tab bar, and calls `LightStatusBarIcons(lightIcons = true)`. The QR modules stay `#0B0B0C` on `#FFFFFF`.
- **F5-AC5.** A fake brightness / keep-awake implementation records that, when the QR tab becomes visible, brightness is raised to full and keep-awake is on; when the coach selects Classes, or the screen is disposed, brightness is restored and keep-awake is off.
- **F5-AC6.** From the QR tab, selecting Classes returns to the class list without changing the back stack. Selecting Attendance shows the existing placeholder: `coach_attendance` exists, the title is "Attendance", one `placeholder_tag` is under `coach_attendance`, and one is under `coach_attendance_account`. Selecting QR again shows the real Daily QR screen, still with no `placeholder_tag`.
- **F5-AC7.** Offline with the D19 sessions already in the store: `offline_banner` shows under the title, the title text is "No internet connection", the subtitle "Showing today’s code saved on this phone." is visible, the QR card still shows, and the three session rows still show. The D20 callout is not showing. `classListUnavailable` is false.
- **F5-AC8.** Offline with no classes for this coach: the banner and subtitle show, the QR card still shows, and the callout shows `UiCopy.classListUnavailableTitle()` and `classListUnavailableBody()`. There are no session rows. `classListUnavailable` is true.
- **F5-AC9.** Online with no classes for this coach: the banner and the D20 callout are both hidden, the code still shows, the section header still shows, and the session list is empty. `classListUnavailable` is false.
- **F5-AC10.** Online with classes but no sessions today: same as F5-AC9 (no banner, no callout, empty list, `classListUnavailable` is false).
- **F5-AC11.** With the QR tab shown on Mon 28 Sep, move the clock to Tue 29 Sep 2026 and return the tab to the foreground (or call the same `refresh()` the tab uses). The subtitle and the "Valid … only" line show "Tue 29 Sep 2026", `state.code` is not the old value, and the old value checks as `WRONG_DATE`. No dialog is shown.

**Wiring, copy, leftovers, and structure**

- **F5-AC12.** `DailyQrViewModel` is bound in `ViewModelModule`. `CoachKeyRepository` and `SecretKeyStore` are bound in the app Koin module. A signed-in coach can open the QR tab without a custom factory in the production graph. Tests may still inject a factory and a fixed clock.
- **F5-AC13.** Start-up sync follows decision 15 and C3. `MutableNetworkMonitor` starting `ONLINE` still syncs once on start. An unconfirmed initial `ONLINE` followed by a real `OFFLINE` does not upload. A later real `ONLINE` syncs once.
- **F5-AC14.** Schema is v2, with a `1.sqm` migration that meets C4. `SchemaTest` asserts version 2. Both v1 shapes migrate successfully.
- **F5-AC15.** `TrxTheme` passes a Material `colorScheme` built from `TrxColors.Light` or `TrxColors.Dark`. In light mode, scheme surface / background / primary / onPrimary / onSurface / error come from the light tokens (`canvas` or `surface` for backgrounds, `accentDefault` for primary, `textOnAccent` for onPrimary, `textPrimary` for onSurface, `dangerFg` for error). In dark mode they come from the dark tokens. A compose test in both modes reads those scheme colours and matches the tokens. (Picker chrome on a device is manual P5.)
- **F5-AC16.** On Manage → Requests, the pill tagged `manage_tab_requests_badge` is hidden when `pendingRequestCount` is 0. At 1 it shows "1"; at 99 it shows "99"; at 100 and at 1000 it shows "99+". The pill’s height is 20.dp and its width is at least 20.dp (`TrxSpacing.s20`). It can grow wider than 20.dp to fit "99+".
- **F5-AC17.** `UiCopy` returns exactly the strings in the copy table below for each new function. Existing QR and offline strings are unchanged.
- **F5-AC18.** Source scans still pass: one public type per file, no nested classes, Koin only in `composeApp`, no Firebase. New files are included in the existing structure suite (extend `F4StructureTest` or add a flow 5 structure test beside it).
- **F5-AC19.** All existing suites still pass (`:shared:jvmTest`, `:shared:phase2JvmTest`, `:composeApp:jvmTest`, `:composeApp:structureJvmTest`, `androidHostTest`). The only test edits allowed are those that required the QR tab to be a placeholder (F4-AC25’s QR title "QR", `placeholder_tag` under `coach_qr`, and `coach_qr_account`) or that asserted `classListUnavailable` whenever the coach had zero classes while online, plus `SchemaTest`’s version 1 assertion. Phase 2 AC75–77 stay green. Attendance placeholder assertions stay.

## 8. Test plan

| What | Suite | ACs or items |
|---|---|---|
| Shared changes (`classListUnavailable`, new `UiCopy`, sync gate, schema v2) | `:shared:phase2JvmTest` (new tests only) | C1–C4; F5-AC13, F5-AC14, F5-AC17 (shared part) |
| Existing shared ViewModel / use-case / schema tests | `:shared:jvmTest` and `:shared:phase2JvmTest` | regression; F5-AC19; Phase 2 AC75–78 |
| D19–D20 screen tests (stateless screen + ViewModel with fakes, `v2.runComposeUiTest`), brightness fake, pill, theme scheme, tab switch | `:composeApp:jvmTest` | F5-AC1–12, F5-AC15, F5-AC16, F5-AC17 (UI part) |
| Source scans and Koin bindings | `:composeApp:structureJvmTest` | F5-AC12 (bindings), F5-AC18 |
| Regression | all of the above, including existing `androidHostTest` | F5-AC19 |
| Brightness + keep-awake on a device; always-dark QR in light mode; iOS airplane-mode cold start; scannable QR; picker colours in light and dark | manual (Sheen / Reviewer) | P1–P5 below, not automated ACs |

**Manual device checks**

- **P1.** On Android and iOS, open the QR tab: brightness goes to full and the screen stays awake. Switch to Classes, or background the app, or leave the screen: brightness and sleep return to what they were.
- **P2.** With the app in light mode, the QR tab is still the dark D19 screen (light status-bar icons, dark tab bar, white QR card). In dark mode it stays correct.
- **P3.** On iOS, a cold start in airplane mode does not mark pending scans as synced (the fake uploader must not have been treated as success). After the first path update the offline banner appears, and the QR code still shows. (Android already reads the real status at start; still worth a quick airplane-mode check.)
- **P4.** The QR is scannable with a real camera / QR reader and decodes to the `trxatt://v1/daily?…` URI for that coach and today.
- **P5.** On Create class, the date and time pickers use TRX colours in light mode and follow dark mode. They are not the default purple, and they are not stuck light when the app is dark.

Test tags (snake_case; new tags live in `DailyQrTestTags`, one object in its own file; existing tags keep their current objects):

- Existing, still used: `coach_qr`, `coach_qr_title`, `coach_tab_classes`, `coach_tab_qr`, `coach_tab_attendance`, `offline_banner`, `manage_tab_requests_badge`, `placeholder_tag`, `coach_attendance`, `coach_attendance_title`, `coach_attendance_account`
- Existing, removed from the QR tab: `coach_qr_account`
- New on Daily QR: `coach_qr_overline`, `coach_qr_subtitle`, `coach_qr_card`, `coach_qr_valid`, `coach_qr_brightness`, `coach_qr_sessions`, `coach_qr_callout`

## 9. Assumptions

These are the only points not fixed by the approved answers, Figma, or existing code:

1. **`OfflineBanner` gains an optional subtitle**, last, defaulting to null, so existing Classes / Create / Manage banners stay title-only. The QR screen passes `UiCopy.qrOfflineSubtitle()`. The banner on the (always-dark) QR tab uses the dark `TrxColors`, so `surfaceInverse` is white and the banner matches D20.
2. **D20 callout** uses the existing `InfoCallout` and the existing UiCopy title and body. Icon `Res.drawable.wifi_off` if it looks right on the dark screen; otherwise the default info icon is acceptable until design QA. Style is `InfoCalloutStyle.Neutral` unless Designer asks for a different existing style.
3. **Session rows** are asserted by text under `coach_qr_sessions`. There is no session id on `TodaySessionRow`, so we do not invent per-row tags.
4. **Production clock and time zone** for `DailyQrViewModel` are `Clock.System` and `TimeZone.currentSystemDefault()`, same as `CreateClassViewModel`. Tests inject the pinned clock and Asia/Manila through the optional factory.
5. **The brightness / keep-awake interface** is a composeApp platform type (expect/actual or a small injected interface), not a new field on `DailyQrViewModel`. Tests use the fake. Builder chooses a plain name (one public type per file) and records it in the PR.
6. **F4-AC9 and F4-AC25** are updated only for the QR tab, as in F5-AC19. The Attendance placeholder, and the Classes account icon, stay as flow 4 left them.
7. **No new demo-seed classes** are required for Daily QR. Tests that need a cancelled session today create it themselves.

## 10. Open notes for later

- A later sign-out must pop to `graph.id` inclusive, because `RootViewModel.startRoot` is captured once.
- Optional later cleanup, not in this flow: drop the `onNavController` parameter on `AppNavHost`. Watch whether TalkBack still reads the student-home greeting twice (PR #11 should-fix).
- Flow 6: real Attendance (D21–D22). Phase 2 AC79 already includes cancelled sessions as sections with no students; D21 shows cancelled Lunch Express, the D22 pretty picture omits it — follow Phase 2 unless Sheen later says otherwise. The long "Chose Sunrise TRX Strength · scanned …" line wraps mid-name in the D21 capture; that is design QA for flow 6. Offline scans appearing after a student upload still need a real uploader.
- Publishing the coach’s public key to other phones, a real `AttendanceUploader`, and Android Keystore / iOS Keychain wait for later.
- The grey box behind the selected Manage tab, if it shows up on a device, is a later visual fix, not part of this spec.

## 11. Known gaps (on purpose)

- The real Attendance screens, student-side scanner changes, sign-out, edit-class, undo, loading skeletons, and pull-to-refresh wait for later phases.
- iOS brightness, keep-awake, airplane-mode cold start, a physically scannable QR, and picker colours are manual checks (P1–P5).
- The QR library choice is Builder’s, within the F5-AC3 contract.
- `SecretKeyStore` stays the in-memory fake.

## Copy table (exact strings; typographic apostrophe U+2019, en dash U+2013, middle dot U+00B7)

Existing functions, do not change:

| Function | String |
|---|---|
| `offlineBanner()` | "No internet connection" |
| `qrOfflineSubtitle()` | "Showing today’s code saved on this phone." |
| `classListUnavailableTitle()` | "Today’s class list can’t load" |
| `classListUnavailableBody()` | "The code still works. Students on the roster can scan it now; their phones upload later." |
| `scheduled()` / `cancelled()` | "Scheduled" / "Cancelled" |
| `tabQr()` / `tabAttendance()` / `tabClasses()` | "QR" / "Attendance" / "Classes" |
| `formatDate(LocalDate(2026, 9, 28))` | "Mon 28 Sep 2026" |
| `formatSlot` for 6:00–6:45, 12:15–12:45, 17:30–18:30 | "6:00 AM – 6:45 AM" / "12:15 PM – 12:45 PM" / "5:30 PM – 6:30 PM" |

New `UiCopy` functions (names follow the existing style; tests assert the strings):

| Function | String |
|---|---|
| `dailyQrOverline()` | "DAILY QR" |
| `todaysCode()` | "Today’s code" |
| `dailyQrSubtitle(date, coachName)` | "{formatDate(date)} · Coach {coachName}" (example: "Mon 28 Sep 2026 · Coach Mara Santos") |
| `validForDate(date)` | "Valid {formatDate(date)} only" (example: "Valid Mon 28 Sep 2026 only") |
| `brightnessRaised()` | "Brightness is raised while this code is open" |
| `thisCodeCoversToday()` | "This code covers today" |
| `oneCodeAllClasses()` | "One code · all classes" |

## Rulings on team questions (Planner, 23 Nov 2026, 3:47 PM Asia/Manila)

1. **C4 migration keeps existing `joined_by` values.** Do not rebuild the table in a way that throws away a `joined_by` value that is already there. Use a code-side migration step: read `PRAGMA table_info(membership)`, add `joined_by` with a default of `REQUEST` only when it's missing, and then set the schema to v2. Tests check only the C4 outcomes: v1 without the column gets `REQUEST` on every row, v1 with the column keeps each row's value, and the version is 2 afterwards.
2. **F5-AC3 seam.** Common `QrEncoder.encode(text: String): QrMatrix`, where `QrMatrix` has `size` and `isDark(x, y)`. Each type goes in its own file. The JVM tests decode the matrix with zxing `core`, added as a test-only dependency, and assert that it equals `state.code`. Production uses a small pure-Kotlin KMP encoder, which is Builder's choice.
3. **Tester's questions.** There's one code for all of the coach's classes today, with no picker. The date changes on tab show or on foreground, never on a timer. QR tests check the text the code holds, not images. None of these change the spec.
4. **Pinned names for PR #14 (confirmed 4:08 PM).**
   - `NetworkMonitor.hasRealReading: StateFlow<Boolean>`. It's true from the start for the fakes, Android and desktop, and false on iOS until the first path update.
   - `object TrxSchema` in `ph.project.active.data.db`. `migrate(driver, 1, 2)` does the ruling 1 step and leaves `PRAGMA user_version` at 2, and both phones' database setup uses it.
   - `ph.project.active.ui.dailyqr.QrEncoder` in composeApp, with a 4-module blank border.
   - `ph.project.active.ui.platform.ScreenBrightness`. `raise()` turns brightness up to full and keeps the screen awake, and `restore()` undoes both. `AppNavHost` gets two new last parameters, an optional `dailyQrViewModel` and `screenBrightness`. The default must not resolve from Koin eagerly.
   - The D20 callout follows the connection live: it's on when offline with nothing saved and off when back online.
   - The F5-AC12 Koin wiring is checked by a source scan.
   - F5-AC4: the dark tab bar uses the dark canvas, surface or surfaceAlt colour, and the title uses the dark scheme's text colour.
