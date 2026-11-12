package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F1Ac5OnePublicTypeTest {
    @Test
    fun F1_AC5_eachMainFileHasOnePublicTypeAndRoutesAndModulesAreNamedFiles() {
        val src = File(repoRoot(), "composeApp/src")
        val mainDirs = src.listFiles()?.filter { it.isDirectory && it.name.endsWith("Main") }.orEmpty()
        val files = mainDirs.flatMap { kotlinSources(it) }
        val problems = mutableListOf<String>()
        for (file in files) {
            val types = KotlinPublicTypes.publicTopLevelTypeNames(file.readText())
            if (types.size > 1) {
                problems += "${file.path} declares ${types.size} public types: $types"
            }
        }
        val required = listOf(
            "GuestGraph",
            "GuestHome",
            "Registration",
            "StudentHome",
            "CoachHome",
            "FakeDataModule",
            "ViewModelModule",
            "PlatformModule",
        )
        for (name in required) {
            val matches = files.filter { it.name == "$name.kt" }
            if (matches.isEmpty()) {
                problems += "$name.kt is missing under composeApp/src/*Main"
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }
}
