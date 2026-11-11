// Thin Android host (Play Store entry point). All UI lives in composeApp.
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "trx.attendance.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "trx.attendance"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation("androidx.activity:activity-compose:1.9.3")
}
