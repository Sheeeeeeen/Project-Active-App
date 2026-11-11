pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "trx-attendance"

include(":shared")      // domain / data / presentation (Phase 1 tests live here)
include(":composeApp")  // shared Compose Multiplatform UI (empty in Phase 1)
include(":androidApp")  // thin Android host (Play Store entry point)
// iosApp/ is a thin Xcode host, not a Gradle module. It embeds composeApp's ComposeApp framework.
