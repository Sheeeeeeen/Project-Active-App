# TRX Class Attendance — Phase 3, Flow 1 Spec (registration and sign-in UI)

Status: **approved by Sheen on 2026-11-12 with the recommended defaults for all three open questions.** Tester writes failing tests on a feature branch from main (`9e6ad0d`). Builder and Reviewer hold until those tests are in.

Baseline: GitHub `main` at `9e6ad0d` (green: `:shared:jvmTest` 116, `:shared:phase2JvmTest` 100). Phase 1 (`SPEC-phase1.md`) and Phase 2 (`SPEC-phase2.md`, AC1–87) are used as-is. Flow 1 adds the first Compose Multiplatform screens on top of the Phase 2 `RootViewModel` and `RegistrationViewModel`. The app still runs on the Phase 2 fakes; there is no Firebase. It must run on Android from Linux. iOS device checks happen in Phase 4.

Design source: Figma file `qbHRsTH5EHXslAnGr7tV3o`, page "02 Screens", section `25:4902` "Section B · Registration & sign in": B1 `22:1565`, B2 `22:1694`, B3 `22:1822`, B4 `22:1950`, B5 `22:2068`, B6 `22:2478`, plus their Dev Notes cards (`22:1687`, `22:1815`, `22:1943`, `22:2061`, `22:2197`, `22:2607`). The node content and copy below were read from Figma on 12 Nov 2026 (`get_metadata` and `get_design_context`). Tokens come from `FIGMA-NOTES.md`.

## What exists on `main` today (read 12 Nov 2026)
- `settings.gradle.kts` includes only `:androidApp` and `:shared`. **There is no `composeApp` module yet.** The Compose template (`App.kt`, `Greeting*`, `Platform*`) sits in `shared/src/commonMain/kotlin/ph/project/active/`, and `MainActivity` calls that template `App()`.
- `shared` already applies the Compose Multiplatform plugin and the KMP lifecycle `viewmodel-compose` and `runtime-compose`, and has a `jvm()` target that runs the Phase 1 and Phase 2 suites.
- `RegistrationViewModel` exposes `RegistrationUiState` (mode, showEmployeeId, the four registration fields, two sign-in fields, `fieldErrors`, `isSubmitting`, `requestingClass`, `signInError`, `completion`, `joinOutcome`, `error`, `connection`, and `navigation`). **It never sets `navigation`**; success is signalled through `completion` only.
- `RootViewModel` exposes `root: Root.Guest | Student(account) | Coach(account)`, which comes from `SessionStore.account`.
- `UiCopy` has the B-screen titles, submit labels, the loading label, and the `InvalidEmail`, `EmployeeIdTaken`, and `PasswordTooShort` texts. `Required`, `EmailTaken`, `AuthFailed`, and `Offline` have no copy yet (Phase 2 left them *open*).
- `domain.crypto.PasswordHasher` is an interface with **no implementation in `commonMain`**. `FakeAuthRepository` needs one to run in the app.

## Decisions locked by Sheen (12 Nov 2026, via Builder)
1. **Navigation.**
   - Use JetBrains Compose Multiplatform Navigation Compose (`org.jetbrains.androidx.navigation:navigation-compose`) with type-safe `@Serializable` routes and one `NavHost` in `composeApp` `commonMain`.
   - ViewModels keep their one-shot `navigation` field and `onNavigationHandled()`. The UI maps each value to a `NavController` call, then calls `onNavigationHandled()` once.
   - A successful sign-in or registration replaces the root: it navigates to the signed-in destination and pops the guest graph inclusive, so Back never returns to sign-in.
   - Each route type goes in its own file.
   - On iOS, navigation runs inside the Compose view, not in a SwiftUI `NavigationStack`, so there is no native swipe-back by default. See Decision 1.
2. **DI.**
   - Use Koin with `koin-compose-viewmodel`. Screens get their ViewModels with `koinViewModel()` in common Compose code.
   - ViewModels and repositories in `shared` stay plain constructor classes with **no Koin imports**.
   - All Koin modules live in `composeApp`, one module per file. The Phase 2 fakes are bound in a single module, and that one module is what swaps to real Firebase at the Phase 5 cutover.
   - Tests stay framework-free. UI tests build ViewModels directly with fakes and call stateless screen composables that take state and callbacks. A test Koin module is used only for the DI wiring checks (F1-AC7, F1-AC8).

## Scope
- A new `composeApp` module (KMP library: Android, `iosArm64`, `iosSimulatorArm64`, and `jvm()` for Linux UI tests) that depends on `:shared`. `androidApp` depends on `composeApp`.
- A theme built from Figma tokens (light and dark semantic colors, spacing, radius, Inter as the stand-in font), plus the flow 1 components: text field, large button (primary and secondary, with disabled and loading states), offline banner, and the requesting-class card.
- One registration and sign-in screen covering B1–B6, driven by `RegistrationViewModel`.
- Shake to switch to coach registration: a pure, testable detector in common code, with the sensor feed from Android only.
- App navigation: the guest graph (a guest home placeholder and Registration) and the signed-in roots (student home and coach home placeholders), with root replacement on success.
- Koin wiring for the Phase 2 fakes, an Android `Application` that starts Koin, and an Android SQLDelight driver so the signed-in account survives an app restart.
- Small additions to `shared`, listed under "Proposed changes to shared" (P1–P4).

