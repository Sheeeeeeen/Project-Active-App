package ph.project.active.structure

import kotlin.test.Test
import kotlin.test.assertTrue

class F4StructureTest {
    private val seedName = "Demo" + "Seed"

    @Test
    fun F4_AC27_demoSeedAndMarasDemoSignInStayOutOfMainAndReleaseSources() {
        val markers = listOf(seedName, "Mara Santos", "mara@")
        val problems = mutableListOf<String>()
        for (file in kotlinSources(repoRoot()).filter { SourceScan.isForbiddenSourceSet(it) }) {
            val text = file.readText()
            for (marker in markers) {
                if (text.contains(marker, ignoreCase = true)) problems += "${file.path} mentions $marker"
            }
        }
        assertTrue(problems.isEmpty(), "demo-only names leaked into main or release sources:\n" + problems.joinToString("\n"))
    }

    @Test
    fun F4_AC27_theDebugSeedSignsInMaraSantos() {
        val source = seedSource()
        assertTrue(source.contains("Mara Santos"), "the debug seed names Mara Santos")
        assertTrue(source.contains("mara@", ignoreCase = true), "the debug seed creates Mara's sign-in (mara@...)")
    }

    @Test
    fun F4_Seed_marasPrivateAndCancelledClassesComeAfterTheOriginalSix() {
        val source = seedSource()
        val sixth = source.indexOf("\"Sunrise TRX Strength\"")
        assertTrue(sixth >= 0, "Sunrise TRX Strength is still seeded")
        val after = source.substring(sixth)
        assertTrue(after.contains("Visibility.PRIVATE"), "a private class is seeded after the original six")
        assertTrue(!source.substring(0, sixth).contains("Visibility.PRIVATE"), "the original six stay public")
        assertTrue(Regex("(?i)cancel").containsMatchIn(after), "a cancelled class is seeded after the original six")
    }

    @Test
    fun F4_Seed_marasRosterShowsBothJoinPaths() {
        // Spec section 7 ("Manage example data"), Planner ruling 23 Nov: on Mara's TRX Core Burn the roster,
        // in join order, is Paolo (request), Dana (invite), Gia (invite) and Hana (request). The class
        // also has 3 pending requests, and Felix is a student who is not on the class.
        val source = seedSource()
        val people = listOf(
            "Paolo Reyes" to "EMP-20417",
            "Dana Uy" to "EMP-20155",
            "Gia Torres" to "EMP-20249",
            "Hana Cho" to "EMP-20877",
            "Ella Navarro" to "EMP-20611",
            "Bea Lim" to "EMP-20388",
            "Carlo Diaz" to "EMP-20502",
            "Felix Ong" to "EMP-20733",
        )
        for ((name, employeeId) in people) {
            assertTrue(source.contains(name), "the debug seed should include $name")
            assertTrue(source.contains(employeeId), "the debug seed should include $name's ID $employeeId")
        }
        assertTrue(source.contains("JoinPath.REQUEST"), "the debug seed records a join by REQUEST (Paolo, Hana)")
        assertTrue(source.contains("JoinPath.INVITE"), "the debug seed records a join by INVITE (Dana, Gia)")
    }

