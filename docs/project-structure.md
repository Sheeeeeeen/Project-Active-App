# Project structure

Gradle is the build. Three modules: `shared` (product code), `androidApp`, and `iosApp`. The apps draw screens. The rules live in `shared`.

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

`presentation.<feature>` is one folder per ViewModel (the ViewModel plus its own state types). Types shared by more than one screen stay in `presentation.common`.

See [ARCHITECTURE.md](../ARCHITECTURE.md) for how the pieces fit together.
