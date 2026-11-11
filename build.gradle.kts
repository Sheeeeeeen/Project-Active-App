plugins {
    kotlin("multiplatform") version "2.0.21" apply false
    kotlin("android") version "2.0.21" apply false
    kotlin("plugin.compose") version "2.0.21" apply false
    id("org.jetbrains.compose") version "1.7.0" apply false
    id("com.android.application") version "8.5.2" apply false
    id("com.android.library") version "8.5.2" apply false
}

// The repo-root build/ directory holds the Figma/design generator scripts, not Gradle output.
// Keep every Gradle output under gradle-build/ so it is never clobbered.
layout.buildDirectory.set(file("gradle-build/root"))
subprojects {
    layout.buildDirectory.set(rootProject.file("gradle-build/${project.name}"))
}
