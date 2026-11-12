package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F1Ac4UiImportsTest {
    @Test
    fun F1_AC4_uiSourcesDoNotImportRepositoriesOrUseCases() {
        val root = repoRoot()
        val ui = File(root, "composeApp/src/commonMain/kotlin/ph/project/active/ui")
        assertTrue(
            ui.isDirectory,
            "composeApp ui package is missing, so the import rule is not enforced on any screen",
        )
        val forbidden = listOf(
            "import ph.project.active.data.repository",
            "import ph.project.active.data.fake",
            "import ph.project.active.domain.repository",
            "import ph.project.active.domain.usecase",
        )
        val uiSources = kotlinSources(ui)
        assertTrue(
            uiSources.isNotEmpty(),
            "composeApp ui package has no Kotlin files, so the import rule is not enforced on any screen",
        )
        val hits = uiSources.filter { file ->
            val text = file.readText()
            forbidden.any { text.contains(it) }
        }
        assertTrue(
            hits.isEmpty(),
            "only the di package may import repositories or use cases:\n" + hits.joinToString("\n"),
        )
    }
}
