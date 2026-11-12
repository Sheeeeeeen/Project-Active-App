package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class F1Ac8FakeDataModuleBindingsTest {
    @Test
    fun F1_AC8_repositoryBindingsLiveOnlyInFakeDataModule() {
        val files = kotlinSources(repoRoot())
        val owners = boundTypes.associateWith { type ->
            files.filter { file -> binds(file.readText(), type) }
        }
        val problems = mutableListOf<String>()
        for ((type, found) in owners) {
            val names = found.map { it.relativeTo(repoRoot()).path }
            if (found.size != 1 || found.single().name != "FakeDataModule.kt") {
                problems += "$type is bound in $names"
            }
        }
        assertEquals(emptyList<String>(), problems)
    }

    private fun binds(text: String, type: String): Boolean {
        val patterns = listOf(
            Regex("""\b(single|factory)\s*<\s*$type\b"""),
            Regex("""\b(single|factory)Of\s*<\s*$type\b"""),
            Regex("""\bbind\s*<\s*$type\b"""),
            Regex("""(?s)\b(single|factory)\s*\{[^}]{0,800}\b$type\b"""),
        )
        return patterns.any { it.containsMatchIn(text) }
    }

    private val boundTypes = listOf(
        "AuthRepository",
        "SessionStore",
        "PersonRepository",
        "ClassRepository",
        "MembershipRepository",
        "NetworkMonitor",
        "UserRepository",
        "PasswordHasher",
    )
}