## Out of scope (flow 1)
- The real A1 guest class list, A2–A7 class detail, and the A4 "Pending" landing. These are flow 2; flow 1 uses a minimal guest placeholder, and `openClassId` is passed to the student home placeholder.
- The real C1 and D1 home screens (flows 3 and 4); flow 1 uses placeholders.
- Showing `joinOutcome = ClassCancelled` (flow 2, where class detail exists).
- Sign-out and account deletion (still out of scope, as in Phase 2).
- Firebase, a real `NetworkMonitor`, and a real password hasher.
- iOS device checks: the iOS shake feed, iOS swipe-back, and wiring the Xcode host to `composeApp`. These are Phase 4.
- Email confirmation, a visible role picker or shake hint, and analytics (all MVP exclusions).

## User stories
1. As a guest, I can open registration from the guest home and create a student account with name, email, password, and employee ID, and then land on the student home.
2. As a guest who mistypes, I see each problem next to its field, with an icon and text, and I can fix it without losing what I typed.
3. As a coach, I can shake the phone on registration to get coach registration (dark, no employee ID), create a coach account, and land on the coach home. Shaking again brings back student registration with my typed values.
4. As an existing user, I can sign in from the same screen and land on my role's home, whatever mode the screen is showing.
5. As a guest who tapped "Request to join", I see which class I am joining. Creating an account or signing in also sends the request, and shaking does nothing.
6. As any user, I see that the account is being created and cannot submit twice. When offline, I see "No internet connection", and nothing is created.
7. As a signed-in user, Back never returns me to registration. Reopening the app keeps me signed in.

## Module and packages
`composeApp/src/commonMain/kotlin/ph/project/active/`, one public type per file:

| Package | Types |
|---|---|
| `ui` | `TrxApp` (root composable: theme and `AppNavHost`) |
| `ui.navigation` | Routes, one file each: `GuestGraph`, `GuestHome`, `Registration(requestClassId: String? = null)`, `StudentHome(openClassId: String? = null)`, `CoachHome`. Also `AppNavHost`. Each route collects its one-shot `Navigation` with `collectAsStateWithLifecycle`, navigates, then calls `onNavigationHandled()` (the separate `NavigationEffect` type was dropped in review). |
| `ui.registration` | `RegistrationRoute` (gets the VM through `koinViewModel { parametersOf(origin) }` and collects shakes), `RegistrationScreen` (stateless: `state`, callbacks, `modifier`), `RegistrationTestTags` |
| `ui.home` | `GuestHomePlaceholder`, `StudentHomePlaceholder`, `CoachHomePlaceholder`, `HomeTestTags` |
| `ui.theme` | `TrxTheme`, `TrxColors` (with `TrxColors.Light` and `TrxColors.Dark`), `TrxSpacing`, `TrxTypography`, `LocalTrxColors` |
| `ui.components` | `TrxTextField`, `TrxLargeButton`, `TrxButtonKind`, `OfflineBanner`, `RequestingClassCard`, `ComponentTestTags` |
| `ui.platform` | `ShakeSource` (interface, `val shakes: Flow<Unit>`), `ShakeDetector` (pure algorithm), `NoShakeSource` |
| `di` | `FakeDataModule`, `ViewModelModule`, `PlatformModule` (`expect`), `AppModules` (`appModules(): List<Module>`) |

Platform source sets:
- `androidMain`:
  - `AndroidShakeSource`, which feeds `SensorManager` accelerometer samples into `ShakeDetector`.
  - The `PlatformModule` actual, which provides `AndroidSqliteDriver("trx.db")` and `AndroidShakeSource`.
- `iosMain`: the `PlatformModule` actual, which provides `NativeSqliteDriver` and `NoShakeSource` (Phase 4 adds the real feed).
- `jvmMain`: the `PlatformModule` actual, which provides an in-memory `JdbcSqliteDriver` and `NoShakeSource`. It exists for tests only.
- `androidApp`: `TrxApplication : Application` calls `startKoin { androidContext(this); modules(appModules()) }`, and `MainActivity` calls `setContent { TrxApp() }`.
- Leave the template `App`, `Greeting*`, and `Platform*` in `shared` as they are; existing tests may reference them, and removing them is a separate cleanup. iOS `ContentView.swift` keeps the template until Phase 4.

