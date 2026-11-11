# shared (KMP)

Phase 1 shared domain for TRX Class Attendance, package `trx.attendance.domain`.

Run the tests on Linux:

```
JAVA_HOME=/home/box/tools/jdk17 /home/box/tools/gradle-8.10.2/bin/gradle :shared:jvmTest
```

Tests live in `src/commonTest/kotlin/trx/attendance/domain/` and each test name starts with its
acceptance-criterion number from `SPEC-phase1.md` (AC1–AC41). AC42 (this command passes on Linux;
iOS targets declared, not built) is covered by the Gradle wiring and goes green once the domain
in `src/commonMain` is implemented. `TestWorld.kt` shows every constructor the tests expect.

Module layout: `shared` (domain/data/presentation), `composeApp` (shared Compose Multiplatform UI,
empty in Phase 1), `androidApp` (thin Android host), `iosApp` (thin Xcode host, built on a Mac).
`org.gradle.configureondemand=true` keeps `:shared:jvmTest` from configuring the Android modules,
so it runs on Linux without an Android SDK.

Gradle output goes to `gradle-build/` at the repo root. The repo-root `build/` folder holds the
design generator scripts and must not be used or cleaned by Gradle.
