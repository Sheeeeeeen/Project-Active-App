package ph.project.active.structure

/** Finds class, interface, and object declarations inside another declaration. */
object NestedTypes {
    fun names(source: String): List<String> {
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
}