Dependencies (Builder pins versions compatible with Kotlin 2.4.21, CMP 1.12.2, AGP 9.1.1, and Gradle 9.5.1, and adds them to `gradle/libs.versions.toml`):
- `navigation-compose` (JetBrains)
- the `kotlinx-serialization` plugin and `kotlinx-serialization-core`
- `koin-core`, `koin-compose`, `koin-compose-viewmodel`, and `koin-android` (in `androidApp`/`androidMain` only)
- `koin-test` (in `composeApp` `jvmTest` only)
- `org.jetbrains.compose.ui:ui-test` (`commonTest`)
- `compose.desktop.currentOs` (`jvmTest` only)

## Screens B1–B6 mapped to `RegistrationUiState`
There is one screen, `RegistrationScreen`. The frames below are states of it, not separate destinations.

| Frame | When | What is shown (from Figma) |
|---|---|---|
| B1 `22:1565` Student default | `origin = FromClassList`, `mode = STUDENT`, empty fields | Nav back "Classes". Overline "TRX ATTENDANCE", title "Create account", subtitle "Join classes and record your attendance.". Fields: Full name ("Your name", user icon); Email ("name@company.com", mail icon); Password ("At least 8 characters", lock icon, eye toggle, helper "At least 8 characters."); Employee ID ("e.g. EMP-20417", id-card icon, helper "Your company employee ID. Each ID can be registered once."). Primary "Create account" (disabled look, `22:1652`). Divider "Already have an account?". Heading "Sign in". Email ("name@company.com"), Password ("Your password", eye). Secondary "Sign in". Dev note: scroll view, no role toggle. |
| B2 `22:1694` Filled | Same, all fields non-blank | Same layout, values shown, primary enabled. Dev note: focused field has a 2px focus ring; submit opens student screens. |
| B3 `22:1822` Errors | `fieldErrors` not empty | Error fields have a danger border and a row below with a `triangle-alert` icon and the text: "Enter a valid email address.", "This employee ID is already registered." The error replaces the helper. Dev note: "Inline errors with icon + text (not color only)." |
| B4 `22:1950` Coach | `mode = COACH` (after shake) | Dark theme, gold top band (`22:1951`, 6pt). Overline "COACH ACCOUNT", title "Coach registration", subtitle "Create classes, invite students and show the daily code.". No Employee ID field. Primary "Create coach account". Sign-in section unchanged. Dev note: shaking again returns to B1 and keeps name, email, and password; sign-in opens the coach screens. |
| B5 `22:2068` From Request to join | `origin = FromRequestToJoin(classId)`, `requestingClass != null` | Nav back "Back". Card (`22:2105`, dumbbell icon): "REQUESTING TO JOIN", "TRX Core Burn", "Coach Mara Santos · Mon · Wed · 5:30 PM – 6:30 PM". Subtitle "Create your student account to send this request.". Primary "Create account and send request". Note above Sign in: "Signing in also sends your request for TRX Core Burn.". Dev note: always student; shake ignored; signing in also sends the request. |
| B6 `22:2478` Submitting | `isSubmitting = true` | Primary shows a spinner (`loader-circle`) and "Creating account…". Dev note: "form disabled". |

Actions:
- Each field's change calls `onNameChange`, `onEmailChange`, `onPasswordChange`, `onEmployeeIdChange`, `onSignInEmailChange`, or `onSignInPasswordChange`.
- The primary button calls `onSubmit()`. "Sign in" calls `onSignIn()`. A shake calls `onShake()`; the ViewModel ignores it for `FromRequestToJoin` (Phase 2 AC28).
- The back arrow pops the back stack. This is UI only.
- The password eye toggle is UI-local state and is not stored in the ViewModel.

Requesting-card subtitle format: `"Coach {coachName} · {formatDays(days)} · {slots}"`. `{slots}` is the distinct `formatSlot` values across all days, sorted by start time and joined with `", "`. The Figma example has one slot, so the result is exactly the string shown in B5.

Offline: no B frame shows it. Use the design-system "Offline Banner" component (`3:726` Core) at the top of the screen whenever `connection.banner != null`.

## Navigation
```
NavHost(startDestination = chosen once from RootViewModel.state.root at first composition)
├── navigation<GuestGraph>(startDestination = GuestHome)
│   ├── GuestHome                       placeholder: one button "Register or sign in" → GuestClassListViewModel.onRegisterOrSignIn()
│   └── Registration(requestClassId?)   null → FromClassList, id → FromRequestToJoin(id)
├── StudentHome(openClassId?)           placeholder (C1 in flow 3; class-detail push in flow 2)
└── CoachHome                           placeholder (D1 in flow 4)
```

How each one-shot `Navigation` maps to a `NavController` call. The route calls `onNavigationHandled()` exactly once per value, right after navigating.

| `Navigation` | NavController call |
|---|---|
| `OpenRegistration(FromClassList)` | `navigate(Registration(null)) { launchSingleTop = true }` |
| `OpenRegistration(FromRequestToJoin(id))` | `navigate(Registration(id)) { launchSingleTop = true }` (entry point used in flow 2) |
| `OpenSignedInRoot(STUDENT, openClassId)` (**P1**) | `navigate(StudentHome(openClassId)) { popUpTo<GuestGraph> { inclusive = true }; launchSingleTop = true }` |
| `OpenSignedInRoot(COACH, _)` (**P1**) | `navigate(CoachHome) { popUpTo<GuestGraph> { inclusive = true }; launchSingleTop = true }` |

