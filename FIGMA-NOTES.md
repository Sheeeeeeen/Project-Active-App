# TRX Class Attendance — MVP · Figma notes

- **File:** https://www.figma.com/design/qbHRsTH5EHXslAnGr7tV3o
- **File key:** `qbHRsTH5EHXslAnGr7tV3o`
- **Location:** Sheen Fabile's personal **Drafts** (Starter plan, team `1302914262820611549`). It has not been published, shared to a team, or moved. No other Figma file was opened or edited.
- **Source of truth:** `REQUIREMENTS.md`
- **Status:** Ready for dev review. Last updated 11 Nov 2026.

## Pages (Starter plan limit: 3 pages)

| Page | Node | Contents |
|---|---|---|
| 00 Cover · Flows · Specs | `0:1` (capture root `10:2`) | 00 Cover · 01 Sitemap & IA · 02 Flows 1–6 · 03 Colour tokens · 04 Typography (iOS/M3) · 05 Spacing, layout & touch targets · 06 Feedback component specimens · 07 Specs & handoff |
| 01 Design System | `2:188` | Variables (Primitives + Semantic Light/Dark) · 15 text styles · real components in 3 sections (listed below) |
| 02 Screens | `2:189` (capture root `9:2`) | Sections A–D · 50 frames at 393 × 852. Each frame has a step label above it and a DEV NOTES card below it. |

### 01 Design System: real Figma components
- **Core** (`3:726`):
  - Status Bar
  - Home Indicator
  - Button/Large: Kind Primary / Secondary / Tertiary / Destructive × State Default / Pressed / Disabled / Loading
  - Button/Small: 4 kinds × Default / Pressed / Disabled
  - Status Chip: Pending, Joined, Scheduled, Cancelled, Upcoming, Present, Absent, Public, Private, Invited, Upload pending
  - Avatar
  - Offline Banner
  - Text Field: Default / Focused / Filled / Error / Disabled
- **Navigation & Controls** (`4:504`):
  - Nav Bar: Large / Inline
  - Tab Bar, 7 variants:
    - Student: Classes / Attend / Sessions / Invites
    - Coach: Classes / QR / Attendance
  - Segmented Control
  - Manage Tabs
  - Weekday Chip
  - Sheet Header
  - Section Header
  - Form Row
- **Rows & Cards** (`6:199`): Class Card, Session Row, Invite Card, Request Row, Roster Row, Schedule Row, Attendance Row, Class Choice Row, Time Slot Row
- The component IDs are in `build/comps.json`. The variable and style IDs are in `build/ids.json`.

## Frame inventory (02 Screens)

**Section A — Guest & public classes**

- A1 · Public classes — guest — Flow 1 · step 1 / Flow 2 · step 1
- A2 · Class detail — guest — Flow 1 · step 2–3
- A3 · Class detail — student, not requested — Flow 3 · step 1–2
- A4 · Class detail — Pending · Awaiting approval — Flow 1 · step 6 / Flow 3 · step 2
- A5 · Class detail — after decline or removal — Flow 3 · step 3
- A6 · Class detail — Joined — Flow 1 · step 8
- A7 · Class detail — coach viewing — Rule: no request for coach

**Section B — Registration & sign in**

- B1 · Registration — student (default) — Flow 2 · step 1
- B2 · Registration — student, filled — Flow 1 · step 5
- B3 · Registration — validation errors — Error state
- B4 · Coach registration (after shake) — Flow 2 · step 2–3 (Dark)
- B5 · Registration — from Request to join — Flow 1 · step 4–5
- B6 · Registration — submitting — Loading state

**Section C — Student**

- C1 · Classes — signed-in student — Home
- C2 · Invites — list — Flow 4 · step 3
- C3 · Invites — after Accept — Flow 4 · step 4
- C4 · Invites — empty — Empty state
- C5 · Sessions — Upcoming · Present · Absent — Flow 5 · step 5–6
- C6 · Attend — home — Flow 5 · step 2
- C7 · Attend — choose class (modal) — Flow 5 · step 2
- C8 · Attend — class chosen — Flow 5 · step 3
- C9 · Camera permission — First scan (Dark)
- C10 · Scanner — class chosen — Flow 5 · step 4 (Dark)
- C11 · Scanner — No internet connection — Offline (Dark)
- C12 · Result — Present — Flow 5 · step 5
- C13 · Result — saved offline — Offline
- C14 · Result — already have attendance — Duplicate scan
- C15 · Result — scan not accepted — Rejected scan

**Section D — Coach**

