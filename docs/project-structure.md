# Project structure

Gradle is the build. Four modules: `shared` (rules and ViewModels), `composeApp` (Compose Multiplatform screens, navigation, and Koin), `androidApp` (Android host; starts Koin and calls `TrxApp()`), and `iosApp` (Xcode host; embeds the static `ComposeApp` framework, which starts Koin and shows `TrxApp()`). The rules live in `shared`. Screens live in `composeApp`. The iOS app links only `ComposeApp`, and that framework already includes `shared`.

SQLDelight database `TrxDatabase` is generated in `ph.project.active.data.db`. Firebase Auth and Firestore arrive in Phase 5. Until then, `ph.project.active.data.fake` stands in for them.

## Packages

```text
ph.project.active.domain.model
ph.project.active.domain.repository
ph.project.active.domain.repository.inmemory
ph.project.active.domain.usecase
ph.project.active.domain.crypto
ph.project.active.data.model
ph.project.active.data.repository
ph.project.active.data.fake
ph.project.active.data.sync
ph.project.active.data.db
ph.project.active.presentation.common
ph.project.active.presentation.<feature>
```

`composeApp` keeps the same root and splits UI by role:

```text
ph.project.active.ui
ph.project.active.ui.navigation
ph.project.active.ui.registration
ph.project.active.ui.home
ph.project.active.ui.theme
ph.project.active.ui.components
ph.project.active.ui.platform
ph.project.active.di
```

Each public type is its own file. Routes are `@Serializable` and there is one `NavHost`. Koin modules (`FakeDataModule`, `ViewModelModule`, `PlatformModule`) live only under `ph.project.active.di`.

`presentation.<feature>` is one folder per ViewModel (the ViewModel plus its own state types). Types shared by more than one screen stay in `presentation.common`.

Registration and sign-in already sit in feature folders: `ui/registration` for the route and screen, `presentation/registration` for the ViewModel and its state. Those packages stay put because the test suites import them by name.

Read that flow in this order:

1. `composeApp/src/commonMain/kotlin/ph/project/active/ui/navigation/AppNavHost.kt`
2. `composeApp/src/commonMain/kotlin/ph/project/active/ui/registration/RegistrationRoute.kt`
3. `composeApp/src/commonMain/kotlin/ph/project/active/ui/registration/RegistrationScreen.kt`
4. `shared/src/commonMain/kotlin/ph/project/active/presentation/registration/RegistrationViewModel.kt`
5. `shared/src/commonMain/kotlin/ph/project/active/data/fake/FakeAuthRepository.kt`

See [ARCHITECTURE.md](../ARCHITECTURE.md) for how the pieces fit together.