Rules:
- Guest-to-signed-in transitions are driven **only** by `RegistrationViewModel.navigation`. `RootViewModel.root` sets only the start destination at launch. Root changes after launch do not navigate in flow 1, since sign-out is out of scope; this prevents double navigation, because `SessionStore.save` updates `RootViewModel` before the registration completion is set.
- Launching with a cached account starts directly at `StudentHome()` or `CoachHome`. The guest graph is never on the stack.
- On iOS, all of this runs inside the Compose `UIViewController`. There is no SwiftUI `NavigationStack` and no native edge swipe-back by default (Decision 1). Android system Back works through Navigation Compose.

## DI (Koin, `composeApp` only)
- `FakeDataModule` binds every repository and contract to its Phase 2 fake or SQL implementation. It is the only module that changes at the Phase 5 Firebase cutover. Bindings:
  - `TrxDatabase` from the platform `SqlDriver`
  - `SessionStore → SqlSessionStore`
  - `PersonRepository → SqlPersonRepository`
  - `ClassRepository → SqlClassRepository`
  - `MembershipRepository → SqlMembershipRepository`
  - `AttendanceRepository → SqlAttendanceRepository`
  - `NetworkMonitor → MutableNetworkMonitor(ONLINE)`
  - `UserRepository → InMemoryUserRepository`
  - `PasswordHasher → FakePasswordHasher` (P2)
  - `AuthRepository → FakeAuthRepository`
  - `AttendanceUploader → FakeAttendanceUploader`
  - `OutboxSync`, and the Phase 1 use cases that flow 1 ViewModels need (`RequestToJoin`)
- `ViewModelModule`: `viewModel { RootViewModel(...) }`, `viewModel { GuestClassListViewModel(...) }`, and `viewModel { (origin: RegistrationOrigin) -> RegistrationViewModel(origin, ...) }`.
- `PlatformModule` (`expect`/`actual`): the `SqlDriver` and the `ShakeSource`.
- Known fake limit: `InMemoryUserRepository` loses users when the process dies. The cached session survives (SQLDelight), but that user cannot sign in again after a restart. This is acceptable until Phase 5.

## Proposed changes to `shared` (small; Tester adds Phase 2-style tests in `phase2JvmTest`)
- **P1. Registration emits navigation.** Add `data class OpenSignedInRoot(val role: Role, val openClassId: String?) : Navigation` to the sealed `Navigation` (same file, same pattern as its siblings). On every success where Phase 2 sets `completion`, `RegistrationViewModel` also sets `navigation = OpenSignedInRoot(completion.role, completion.openClassId)`. `completion` is unchanged, so Phase 2 AC32–35 still hold.
- **P2. `FakePasswordHasher`** in `data.fake`, implementing `PasswordHasher`. It is deterministic and not for production, and `verify(p, hash(p))` is true. Phase 5 removes it (Firebase Auth owns passwords).
- **P3. `RegistrationUiState.canSubmit: Boolean`.** True when not `isSubmitting` and every *visible* registration field is non-blank: name, email, password, and employee ID only in `STUDENT` mode. This drives the B1 disabled look (Decision 2). `onSubmit()` behavior is unchanged.
- **P4. `UiCopy` additions** for every flow 1 string in the copy table below. Typed values marked *Q3* use the approved copy from Decision 3.
- **P5. `RequestingClassHeader.scheduleLine()`** (added in review, 2026-11-12): a public extension in `presentation.classdetail` that formats the B5 card line ("Coach … · days · slots"), so the Phase 4 SwiftUI card can reuse it. It needs a direct `phase2JvmTest`.
- **P6. Shake ignored while submitting** (added in review): `onShake()` does nothing while `isSubmitting` is true. It needs a `phase2JvmTest`.
- Launches in `RegistrationViewModel` use plain `viewModelScope.launch` (no `CoroutineStart.UNDISPATCHED`, no `Dispatchers.Unconfined`).

No other ViewModel or business-logic change. The UI never calls a repository or use case directly.

