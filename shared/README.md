# shared (KMP)

Sheen's Compose Multiplatform library (`ph.com.sheen.projectactive`) plus the Phase 1 TRX attendance domain (`trx.attendance.domain`).

Domain sources: `src/commonMain/kotlin/trx/attendance/domain/`.
Tests: `src/commonTest/kotlin/trx/attendance/domain/`. Each test name starts with its acceptance-criterion number from `SPEC-phase1.md` (AC1–AC41).

Run the domain tests on the JVM (JDK Ed25519). iOS Ed25519 is still the Phase 1 stub and is not built on Linux.

```
./gradlew :shared:jvmTest
```
