# TRX Class Attendance — Phase 3, Flow 4 Spec (coach classes, create, and manage)

## 1. Status
**Approved by Sheen on 23 Nov 2026, 8:41 AM (Asia/Manila).** Sheen approved all 13 recommended answers in `/workspace/flow4-questions.md` as written, on 23 Nov 2026 at 8:33 AM (Asia/Manila), including the Q3 clarification that the coach account icon wears `PlaceholderTag`. That sits on top of the five locked Tester / Designer answers and the network-monitor notes in `/workspace/flow4-carryover.md`, approved 23 Nov 2026 at 8:25 AM (Asia/Manila). This spec turns those answers, Figma, and the code on `main` into a build plan. Tester writes the failing tests on a branch from `main`, with this spec as the first commit.

- Baseline: `main` at `4acb311` (flow 3 / PR #11 merged). `CoachHome` is still a placeholder.
- Design: Figma `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", section D `25:4904` (D1 `22:6218`, notes `22:6386`; D2 `22:6393`; D3 `22:6611`; D4 `22:6713`; D5 `22:6859`; D6 `22:7097`; D7 `22:7343`; D8 `22:7628` / dialog `22:7835`; D9 `22:7856`; D10 `22:8067`; D11 `22:8195`; D12 `22:8279`; D13 `22:8595` / dialog `22:8707`; D14 `22:8724`; D15 `22:8828`; D16 `22:8941`; D17 `22:9037`; D18 `22:9501` / dialog `22:9708`). Details are in `/workspace/flow4-questions.md` and `/workspace/flow4-context.md`.
- Naming: Figma labels invite as "Flow 4" and cancel-one-date as "Flow 6". In our Phase 3 plan, flow 4 is all of D1–D18. Daily QR and coach attendance stay for flows 5 and 6.
- Package: `ph.project.active`. Code rule: one public type per file, no nested classes. Koin only in `composeApp`. One NavHost. One-shot `navigation` plus `onNavigationHandled()`. New constructor parameters go last, with defaults. New `AppNavHost` / `TrxApp` ViewModel factory parameters are optional, like `classDetailViewModel`. Placeholders wear `PlaceholderTag`.

## 2. Decisions we made for you (veto any line)

**What’s in**

1. Flow 4 builds the signed-in coach **Classes** home (D1–D2), **create class** (D3–D6), and **manage class** (D7–D18): Schedule, cancel one date, Requests, Roster, Invite, class options, and cancel the whole class. Class-list and manage behaviour already exists in the shared ViewModels (Phase 2 AC61–74); this flow is the Compose UI, navigation, the real network monitor, and the small shared gaps in section 6.
2. QR and Attendance are flows 5–6. The tab bar still shows all three items: Classes, QR, Attendance. QR and Attendance open simple PLACEHOLDER screens (the same `PlaceholderTag`). Classes, Create, and Manage are real screens and do not wear that tag.
3. The profile icon in the top-right of Classes (and of the QR / Attendance placeholders) is visible so the layout matches Figma. Tapping it does nothing in flow 4, and it wears the `PlaceholderTag` (Sheen’s rule that every placeholder is marked). Sign-out and a real account screen come later.
4. Create class opens as a full-screen route that looks like the Figma sheet (title "New class", Cancel, "Save class"). Cancel and a successful Save both pop back to D1. We do not automatically open Manage.
5. There is no edit-class screen. After a class is saved, the coach cannot change its name, days, times, visibility, or end date. To change a class, they cancel a date or cancel the whole class.
6. Debug builds also seed Mara’s sign-in and a mix of extra classes (see section 5). Release builds never include this. The six public classes and Paolo’s memberships from flow 3 do not change. Sunrise TRX Strength stays Public.

**List states, counts, and the private callout**

7. The coach list has only two states: **empty** and **filled**. There is no loading spinner, no skeleton, and no error-with-retry screen. Pull-to-refresh waits for Phase 5. A failed write is not a list state: nothing is saved and an error message is shown (decision 16).
8. Empty (D2): "No classes yet" / "Create your first class. Public classes appear in the class list for everyone." plus a "Create class" button. The tab bar stays. The count and the "YOUR CLASSES" header are hidden.
9. The count reads "1 class" or "N classes" (`UiCopy.classCount`), and is hidden when the list is empty.
10. The callout "Private classes are invite-only" / "Guests and students only see your public classes." shows only when the coach has at least one private class. It is not dismissible. It is hidden on D2.
11. The list is the coach’s own classes, public and private, in creation order (Phase 2 AC61). Ended classes stay on the list (they are still theirs); there is no new Ended chip. Cancelled classes stay on the list and show the Cancelled chip. Opening any of them goes to Manage.

**Creating and managing**

12. Create-class defaults stay as Phase 2 AC62: empty name, Specific days, Public, no end date. "Save class" is enabled only when `canSave` is true (AC64). The D4 / D5 preview callouts are in section 5. A blank name shows "Required." and leaves Save disabled. A slot with start at or after end is not added and shows "The end time must be after the start time." An end date before today shows "End date cannot be in the past." and creates nothing.
13. Approve and Decline on a request have no confirm dialog. Destructive actions use a confirm dialog only, as D8, D13, and D18 show. There is no undo.
14. Invite search runs when the coach submits the field (keyboard Search, or a search action), not on every keystroke. An empty query does not search.
15. A cancelled class still opens Manage, so the coach can see the dates. Approve, Decline, invite, remove, and cancel-a-date are blocked and show "That action is not available." or "This class has been cancelled. Nothing was sent." if the use case already returns that. Class options does not offer "Cancel whole class" again.
16. Offline: the banner sits under the title on Classes and under the header on Create and Manage. Typed values stay in the create form. Create, cancel (a date or the whole class), approve, decline, remove, and invite (search and send) all show "You’re offline. Nothing was saved." and write nothing. There is no auto-retry; the coach taps again when they are back online. Only the daily QR (D20) will work offline, and those screens are out of this flow.
17. Classes reloads whenever the Classes tab is shown, and when we return from Create or Manage (`refresh()`). There is no pull-to-refresh in flow 4.

**Already decided (from Phase 2, flows 2–3, and the 23 Nov team answers; not asked again)**

18. Shared offline logic stays covered by `MutableNetworkMonitor` in shared tests. On Android, we unit-test only the mapping from the system network callback to ONLINE / OFFLINE with a fake signal, no device. On iOS there is no automated test; Sheen checks airplane mode on and off by hand on his Mac.
19. Real network detection ships in this flow, not a separate PR. The `NetworkMonitor` interface stays in shared. Add `AndroidNetworkMonitor` (`androidMain`, `registerDefaultNetworkCallback` in `callbackFlow`, INTERNET + VALIDATED = online) and `IosNetworkMonitor` (`iosMain`, `nw_path_monitor` from `platform.Network`, no Swift glue), each in its own file. Koin in the app binds the platform one. Tests keep `MutableNetworkMonitor`. Android needs `ACCESS_NETWORK_STATE` and must unregister the callback. Both monitors read the real current status at start and never assume ONLINE.
20. Copy already in `UiCopy` stays as written: D2 empty, D9 toast, D11 empty, invite sent, "Invite sent", no-match empty, the offline line, "End date cannot be in the past.", "Cannot cancel a past date.", "That action is not available.", "This class has been cancelled. Nothing was sent.", and "Required." New error lines (question 8) are in section 5.
21. Koin + `koinViewModel`, one NavHost, one-shot `navigation` + `onNavigationHandled()`, Compose UI on both platforms, the offline banner component, and the flow 2 accessibility rule (each class card is one spoken button, every tap target is at least 48dp).
22. The test clock, unless an AC says otherwise, is Mon 28 Sep 2026, 9:00 AM, Asia/Manila — the same pin as flow 3. Cancel-one-date ACs use Mon 5 Oct 2026, as Figma D7–D9 do. The create-class 10-date preview uses the Phase 2 AC65 example (saved Fri 25 Sep 2026).

**Out of flow 4**

23. The real QR and Attendance screens (D19–D22, flows 5–6), student-side changes, sign-out and account deletion, editing a class after it is saved, undo after a confirm, notifications, loading skeletons, pull-to-refresh, and A7 (a coach viewing someone else’s public class detail, which still has no entry point) stay out.

## 3. Scope
**In**
- Coach tab host inside `CoachHome`, replacing the CoachHome placeholder.
- D1–D2 Classes screen (`CoachClassesScreen`).
- D3–D6 Create class screen (`CreateClassScreen`).
- D7–D18 Manage class screen (`ManageClassScreen`), including the three confirm dialogs and class options.
- QR and Attendance placeholder screens, with `PlaceholderTag`.
- The coach tab bar (Classes, QR, Attendance).
- Real `AndroidNetworkMonitor` and `IosNetworkMonitor`, wired only in the app’s Koin.
- Debug demo seed for Mara and the extra class mix (section 5).
- Small shared changes P1–P7 (section 6).
- New UI copy in `UiCopy`.

**Out**
- The real QR and Attendance flows, student changes, sign-out, edit-class, undo, A7, pull-to-refresh, and loading states (decision 23).
- Changing Phase 1 create / cancel / approve / invite use cases, or the flow 3 student screens.
- A separate network-monitor PR (decision 19).
- iOS automated tests for the path monitor (decision 18).

## 4. User stories
- As a signed-in coach, I can see the classes I have created, open one to manage it, and start a new class.
- As a signed-in coach, I can set a class’s name, days, times, visibility, and end date, then save it.
- As a signed-in coach, I can cancel one date or the whole class (after a confirm), approve or decline requests, remove a student (after a confirm), and invite a student by employee ID.
- As a signed-in coach, I can switch between Classes, QR, and Attendance. QR and Attendance are placeholders until later flows.
- As a coach who goes offline, I can still read my list and a class I already have, but writes and invite search are blocked until I am back online.

## 5. Screens and navigation

| Screen | Shows |
|---|---|
| D1 Classes | Eyebrow "TRX ATTENDANCE"; **+** and account icon (account is no action, wears `PlaceholderTag`); title "Classes"; subtitle "Coach {full name}" (so "Coach Mara Santos"). Section "YOUR CLASSES" plus "1 class" / "N classes". One card per class, in creation order: name, Public / Private or Cancelled chip, days line, time line, "1 student" / "N students", and " · 1 request" / " · N requests" when `pendingRequestCount` is greater than 0, plus a chevron. Private callout when decision 10 applies. Tab bar, Classes selected. Offline banner under the title when offline. |
| D2 empty | Same chrome. Empty State: "No classes yet" / "Create your first class. Public classes appear in the class list for everyone." plus "Create class". No section header, no count, no callout. Tab bar stays. |
| D3–D6 New class | Title "New class"; Cancel; field label "Class name", placeholder "e.g. TRX Core Burn". "Repeats": "Specific days" / "Every day", then weekday chips. "Time slots": when no day is selected, "Choose at least one day. Each day gets its own time slots."; when days are selected, a block per day ("Monday", "1 slot" / "N slots", Start / End, "Add time slot"). "Visibility": "Public" / "Private", with "Public: anyone can see it and request to join." or "Private: only you and students on the roster see it. Students join by your invite." "Ends": "No end date" toggle; when the toggle is off, an "End date" row (Choose, or the chosen date). "Save class" (disabled until `canSave`). Offline banner when offline. A form error under the title when Save fails. |
| D4 preview | When `canSave` and an end date is set: callout title "{N} dates will be created"; body "Saving today, {formatDate(today)}: every {day names} from {formatShortDate(first)} to {formatShortDate(last)}. Past dates are not created. The end date is included." The AC65 example is "10 dates will be created" / "Saving today, Fri 25 Sep 2026: every Monday and Wednesday from Mon 28 Sep to Wed 28 Oct 2026. Past dates are not created. The end date is included." |
| D5, no end date | When `canSave` and there is no end date: callout title "No end date"; body "This class keeps repeating until you cancel the whole class. Dates are created from today." The End date picker is hidden. |
| D7 Manage, Schedule | Back "Classes"; ··· (class options). Header: Public or Private chip, "Ends {formatDate(endDate)} · {N} dates" or "No end date · {N} dates", class name, "{days} · {slot}" (middle dot, en dash). Top tabs: Schedule, Requests, Roster, Invite. Requests shows a badge when `pendingRequestCount` is greater than 0, hidden at 0. Callout: "Cancel a single date" / "Only today or a future date can be cancelled. The class keeps repeating." Section "DATES" / "{N} dates". One row per session in session-number order: "SESSION {n}", `formatDate`, time, Scheduled or Cancelled chip; "TODAY" on today; "Past date" on a past Scheduled date; **Cancel date** when `canCancelDate`. Offline banner when offline. |
| D8 confirm cancel date | Dialog: "Cancel {formatShortDate}?" / "Session {n} of {class} will show as Cancelled and won’t accept attendance. {next short date} and every other date stay scheduled." (If there is no later scheduled date, drop that second sentence.) Buttons: Keep date, Cancel date. Keep date dismisses and changes nothing. |
| D9 after cancel | The date’s row is Cancelled, other dates stay Scheduled, and a toast shows `UiCopy.dateCancelled` ("{Mon 5 Oct} cancelled. Other dates stay scheduled."). The toast auto-dismisses after 4 seconds. No undo. |
| D10 / D11 Requests | "WAITING FOR YOU" / "1 request" / "N requests". Each row: name, employee ID, **Decline**, **Approve**. No requested date (see Assumptions). Footnote: "Approve adds the student to the roster. Decline leaves them off; they can request again." Empty: "No requests" / "Students who request this class will appear here." Approve moves the row to Roster at once, no dialog; Decline leaves them off, no dialog. |
| D12 / D13 Roster | "ON THE ROSTER" / "1 student" / "N students". Each row: name, employee ID, "Joined by request" or "Joined by invite" (see Assumptions), **Remove**. Footnote: "A removed student is taken off this class only and can request to join again." Dialog: "Remove {full name}?" / "{first name} will be taken off {class} and can’t record attendance for it. They can request to join again. Past attendance stays as recorded." (Use the student’s first name. "They" replaces Figma’s "She" because the app doesn’t know anyone’s pronouns.) Buttons: Keep, Remove. Keep dismisses and changes nothing. After Remove, the row leaves; past attendance is unchanged. No undo. |
| D14–D16 Invite | Intro: "Search a student by employee ID to invite them to this class." Label "Employee ID". Submit runs the search. One match: "1 match", name, "{employeeId} · Student · Not on this class", **Send invite**. After send: toast "Invite sent to {name}. They’ll see it in Invites.", the button reads "Invite sent" and cannot be sent again, and `canSendInvite` is false. The toast auto-dismisses after 4 seconds. No match: "No student found" / "No account uses {id}. Nothing was sent." Already invited / requested / already on the roster: the Q8 lines in the copy list below, and nothing is sent. |
| D17 / D18 Class options | Sheet title "Class options"; the only action is "Cancel whole class". Dialog: "Cancel {class}?" / "All remaining dates are cancelled and the class stops repeating. It leaves the public class list. Past sessions and their attendance stay as recorded." Buttons: Keep class, Cancel class. Keep class dismisses the dialog and stays on Manage. Cancel class pops back to D1; the class shows as Cancelled. No undo. When the class is already cancelled, this action is not offered (decision 15). |
| Failed write (any of the above) | Nothing is written. A Form Error row shows "You’re offline. Nothing was saved." or the matching error line. There is no Retry button and no failure toast. The create form keeps its typed values. A later successful tap then follows the success row above and clears the error. |
| QR placeholder | Eyebrow "TRX ATTENDANCE"; title "QR"; account icon (wears `PlaceholderTag`); tab bar, QR selected; `PlaceholderTag`. No other body content. |
| Attendance placeholder | Same chrome; title "Attendance"; tab bar, Attendance selected; `PlaceholderTag`. |

**New and existing copy (exact typography: apostrophe U+2019, en dash U+2013, middle dot U+00B7)**

Already in `UiCopy`: D2 empty, D9 toast, D11 empty, invite-sent toast and button, no-match empty, "You’re offline. Nothing was saved.", "End date cannot be in the past.", "Cannot cancel a past date.", "That action is not available.", "This class has been cancelled. Nothing was sent.", "Required.", "1 class" / "N classes", "Public", "Private", "Cancelled", "Scheduled", "No end date".

Add to `UiCopy` (Figma, plus question 8):
- "The end time must be after the start time." (`InvalidSlot`)
- "This student already has an invite." (`AlreadyInvited`)
- "This student already requested this class." (`AlreadyRequested`)
- "This student is already on the roster." (`AlreadyMember`)
- The D1 callout, D3–D6 labels and helpers, D4 / D5 preview callouts, D7 cancel-date callout, D8 / D13 / D18 dialogs, D10 / D12 footnotes, D14 intro, "1 student" / "N students", "1 request" / "N requests", "1 slot" / "N slots", "Save class", "Create class", "Send invite", "Cancel date", "Cancel whole class", and the other chrome strings in the table above.

**Routes** (in `ui.navigation`, one file each):
- `CoachHome` stays the coach root. It is no longer a placeholder. It hosts the three tabs as its own content. There is no second NavHost and no new top-level tab routes.
- `CreateClass` and `ManageClass(classId: String)` are top-level destinations on the same NavHost, pushed from Classes.
- New `AppNavHost` factory parameters are optional and go last, like `classDetailViewModel`: `coachClassesViewModel`, `createClassViewModel`, `manageClassViewModel`.

**Paths**
```
Sign-in / launch as coach:   [CoachHome]  ->  Classes tab (D1 or D2)
D1 +  or D2 Create class:    [CoachHome, CreateClass]
Cancel or successful Save:   pops one level, Classes tab, list refreshed
D1 tap card:                 [CoachHome, ManageClass(id)]
Back on Manage:              pops one level, Classes tab, list refreshed
Cancel whole class:          navigation = Back, same pop, class shows Cancelled
Tabs:                        switch Classes / QR / Attendance inside CoachHome; the back stack does not change
```
- Tapping a D1 card twice quickly adds only one `ManageClass` (`launchSingleTop`). Opening Create twice quickly adds only one `CreateClass`.
- The starting tab is Classes.
- `CoachClassesViewModel` is bound in `ViewModelModule` (Koin in `composeApp` only) and scoped to the `CoachHome` back-stack entry, so it survives tab switches. Call `refresh()` each time the Classes tab is shown, and when we return from Create or Manage.
- `CreateClassViewModel` and `ManageClassViewModel` are also bound in `ViewModelModule` and created for their destinations.
- `placeholder_tag` moves off `CoachHome` and onto the account button plus the QR and Attendance placeholders only. The coach-home root keeps a stable test tag (`coach_home`).

**Debug demo seed:** extend the existing debug-only `DemoSeed` (it still runs once at startup for classes, only when there are no classes). Release builds never include it. Keep the six public classes and Paolo’s memberships exactly as flow 3 left them (Sunrise stays Public). Also:

| Who | Role |
|---|---|
| Mara Santos | Coach of Sunrise TRX Strength, Lunch Express TRX, and TRX Core Burn, plus the extra mix below. Sign-in is documented in the PR (see Assumptions for the draft email and password). The app does not sign in as Mara on its own. |
| Paolo Reyes, EMP-20417 | Unchanged from flow 3. |

Add to Mara, after the original six so their creation order does not change: one **private** class with **no students**, and one **cancelled** class. The original three Mara classes already cover **active** and **no end date**. A guest on a fresh debug install still sees the same six public classes.

The Figma D1 snapshot (Sunrise as Private, "3 classes", "12 students") and the D7–D16 snapshot (four roster students, three requests, Felix, Gia, cancel Mon 5 Oct) are **not** this seed. Those are covered by automated tests using the example data in section 7.

## 6. Shared changes (each gets a `phase2JvmTest`; new constructor parameters go last, with defaults)

- **P1. Classes refresh.** `CoachClassesViewModel.refresh()` reloads `classes` and `isEmpty` from the repositories, and keeps the current `connection`. Test: create a class for this coach in the repository, call `refresh()`, and the new class is in `classes`; a class created for a different coach is not.
- **P2. Card helpers.** Add `dayLine()`, `timeLine()`, `metaLine()`, and `spokenCard()` on `CoachClassItem` in the same pattern as `GuestClassItem` / `StudentClassItem`. `metaLine()` is "0 students" / "1 student" / "N students", and when `pendingRequestCount` is greater than 0 it appends " · 1 request" / " · N requests". Test: a published item matches the D1 lines ("Mon · Wed", "5:30 PM – 6:30 PM", "4 students · 3 requests"; "12 students" when there are no requests).
- **P3. Empty invite search.** `ManageClassViewModel.onSearch()` does nothing when the trimmed query is empty: it does not set `noMatch`, does not change `match`, and does not set an error. Test: a blank or whitespace query leaves the state unchanged; a real unknown id still sets `noMatch`.
- **P4. Open error lines.** Add `UiError.InvalidSlot`, `AlreadyInvited`, `AlreadyRequested`, and `AlreadyMember`. `UiCopy.uiError` returns the question 8 strings. `CreateClassViewModel` can keep using `slotError` for a bad slot; the screen shows the InvalidSlot line. `onSendInvite()` sets `error` to the matching new value for AlreadyInvited / AlreadyRequested / AlreadyMember (and still updates the match status, and still creates nothing). Test: those four typed values map to the four strings; sending an invite to an already-invited student sets `error = AlreadyInvited` and does not add a membership.
- **P5. Cancelled class in Manage.** `ManageClassUiState` gains `isCancelled: Boolean = false` (last, with a default). It is true when the class is cancelled. The UI uses it to hide "Cancel whole class" (and to hide ··· if that would leave an empty menu). `canCancelDate` is already false in this case. Test: after `CancelClass`, a new `ManageClassViewModel` on that id has `isCancelled = true` and every row has `canCancelDate = false`.
- **P6. Confirmation and options handlers.** `ManageClassViewModel.onConfirmationHandled()` sets `confirmation = null`. `onCloseOptions()` sets `optionsOpen = false` and `confirmCancelClass = false`. Test: after a successful cancel-date, `confirmation` is `DateCancelled`; calling `onConfirmationHandled()` clears it. After `onOpenOptions()` and `onCancelWholeClass()`, `onCloseOptions()` / `onKeepClass()` leave the class unchanged.
- **P7. Roster join path.** `RosterRow` gains `joinedBy: String = ""` as the last parameter. Fill it from membership history for that student (the records are already kept, oldest first): "Joined by request" if they became `ACTIVE` from `PENDING_REQUEST`, "Joined by invite" if they became `ACTIVE` from `INVITED`. Test: an approved request shows "Joined by request"; an accepted invite shows "Joined by invite".

Real `NetworkMonitor` actuals are in decisions 18–19 and the ACs below, not as a `phase2JvmTest`. Shared tests keep `MutableNetworkMonitor`. The new mapping test is `androidUnitTest` (F4-AC34).

## 7. Acceptance criteria
Unless an AC says otherwise, tests use a clock fixed on **Mon 28 Sep 2026**, 9:00 AM, Asia/Manila.

**D1 example data** (the list): coach Mara Santos; her classes in creation order are TRX Core Burn (Public, Mon · Wed, 5:30 PM – 6:30 PM, 4 students · 3 requests), Lunch Express TRX (Public, Mon · Tue · Thu, 12:15 PM – 12:45 PM, 8 students), Sunrise TRX Strength (Public, Mon · Wed · Fri, 6:00 AM – 6:45 AM, 12 students), plus one private class with no students and one cancelled class, both created after Sunrise.

**Manage example data** (Core Burn as in D7–D16): TRX Core Burn, Public, ends Sat 31 Oct 2026, 10 dates, Mon · Wed, 5:30 PM – 6:30 PM. Roster, in join order: Paolo Reyes (EMP-20417, Joined by request), Dana Uy (EMP-20155, Joined by invite), Gia Torres (EMP-20249, Joined by invite), Hana Cho (EMP-20877, Joined by request). Requests: Ella Navarro (EMP-20611), Bea Lim (EMP-20388), Carlo Diaz (EMP-20502). Felix Ong (EMP-20733) is a student who is not on this class.

**Create example data** (Phase 2 AC65 / Figma D4): saved Fri 25 Sep 2026, name "TRX Core Burn", Specific days, Monday and Wednesday, 5:30 PM – 6:30 PM, Public, end date Sat 31 Oct 2026.

**Classes (D1–D2)**

- **F4-AC1.** With the D1 example data, D1 shows the title "Classes", the subtitle "Coach Mara Santos", "YOUR CLASSES", the count "5 classes", and five cards in creation order: TRX Core Burn (Public, "4 students · 3 requests"), Lunch Express TRX (Public, "8 students"), Sunrise TRX Strength (Public, "12 students"), the private class (Private, "0 students"), and the cancelled class (Cancelled chip). The private callout is visible. The tab bar shows Classes, QR, and Attendance, with Classes selected. There is no loading spinner.
- **F4-AC2.** The count reads "1 class" for one class and "5 classes" for five.
- **F4-AC3.** With no classes, `coach_classes_empty` shows the D2 title and body already in `UiCopy`, a "Create class" button, no section header, no count, and no private callout. The tab bar is still showing. Tapping "Create class" or the **+** opens `CreateClass`.
- **F4-AC4.** With only public (and cancelled public) classes, the private callout is hidden. Adding a private class and calling `refresh()` shows the callout. The callout cannot be dismissed.
- **F4-AC5.** An ended class stays on the list and opens `ManageClass(id)`. A cancelled class stays on the list, shows the Cancelled chip, and opens `ManageClass(id)`.
- **F4-AC6.** Offline: `offline_banner` shows under the title, the cards still show, and tapping a card still opens `ManageClass`. Tapping **+** still opens `CreateClass`.
- **F4-AC7.** Refresh: on the D1 example, save a new class in the repository (or return from Create), select the Classes tab (or call the same `refresh()` the tab uses), and the new class is in the list and the count has gone up by one. After confirming cancel-whole-class on Core Burn and returning to D1, Core Burn shows the Cancelled chip.
- **F4-AC8.** Tapping a card opens `ManageClass(id)`. Tapping it twice quickly still gives one `ManageClass` entry. Back (`manage_class_back`, "Classes") returns to D1 with the tab bar visible.
- **F4-AC9.** The account icon is visible on D1 and on the QR / Attendance placeholders and wears `placeholder_tag` (inside the account button: `coach_classes_account` on D1). Tapping it does not add a back-stack entry and does not change the current tab.
- **F4-AC10.** Each D1 card is one merged node with a spoken description in the same pattern as flow 2 and a height of at least 48dp.

**Create class (D3–D6)**

- **F4-AC11.** Opening Create shows "New class", Cancel, an empty "Class name" field, Specific days selected, no days selected, Public selected, the Public helper, the no-end-date toggle on (End date picker hidden), and "Save class" disabled. Cancel pops back to D1 and creates nothing. Opening Create twice quickly adds only one `CreateClass` entry.
- **F4-AC12.** With the create example data and the clock on Fri 25 Sep 2026, `canSave` is true, the D4 callout shows "10 dates will be created" and the AC65 body in the table above, and Save writes the class, pops to D1, and the new class is in the list after `refresh()`.
- **F4-AC13.** The same form with no end date shows the D5 callout ("No end date" / "This class keeps repeating until you cancel the whole class. Dates are created from today.") and hides the End date picker.
- **F4-AC14.** A slot with start at or after end is not added and shows "The end time must be after the start time." A blank name shows "Required." and leaves Save disabled. Saving with an end date before today shows "End date cannot be in the past." and creates nothing; typed values stay.
- **F4-AC15.** Offline Save (form already valid): the error shows "You’re offline. Nothing was saved.", nothing is written, typed values stay, and there is no auto-retry. Going online and tapping "Save class" again follows F4-AC12 (or F4-AC13) and clears the error.
- **F4-AC16.** Manage has no control that changes the class’s name, days, times, visibility, or end date.

**Manage (D7–D18)**

- **F4-AC17.** With the manage example data and the clock on Mon 5 Oct 2026, Schedule shows the Public chip, "Ends Sat 31 Oct 2026 · 10 dates", "TRX Core Burn", "Mon · Wed · 5:30 PM – 6:30 PM", the four tabs with Requests badge "3", the cancel-date callout, and the date rows: Sessions 1–2 are past ("Past date", no Cancel date); Session 3 is today ("TODAY", Cancel date); later rows have Cancel date. The Schedule tab is selected.
- **F4-AC18.** Cancel date on Mon 5 Oct: the D8 dialog shows "Cancel Mon 5 Oct?" and the body in the table above (Session 3, Mon 12 Oct stays scheduled). Keep date dismisses it and changes nothing. Cancel date dismisses it, Session 3 becomes Cancelled, Session 5 (Mon 12 Oct) stays Scheduled, and the toast shows "Mon 5 Oct cancelled. Other dates stay scheduled." After 4 seconds the toast is gone. There is no undo.
- **F4-AC19.** With the manage example data, Requests shows "WAITING FOR YOU", "3 requests", Ella, Bea, and Carlo, Decline and Approve on each row, and the D10 footnote. There is no requested date on the row. Approve on Ella: no dialog; Ella leaves Requests at once and appears on Roster. Decline on Bea: no dialog; Bea leaves Requests and does not appear on Roster. After both, the badge reads "1". D11: with no requests, `manage_requests_empty` shows the D11 copy already in `UiCopy`.
- **F4-AC20.** Roster shows "ON THE ROSTER", "4 students", the four students in join order with the join-path line, Remove, and the D12 footnote. Remove on Gia Torres: the D13 dialog shows "Remove Gia Torres?" and the body in the table above. Keep dismisses it and changes nothing. Remove dismisses it, Gia leaves the roster, the count reads "3 students", and past attendance is unchanged. There is no undo.
- **F4-AC21.** Invite: the intro in the table above, label "Employee ID". Submitting "EMP-20733" shows "1 match", "Felix Ong", "EMP-20733 · Student · Not on this class", and Send invite. Send invite shows the invite-sent toast already in `UiCopy`, the button reads "Invite sent", and Felix cannot be invited again. After 4 seconds the toast is gone. Submitting an unknown id shows the no-match copy already in `UiCopy`. A blank query does not search and does not show no-match. Sending to a student who is already invited, who already requested, or who is already on the roster shows the matching Q8 line and creates nothing.
- **F4-AC22.** Class options shows "Cancel whole class" only. The D18 dialog shows "Cancel TRX Core Burn?" and the body in the table above. Keep class dismisses it and stays on Manage. Cancel class pops to D1, and Core Burn shows the Cancelled chip. There is no undo.
- **F4-AC23.** Opening a cancelled class still shows Manage and the cancelled dates. Cancel date, Approve, Decline, Remove, Send invite, and Invite search are blocked: the error is "That action is not available." or "This class has been cancelled. Nothing was sent." if the use case returns that. "Cancel whole class" is not offered.
- **F4-AC24.** Offline on Manage (clock and data as in F4-AC17 / F4-AC21): the banner shows. Cancel date, Approve, Decline, Remove, invite search, Send invite, and Cancel class each show "You’re offline. Nothing was saved." and write nothing (Cancel date does not open the dialog). Typed search text stays. Going online and retrying then follows the matching success AC and clears the error.

**Tabs, navigation, network, and build**

- **F4-AC25.** The tab bar has exactly three items, in this order, with these labels: Classes, QR, Attendance. QR shows the title "QR" and `placeholder_tag`. Attendance shows the title "Attendance" and `placeholder_tag`. On Classes, Create, and Manage, the only `placeholder_tag` is the one inside the account button on Classes (F4-AC9); the screen body has none. QR and Attendance also have the account button’s tag.
- **F4-AC26.** Signing in as a coach lands on `CoachHome`, Classes tab, D1 or D2. The back stack after opening Create is `[CoachHome, CreateClass]`; after opening a class it is `[CoachHome, ManageClass(id)]`. Switching tabs does not change the back stack.
- **F4-AC27.** Source scan: `DemoSeed`, "Mara Santos", and "mara@" appear only under a debug source set. No file under `*/src/main`, `*/src/commonMain`, `*/src/androidMain`, `*/src/iosMain`, or `*/src/*release*` mentions them. (As in F3-AC21: "password1" is not scanned, because existing sign-in hints in commonMain already use it.) The seed checks may read source text, since `DemoSeed` uses global Koin and androidApp has no test source set. Paolo’s existing debug-only scan (F3-AC21) still passes.
- **F4-AC28.** Source scan: each new route, screen, placeholder, tab-bar, or test-tag file holds one public type. No nested classes. The test checks the files it names; Reviewer checks any other new file by eye.
- **F4-AC29.** The app’s Koin binds `AndroidNetworkMonitor` or `IosNetworkMonitor`, not `MutableNetworkMonitor`. Tests still use `MutableNetworkMonitor`. Both real monitors read the current status at start and never assume ONLINE. Android declares `ACCESS_NETWORK_STATE` and unregisters the callback. Existing shared offline tests still pass.
- **F4-AC30.** `androidUnitTest` (no device): a fake network callback that is INTERNET + VALIDATED maps to `ONLINE`; a callback that is not (lost network, or INTERNET without VALIDATED) maps to `OFFLINE`.
- **F4-AC31.** All existing suites still pass. The only test edits allowed are any that required `placeholder_tag` on CoachHome, or that treated CoachHome as the old placeholder body.

**Manual checks** (Reviewer / Sheen; these are not ACs):
- A fresh debug install still starts as a guest. The guest list still shows the six seeded public classes. Relaunching does not add them again.
- Sign in as Mara with the documented demo account. D1 shows her original three public classes plus the private empty class and the cancelled class. Sunrise is still Public. The private callout is visible.
- Open Create, cancel, and return to D1. Open Create, save a public class, and see it on D1. There is no edit control on Manage.
- Open Core Burn (or a test build with the manage example data): Schedule, Requests (approve / decline), Roster (remove confirm, then Keep), Invite (match, send, no-match). Cancel one date (Keep, then Cancel) and cancel the whole class (Keep, then Cancel) on a disposable class.
- The profile icon does nothing. QR and Attendance show the PLACEHOLDER pill.
- Airplane mode on Android and on iOS (Sheen, on his Mac for iOS): the banner shows; Save / Approve / invite search show "You’re offline. Nothing was saved." and write nothing; typed create-class values stay; turning the radio back on and retrying then saves. The real QR screen is out of this flow, so it is not part of this check.

## 8. Test plan
| What | Suite | ACs or items |
|---|---|---|
| Shared changes | `:shared:phase2JvmTest` (new tests only) | P1–P7 |
| Existing shared ViewModel / use-case tests | `:shared:jvmTest` and `:shared:phase2JvmTest` | regression; F4-AC31 |
| D1–D2, D3–D6, D7–D18, tab, and placeholder screen tests (stateless screens + ViewModels with fakes, `v2.runComposeUiTest`) | `:composeApp:jvmTest` | F4-AC1–25 |
| Navigation on `AppNavHost` (ViewModel factories, test NavController) | `:composeApp:jvmTest` | F4-AC26 |
| Source scans | `:composeApp:structureJvmTest` | F4-AC27, F4-AC28 |
| App Koin binds the real monitor; tests keep the fake | `:composeApp:structureJvmTest` or `:composeApp:jvmTest` | F4-AC29 |
| Android callback → ONLINE / OFFLINE mapping (fake signal, no device) | `androidUnitTest` | F4-AC30 |
| Regression | all of the above | F4-AC31 |
| Airplane mode on Android and iOS; Mara demo sign-in | manual (Sheen / Reviewer) | not ACs |

Test tags (snake_case, one object per screen: `CoachClassesTestTags`, `CreateClassTestTags`, `ManageClassTestTags`, `CoachTabBarTestTags`):
- D1–D2: `coach_home`, `coach_classes_title`, `coach_classes_subtitle`, `coach_classes_account`, `coach_classes_add`, `coach_classes_header`, `coach_classes_count`, `coach_class_card_<id>`, `coach_classes_empty`, `coach_classes_empty_create`, `coach_classes_callout`
- Create: `create_class_title`, `create_class_cancel`, `create_class_name`, `create_class_save`, `create_class_preview`, `create_class_form_error`
- Manage: `manage_class_back`, `manage_class_options`, `manage_tab_schedule`, `manage_tab_requests`, `manage_tab_roster`, `manage_tab_invite`, `manage_tab_requests_badge`, `manage_date_row_<iso>`, `manage_cancel_date_<iso>`, `manage_requests_empty`, `manage_approve_<id>`, `manage_decline_<id>`, `manage_remove_<id>`, `manage_invite_search`, `manage_invite_send`, `manage_invite_empty`, `manage_toast`, `manage_form_error`, `manage_dialog`, `manage_dialog_keep`, `manage_dialog_confirm`
- Tabs: `coach_tab_classes`, `coach_tab_qr`, `coach_tab_attendance`
- Placeholders: existing `placeholder_tag`; `coach_qr`, `coach_attendance` on the placeholder roots
- Shared: `offline_banner`

## 9. Assumptions
These are the only points not fixed by the approved answers, Figma, or existing code:

1. **List order is creation order** (Phase 2 AC61), not the visual order on Figma D1 (Sunrise first). Tests use creation order. The D1 pretty picture (Sunrise as Private, three classes, large roster counts) is not the demo seed.
2. **Request rows drop the "Requested {date}" line**, because memberships have no timestamp (the same reason flow 2 dropped A4’s Requested line and flow 3 dropped the invite date). The row shows the student name and employee ID only.
3. **"Joined by request" / "Joined by invite"** is inferred from membership history (the record that preceded `ACTIVE`). If history is missing, the line is omitted.
4. **Create class starts with no end date** (Phase 2 AC62): the no-end-date toggle is on and the End date picker is hidden, as in D5. Figma D3 shows the picker variant of an incomplete form; that is not the start state.
5. **A manage header with no end date** reads "No end date · {N} dates". The Figma "10 dates · 4 more below" line is a scroll hint and is not required in tests; tests assert "DATES" and "{N} dates".
6. **The D13 body says "They can request to join again."** instead of Figma’s "She", because the app has no pronoun data. Designer updates the frame text.
7. **Form errors** use the existing Form Error row, under the title (Create) or under the manage header, above the body. There is no Retry action, because decision 7 has no error-with-retry state.
8. **Demo sign-in** (draft, change if you prefer): email `mara@company.com`, password `password1`, name Mara Santos. Document the final values in the PR. The app never auto-signs in as Mara.
9. **The three tabs live inside `CoachHome`**, not as their own NavHost destinations. QR and Attendance placeholders reuse the large-title chrome (eyebrow, title, account icon, tab bar) and otherwise only show `PlaceholderTag`.
10. **A cancelled card** shows the Cancelled chip and hides the Public / Private chip, so the cancelled state is obvious.
11. **Toasts** auto-dismiss after 4 seconds, the same as flow 3.
12. **Time and date pickers** on Create class are the platform pickers.

## 10. Open notes for later
A later sign-out must pop to `graph.id` inclusive, because `RootViewModel.startRoot` is captured once. If sign-out only pops the current screen, the next sign-in can reuse the old start destination.

Optional later cleanup, not in this flow: drop the `onNavController` parameter on `AppNavHost`. Watch whether TalkBack still reads the student-home greeting twice (PR #11 should-fix).

## 11. Known gaps (on purpose)
- The real QR and Attendance screens, student-side changes, sign-out, edit-class, undo, invite / request timestamps, loading skeletons, and pull-to-refresh wait for later phases.
- iOS path-monitor behaviour is a manual airplane-mode check only (decision 18).
