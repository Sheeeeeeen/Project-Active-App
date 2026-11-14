package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F1Ac1ModuleTest {
    @Test
    fun F1_AC1_composeAppIsIncludedAndHostedByAndroid() {
        val root = repoRoot()
        val problems = mutableListOf<String>()
        val settings = File(root, "settings.gradle.kts").readText()
        if (!settings.contains("include(\":composeApp\")")) {
            problems += "settings.gradle.kts does not include :composeApp"
        }
        val build = File(root, "composeApp/build.gradle.kts")
        if (!build.isFile) {
            problems += "composeApp/build.gradle.kts is missing"
        } else {
            val text = build.readText()
            if (!Regex("""\bjvm\s*(\(|\{)""").containsMatchIn(text)) problems += "composeApp has no jvm() target"
            if (!text.contains("iosArm64()")) problems += "composeApp does not declare iosArm64()"
            if (!text.contains("iosSimulatorArm64()")) problems += "composeApp does not declare iosSimulatorArm64()"
            if (!Regex("""(?m)^\s*android(Target)?\s*(\(|\{)""").containsMatchIn(text)) {
                problems += "composeApp does not declare an Android target"
            }
            if (!text.contains("project(\":shared\")")) problems += "composeApp does not depend on :shared"
        }
        val androidBuild = File(root, "androidApp/build.gradle.kts").readText()
        if (!androidBuild.contains("project(\":composeApp\")")) {
            problems += "androidApp does not depend on :composeApp"
        }
        val activity = File(root, "androidApp/src/main/kotlin/ph/project/active/MainActivity.kt").readText()
        if (!Regex("""setContent\s*\{\s*TrxApp\s*\(\s*\)\s*\}""").containsMatchIn(activity)) {
            problems += "MainActivity does not call setContent { TrxApp() }"
        }
        val manifest = File(root, "androidApp/src/main/AndroidManifest.xml").readText()
        if (!manifest.contains(".TrxApplication")) {
            problems += "AndroidManifest does not name .TrxApplication"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }
}
