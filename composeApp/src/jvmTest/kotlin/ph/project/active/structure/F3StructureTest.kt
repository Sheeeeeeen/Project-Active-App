package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class F3StructureTest {
    @Test
    fun F3_AC21_demoSeedAndPaolosDemoSignInStayOutOfMainAndReleaseSources() {
        val sources = kotlinSources(repoRoot())
        val seedName = "Demo" + "Seed"
        val paolo = "Pao" + "lo"
        val markers = listOf(seedName, paolo, "$paolo Reyes", "${paolo.lowercase()}@")
        val problems = mutableListOf<String>()
        for (file in sources) {
            val path = file.path.replace('\\', '/')
            if (!isForbiddenSourceSet(path)) continue
            val text = file.readText()
            for (marker in markers) {
                if (text.contains(marker, ignoreCase = true)) problems += "$path mentions $marker"
            }
        }
        assertTrue(problems.isEmpty(), "demo-only names leaked into main or release sources:\n" + problems.joinToString("\n"))

        val seeds = sources.filter { it.name == "$seedName.kt" }
        assertTrue(seeds.isNotEmpty(), "$seedName.kt is missing, so the debug-only rule is not enforced")
        assertTrue(
            seeds.any { it.readText().contains("$paolo Reyes") },
            "the debug $seedName should seed the demo student $paolo Reyes",
        )
    }

    @Test
    fun F3_AC22_newScreensAndTestTagObjectsHaveOnePublicTypeAndNoNestedClasses() {
        val src = File(repoRoot(), "composeApp/src")
        val mainDirs = src.listFiles()?.filter { it.isDirectory && it.name.endsWith("Main") }.orEmpty()
        val files = mainDirs.flatMap { kotlinSources(it) }
        val problems = mutableListOf<String>()

        for (name in listOf("StudentClassesScreen.kt", "InvitesScreen.kt")) {
            val matches = files.filter { it.name == name }
            if (matches.isEmpty()) problems += "$name is missing under composeApp/src/*Main"
            matches.forEach { problems += onePublicTypeProblems(it) }
        }
        for (type in listOf("StudentClassesTestTags", "InvitesTestTags", "StudentTabBarTestTags")) {
            val declaring = files.filter { type in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
            if (declaring.isEmpty()) problems += "public object $type is missing under composeApp/src/*Main"
            declaring.forEach { problems += onePublicTypeProblems(it) }
        }
        assertTrue(problems.isEmpty(), problems.distinct().joinToString("\n"))
    }

    @Test
    fun F3_Seed_seedsTheSixClassesInOrderTheFourCoachesAndPaolo() {
        val source = demoSeedSource()
        val classes = listOf(
            "TRX Core Burn",
            "Lunch Express TRX",
            "TRX Mobility Flow",
            "TRX Every Day 20",
            "Weekend TRX Bootcamp",
            "Sunrise TRX Strength",
        )
        val positions = classes.map { name -> source.indexOf("\"$name\"") }
        assertTrue(positions.none { it < 0 }, "missing seeded classes: ${classes.filterIndexed { i, _ -> positions[i] < 0 }}")
        assertTrue(positions == positions.sorted(), "classes must be created in this order: $classes")
        for (person in listOf("Mara Santos", "Leo Ramirez", "Jun Tan", "Ana Cruz", "Paolo Reyes", "EMP-20417")) {
            assertTrue(source.contains(person), "the debug seed should include $person")
        }
    }

    @Test
    fun F3_Seed_weekendBootcampRunsOnSaturdayOnly() {
        // Every Day 20 uses all weekdays without naming them, so no seeded class should name Sunday.
        assertTrue(!demoSeedSource().contains("SUNDAY"), "Weekend TRX Bootcamp is Saturday only (spec section 9, item 8)")
    }
}

private fun demoSeedSource(): String {
    val seedName = "Demo" + "Seed"
    val seeds = kotlinSources(repoRoot()).filter { it.name == "$seedName.kt" }
    assertTrue(seeds.size == 1, "expected one $seedName.kt, found ${seeds.map { it.path }}")
    return seeds.single().readText()
}

private fun isForbiddenSourceSet(path: String): Boolean {
    val parts = path.split('/')
    val srcIndex = parts.indexOf("src")
    if (srcIndex < 0 || srcIndex + 1 >= parts.size) return false
    val sourceSet = parts[srcIndex + 1]
    return sourceSet in listOf("main", "commonMain", "androidMain", "iosMain") ||
        sourceSet.contains("release", ignoreCase = true)
}

private fun onePublicTypeProblems(file: File): List<String> {
    val source = file.readText()
    val problems = mutableListOf<String>()
    val types = KotlinPublicTypes.publicTopLevelTypeNames(source)
    if (types.size != 1) problems += "${file.path} has ${types.size} public types: $types"
    val nested = NestedTypes.names(source)
    if (nested.isNotEmpty()) problems += "${file.path} has nested types: $nested"
    return problems
}
