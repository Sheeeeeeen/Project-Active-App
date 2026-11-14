package ph.project.active.structure

import java.io.File

object KotlinPublicTypes {
    fun publicTopLevelTypeNames(source: String): List<String> {
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
                depth == 0 && (character.isLetter() || character == '_') -> {
                    val start = index
                    while (index < cleaned.length && (cleaned[index].isLetterOrDigit() || cleaned[index] == '_')) {
                        index += 1
                    }
                    val word = cleaned.substring(start, index)
                    if ((word == "class" || word == "interface" || word == "object") && isPublic(cleaned, start)) {
                        readIdentifier(cleaned, index)?.let { names += it }
                    }
                }
                else -> index += 1
            }
        }
        return names
    }

    private fun isPublic(cleaned: String, keywordStart: Int): Boolean {
        val seen = mutableListOf<String>()
        var index = keywordStart - 1
        while (index >= 0) {
            while (index >= 0 && cleaned[index].isWhitespace()) index -= 1
            if (index < 0 || !(cleaned[index].isLetterOrDigit() || cleaned[index] == '_')) break
            val end = index + 1
            while (index >= 0 && (cleaned[index].isLetterOrDigit() || cleaned[index] == '_')) index -= 1
            val token = cleaned.substring(index + 1, end)
            if (token !in MODIFIERS) break
            seen += token
        }
        return "private" !in seen && "internal" !in seen
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

    private val MODIFIERS = setOf(
        "public",
        "private",
        "internal",
        "data",
        "sealed",
        "inner",
        "value",
        "annotation",
        "enum",
        "open",
        "abstract",
        "expect",
        "actual",
        "fun",
        "companion",
        "external",
        "suspend",
        "operator",
        "inline",
        "tailrec",
        "infix",
    )
}

fun repoRoot(): File {
    var dir: File? = File(System.getProperty("user.dir")).absoluteFile
    while (dir != null) {
        if (File(dir, "settings.gradle.kts").isFile && File(dir, "shared").isDirectory) return dir
        dir = dir.parentFile
    }
    error("repo root not found from ${System.getProperty("user.dir")}")
}

fun kotlinSources(root: File): List<File> = root.walkTopDown()
    .onEnter { directory -> directory.name !in SKIP_DIRECTORIES }
    .filter { file -> file.isFile && file.extension == "kt" }
    .toList()

private val SKIP_DIRECTORIES = setOf("build", ".gradle", ".git")