## Copy table (flow 1; exact strings, with the source frame in brackets)
| Typed value / slot | String |
|---|---|
| Overline student / coach | "TRX ATTENDANCE" / "COACH ACCOUNT" [B1 `22:1603`, B4 `22:1989`] |
| Subtitle FromClassList / coach / FromRequestToJoin | "Join classes and record your attendance." / "Create classes, invite students and show the daily code." / "Create your student account to send this request." [B1, B4, B5] |
| Field labels | "Full name", "Email", "Password", "Employee ID" [B1] |
| Placeholders | "Your name", "name@company.com", "At least 8 characters" (registration password), "e.g. EMP-20417", "Your password" (sign-in password) [B1] |
| Helpers | Password "At least 8 characters." · Employee ID "Your company employee ID. Each ID can be registered once." [B1] |
| Divider / section | "Already have an account?" / "Sign in" (heading and button) [B1] |
| Requesting card | "REQUESTING TO JOIN", then `{class name}`, then `"Coach {coach} · {days} · {slots}"` [B5 `22:2105`] |
| Sign-in note (FromRequestToJoin) | "Signing in also sends your request for {class name}." [B5 `22:2168`] |
| Nav back label | FromClassList "Classes" · FromRequestToJoin "Back" [B1, B5] |
| Existing Phase 2 copy reused | titles, submit labels, "Creating account…", `InvalidEmail`, `EmployeeIdTaken`, `PasswordTooShort`, banner "No internet connection" |
| `FieldError.Required` *Q3* | "Required." |
| `FieldError.EmailTaken` *Q3* | "This email is already registered." |
| `UiError.AuthFailed` (sign in) *Q3* | "Email or password is incorrect." |
| `UiError.Offline` (submit or sign in) *Q3* | "You’re offline. Nothing was saved." |
| Guest placeholder button | "Register or sign in" [FIGMA-NOTES: "A1 → 'Register or sign in' also reaches B1"] |

## Task breakdown
1. **Tester.** Write failing tests for F1-AC1–40: `composeApp` `commonTest` UI tests, `composeApp` `jvmTest` for the Koin and source-scan checks, and `shared` `phase2JvmTest` additions for P1–P4. Use the minimum Gradle scaffolding needed for them to compile red: add the module and `jvm()` target, but no production UI.
2. **Builder.**
   1. The `composeApp` module: Gradle setup, catalog entries, and `androidApp` switched to it with `TrxApplication`.
   2. Shared P1–P4.
   3. Theme and components.
   4. `RegistrationScreen` and `RegistrationRoute`.
   5. `ShakeDetector`, `AndroidShakeSource`, and `NoShakeSource`.
   6. Routes, `AppNavHost`, and placeholders.
   7. The Koin modules.
   8. Update `docs/project-structure.md` and the ARCHITECTURE.md DI line ("Koin only if wiring gets noisy" becomes the locked Koin decision).
3. **Reviewer.** Check against these ACs, run all suites, and do a manual Android emulator pass (see "Manual checks").
4. **Sheen.** Review and merge the PR. Phase 4 iOS checks of flow 1 can start once it is merged.

## Test approach
- **Compose UI tests:** `composeApp/src/commonTest`, using `runComposeUiTest` (`@OptIn(ExperimentalTestApi::class)`, `org.jetbrains.compose.ui:ui-test`). They run on Linux as `:composeApp:jvmTest` through the `jvm()` target (headless Skiko). They do not need an emulator or `androidUnitTest`/Robolectric. Android device or instrumented tests are not required in flow 1.
- **Screen tests** call `RegistrationScreen(state, callbacks)`, or `RegistrationRoute` with a ViewModel built directly with fakes:
  - `FakeAuthRepository` on an `InMemoryUserRepository`, with `FakePasswordHasher`, `MutableNetworkMonitor`, `SqlSessionStore`, and `SqlPersonRepository` on an in-memory JDBC `TrxDatabase`.
  - For B6, a test-only `GatedAuthRepository` that suspends on a `CompletableDeferred` and counts calls.
  - A `FakeShakeSource` (a `MutableSharedFlow<Unit>`) is passed as a parameter.
- **Navigation tests** compose `AppNavHost` with ViewModel factories passed as parameters (no Koin) and a test `NavHostController` from `rememberNavController()`, exposed through a callback. They assert on `navController.currentBackStack.value` and on `currentDestination?.hasRoute<T>()`.
- **Koin is used in only two tests:** `startKoin { modules(appModules()) }` with the jvm `PlatformModule` in `jvmTest`, to check that the graph resolves.
- **Source-scan tests** (`jvmTest`) read files under the repo root to enforce structural rules (F1-AC3–6).
- **Test tag convention:** `snake_case` `"<screen>_<element>[_<qualifier>]"`. Constants live in one `object` per screen (`RegistrationTestTags`, `HomeTestTags`, `ComponentTestTags`). Field tags are `registration_<field>_field`, `registration_<field>_error`, `registration_<field>_error_icon`, and `registration_<field>_helper`, where `<field>` is one of `name`, `email`, `password`, `employee_id`, `signin_email`, `signin_password`. Other tags:
  - Buttons: `registration_submit_button`, `registration_submit_progress`, `registration_signin_button`, `registration_signin_progress`, `registration_password_toggle`, `registration_signin_password_toggle`
  - Screen elements: `registration_back`, `registration_title`, `registration_requesting_card`, `registration_signin_note`, `registration_coach_band`, `registration_form_error`, `registration_signin_error`
  - Other screens: `offline_banner`, `guest_home_register_button`, `student_home`, `coach_home`
