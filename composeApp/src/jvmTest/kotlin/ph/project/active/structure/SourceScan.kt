package ph.project.active.structure

import java.io.File

/** Small source-text helpers for the flow 4 scans. */
object SourceScan {
    /** True for main, commonMain, androidMain, iosMain, or any *release* source set. */
    fun isForbiddenSourceSet(file: File): Boolean {
        val sourceSet = sourceSetOf(file) ?: return false
        return sourceSet in listOf("main", "commonMain", "androidMain", "iosMain") ||
            sourceSet.contains("release", ignoreCase = true)
    }

    /** The folder right after `src`, such as `commonMain` or `debug`. */
    fun sourceSetOf(file: File): String? {
        val parts = file.path.replace('\\', '/').split('/')
        val srcIndex = parts.indexOf("src")
        return if (srcIndex < 0 || srcIndex + 1 >= parts.size) null else parts[srcIndex + 1]
    }

    /** Kotlin files under `src/<sourceSet>` of every module. */
    fun sourcesIn(sourceSet: String): List<File> =
        kotlinSources(repoRoot()).filter { sourceSetOf(it) == sourceSet }

    /** Problems if [file] does not hold exactly one public type or holds a nested type. */
    fun onePublicTypeProblems(file: File): List<String> {
        val source = file.readText()
        val problems = mutableListOf<String>()
        val types = KotlinPublicTypes.publicTopLevelTypeNames(source)
        if (types.size != 1) problems += "${file.path} has ${types.size} public types: $types"
        val nested = NestedTypes.names(source)
        if (nested.isNotEmpty()) problems += "${file.path} has nested types: $nested"
        return problems
    }

    /** Every non-build file named [name] under the repo, Kotlin or not. */
    fun filesNamed(name: String): List<File> = repoRoot().walkTopDown()
        .onEnter { it.name != "build" && it.name != ".gradle" && it.name != ".git" }
        .filter { it.isFile && it.name == name }
        .toList()
}
