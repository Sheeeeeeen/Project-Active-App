package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class PNoUnconfinedDispatcherTest {
    @Test
    fun P_noUnconfinedDispatcherInSharedMain() {
        val sharedSrc = File(repoRoot(), "shared/src")
        val mainDirs = sharedSrc.listFiles()
            ?.filter { directory -> directory.isDirectory && directory.name.endsWith("Main") }
            .orEmpty()
        val files = mainDirs.flatMap { kotlinSources(it) }
        assertTrue(files.isNotEmpty(), "no Kotlin files were scanned under shared/src/*Main")
        val hits = files.filter { file -> file.readText().contains("Dispatchers.Unconfined") }
        assertTrue(
            hits.isEmpty(),
            "shared main sources use Dispatchers.Unconfined:\n" + hits.joinToString("\n") { it.path },
        )
    }
}