- **Theme mode** is exposed for tests as a custom semantics property `TrxThemeModeKey` (`Light`/`Dark`) on the screen root.
- **Manual checks** (Reviewer, Android emulator; not ACs):
  - A real shake (emulator virtual sensors) toggles student and coach.
  - The keyboard types are email, password, and text.
  - System Back on the student home exits the app.

## Acceptance criteria

### Build and structure
- **F1-AC1.**
  - `settings.gradle.kts` includes `:composeApp`.
  - `composeApp` is a KMP module with Android, `iosArm64`, `iosSimulatorArm64`, and `jvm()` targets, and depends on `:shared`. `androidApp` depends on `:composeApp`.
  - `MainActivity` calls `setContent { TrxApp() }`, and the manifest names `.TrxApplication`.
  - `./gradlew :androidApp:assembleDebug` succeeds, and `./gradlew :composeApp:jvmTest` runs every flow 1 UI test. iOS targets are declared and not built.
- **F1-AC2.**
  - `JAVA_HOME=/home/box/tools/jdk17 ./gradlew :shared:jvmTest` still passes all 116 existing tests, and `:shared:phase2JvmTest` passes the existing 100 plus the new P1–P4 tests. No existing test is changed or weakened.
  - P1: after a registration or sign-in success, `navigation == OpenSignedInRoot(role, openClassId)`, matching `completion`, and `onNavigationHandled()` clears it.
  - P2: `FakePasswordHasher.verify(p, hash(p))` is true, and `verify("other", hash(p))` is false.
  - P3: `canSubmit` follows its definition in STUDENT mode, in COACH mode (employee ID ignored), and while submitting.
  - P4: every *Q3* and flow 1 string in the copy table matches `UiCopy` exactly.
- **F1-AC3.** No file under `shared/src` contains `import org.koin` (source-scan test).
- **F1-AC4.** Files in `composeApp/src/commonMain/kotlin/ph/project/active/ui/**` do not import `ph.project.active.data.repository`, `ph.project.active.data.fake`, `ph.project.active.domain.repository`, or `ph.project.active.domain.usecase`. Only the `di` package may (source-scan test).
- **F1-AC5.** Each `.kt` file under `composeApp/src/*Main` declares at most one public top-level class, interface, or object. Each route (`GuestGraph`, `GuestHome`, `Registration`, `StudentHome`, `CoachHome`) and each Koin module (`FakeDataModule`, `ViewModelModule`, `PlatformModule`) is in a file named after it (source-scan test).
- **F1-AC6.** No Gradle file or `libs.versions.toml` mentions `firebase` (source-scan test).

### DI
- **F1-AC7.** In `jvmTest`, `startKoin { modules(appModules()) }` resolves `RootViewModel`, `GuestClassListViewModel`, `RegistrationViewModel` with `parametersOf(FromClassList)`, and `RegistrationViewModel` with `parametersOf(FromRequestToJoin("c1"))` without throwing. `get<AuthRepository>()` is a `FakeAuthRepository`, and two `get<SessionStore>()` calls return the same instance.
- **F1-AC8.** `AuthRepository`, `SessionStore`, `PersonRepository`, `ClassRepository`, `MembershipRepository`, `NetworkMonitor`, `UserRepository`, and `PasswordHasher` are bound only in `FakeDataModule.kt` (source-scan: their `single`/`factory` bindings appear in no other file).

### Theme
- **F1-AC9.** `TrxColors.Light` and `TrxColors.Dark` hold the `FIGMA-NOTES.md` values for:
  - bg: `canvas`, `surface`, `surfaceAlt`
  - border: `default`, `strong`
  - text: `primary`, `secondary`, `tertiary`
  - accent: `default`, `text`, `subtle`
  - status: `dangerFg`
  - other: `focusRing`

  For example, light `canvas = #F8F8F9`, dark `canvas = #0B0B0C`, and `accentDefault = #FFC20E` in both modes.

### B1/B2: student registration
- **F1-AC10.** With a fresh `FromClassList` ViewModel, the screen shows every B1 string from the copy table in its slot:
  - the overline, title, and subtitle
  - the four labels, placeholders, and two helpers
  - "Already have an account?", the "Sign in" heading, and the sign-in placeholders
  - the "Create account" and "Sign in" buttons
  - back label "Classes"

  `registration_requesting_card` and `registration_signin_note` do not exist.
- **F1-AC11.** Typing into each of the six fields updates the matching ViewModel field (for example, `state.name == "Ella Navarro"`) and the field shows that value.
- **F1-AC12.** Both password fields are masked by default. Tapping `registration_password_toggle` shows the plain text, and tapping it again masks it. The sign-in toggle works the same way. The ViewModel state does not change on toggle.
- **F1-AC13.** `registration_submit_button` is disabled when `canSubmit` is false (B1) and enabled when it is true (B2). `registration_signin_button` is enabled whenever `isSubmitting` is false.
- **F1-AC14.** Filling valid student fields and tapping "Create account" creates exactly one account. `SessionStore.account` is a `STUDENT` with the trimmed values.

