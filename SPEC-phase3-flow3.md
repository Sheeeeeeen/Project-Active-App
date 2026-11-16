# TRX Class Attendance — Phase 3, Flow 3 Spec (student home, invites, and requests)

## 1. Status
**Approved by Sheen on 16 Nov 2026, 8:33 PM (Asia/Manila).** Sheen approved all 16 recommended answers in `/workspace/flow3-questions.md` as written, on 16 Nov 2026 at 7:21 PM (Asia/Manila). This spec turns those answers, the already-decided items, Figma, and the code on `main` into a build plan. Tester writes the failing tests on a branch from `main`, with this spec as the first commit.

- Baseline: `main` at `486593d` (flow 2 / PR #9 merged). `StudentHome` is still a placeholder.
- Design: Figma `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", section C `25:4903` (C1 `22:4010`, C2 `22:4317`, C3 `22:4482`, C4 `22:4614`, Dev Notes `22:4310`, `22:4475`, `22:4607`, `22:4705`). Class detail states A3 `22:910`, A4 `22:1004`, A5 `22:1165`, and A6 `22:1266` were already specified in flow 2 and are only wired from C1 here. Details are in `/workspace/flow3-questions.md` and `/workspace/flow3-context.md`.
- Package: `ph.project.active`. Code rule: one public type per file, no nested classes. Koin only in `composeApp`. One NavHost. One-shot `navigation` plus `onNavigationHandled()`.

## 2. Decisions we made for you (veto any line)

**What’s in, and what is not a Requests screen**

1. Flow 3 builds the signed-in student **Classes** home (C1), the **Invites** tab (C2–C4), and the four-item tab bar. Class detail is already built; C1 is now the way a student opens it.
2. There is **no student Requests screen**, no Requests tab, and no Requests section. A pending request is a **Pending** chip on a card in C1’s **Public classes** section, not in My classes. Tapping that card opens A4. An invited public class also lives in Public classes (Invited chip) and opens the Invited detail from flow 2. **My classes is Joined only.**
3. Attend and Sessions are flow 5. The tab bar still shows all four items: Classes, Attend, Sessions, Invites. Attend and Sessions open simple PLACEHOLDER screens (the same `PlaceholderTag` used on today’s StudentHome placeholder). Classes and Invites are real screens and do not wear that tag.
4. The profile icon in the top-right is visible so the layout matches Figma. Tapping it does nothing in flow 3. Sign-out and a real account screen come later.
5. A student cannot leave a class in flow 3. They stay Joined unless a coach removes them (flow 4) or the class is cancelled.
6. Debug builds also seed the C1 story and a demo student, Paolo, so a manual check can walk Classes and Invites. Release builds never include this. See section 4.

**List states, empty copy, and counts**

7. Each of **My classes**, **Public classes**, and **Invites** has only two states: **empty** and **filled**. There is no loading spinner, no skeleton, and no error-with-retry screen. Pull-to-refresh waits for Phase 5. A failed Accept or Decline is not a list state: the invite stays on the list and an error message is shown (decision 18).
8. Hide a C1 section (and its count) when that section has no cards. If both My classes and Public classes are empty, show an Empty State: "No classes yet" / "When you join a class, or when a coach creates a public class, it shows up here." The tab bar stays.
9. The My classes count reads "1 joined" or "N joined", and is hidden with that section when the count is 0. Public classes has a header and no count.

**What the cards show, and in what order**

10. Hide ended classes from Public classes, the same way the guest list already does. Keep an ended class in My classes if the student is still Joined; opening it shows the ended detail from flow 2. Cancelled classes stay out of My classes and Public classes. A private class the student is not Joined on never appears on C1. Outstanding invites to a cancelled class still appear in Invites (Phase 2 decision 3).
11. Sorting: **My classes** by next session time, soonest first. **Public classes** in creation order — Pending and Invited cards sort **with the rest of Public classes**, not in a separate group or a different order. **Invites** newest first, by the order the invite was stored (the membership id), because memberships have no date.
12. The invite line is "Coach {name} invited you." The date Figma shows ("· Sun 27 Sep") is dropped, for the same reason flow 2 dropped A4’s "Requested" line. A Private invite shows a Private chip.
13. Classes and Invites reload whenever that tab is shown (`refresh()`). There is no pull-to-refresh in flow 3.
14. Offline: the banner sits under the title on Classes and on Invites, the lists still show, and you can open a class from C1. Accept and Decline stay visible; if you tap them offline, nothing is saved and you see "You’re offline. Nothing was saved."

**Invites actions**

15. Accept is as Figma C3 and Phase 2 already specified: the card leaves Invites at once, a toast says "You joined {class}. It’s now in My classes.", the student stays on Invites (no extra confirm screen), the badge drops, and the class shows in My classes as Joined. If none are left, C4 is shown. The toast auto-dismisses after 4 seconds.
16. Decline has no confirmation dialog. The card leaves at once, a toast says "Invite declined" and auto-dismisses after 4 seconds. If none are left, show C4.
17. Tapping the invite card body does nothing. Only Accept and Decline are tappable. To read the class, the student uses the Invited card on Classes.
18. If Accept or Decline fails (offline, or the action is rejected), the invite stays on the list and an error is shown, using the existing messages ("You’re offline. Nothing was saved." or "That action is not available."). There is no failure toast and no Retry button. A later successful tap then follows 15 or 16.

**Already decided (from flow 2 and Phase 2; not asked again)**

19. Class detail keeps every state already built in flow 2. From C1: a public card with no chip opens A3; Pending opens A4; Invited opens the Invited state (flow 2 decision 19); a My classes card opens A6. After a decline or a removal, A5 still uses the flow 2 decision 17 message: "You can send a new request. The coach will need to approve it again." The student can request again straight away, with no waiting period.
20. No tab bar on any class detail screen (flow 2 decision 22). After sign-up or sign-in from a class, `StudentHome` still pushes class detail once, as in flow 2. Back from detail returns to Classes (C1).
21. Accept and Decline are online-only (Phase 2 AC22). The Invites badge is the pending invite count and is hidden at zero (C4). C4 empty copy, the accept toast, and the A3–A6 / Invited strings are already in `UiCopy`.
22. Koin + `koinViewModel`, one NavHost, one-shot `navigation` + `onNavigationHandled()`, Compose UI on both platforms, the offline banner component, and the flow 2 accessibility rule (each card is one spoken button, every tap target is at least 48dp).

**Out of flow 3**

23. The real Attend and Sessions screens (C5–C15), coach create / manage / approve (flow 4), QR and attendance (flows 5–6), sign-out and account deletion, notifications, a student leaving a class, invite dates, loading skeletons, pull-to-refresh, and any change to the A5 message stay out.

## 3. Scope
**In**
- Student tab host inside `StudentHome`, replacing the StudentHome placeholder.
- C1 Classes screen (`StudentClassesScreen`).
- C2–C4 Invites screen (`InvitesScreen`).
- Attend and Sessions placeholder screens, with `PlaceholderTag`.
- The student tab bar (Classes, Attend, Sessions, Invites) and the Invites badge.
- Wiring C1 cards to the class-detail states already built in flow 2.
- Debug demo seed for Paolo and the C1 story.
- Small shared changes P1–P7 (section 6).
- New UI copy in `UiCopy`.

**Out**
- A student Requests screen or tab (decision 2).
- The real Attend and Sessions flows, coach screens, QR, attendance, sign-out, leaving a class, invite timestamps, pull-to-refresh, and loading states (decision 23).
- Changing the Phase 1 accept / decline / request use cases, or the A5 copy.
- iOS device checks (Phase 4).

## 4. User stories
- As a signed-in student, I can see the classes I’ve joined and the public classes I can still request, and I can open a class to request to join, see that I’m waiting, or see that I’ve joined.
- As a signed-in student, I can see invites from coaches and accept or decline them.
- As a signed-in student, I can switch between Classes, Attend, Sessions, and Invites. Attend and Sessions are placeholders until flow 5.

## 5. Screens and navigation

| Screen | Shows |
|---|---|
| C1 Classes | Eyebrow "TRX ATTENDANCE"; account icon (no action); title "Classes"; subtitle "Hi {name} · {employeeId}". Section "MY CLASSES" plus "1 joined" / "N joined"; one card per Joined class (name, Joined chip, "Coach {name}", "Mon · Wed", time line, chevron). Section "Public classes" (no count); one card per public class the student is not Joined on, with an Invited chip, a Pending chip, or no chip, plus the same coach / days / time / chevron. Tab bar, Classes selected. Offline banner under the title when offline. |
| C1, one section empty | That section and its count are hidden. The other section still shows. Tab bar stays. |
| C1, both sections empty | Empty State: "No classes yet" / "When you join a class, or when a coach creates a public class, it shows up here." No section headers and no count. Tab bar stays. |
| C2 Invites, filled | Title "Invites"; subtitle "Accept to join a class roster."; account icon (no action). One card per outstanding invite: class name, "Coach {name} invited you." (no date), Public or Private chip, days, time, **Decline** and **Accept**. Tab bar, Invites selected, badge = pending count. Offline banner under the title when offline. |
| C3 after Accept | Same as C2, with the accepted card gone, the badge dropped, and a toast: "You joined {class}. It’s now in My classes." The student stays on Invites. The toast auto-dismisses after 4 seconds. |
| After Decline | Same list behaviour as accept (card gone, badge dropped, C4 if none left), with toast "Invite declined", auto-dismiss after 4 seconds. No confirmation dialog. |
| C4 Invites, empty | Title "Invites" only (no subtitle). Empty State: "No invites yet" / "When a coach invites you to a class, it shows up here. You can accept or decline it." Tab bar, Invites selected, no badge. |
| Failed Accept or Decline | The invite stays on the list. A Form Error row under the title shows "You’re offline. Nothing was saved." or "That action is not available." No toast and no Retry button. |
| Attend placeholder | Eyebrow "TRX ATTENDANCE"; title "Attend"; account icon; tab bar, Attend selected; `PlaceholderTag`. No other body content. |
| Sessions placeholder | Same chrome; title "Sessions"; tab bar, Sessions selected; `PlaceholderTag`. No other body content. |
| Class detail | Unchanged from flow 2. No tab bar. Back label "Classes" pops to C1. |

**Routes** (in `ui.navigation`, one file each):
- `StudentHome(openClassId: String? = null, joinOutcome: JoinOutcome? = null)` stays the student root. It is no longer a placeholder. It hosts the four tabs as its own content. There is no second NavHost and no new top-level tab routes, so the flow 2 back stack stays the same.
- `ClassDetail` stays a top-level destination, pushed on the same NavHost.
- No new Requests route.

**Paths**
```
Sign-in / launch as student:  [StudentHome]  ->  Classes tab (C1)
C1 tap card:                  [StudentHome, ClassDetail(id)]
Back on ClassDetail:          pops one level, Classes tab and tab bar are showing
Sign-up from a class (flow 2): [StudentHome(id, outcome)] -> pushes once -> [StudentHome, ClassDetail(id, outcome)]
Tabs:                         switch Classes / Attend / Sessions / Invites inside StudentHome; the back stack does not change
```
- Tapping a C1 card twice quickly adds only one `ClassDetail` (`launchSingleTop`).
- `StudentHome` still pushes `ClassDetail` from `openClassId` only once, as in flow 2.
- The starting tab is Classes.
- `StudentClassesViewModel` and `InvitesViewModel` are bound in `ViewModelModule` (Koin in `composeApp` only) and scoped to the `StudentHome` back-stack entry, so they survive tab switches. Call `refresh()` each time that tab is shown.
- The C1 root keeps testTag `student_home`. The subtitle includes the account name, so existing name checks can still pass. `placeholder_tag` moves off StudentHome and onto Attend and Sessions only.

**Debug demo seed:** extend the existing debug-only `DemoSeed` (it still runs once at startup, only when there are no classes). Release builds never include it. It now also sets up the C1 story:

| Who | Role |
|---|---|
| Mara Santos | Coach of Sunrise TRX Strength, Lunch Express TRX, TRX Core Burn |
| Leo Ramirez | Coach of TRX Mobility Flow |
| Jun Tan | Coach of TRX Every Day 20 |
| Ana Cruz | Coach of Weekend TRX Bootcamp |
| Paolo Reyes, EMP-20417 | Demo student. Sign-in is documented in the PR (see Assumptions for the draft email and password). The app does not sign in as Paolo on its own. |

Classes, in creation order (the original five stay ahead of Sunrise, so the guest list order of those five does not change, only their times and coaches are aligned with Figma):

1. TRX Core Burn — Mara, Public, Mon · Wed, 5:30 PM – 6:30 PM
2. Lunch Express TRX — Mara, Public, Mon · Tue · Thu, 12:15 PM – 12:45 PM
3. TRX Mobility Flow — Leo, Public, Tue · Thu, 7:00 PM – 8:00 PM
4. TRX Every Day 20 — Jun, Public, Every day, 6:30 AM – 6:50 AM
5. Weekend TRX Bootcamp — Ana, Public, Sat, 8:00 AM – 9:30 AM
6. Sunrise TRX Strength — Mara, Public, Mon · Wed · Fri, 6:00 AM – 6:45 AM

Paolo’s memberships (the C1 snapshot): `ACTIVE` on Sunrise, Lunch Express, and Core Burn; `INVITED` on Mobility Flow; `PENDING_REQUEST` on Bootcamp; none on Every Day 20. After signing in as Paolo, C1 matches Figma, and Invites has **one** card (Mobility Flow), badge 1. Figma C2’s two-invite snapshot is not this seed — it is covered by automated tests (see Assumptions).

A guest on a fresh debug install now sees six public classes, not five. Document that in the PR.

## 6. Shared changes (each gets a `phase2JvmTest`; new constructor parameters go last, with defaults)

- **P1. Card fields.** `StudentClassItem` gains `coachName: String = ""` and `slotsByWeekday: Map<DayOfWeek, List<TimeSlot>> = emptyMap()` as the last parameters, so existing tests compile. The ViewModel fills both from the class and the coach’s `PersonProfile`. Add `dayLine()`, `timeLine()`, and `spokenCard()` in the same pattern as `GuestClassItem`. Test: a published item has the class’s coach name and slots, and the helpers match the C1 lines ("Mon · Wed", "5:30 PM – 6:30 PM").
- **P2. Classes refresh.** `StudentClassesViewModel.refresh()` reloads `myClasses` and `publicClasses` from the repositories. Test: make a public invited class `ACTIVE`, call `refresh()`, and the class has moved to `myClasses` with chip `Joined` and is gone from `publicClasses`.
- **P3. Ended rule on Public classes.** `StudentClassesViewModel` takes `clock: Clock = Clock.System` and `timeZone: TimeZone = TimeZone.currentSystemDefault()` as the last parameters. It hides a public class when `hasEnded(today)` is true. An ended class the student is still `ACTIVE` on stays in `myClasses`. Test: with a fixed clock, a public class that ended yesterday is not listed, one that ends today is listed, and an ended Joined class remains in `myClasses`.
- **P4. Sorting.** `myClasses` is ordered by next session time, soonest first. `publicClasses` stays in `allClasses()` creation order — a Pending or Invited class keeps its place among the other public cards, not a separate group. `InvitesViewModel` lists invites newest first, by membership id descending (the order the invite was stored). Add a top-level `nextSessionStart(...)` in a new file: the earliest session start at or after local midnight today, honouring `endDate`, or `null` if there is none. Classes with `null` sort last, then by creation order; a tie on the same start also uses creation order. Test: with the clock fixed on Mon 28 Sep 2026, Sunrise (6:00 AM), Lunch Express (12:15 PM), and Core Burn (5:30 PM) come in that order even after 6:00 AM; a Pending class created between two open public classes stays in the middle; a later invite id is listed above an earlier one.
- **P5. Decline confirmation.** `InviteConfirmation` gains `InviteDeclined`. `onDecline` sets `confirmation = InviteDeclined` after a successful decline (the row has left the list). `InvitesViewModel.onConfirmationHandled()` clears `confirmation`. Accept still sets `InviteAccepted(className)`. Test: decline one of two invites, assert the remaining row, `InviteDeclined`, then `onConfirmationHandled()` clears it; accept still yields `InviteAccepted`. **Allowed existing-test edit:** `InvitesViewModelTest` AC47 currently asserts `confirmation` is null after decline; Tester updates that one assertion in this PR, because the behaviour is changing on purpose.
- **P6. Invites refresh.** `InvitesViewModel.refresh()` reloads the list, `pendingInviteCount`, and `isEmpty`, clears `error`, and leaves `confirmation` as it is. Test: add an invite in the repository, call `refresh()`, and the row appears; a confirmation that was already set is still set.
- **P7. Copy.** New `UiCopy` functions for every new string in sections 2 and 5: `classesTitle()`, `classesGreeting(name, employeeId)`, `myClassesHeader()` ("MY CLASSES"), `studentPublicClassesHeader()` ("Public classes"), `joinedCount(count)`, `classesEmptyTitle()`, `classesEmptyBody()`, `invitesTitle()`, `invitesSubtitle()`, `inviteInvitedYou(coachName)`, `inviteDeclined()`, and the four tab labels. Existing `inviteAccepted`, `invitesEmptyTitle` / `invitesEmptyBody`, and the status labels stay as they are. Test: exact strings, including "1 joined" / "3 joined" and "Coach Leo Ramirez invited you."

`onAccept` / `onDecline` already set `error` and, on success, clear it. They also keep the list unchanged when offline (early return). No further shared API is needed for decision 18; the screen tests in F3-AC16 cover it. A new action tap should clear the previous error before it runs, which today’s success path already does by writing `error = null`; the offline path should also clear a previous confirmation so a failure never shows a success toast. If that last part is not true today, fix it in this PR and cover it in the P5 or P6 test.

## 7. Acceptance criteria
Unless an AC says otherwise, tests use a clock fixed on **Mon 28 Sep 2026** and the C1 / C2 example data below.

**C1 example data** (the home snapshot): the six classes and four coaches from the demo seed; the student is Paolo Reyes, EMP-20417; memberships are `ACTIVE` on Sunrise, Lunch Express, and Core Burn, `INVITED` on Mobility Flow, `PENDING_REQUEST` on Bootcamp, and none on Every Day 20.

**C2 example data** (two outstanding invites): the same people and classes, but Weekend TRX Bootcamp is `INVITED` instead of `PENDING_REQUEST` (so both Mobility Flow and Bootcamp are invites). Store Bootcamp’s invite first, then Mobility Flow’s, so newest-first shows Mobility Flow above Bootcamp, as in Figma C2.

**Classes (C1)**

- **F3-AC1.** With the C1 example data, C1 shows the title "Classes", the greeting "Hi Paolo · EMP-20417", "MY CLASSES", the count "3 joined", and three Joined cards in next-session order: Sunrise TRX Strength, Lunch Express TRX, TRX Core Burn. Each Joined card shows the Joined chip, "Coach Mara Santos", the days line, and the time line. **My classes contains only those Joined cards — no Pending card, no Invited card, and no Requests section.** "Public classes" then lists, in creation order, TRX Mobility Flow (Invited), TRX Every Day 20 (no chip), and Weekend TRX Bootcamp (Pending). The tab bar shows Classes, Attend, Sessions, and Invites only (no Requests tab), with Classes selected and the Invites badge "1". There is no loading spinner.
- **F3-AC2.** The count reads "1 joined" for one Joined class and "3 joined" for three.
- **F3-AC3.** With no Joined classes and some public classes, the My classes section and its count are hidden, and Public classes still shows. With only Joined classes and no public cards, Public classes is hidden and My classes still shows.
- **F3-AC4.** With no Joined and no public cards, `student_classes_empty` shows the decision 8 text, there is no section header and no count, and the tab bar is still showing.
- **F3-AC5.** A private class the student is not Joined on does not appear. A cancelled class appears in neither section. A public class that ended yesterday does not appear in Public classes; one that ends today does. An ended class the student is still Joined on stays in My classes, and opening it shows the ended detail from flow 2.
- **F3-AC6.** Offline: `offline_banner` shows under the title, the cards still show, and tapping a card still opens `ClassDetail`.
- **F3-AC7.** Refresh: on the C1 example, accept Mobility Flow in the repository, select the Classes tab (or call the same `refresh()` the tab uses), and Mobility Flow is in My classes with a Joined chip, gone from Public classes, and the count reads "4 joined".
- **F3-AC8.** Tapping a My classes card opens `ClassDetail(id)` on A6 (Joined chip, "You’re on this class"). Tapping it twice quickly still gives one `ClassDetail` entry. The detail screen has no tab bar. Back (`class_detail_back`, "Classes") returns to C1 with the tab bar visible.
- **F3-AC9.** Tapping Every Day 20 opens A3 (Request to join). Tapping Bootcamp (Pending) opens A4 (Awaiting approval, no button). Tapping Mobility Flow (Invited) opens the Invited state from flow 2, with no button. After a public class is `DECLINED` (or `REMOVED`) and `refresh()` runs, that card has no chip in Public classes; tapping it opens A5 with the flow 2 decision 17 text and "Request to join"; one tap sends a new pending request and shows A4.
- **F3-AC10.** Each C1 card is one merged node with a spoken description in the same pattern as flow 2 ("Sunrise TRX Strength, Coach Mara Santos, Monday, Wednesday and Friday, 6:00 AM to 6:45 AM") and a height of at least 48dp.
- **F3-AC11.** The account icon is visible on C1 and C2. Tapping it does not add a back-stack entry and does not change the current tab.

**Invites (C2–C4)**

- **F3-AC12.** With the C2 example data, Invites shows the title "Invites", the subtitle "Accept to join a class roster.", and two cards, Mobility Flow above Bootcamp. Each card shows "Coach {name} invited you." with **no date**, a Public chip, the days line, the time line, Decline, and Accept. The Invites tab is selected and the badge reads "2". Tapping the card body (not Decline or Accept) does not open class detail and does not change the back stack. Accept and Decline are at least 48dp high.
- **F3-AC13.** Accept on Mobility Flow: the Mobility Flow card leaves at once, the toast shows "You joined TRX Mobility Flow. It’s now in My classes.", the student is still on Invites, Bootcamp is still there, and the badge reads "1". After 4 seconds the toast is gone. Opening the Classes tab shows Mobility Flow in My classes with a Joined chip.
- **F3-AC14.** Decline on Mobility Flow (C2 example, two invites): no dialog appears; the Mobility Flow card leaves at once; the toast shows "Invite declined"; Bootcamp remains; the badge reads "1". After 4 seconds the toast is gone. Declining the last remaining invite shows C4.
- **F3-AC15.** C4 shows the title "Invites", no subtitle, `invites_empty` with the C4 empty copy already in `UiCopy`, no badge, and the tab bar with Invites selected.
- **F3-AC16.** Offline Accept or Decline (C2 example): the tapped invite stays on the list, `invites_form_error` shows "You’re offline. Nothing was saved.", there is no toast, and there is no Retry button. Going online and tapping Accept (or Decline) again follows F3-AC13 (or F3-AC14) and clears the error.
- **F3-AC17.** An invite to a private class shows the Private chip and the same Accept / Decline actions.

**Tabs and navigation**

- **F3-AC18.** The tab bar has exactly four items, in this order, with these labels: Classes, Attend, Sessions, Invites. There is no Requests tab. Attend shows the title "Attend" and `placeholder_tag`. Sessions shows the title "Sessions" and `placeholder_tag`. Classes and Invites do not show `placeholder_tag`.
- **F3-AC19.** Signing in as a student with no `openClassId` lands on `StudentHome`, Classes tab, C1. Signing up or signing in from a class still ends with the back stack `[StudentHome(id), ClassDetail(id)]` and A4 on screen, as in flow 2. Back shows C1 (`student_home`) with the tab bar.
- **F3-AC20.** When `pendingInviteCount` is 0, the Invites tab has no badge. When it is 2, the badge reads "2".

**Structure and build**

- **F3-AC21.** Source scan: `DemoSeed` and Paolo’s demo sign-in (name, employee ID, email, password) appear only under a debug source set. No file under `*/src/main`, `*/src/commonMain`, `*/src/androidMain`, `*/src/iosMain`, or `*/src/*release*` mentions them.
- **F3-AC22.** Source scan: each new route, screen, placeholder, tab-bar, or test-tag file holds one public type. No nested classes.
- **F3-AC23.** All existing suites still pass. The only test edits allowed are: the AC47 confirmation assertion in `InvitesViewModelTest` (P5); any flow 1 or flow 2 test that required `placeholder_tag` on StudentHome, or that treated StudentHome as the old placeholder body; and F2-AC34’s wording that StudentHome is a placeholder (Attend and Sessions now carry the tag instead).

**Manual checks** (Reviewer, on the Android emulator; these are not ACs):
- A fresh debug install still starts as a guest. The guest list shows the six seeded classes. Relaunching does not add them again.
- Sign in as Paolo with the documented demo account. C1 matches the C1 example (three Joined cards, then Mobility Flow Invited, Every Day 20, Bootcamp Pending; Invites badge 1).
- Open a Joined card (A6), a Pending card (A4), an Invited card (Invited detail), and Every Day 20 (A3 -> Request to join -> A4). Back from each returns to C1.
- On Invites, accept the Mobility Flow invite: toast, empty Invites (C4), and the class in My classes after switching tabs.
- Decline is covered if you reinstall or use a build against test data with two invites; otherwise the automated test is enough.
- The profile icon does nothing. Attend and Sessions show the PLACEHOLDER pill. There is no Requests tab.
- Offline on Invites: Accept shows "You’re offline. Nothing was saved." and the card stays.

## 8. Test plan
| What | Suite | ACs or items |
|---|---|---|
| Shared changes | `:shared:phase2JvmTest` (new tests only, plus the one AC47 assertion in P5) | P1–P7 |
| C1, C2–C4, tab, and placeholder screen tests (stateless screens + ViewModels with fakes, `v2.runComposeUiTest`) | `:composeApp:jvmTest` | F3-AC1–18, F3-AC20 |
| Navigation on `AppNavHost` (ViewModel factories, test NavController) | `:composeApp:jvmTest` | F3-AC19 |
| Source scans | `:composeApp:structureJvmTest` | F3-AC21, F3-AC22 |
| Regression | all four suites | F3-AC23 |

Test tags (snake_case, one object per screen: `StudentClassesTestTags`, `InvitesTestTags`, `StudentTabBarTestTags`):
- C1: `student_home`, `student_classes_title`, `student_classes_greeting`, `student_classes_account`, `student_my_classes_header`, `student_my_classes_count`, `student_public_classes_header`, `student_class_card_<id>`, `student_classes_empty`
- Invites: `invites_title`, `invites_subtitle`, `invites_card_<id>`, `invites_accept_<id>`, `invites_decline_<id>`, `invites_empty`, `invites_toast`, `invites_form_error`, `invites_account`
- Tabs: `student_tab_classes`, `student_tab_attend`, `student_tab_sessions`, `student_tab_invites`, `student_tab_invites_badge`
- Placeholders: existing `placeholder_tag`; `student_attend`, `student_sessions` on the placeholder roots
- Shared: `offline_banner`
- Class detail tags stay as in flow 2

## 9. Assumptions
These are the only points not fixed by the 16 answers, Figma, or existing code:

1. **Next session time** is the earliest session start **at or after local midnight today**, not after the current time of day. That is what keeps Sunrise first on the Monday C1 example even after 6:00 AM. If two classes share that start, or a Joined class has no upcoming session (for example it has ended), they sort after the ones that do, in creation order.
2. **Paolo’s demo seed follows the C1 snapshot** (one invite, one pending request). Figma C2 shows two invites and includes Bootcamp, which C1 shows as Pending — those cannot both be true for one membership. The two-invite list, the after-accept list (C3), and declining one of two are covered by tests using the C2 example data, not by the demo account.
3. **Demo sign-in** (draft, change if you prefer): email `paolo@company.com`, password `password1`, name Paolo Reyes, employee ID EMP-20417. Document the final values in the PR. The app never auto-signs in as Paolo.
4. **Invite errors** use the existing Form Error row, under the title and above the list. There is no Retry action, because decision 7 has no error-with-retry state.
5. **The four tabs live inside `StudentHome`**, not as their own NavHost destinations, so the flow 2 back stack is unchanged.
6. **Attend and Sessions placeholders** reuse the large-title chrome (eyebrow, title, account icon, tab bar) and otherwise only show `PlaceholderTag`.
7. **If a student has no employee ID**, the greeting is "Hi {name}" with no middle dot. Real students have an employee ID.
8. **Weekend TRX Bootcamp** is seeded as Saturday only, which is what C1 and C2 draw (today’s seed also had Sunday).

## 10. Open notes for later
A later sign-out must pop to `graph.id` inclusive, because `RootViewModel.startRoot` is captured once. If sign-out only pops the current screen, the next sign-in can reuse the old start destination.

## 11. Known gaps (on purpose)
- Accepting an outstanding invite to a class that was cancelled still follows Phase 2: the membership can become `ACTIVE`, the toast still says the class is in My classes, and the card does not appear in My classes because cancelled classes are dropped there. A clearer toast can wait until a later flow.
- Invite dates, loading skeletons, pull-to-refresh, sign-out, and the real Attend / Sessions screens wait for later phases.