- D1 · Classes — coach — Home
- D2 · Classes — coach, empty — Empty state
- D3 · Create class — empty — Create
- D4 · Create class — requirements example — Saved Fri 25 Sep 2026
- D5 · Create class — multiple slots, different times — Slots per weekday
- D6 · Create class — every day — Every day
- D7 · Manage — Schedule — Flow 6 · step 1
- D8 · Cancel one date — confirm — Flow 6 · step 2
- D9 · Schedule — after cancel — Flow 6 · step 3–5
- D10 · Manage — Requests — Flow 1 · step 7
- D11 · Manage — Requests empty — Empty state
- D12 · Manage — Roster — Roster
- D13 · Remove student — confirm — Roster
- D14 · Invite — one match — Flow 4 · step 1–2
- D15 · Invite — sent — Flow 4 · step 2
- D16 · Invite — no match — No match
- D17 · Class options — Whole class
- D18 · Cancel whole class — confirm — Whole class
- D19 · Daily QR — Flow 5 · step 1 (Dark)
- D20 · Daily QR — No internet connection — Offline (Dark)
- D21 · Attendance — finished date — Results
- D22 · Attendance — today, in progress — Results

## Tokens

### Colour
The semantic collection has Light and Dark modes. Values are listed as Light → Dark.

**Backgrounds**
- bg/canvas: #F8F8F9 → #0B0B0C
- bg/surface: #FFFFFF → #141416
- bg/surface-alt: #F1F1F3 → #1F1F23
- bg/inverse: #0B0B0C → #FFFFFF

**Borders**
- border/default: #E3E3E6 → #2C2C31
- border/strong: #C4C4CA → #45454D

**Text**
- text/primary: #0B0B0C → #FFFFFF
- text/secondary: #45454D → #C4C4CA
- text/tertiary: #63636B → #8E8E96
- text/inverse: #FFFFFF → #0B0B0C

**Accent**
- accent/default: #FFC20E in both modes
- accent/pressed: #E0A800 → #FFD54A
- accent/subtle: #FFF4CC → #3A2E06
- accent/text: #7A5A00 → #FFD54A

**Status**
- success fg/bg: #16794A / #DDF3E6 → #5BE08F / #0F3A22
- danger: #B42318 / #FDE2E1 → #FF7A7A / #4A1414
- info: #1F57C3 / #E1EBFC → #8DB4FF / #13254A
- warning: #8A4B00 / #FFEBC7 → #FFC266 / #3D2A05
- neutral: #45454D / #F1F1F3 → #C4C4CA / #1F1F23

**Other**
- focus/ring: #1F57C3 → #FFD54A
- overlay/scrim: #0B0B0C at 48%
- QR: always #0B0B0C on #FFFFFF in both modes, for scanner contrast.

### Spacing and radius
- **Spacing:** 4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48. The layout steps are multiples of 8.
- **Radius:** sm 8 / md 12 / lg 16 / xl 24 / full 999.
- **Screen layout:** 24 side margins, 16 card padding, 52 primary button height.
- **Touch targets:** at least 44pt on iOS and 48dp on Android.

### Type (pt/sp; iOS SF Pro ↔ Android M3)

| Style | Size | iOS | Android |
|---|---|---|---|
| Large Title | 34/41 Bold | `.largeTitle` | Display Small |
| Title 1 | 28/34 Bold | `.title` | Headline Medium |
| Title 2 | 22/28 Bold | `.title2` | Headline Small |
| Title 3 | 20/25 Semibold | `.title3` | Title Large |
| Headline | 17/22 Semibold | `.headline` | Title Medium |
| Body | 17/22 | `.body` | Body Large |
| Callout | 16/21 | `.callout` | Body Large |
| Subheadline | 15/20 | `.subheadline` | Body Medium |
| Subheadline Emphasis | 15/20 Semibold | — | Label Large |
| Footnote | 13/18 | `.footnote` | Body Small |
| Caption 1 | 12/16 Semibold | `.caption` | Label Medium |
| Caption 2 | 11/13 Medium | `.caption2` | Label Small |
| Overline | 12/16 Semibold, caps, +1.2 | — | — |

The mock-ups use **Inter** as a stand-in. Build with SF Pro (iOS, through Dynamic Type) and Roboto (Android, through MaterialTheme.typography).

## IA and navigation decisions
- **Three separate roots:**
  - Guest stack: A1 → A2 → B registration
  - Student TabView / NavigationBar: Classes · Attend · Sessions · Invites
  - Coach tabs: Classes · QR · Attendance
