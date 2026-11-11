// Shared Compose Multiplatform UI for Android and iOS. Empty in Phase 1 (no screens yet).
plugins {
    kotlin("multiplatform")
    kotlin("plugin.compose")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }
    }
}

android {
    namespace = "trx.attendance.composeapp"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
}