### B3: errors
- **F1-AC15.**
  - (a) Submitting a filled form with email "ella.navarro@acme" shows `registration_email_error` "Enter a valid email address.".
  - (b) With an existing student `EMP-20611`, submitting a valid form with employee ID "EMP-20611" shows `registration_employee_id_error` "This employee ID is already registered.".
  - In both cases the error node has its `_error_icon` sibling, the field reports the semantics `error`, and the error replaces that field's helper (`registration_<field>_helper` does not exist while the error shows). No account is created.
- **F1-AC16.** Each `FieldError` renders its copy-table text:
  - `PasswordTooShort` → "At least 8 characters."
  - `EmailTaken` → "This email is already registered."
  - `Required` → "Required." (sign-in fields, F1-AC27)
- **F1-AC17.** Editing a field that has an error removes that field's error node and leaves other fields' errors in place.

### B4: coach (shake)
- **F1-AC18.** On a `FromClassList` screen, one emission on `FakeShakeSource` shows the coach state:
  - `registration_title` reads "Coach registration", the overline "COACH ACCOUNT", and the coach subtitle.
  - `registration_employee_id_field` does not exist, and the primary button reads "Create coach account".
  - `registration_coach_band` exists, and `TrxThemeModeKey == Dark`.

  A second emission restores B1 (`Light`, employee ID field back).
- **F1-AC19.** Name, email, and password typed before a shake still show after it and after shaking back. The employee ID typed before the first shake shows again after shaking back.
- **F1-AC20.** In both modes there is no visible role control: no node has the `Switch`, `Checkbox`, `RadioButton`, `Tab`, or `DropdownList` role, and no text contains "shake" (case-insensitive).
- **F1-AC21.** Filling the coach fields and tapping "Create coach account" creates one `COACH` account with `employeeId == null`, and the app shows `coach_home` (see F1-AC33).
- **F1-AC22.** `ShakeDetector(threshold, window, minHits, cooldown)` gets a sequence of `(x, y, z, timestamp)` samples.
  - It emits exactly one shake when at least `minHits` samples within `window` have `|a| − g > threshold`.
  - It emits nothing for a phone at rest (`|a| ≈ 9.81`).
  - It emits no second shake within `cooldown` of the first.

  These are pure `commonTest` tests with explicit parameters.

### B5: from Request to join
- **F1-AC23.** A class "TRX Core Burn" by coach "Mara Santos" (Mon and Wed, 17:30–18:30) is saved in the repository, and `Registration("<id>")` is opened. The screen shows:
  - `registration_requesting_card` with "REQUESTING TO JOIN", "TRX Core Burn", and "Coach Mara Santos · Mon · Wed · 5:30 PM – 6:30 PM"
  - the subtitle "Create your student account to send this request."
  - the primary button "Create account and send request"
  - `registration_signin_note` "Signing in also sends your request for TRX Core Burn."
  - back label "Back"
- **F1-AC24.** On that screen, a shake emission changes nothing: the title stays "Create account" and the employee ID field stays.
- **F1-AC25.** For an unknown class id (`requestingClass == null`), the card and the sign-in note are absent. The B5 subtitle and submit label still show.

### B6: submitting
- **F1-AC26.** With `GatedAuthRepository` holding the call:
  - Tapping "Create account" shows `registration_submit_progress` and the label "Creating account…".
  - Every text field, both password toggles, and `registration_signin_button` are disabled.
  - A second tap on the primary button makes no second repository call (count 1).

  After the gate is released, the progress indicator is gone. While a sign-in is gated, `registration_signin_progress` shows, the sign-in label stays "Sign in", and the submit button is disabled.

### Sign in
- **F1-AC27.** Tapping "Sign in" with both sign-in fields blank shows "Required." under each of them and makes no `signIn` call.
- **F1-AC28.** A wrong password shows `registration_signin_error` "Email or password is incorrect.". No session is saved, and the current destination stays `Registration`.
- **F1-AC29.**
  - An existing student signing in lands on `student_home`; an existing coach lands on `coach_home`.
  - A coach signing in from the student mode (B1) lands on `coach_home`, and a student signing in from coach mode (B4) lands on `student_home`.
- **F1-AC30.** On a `Registration("<id>")` screen:
  - A student signing in lands on `StudentHome(openClassId = "<id>")`, and exactly one `PENDING_REQUEST` exists for that student and class.
  - A coach signing in there lands on `CoachHome`, and no membership is created.

### Offline
- **F1-AC31.**
  - While `MutableNetworkMonitor` is `OFFLINE`, `offline_banner` shows "No internet connection". Setting `ONLINE` removes it without recreating the screen.
  - Submitting valid fields offline creates no account and shows `registration_form_error` "You’re offline. Nothing was saved.". The destination stays `Registration`.
  - Signing in offline shows the same text in `registration_signin_error`.

### Navigation
- **F1-AC32.**
  - Launching with no cached account starts at `GuestHome` inside `GuestGraph`.
  - Tapping `guest_home_register_button` leaves the back stack as `[GuestHome, Registration(null)]`, and `GuestClassListViewModel.state.navigation == null` afterwards.
  - Tapping it twice quickly still leaves exactly one `Registration` entry.