- **Sign-in replaces the root.** There is no role switch afterwards. One account has one role.
- **Coach class management (D7–D18)** is pushed from a coach class row. It has top tabs: Schedule · Requests · Roster · Invite. A "•••" menu opens Class options, which contains "Cancel whole class".
- **Create class** is a full-height sheet. **Choose class** is a medium/large sheet. Dialogs are used only for destructive confirmations.
- **Registration and sign in are on one screen.** It opens as student registration. Shaking the device switches to coach registration; this is documented in dev notes only and has no on-screen hint, toggle or CTA. Shake is ignored when registration is opened from Request to join (B5).
- **When a coach opens a public class detail (A7),** it shows information only: no request button, no roster, no QR.

## Design decisions and rationale
1. **Student and coach registration look different.**
   - Student: light screen titled "Create account", with Employee ID.
   - Coach: dark screen with a gold top band, titled "Coach registration", with no Employee ID.
   - This makes the shake result unmistakable without adding any visible control.
2. **Status always pairs an icon with the exact label,** so it never relies on colour alone. This covers chips, callouts and result screens.
3. **Offline:**
   - A banner reads "No internet connection" with a wifi-off icon. It uses the inverse surface.
   - Student scans are validated locally, queued, and shown as "Upload pending" (C13).
   - The coach's QR is generated on the device, so it works offline (D20).
4. **The attendance modal is select-then-scan.** The student picks one class, then taps "Scan coach's code". Closing the modal stores nothing.
   - Cancelled sessions (Lunch Express today) are left out of the modal.
   - **Confirm with product:** a day with several time slots for one class shows one row per slot.
5. **Before a day ends,** coach attendance shows "Not scanned yet" (D22) instead of Absent, because Absent applies only after the day is over (D21).
6. **Cancelled dates:**
   - They stay on the coach schedule as Cancelled and keep their session number.
   - Next Monday stays Scheduled.
   - They are not listed in the student's Sessions. **Confirm with product.**
7. **"Cancel whole class"** (D17–D18) ends the series: remaining dates are cancelled, and past sessions and attendance are kept. This meets the requirement to delete or cancel a class without losing history.
8. **Create class example (D4):**
   - Saved Fri 25 Sep 2026, Mon + Wed, 5:30–6:30 PM, ends Sat 31 Oct 2026.
   - This gives 10 dates, Mon 28 Sep to Wed 28 Oct.
   - Each weekday can have one or more slots through "Add time slot" (D5).
   - "Every day" selects all 7 weekdays (D6).
9. **There is no visible shake hint, role toggle, email confirmation, present-without-scan button, NFC, payment, capacity, waitlist, notification or chat UI anywhere.**
10. **Scenario:**
    - "Today" is Mon 28 Sep 2026. The coach is Mara Santos, the student is Paolo Reyes (EMP-20417), and the guest is Ella Navarro (EMP-20611).
    - D7–D9 use a device date of Mon 5 Oct to show cancelling "this Monday" while Mon 12 Oct stays Scheduled.

## What failed or is incomplete (exact)
- **Figma Starter plan limits:**
  - Only 3 pages are allowed, so the planned page set was merged into 3 pages.
  - The monthly MCP tool-call quota (about 20 `use_figma` / `get_screenshot` calls) ran out while building the component library.
- **Effects of the quota:**
  1. These feedback components were **not** built as real Figma components: Dialog, Toast, Callout, Empty State, QR Card, Search Field and Scanner Viewfinder (script `build/02d_feedback.js`, not run). They appear only as visual specimens with specs on page 00, section 06.
  2. **The 50 screens and the docs page are HTML-to-Figma captures** (`generate_figma_design`, which does not count toward the quota). They are pixel-accurate frames and auto-layout layers, but **not instances** of the page-01 components. Variables may be bound only where the capture matched them.
  3. **I could not check the captured result inside Figma** (`get_screenshot` is over quota). I checked the layouts by rendering the same HTML in headless Chrome before capture. Both captures reported "completed" (Screens → node `9:2`, Docs → node `10:2`).
  4. **The page-01 component sections could not be edited after the quota ran out.** If they need small layout fixes, those must be done by hand.
- **Font:** SF Pro is not available to the file (missing font), so Inter is used everywhere as a stand-in.
- **Next step:** upgrade the Figma plan, or wait for the monthly MCP reset, then swap the captured layers for component instances and build the 7 remaining feedback components.

## Build sources (box)
- `/workspace/trx-attendance/build/` contains:
  - `ui.py`, `screens.py`, `build_screens.py`, `build_docs.py`: the HTML generators
  - `html/screens.html`, `html/docs.html`: the captured pages
  - `lib.js` and `0*.js`: Figma plugin scripts
  - `qr.json`: the QR payload `trxatt://v1/daily?coach=CH-0412&date=2026-09-28&sig=…`
