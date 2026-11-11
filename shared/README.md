# shared (KMP)

Product code for TRX class attendance. Package root is `ph.project.active`.

- `domain.model`, `domain.repository`, `domain.repository.inmemory`, `domain.usecase`, `domain.crypto`
- `data.model`, `data.repository`, `data.fake`, `data.sync`, `data.db` (`TrxDatabase`)
- `presentation.common` and one `presentation.<feature>` folder per ViewModel

Phase 1 tests live in `src/commonTest/kotlin/ph/project/active/domain/`. Phase 2 tests live under `src/commonTest/kotlin/ph/project/active/` and run as `phase2JvmTest`, which `:shared:jvmTest` finalizes.

```
./gradlew :shared:jvmTest
```

JDK 17 comes from `jvmToolchain(17)`. iOS targets stay declared and are not built on Linux.
