import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvmToolchain(17)
    // Flow 1 UI tests run on the JVM. iOS targets are declared and not built on Linux.
    jvm {
        val jvmTarget = this
        // Source scans stay in their own compilation so they still run if a UI test fails.
        compilations.create("structureTest") {
            associateWith(jvmTarget.compilations.getByName("main"))
            defaultSourceSet {
                kotlin.setSrcDirs(listOf("src/jvmTest/kotlin"))
                kotlin.include("ph/project/active/structure/**/*.kt")
                dependencies {
                    implementation(libs.kotlin.test)
                    implementation(libs.kotlin.testJunit)
                }
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }

    android {
        namespace = "ph.project.active.compose"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.components.resources)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        // UI tests live under src/commonTest and compile on the JVM test compilation,
        // which can see the JDBC driver and the desktop test runtime.
        getByName("commonTest") {
            kotlin.setSrcDirs(emptyList<Any>())
        }
        jvmTest.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlin.test)
            implementation(libs.kotlin.testJunit)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.uiTest)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.test)
            implementation(libs.sqldelight.sqlite.driver)
        }
        getByName("jvmTest") {
            kotlin.srcDir("src/commonTest/kotlin")
        }
    }
}

val structureCompilation =
    kotlin.targets.getByName("jvm").compilations.getByName("structureTest") as KotlinJvmCompilation

tasks.register<Test>("structureJvmTest") {
    group = "verification"
    description = "Phase 3 flow 1 source-scan tests (F1-AC1, F1-AC3 to F1-AC6, F1-AC8)."
    testClassesDirs = structureCompilation.output.classesDirs
    classpath = structureCompilation.output.classesDirs + structureCompilation.runtimeDependencyFiles
    dependsOn(structureCompilation.compileTaskProvider)
    useJUnit()
}
