# TRX Class Attendance — Phase 3, Flow 2 Spec (guest class list, class detail, request to join)

## 1. Status
**Approved by Sheen on 2026-11-14, as drafted.** The team used its recommended answers (section 2) and Sheen approved them all. Tester writes the failing tests on a branch from `main` after the readability refactor (PR #5, merged); that refactor moved no packages, so the names below stand.

- Baseline: `main` at `12da87a` (flow 1 merged). Suites green: `:shared:jvmTest` 116, `:shared:phase2JvmTest` 116, `:composeApp:structureJvmTest` 7, `:composeApp:jvmTest` 52.
- Order: the readability refactor ([PR #5](https://github.com/Sheeeeeeen/Project-Active-App/pull/5)) is merged. Tester branches flow 2 from `main`.
- **Package names:** PR #5 moved no packages or public names (checked 16 Nov 2026), so every name in this spec stands as written.
- Design: Figma `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", section A `25:4901` (frames A1–A7). Details are in `/workspace/flow2-questions.md`, section 1.
- Code rule: one public type per file, no nested classes.

## 2. Decisions we made for you (veto any line)
**What's in**
1. We build the class detail screen for every state (A2–A7, plus Invited). Only the path A1 → A2 → sign up → A4 is wired up; the other states are covered by screen tests.
2. Debug builds add 5 demo classes (the A1 ones) when the database is empty, so a fresh install isn't blank. Release builds never include this.
3. There's no debug "approve" button. Approve and decline (A4 → A6, A4 → A5) are covered by automated tests, so a manual check stops at A4.
4. A class is still open on its end date and counts as ended the day after. Ended classes are hidden from A1, and their detail shows "This class ended on {date}." with no button.
5. There's no coach link to class detail yet (A7). One screen test covers it.

**Class list (A1)**

6. A class with several time slots shows them all on one line, joined by ", ". On detail, if the days have different times, each day gets its own line.
7. The count reads "1 class" or "N classes" and is hidden when the list is empty.
8. The empty list shows "No public classes yet" / "When a coach creates a public class, it shows up here." The "Register or sign in" bar stays.
9. There's no loading spinner (the data is local). It comes with Firebase in Phase 5.
10. The list reloads every time A1 comes back on screen. There's no pull-to-refresh until Phase 5.
11. There's no search or filter.
12. Offline: the banner sits under the title, the list still shows, and "Register or sign in" stays enabled.

**Class detail (A2–A7)**

13. No description line, because the model has no field for it.
14. The Days row uses long names ("Monday and Wednesday"). The A1 cards keep "Mon · Wed".
15. The Runs row shows "Until {date}", or "No end date".
16. A4 has no "Requested {date}" line, because we don't store the time yet.
17. A5 uses one neutral message for both a decline and a removal: "You can send a new request. The coach will need to approve it again."
18. "Request to join" shows a loading state while it sends.
19. Invited: an Invited chip, no button, and "Coach {name} invited you. Accept or decline it in Invites."
20. Cancelled class: a Cancelled chip, "This class has been cancelled.", and no button.
21. Private or unknown class: "Class not available" / "It may be private or no longer listed." with only the back button.
22. No tab bar on any detail screen. Tabs arrive in flow 3.
23. A guest who taps "Request to join" offline still goes to sign-up, which shows its own offline message.
24. After signing up from a class, you land on that class's detail showing Pending. Back takes you to the student home.
25. If the request couldn't be sent (class cancelled, private, or gone), you still land on the detail. A one-time note there says why.
26. New error text: "This class has been cancelled. Nothing was sent." and "This class is private. Joining is by coach invite only."
27. Request errors show in a red error row just above the button. The row clears on the next tap or refresh.
28. Screen readers read each A1 card as one button ("TRX Core Burn, Coach Mara Santos, Monday and Wednesday, 5:30 PM to 6:30 PM"). Chips read their label, and the class name and "Awaiting approval" are headings. Every tap target is at least 48dp.
29. Android uses light status-bar icons over the dark detail header and restores them when you leave.
30. Testing works the same as flow 1. New UI tests use `v2.runComposeUiTest`.

**From Designer and Tester**

31. Designer adds an A8 frame for a cancelled class (Cancelled chip in neutral grey, not red) and an A9 frame for "Class not available".
32. The one-time note in item 25 uses the existing Form Error row component.
33. Fields we dropped (description, "Requested {date}") stay in Figma, marked "Phase 5".
34. Designer adds a version of A6 without the tab bar.
35. Tests added: a check that the demo seed can't reach release code, a double-tap test, a refresh test, and a one-time-note test.
36. The three flow 1 clean-ups go in their **own small PR**, not in flow 2: the shake collector, `v2.runComposeUiTest` in `Flow1Compose.kt`, and the `OfflineBanner` imports. That PR also updates the F1-AC18 and F1-AC21 tests so the shake happens after the screen is showing, which is what lets the shake collector be simplified.

## 3. Scope
**In**
- A1 guest class list (`GuestClassListScreen`), replacing the guest home placeholder.
- One class detail screen (`ClassDetailScreen`) for all states.
- Navigation: A1 → detail → sign-up → detail (Pending).
- Debug demo seed.
- Small shared changes P1–P9 (section 5).
- New UI copy in `UiCopy`.
- Icons as vector drawables: log-in, user, calendar-days, clock, chevron-left, chevron-right, globe, hourglass, check, and info.

**Out**
- Student home, Invites, and tabs (flow 3).
- Any coach screens, plus approve and decline (flow 4).
- Pull-to-refresh, loading skeletons, the description field, and request timestamps (Phase 5).
- The flow 1 clean-ups (their own PR, decision 36).
- iOS device checks, including the iOS status bar (Phase 4).
- Changing the Phase 1 `RequestToJoin` use case. The ended check lives in the ViewModels (see Known gaps).

## 4. Screens and navigation
| Screen | Shows |
|---|---|
| A1 list | Title "Classes"; subtitle "Public TRX classes. No account needed."; header "PUBLIC CLASSES" plus a count; one card per class (name, "Coach {name}", "Mon · Wed", time line, chevron); a sticky bar with "Register or sign in" and "Create an account to request a class." |
| Detail hero | A dark header with a chip (Public, Pending, Joined, Invited, or Cancelled) and the class name. Back label "Classes". |
| Detail card | "Class details": Coach, Days (long names), Time, Runs |
| Detail bottom | Depends on the state (table below) |

| State | Bottom of the detail screen |
|---|---|
| Guest (A2) | "Request to join" + "You’ll create a student account first. Your request is sent after the account exists." |
| Student, not requested (A3) | "Request to join" + "Your request goes to Coach {name} for approval." |
| Pending (A4) | "Awaiting approval" + "Coach {name} will approve or decline your request. You don’t need to send it again." No button. |
| Request again (A5) | Callout "You can request again" / decision 17 text, then "Request to join" |
| Joined (A6) | "You’re on this class" + "Record attendance from the Attend tab during each session. Your dates are in Sessions." |
| Invited | Decision 19 |
| Coach (A7) | Info only. No button. |
| Cancelled (A8) | "This class has been cancelled." No button. |
| Ended | "This class ended on {date}." No button. |
| Unavailable | Decision 21 (empty-state layout) |

**Routes** (in `ui.navigation`, one file each):
- `GuestHome` now shows A1. The route name is kept, so the flow 1 tests still apply.
- **New** `ClassDetail(classId: String, joinOutcome: JoinOutcome? = null)`. It's a top-level destination used by both guests and students.
- `StudentHome(openClassId: String? = null, joinOutcome: JoinOutcome? = null)` gains `joinOutcome`.

**Paths**
```
Guest:   [GuestHome A1] → tap card → [GuestHome, ClassDetail(id)]
         → Request to join → [GuestHome, ClassDetail(id), Registration(id)]
         → account created → [StudentHome(id, outcome)] → pushes once → [StudentHome, ClassDetail(id, outcome)]
Back on ClassDetail → pops one level.
```
- Tapping a card twice quickly adds only one `ClassDetail` (`launchSingleTop`).
- `StudentHome` pushes `ClassDetail` only once. It doesn't push again after recomposition, rotation, or process restore (it keeps a saved "already opened" flag).
- The detail ViewModel's `viewer` is the account from `RootViewModel` (null for guests). It comes from `koinViewModel { parametersOf(classId, viewer, joinOutcome) }`.
- A1 calls `refresh()` each time it comes back on screen (lifecycle RESUMED).

**Changes to flow 1 tests (planned, not a weakening):** flow 1 deliberately left "push class detail" for flow 2. When sign-up starts from a class, the back stack is now `[StudentHome(id), ClassDetail(id)]` instead of `[StudentHome(id)]`. Tester updates these assertions in the flow 2 PR:
- `F1NavigationTest` line 94
- `F1SignInTest` line 74
- `F1HomePlaceholderTest` lines 34–36

`Flow1Compose.screenRoutes` also learns `ClassDetail`. Nothing else in flow 1 changes. The tag `guest_home_register_button` moves to A1's button.

**Debug demo seed:** `DemoSeed` lives only in a debug source set (Builder picks the simplest, for example `androidApp/src/debug`). It runs once at startup, only when there are no classes. It adds the 5 A1 classes (TRX Core Burn, Lunch Express TRX, TRX Mobility Flow, TRX Every Day 20, Weekend TRX Bootcamp) and their coaches.

## 5. Shared changes (each gets a `phase2JvmTest`; no existing test changes)
New constructor parameters go **last, with defaults**, so existing tests compile unchanged.

- **P1. List refresh.** `GuestClassListViewModel.refresh()` reloads from the repository. Test: add a class, call `refresh()`, and the class shows up.
- **P2. Ended rule.** A new top-level function in a new file in `domain.model`: `TrxClass.hasEnded(today: LocalDate): Boolean = endDate != null && endDate < today`. Both ViewModels take `clock: Clock = Clock.System` and `timeZone: TimeZone = TimeZone.currentSystemDefault()`. Test: a class ending today is open; one that ended yesterday is ended; one with no end date is never ended.
- **P3. List hides ended classes.** `GuestClassListViewModel` hides classes where `hasEnded(today)` is true. Test: with a fixed clock, a class ending yesterday isn't listed, and one ending today is.
- **P4. Detail knows cancelled and ended.** `ClassContent` gains `isCancelled: Boolean` and `isEnded: Boolean`. When either is true, `requestAction = None` and `requestAgain = false`. `onRequestToJoin()` does nothing for an ended class. For a cancelled class, it keeps today's behavior, so Phase 2 AC44 still passes. Test: both flags, the button state, and that no membership is created for an ended class.
- **P5. Sending flag.** `ClassDetailUiState.isSending: Boolean` is true only while the request runs. Test: it's false before and after a request, and a second call while sending is ignored (one membership).
- **P6. Errors clear.** `onRequestToJoin()` clears `error` before it tries. `refresh()` clears `error` and `notice`. Test: offline tap sets `Offline`; going online, then refresh, clears it.
- **P7. Request not sent.** `JoinOutcome` becomes `enum class JoinOutcome { ClassCancelled, ClassUnavailable }` so it works as a route argument. `RegistrationViewModel` maps `PrivateClass` and `ClassNotFound` to `ClassUnavailable`. `Navigation.OpenSignedInRoot` gains `joinOutcome: JoinOutcome? = null`, which is set from the same result. Test: each `JoinResult` maps to the right outcome and navigation, and a normal request leaves both null.
- **P8. One-time note.** `ClassDetailViewModel` takes `joinOutcome: JoinOutcome? = null` and exposes it as `ClassDetailUiState.notice`. The note clears on the next request tap or `refresh()`. Test: the note is set from the parameter and gone after `refresh()`. A new ViewModel without the parameter has no note.
- **P9. Copy.** New `UiCopy` functions for every string in sections 2 and 4. Also `formatDaysLong(days)` ("Monday and Wednesday", "Monday, Tuesday and Thursday", "Every day") and `ClassContent.timeLines()` (decision 6). `uiError(ClassCancelled)` and `uiError(PrivateClass)` use the decision 26 text, and `sessionCancelled()` stays "This date is cancelled.". Test: exact strings.

Note text for P8 (shown in the Form Error row):
- `ClassCancelled`: "Your account is ready, but no request was sent because this class was cancelled."
- `ClassUnavailable`: "Your account is ready, but no request was sent because this class isn’t available."

## 6. Acceptance criteria
Unless an AC says otherwise, its test uses the A1 example "TRX Core Burn", Coach Mara Santos, Mon and Wed 17:30–18:30, ending Sat 31 Oct 2026, with a clock fixed on or before that date.

**A1 list**
- **F2-AC1.** A1 shows the title, subtitle, "PUBLIC CLASSES", the count, and one card per public, open class, in creation order. Each card shows its name, "Coach {name}", "Mon · Wed", and "5:30 PM – 6:30 PM".
- **F2-AC2.** Private, cancelled, and ended classes don't appear. A class whose end date is today does appear.
- **F2-AC3.** The count reads "1 class" for one class and "5 classes" for five.
- **F2-AC4.** With no classes, `guest_list_empty` shows the decision 8 text, there's no count, and `guest_home_register_button` still shows.
- **F2-AC5.** A class with two different slots shows them in one line, sorted and joined by ", ".
- **F2-AC6.** Offline: `offline_banner` shows, the cards still show, and the register button is enabled.
- **F2-AC7.** Refresh: add a class to the repository, go to Registration and back, and the new card shows up.
- **F2-AC8.** Tapping a card opens `ClassDetail(id)`. Tapping it twice quickly still gives one `ClassDetail` entry.
- **F2-AC9.** Each card is one merged node with the decision 28 description and a height of at least 48dp.

**Class detail**
- **F2-AC10.** Guest (A2): the Public chip, the name (a heading), Coach, Days "Monday and Wednesday", Time, Runs "Until Sat 31 Oct 2026", "Request to join", and the A2 footnote. There's no description and no tab bar.
- **F2-AC11.** No end date: Runs shows "No end date". Different times on different days: one Time line per day ("Mon 5:30 PM – 6:30 PM").
- **F2-AC12.** Student, not requested (A3): the button plus the A3 footnote with the coach's name.
- **F2-AC13.** Tapping "Request to join" as a student creates one pending request, and the screen shows A4: the Pending chip, "Awaiting approval" (a heading), and the A4 text, with no button and no "Requested" line.
- **F2-AC14.** Double tap: two quick taps make exactly one membership.
- **F2-AC15.** While `isSending` is true (stateless screen test), the button shows its spinner and is disabled.
- **F2-AC16.** After approval, then `refresh()`: A6 (the Joined chip, "You’re on this class", and the A6 text, with no button).
- **F2-AC17.** After a decline, or a removal, then `refresh()`: A5 shows the callout title, the decision 17 text, and the button.
- **F2-AC18.** Invited: the Invited chip and the decision 19 text, with no button.
- **F2-AC19.** Coach (A7): the info shows and there's no button.
- **F2-AC20.** Cancelled class (A8): the Cancelled chip and "This class has been cancelled.", with no button.
- **F2-AC21.** Ended class (end date yesterday): "This class ended on {date}.", with no button. On its end date, the button still shows.
- **F2-AC22.** Private (guest) or unknown id: `class_detail_unavailable` shows the decision 21 text. Only the back button is shown.
- **F2-AC23.** Request errors show in `class_detail_form_error` above the button:
  - Offline: "You’re offline. Nothing was saved."
  - Private: the decision 26 text.

  Going online, then tapping again, clears the row.
- **F2-AC24.** Back (`class_detail_back`, "Classes") pops one screen.

**Request to join, end to end**
- **F2-AC25.** Guest path: A1, tap card, "Request to join". The back stack is `[GuestHome, ClassDetail(id), Registration(id)]`, and the B5 card shows the class.
- **F2-AC26.** Offline guest tap: still opens `Registration(id)` (decision 23).
- **F2-AC27.** Signing up there ends with the back stack `[StudentHome(id), ClassDetail(id)]`, one pending request, and A4 on screen. Back shows `student_home`. Signing in as an existing student behaves the same.
- **F2-AC28.** Exactly once: after three forced recompositions and a state-restoration round trip, there's still exactly one `ClassDetail` on the stack.
- **F2-AC29.** If the class is cancelled while on B5, you land on A8 with `class_detail_notice` showing the `ClassCancelled` note, and no membership is created. If it was made private or deleted, you get the unavailable screen (A9) with the `ClassUnavailable` note.
- **F2-AC30.** One-time note: after `refresh()`, or when the same class is opened again without an outcome, the note is gone.

**Structure and build**
- **F2-AC31.** Source scan: `DemoSeed` appears only under a debug source set. No file under `*/src/main`, `*/src/commonMain`, `*/src/androidMain`, `*/src/iosMain`, or `*/src/*release*` mentions it.
- **F2-AC32.** Source scan: each new route (`ClassDetail`) and each new screen file holds one public type. No nested classes.
- **F2-AC33.** All existing suites still pass. The only test edits allowed are the three flow 1 back-stack assertions listed in section 4.

**Manual checks** (Reviewer, on the Android emulator; these are not ACs):
- A fresh debug install shows the 5 demo classes. Relaunching doesn't add them again.
- Walk A1 → A2 → sign up → A4.
- Status-bar icons are light on detail and back to normal on A1.
- Large font: card names wrap and don't cut off.

## 7. Test plan
| What | Suite | ACs or items |
|---|---|---|
| Shared changes | `:shared:phase2JvmTest` (new tests only) | P1–P9 |
| A1 and detail screen tests (stateless screens + ViewModels with fakes, `v2.runComposeUiTest`) | `:composeApp:jvmTest` | F2-AC1–24 |
| Navigation on `AppNavHost` (ViewModel factories, test NavController) | `:composeApp:jvmTest` | F2-AC25–30 |
| Source scans | `:composeApp:structureJvmTest` | F2-AC31, F2-AC32 |
| Regression | all four suites | F2-AC33 |

Test tags (snake_case, one object per screen: `GuestClassListTestTags` and `ClassDetailTestTags`):
- A1: `guest_list_title`, `guest_list_count`, `guest_list_card_<id>`, `guest_list_empty`, `guest_home_register_button`
- Detail: `class_detail_back`, `class_detail_chip`, `class_detail_name`, `class_detail_days`, `class_detail_time`, `class_detail_runs`, `class_detail_request_button`, `class_detail_request_progress`, `class_detail_footnote`, `class_detail_status_title`, `class_detail_status_body`, `class_detail_callout`, `class_detail_form_error`, `class_detail_notice`, `class_detail_unavailable`
- Shared: `offline_banner`

## Known gaps (on purpose)
- The ended check lives in the ViewModels, not in the Phase 1 `RequestToJoin` use case. If a class ends while a guest is on B5, the request can still be sent. A server-side rule closes this in Phase 5.
- The one-time note can show again after Android kills and restores the process, because it rides on the route argument. That's acceptable until Phase 5.
