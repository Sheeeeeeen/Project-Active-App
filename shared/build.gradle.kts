// AC42: `./gradlew :shared:jvmTest` must pass on Linux once the Phase 1 domain is implemented.
// iOS targets are declared so the module shape is right, but they are not built on Linux.
// Phase 1 deliberately has no Android target here so the tests need no Android SDK; composeApp's
// Android target consumes the jvm() variant. Add androidTarget() in a later phase if needed.
plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(17)

    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
        }
    }
}
