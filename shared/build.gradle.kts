import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("TrxDatabase") {
            packageName.set("ph.project.active.data.db")
            srcDirs("src/commonMain/sqldelight")
        }
    }
}

kotlin {
    jvmToolchain(17)
    // Phase 1 domain tests run as :shared:jvmTest (JDK Ed25519). Sheen's Android and iOS
    // targets stay in place; iOS Ed25519 remains the Phase 1 stub.
    jvm {
        // Phase 2 tests live under src/commonTest/.../ph/project/active and reference
        // types Builder has not written yet. They compile as their own JVM suite so the
        // Phase 1 commonTest compilation still runs. :shared:jvmTest finalizes with this suite.
        val jvmTarget = this
        compilations.create("phase2Test") {
            associateWith(jvmTarget.compilations.getByName("main"))
            defaultSourceSet {
                kotlin.setSrcDirs(listOf("src/commonTest/kotlin"))
                kotlin.include("ph/project/active/**/*.kt")
                // Phase 1 domain tests stay on the jvmTest classpath (AC85 Class.forName).
                kotlin.exclude("ph/project/active/domain/**")
                dependencies {
                    implementation(libs.kotlin.test)
                    implementation(libs.kotlin.testJunit)
                    implementation(libs.kotlinx.datetime)
                    implementation(libs.kotlinx.coroutines.core)
                    implementation(libs.kotlinx.coroutines.test)
                    implementation(libs.sqldelight.sqlite.driver)
                    implementation(libs.androidx.lifecycle.viewmodelCompose)
                }
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts.add("-lsqlite3")
        }
    }
    android {
       namespace = "ph.project.active.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.sqldelight.android.driver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        getByName("commonTest") {
            // JDBC Phase 2 tests compile only in the JVM phase2Test source set.
            // Domain tests stay here so AC85 can load them from the jvmTest classpath.
            kotlin.exclude("ph/project/active/data/**")
            kotlin.exclude("ph/project/active/presentation/**")
            kotlin.exclude("ph/project/active/testkit/**")
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.androidx.testRunner)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

val phase2Compilation = kotlin.targets.getByName("jvm").compilations.getByName("phase2Test") as KotlinJvmCompilation

tasks.register<Test>("phase2JvmTest") {
    group = "verification"
    description = "Phase 2 SQLDelight, fake, and ViewModel tests. Red until Builder adds ph.project.active."
    testClassesDirs = phase2Compilation.output.classesDirs
    classpath = phase2Compilation.output.classesDirs + phase2Compilation.runtimeDependencyFiles
    dependsOn(phase2Compilation.compileTaskProvider)
    useJUnit()
}

tasks.named("jvmTest") {
    finalizedBy("phase2JvmTest")
}