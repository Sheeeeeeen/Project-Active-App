package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F1Ac3KoinImportTest {
    @Test
    fun F1_AC3_sharedSourcesDoNotImportKoin() {
        val shared = File(repoRoot(), "shared/src")
        val hits = kotlinSources(shared).filter { file ->
            file.readText().contains("import org.koin")
        }
        assertTrue(hits.isEmpty(), "shared sources import Koin:\n" + hits.joinToString("\n"))
    }
}