    @Test
    fun F4_AC28_newScreensRoutesAndTestTagObjectsHaveOnePublicTypeAndNoNestedClasses() {
        val mainFiles = listOf("commonMain", "androidMain", "iosMain", "jvmMain")
            .flatMap { SourceScan.sourcesIn(it) }
            .filter { "/composeApp/" in it.path.replace('\\', '/') }
        val problems = mutableListOf<String>()

        for (name in listOf("CoachClassesScreen.kt", "CreateClassScreen.kt", "ManageClassScreen.kt")) {
            val matches = mainFiles.filter { it.name == name }
            if (matches.isEmpty()) problems += "$name is missing under composeApp/src/*Main"
            matches.forEach { problems += SourceScan.onePublicTypeProblems(it) }
        }
        for (name in listOf("CreateClass.kt", "ManageClass.kt")) {
            val matches = mainFiles.filter { it.name == name && it.path.replace('\\', '/').contains("/ui/navigation/") }
            if (matches.isEmpty()) problems += "route $name is missing under composeApp ui/navigation"
            matches.forEach { problems += SourceScan.onePublicTypeProblems(it) }
        }
        for (type in listOf("CoachClassesTestTags", "CreateClassTestTags", "ManageClassTestTags", "CoachTabBarTestTags")) {
            val declaring = mainFiles.filter { type in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
            if (declaring.isEmpty()) problems += "public object $type is missing under composeApp/src/*Main"
            declaring.forEach { problems += SourceScan.onePublicTypeProblems(it) }
        }
        assertTrue(problems.isEmpty(), problems.distinct().joinToString("\n"))
    }

    @Test
    fun F4_AC29_theAppBindsTheRealMonitorsAndNotTheFake() {
        val fakeModule = SourceScan.filesNamed("FakeDataModule.kt")
        assertTrue(fakeModule.size == 1, "expected one FakeDataModule.kt, found ${fakeModule.map { it.path }}")
        assertTrue(
            !fakeModule.single().readText().contains("MutableNetworkMonitor"),
            "the app's Koin must not bind MutableNetworkMonitor",
        )
        val androidMain = SourceScan.sourcesIn("androidMain")
        val iosMain = SourceScan.sourcesIn("iosMain")
        assertTrue(
            androidMain.any { it.name != "AndroidNetworkMonitor.kt" && it.readText().contains("AndroidNetworkMonitor(") },
            "androidMain constructs AndroidNetworkMonitor for the app's Koin",
        )
        assertTrue(
            iosMain.any { it.name != "IosNetworkMonitor.kt" && it.readText().contains("IosNetworkMonitor(") },
            "iosMain constructs IosNetworkMonitor for the app's Koin",
        )
    }

    @Test
    fun F4_AC29_theAndroidMonitorListensUnregistersAndNeverAssumesOnline() {
        val files = SourceScan.sourcesIn("androidMain").filter { it.name == "AndroidNetworkMonitor.kt" }
        assertTrue(files.size == 1, "expected one AndroidNetworkMonitor.kt in an androidMain, found ${files.map { it.path }}")
        val source = files.single().readText()
        assertTrue(source.contains("registerDefaultNetworkCallback"), "listens with registerDefaultNetworkCallback")
        assertTrue(source.contains("unregisterNetworkCallback"), "unregisters the callback")
        assertNoOnlineDefault(source, "AndroidNetworkMonitor")

        val manifests = SourceScan.filesNamed("AndroidManifest.xml")
        assertTrue(
            manifests.any { it.readText().contains("android.permission.ACCESS_NETWORK_STATE") },
            "a manifest declares ACCESS_NETWORK_STATE",
        )
    }

    @Test
    fun F4_AC29_theIosMonitorUsesNwPathMonitorAndNeverAssumesOnline() {
        val files = SourceScan.sourcesIn("iosMain").filter { it.name == "IosNetworkMonitor.kt" }
        assertTrue(files.size == 1, "expected one IosNetworkMonitor.kt in an iosMain, found ${files.map { it.path }}")
        val source = files.single().readText()
        assertTrue(source.contains("nw_path_monitor"), "reads the path with nw_path_monitor")
        assertNoOnlineDefault(source, "IosNetworkMonitor")
    }

    @Test
    fun F4_AC29_testHarnessesStillUseTheFakeMonitor() {
        val harnesses = listOf("Flow1Harness.kt", "Flow3Harness.kt", "Flow4Harness.kt")
            .flatMap { SourceScan.filesNamed(it) }
        assertTrue(harnesses.size == 3, "expected the three UI harnesses, found ${harnesses.map { it.name }}")
        for (file in harnesses) {
            assertTrue(file.readText().contains("MutableNetworkMonitor"), "${file.name} keeps MutableNetworkMonitor")
        }
    }

    private fun assertNoOnlineDefault(source: String, name: String) {
        val compact = source.replace(Regex("\\s+"), "")
        assertTrue(!compact.contains("MutableStateFlow(NetworkStatus.ONLINE"), "$name must not start as ONLINE")
        assertTrue(!compact.contains("initialValue=NetworkStatus.ONLINE"), "$name must not start as ONLINE")
    }

    private fun seedSource(): String {
        val seeds = kotlinSources(repoRoot()).filter { it.name == "$seedName.kt" }
        assertTrue(seeds.size == 1, "expected one $seedName.kt, found ${seeds.map { it.path }}")
        return seeds.single().readText()
    }
}