- **F1-AC33.**
  - After a successful student registration from `Registration(null)`, the current destination is `StudentHome(openClassId = null)`. No back-stack entry has a `GuestGraph`, `GuestHome`, or `Registration` route, and `navController.previousBackStackEntry == null`.
  - The coach equivalent ends at `CoachHome` with the same back-stack conditions.
- **F1-AC34.** After a successful registration from `Registration("<id>")`, the current destination is `StudentHome(openClassId = "<id>")`, and exactly one `PENDING_REQUEST` exists (one tap, one request).
- **F1-AC35.** Exactly-once handling:
  - After any successful registration or sign-in, the back stack holds exactly one signed-in entry.
  - It still holds exactly one after three forced recompositions (flip a test `mutableStateOf`, `waitForIdle`) and after `RootViewModel` emits the new root.
  - `RegistrationViewModel.state.navigation == null` once idle.
- **F1-AC36.** The nav bar back (`registration_back`) on `Registration(null)` pops to `GuestHome` (back stack `[GuestHome]`), and no account is created.
- **F1-AC37.**
  - Launching with a cached `STUDENT` account (`SessionStore` saved before composition) starts at `StudentHome(null)`, and a cached `COACH` starts at `CoachHome`. No guest route is on the back stack.
  - On a file-backed JDBC database, an account saved in one composition still shows `student_home` after a new `TrxApp` composition, with a new driver on the same file.
- **F1-AC38.** `student_home` shows the account's name. `coach_home` shows the coach's name. `StudentHome` exposes its `openClassId` route argument for tests (for example, as a tagged text `student_home_open_class` when it is non-null). Neither placeholder offers sign-out.

### Accessibility
- **F1-AC39.** Each text field, both large buttons, both password toggles, `registration_back`, and `guest_home_register_button` are at least 48dp tall (`assertHeightIsAtLeast(48.dp)`). Each large button is 52dp tall.
- **F1-AC40.** Every error row pairs its icon with text (F1-AC15). The password toggles have content descriptions "Show password" and "Hide password", matching the current state.

## Decisions (were open questions; Sheen approved the defaults on 2026-11-12)
1. **iOS swipe-back.** Navigation runs inside the Compose view on iOS, so there is no native edge swipe-back unless we add one (for example, a Compose back gesture handler or a UIKit bridge). **Decision:** no iOS swipe-back in flow 1. The nav bar back arrow works on both platforms. Revisit during the Phase 4 iOS device checks.
2. **"Create account" disabled until filled?** B1 (`22:1652`) draws the primary button in the disabled style while fields are empty, and B3 draws it enabled once fields are filled. "Sign in" is drawn in the secondary style on every frame, not disabled. **Decision:** match Figma. The primary is disabled until every visible registration field is non-blank (`canSubmit`, P3). "Sign in" is always enabled, so blank sign-in fields show "Required.". Typed errors (invalid email, short password, taken email or ID) still come from submit.
3. **Copy Figma doesn't show.** No B frame has text for a required field, a taken email, a failed sign-in, or an offline submit. **Decisions:**
   - Required field: "Required."
   - Taken email: "This email is already registered." (mirrors the B3 employee ID line)
   - Failed sign-in: "Email or password is incorrect." (does not reveal which one is wrong)
   - Offline submit: "You’re offline. Nothing was saved." (shown with the existing "No internet connection" banner)

   Designer can override any of these before Tester starts.

## Sources read
- **GitHub `main` @ `9e6ad0d`:**
  - Docs: `ARCHITECTURE.md`, `SPEC-phase2.md`, `docs/project-structure.md`.
  - Build: `settings.gradle.kts`, `gradle/libs.versions.toml`, `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `MainActivity.kt`, and `App.kt` in `shared`.
  - `presentation/root`: `Root`, `RootUiState`, `RootViewModel`.
  - `presentation/registration`: `RegistrationViewModel`, `RegistrationUiState`, `AuthCompletion`.
  - `presentation/common`: `UiCopy`, `UiError`, `FieldError`, `Field`, `Navigation`, `RegistrationOrigin`, `ConnectionUi`, `AccountRole`.
  - `presentation/classdetail`: `RequestingClassHeader`, `JoinOutcome`.
  - Data and domain: `data/fake/FakeAuthRepository`, the `data/repository` listing, `domain/crypto/PasswordHasher`, and the `domain/repository/inmemory` listing.
  - The `iosApp/iosApp` listing.
- **Local, read-only:** `/workspace/trx-attendance/FIGMA-NOTES.md` and `/workspace/trx-attendance/ARCHITECTURE.md`.
- **Figma** `qbHRsTH5EHXslAnGr7tV3o`: section `25:4902` metadata; design context for B1, B3, B4, B5, and B6 and all six Dev Notes cards. B2 was read through metadata and its Dev Notes only, since its layout matches B1.
