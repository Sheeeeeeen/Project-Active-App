package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F1Ac6NoFirebaseTest {
    @Test
    fun F1_AC6_gradleFilesDoNotMentionFirebase() {
        val root = repoRoot()
        val hits = root.walkTopDown()
            .onEnter { directory -> directory.name !in setOf("build", ".gradle", ".git") }
            .filter { file ->
                file.isFile && (
                    file.name.endsWith(".gradle") ||
                        file.name.endsWith(".gradle.kts") ||
                        file.name == "libs.versions.toml"
                    )
            }
            .filter { file -> file.readText().contains("firebase", ignoreCase = true) }
            .toList()
        assertTrue(hits.isEmpty(), "Gradle files mention firebase:\n" + hits.joinToString("\n"))
    }
}
