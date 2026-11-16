package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F2StructureTest {
    @Test
    fun F2_AC31_demoSeedAppearsOnlyInADebugSourceSet() {
        val seedName = "Demo" + "Seed"
        val files = kotlinSources(repoRoot()).filter { file -> file.readText().contains(seedName) }
        assertTrue(files.isNotEmpty(), "$seedName is missing, so the debug-only rule is not enforced")
        val problems = files.map { it.path.replace('\\', '/') }.filter { path -> !isDebugSourceSet(path) }
        assertTrue(problems.isEmpty(), "$seedName leaked outside a debug source set:\n" + problems.joinToString("\n"))
    }

    @Test
    fun F2_AC32_newRouteAndScreensHaveOnePublicTypeAndNoNestedClasses() {
        val src = File(repoRoot(), "composeApp/src")
        val mainDirs = src.listFiles()?.filter { it.isDirectory && it.name.endsWith("Main") }.orEmpty()
        val files = mainDirs.flatMap { kotlinSources(it) }
        val required = listOf("ClassDetail.kt", "GuestClassListScreen.kt", "ClassDetailScreen.kt")
        val problems = mutableListOf<String>()
        for (name in required) {
            val matches = files.filter { it.name == name }
            if (matches.isEmpty()) problems += "$name is missing under composeApp/src/*Main"
            for (file in matches) {
                val source = file.readText()
                val types = KotlinPublicTypes.publicTopLevelTypeNames(source)
                if (types.size != 1) problems += "${file.path} has ${types.size} public types: $types"
                val nested = nestedTypeNames(source)
                if (nested.isNotEmpty()) problems += "${file.path} has nested types: $nested"
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }
}

private fun isDebugSourceSet(path: String): Boolean {
    val parts = path.split('/')
    val srcIndex = parts.indexOf("src")
    if (srcIndex < 0 || srcIndex + 1 >= parts.size) return false
    val sourceSet = parts[srcIndex + 1]
    val forbidden = listOf("main", "commonMain", "androidMain", "iosMain")
    if (sourceSet in forbidden || sourceSet.contains("release", ignoreCase = true)) return false
    return sourceSet.contains("debug", ignoreCase = true)
}

private fun nestedTypeNames(source: String): List<String> {
    val cleaned = maskStringsAndComments(source)
    val names = mutableListOf<String>()
    var depth = 0
    var index = 0
    while (index < cleaned.length) {
        val character = cleaned[index]
        when {
            character == '{' -> {
                depth += 1
                index += 1
            }
            character == '}' -> {
                depth = (depth - 1).coerceAtLeast(0)
                index += 1
            }
            depth > 0 && (character.isLetter() || character == '_') -> {
                val start = index
                while (index < cleaned.length && (cleaned[index].isLetterOrDigit() || cleaned[index] == '_')) index += 1
                val word = cleaned.substring(start, index)
                if (word == "class" || word == "interface" || word == "object") {
                    readIdentifier(cleaned, index)?.let { names += it }
                }
            }
            else -> index += 1
        }
    }
    return names.distinct()
}

private fun readIdentifier(cleaned: String, from: Int): String? {
    var index = from
    while (index < cleaned.length && cleaned[index].isWhitespace()) index += 1
    if (index >= cleaned.length || !(cleaned[index].isLetter() || cleaned[index] == '_')) return null
    val start = index
    index += 1
    while (index < cleaned.length && (cleaned[index].isLetterOrDigit() || cleaned[index] == '_')) index += 1
    return cleaned.substring(start, index)
}

private fun maskStringsAndComments(source: String): String {
    val masked = CharArray(source.length) { ' ' }
    var index = 0
    while (index < source.length) {
        when {
            source.startsWith("/*", index) -> {
                val end = source.indexOf("*/", index + 2)
                index = if (end < 0) source.length else end + 2
            }
            source.startsWith("//", index) -> {
                val end = source.indexOf('\n', index)
                index = if (end < 0) source.length else end
            }
            source.startsWith("\"\"\"", index) -> {
                val end = source.indexOf("\"\"\"", index + 3)
                index = if (end < 0) source.length else end + 3
            }
            source[index] == '"' -> {
                index += 1
                while (index < source.length && source[index] != '"') {
                    index += if (source[index] == '\\' && index + 1 < source.length) 2 else 1
                }
                if (index < source.length) index += 1
            }
            source[index] == '\'' -> {
                index += 1
                while (index < source.length && source[index] != '\'') {
                    index += if (source[index] == '\\' && index + 1 < source.length) 2 else 1
                }
                if (index < source.length) index += 1
            }
            else -> {
                masked[index] = source[index]
                index += 1
            }
        }
    }
    return String(masked)
}
