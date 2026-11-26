package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Flow 5 source scans: Daily QR files, Koin wiring, the always-dark status bar, schema v2 and the sync gate. */
class F5StructureTest {

    // F5-AC18: "one public type per file, no nested classes … New files are included in the existing structure suite"
    @Test
    fun F5_AC18_newDailyQrFilesHaveOnePublicTypeAndNoNestedClasses() {
        val problems = mutableListOf<String>()
        // DailyQrScreen.kt holds composable functions; its tags live in DailyQrTestTags, so it may declare no type at all.
        val screens = composeMainFiles().filter { it.name == "DailyQrScreen.kt" && "/ui/dailyqr/" in it.unixPath() }
        if (screens.isEmpty()) problems += "DailyQrScreen.kt is missing under composeApp/src/*Main/…/ui/dailyqr"
        for (file in screens) {
            val source = file.readText()
            val types = KotlinPublicTypes.publicTopLevelTypeNames(source)
            if (types.size > 1) problems += "${file.path} has ${types.size} public types: $types"
            val nested = NestedTypes.names(source)
            if (nested.isNotEmpty()) problems += "${file.path} has nested types: $nested"
        }
        for (name in listOf("QrEncoder.kt", "QrMatrix.kt")) {
            val matches = composeMainFiles().filter { it.name == name && "/ui/dailyqr/" in it.unixPath() }
            if (matches.isEmpty()) problems += "$name is missing under composeApp/src/*Main/…/ui/dailyqr"
            matches.forEach { problems += SourceScan.onePublicTypeProblems(it) }
        }
        for (type in listOf("DailyQrTestTags", "ScreenBrightness")) {
            val declaring = composeMainFiles().filter { type in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
            if (declaring.isEmpty()) problems += "public type $type is missing under composeApp/src/*Main"
            declaring.forEach { problems += SourceScan.onePublicTypeProblems(it) }
        }
        assertTrue(problems.isEmpty(), problems.distinct().joinToString("\n"))
    }

    // Spec section 8 test tags: "New on Daily QR: coach_qr_overline, coach_qr_subtitle, coach_qr_card, coach_qr_valid, coach_qr_brightness, coach_qr_sessions, coach_qr_callout" in DailyQrTestTags
    @Test
    fun F5_AC18_dailyQrTestTagsHoldTheNewTags() {
        val declaring = composeMainFiles().filter { "DailyQrTestTags" in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
        assertEquals(1, declaring.size, "one DailyQrTestTags object, found ${declaring.map { it.path }}")
        val source = declaring.single().readText()
        val tags = listOf(
            "coach_qr_overline",
            "coach_qr_subtitle",
            "coach_qr_card",
            "coach_qr_valid",
            "coach_qr_brightness",
            "coach_qr_sessions",
            "coach_qr_callout",
        )
        val missing = tags.filter { "\"$it\"" !in source }
        assertTrue(missing.isEmpty(), "DailyQrTestTags is missing $missing")
    }

    // Decision 6 / F5-AC1: "coach_qr_account goes away"
    @Test
    fun F5_AC1_coachQrAccountIsGoneFromMainSources() {
        val users = composeMainFiles().filter { "coach_qr_account" in it.readText() }
        assertTrue(users.isEmpty(), "coach_qr_account is still in ${users.map { it.path }}")
    }

    // F5-AC4: "and calls LightStatusBarIcons(lightIcons = true)" (a no-op on the desktop test host, so scanned here)
    @Test
    fun F5_AC4_theDailyQrScreenAsksForLightStatusBarIconsAndTheDarkColours() {
        val source = dailyQrScreenSource()
        assertTrue(source.compact().contains("LightStatusBarIcons(lightIcons=true)"), "DailyQrScreen calls LightStatusBarIcons(lightIcons = true)")
        assertTrue(source.contains("TrxColors.Dark"), "DailyQrScreen draws with TrxColors.Dark")
    }

    // F5-AC12: "DailyQrViewModel is bound in ViewModelModule" (spec assumption 4: Clock.System and TimeZone.currentSystemDefault())
    @Test
    fun F5_AC12_viewModelModuleBindsDailyQrViewModelWithTheSystemClock() {
        val module = singleMainFile("ViewModelModule.kt").readText()
        val block = blockAround(module, "DailyQrViewModel(")
        assertTrue(block.isNotEmpty(), "ViewModelModule constructs DailyQrViewModel")
        assertTrue(block.contains("Clock.System"), "DailyQrViewModel gets Clock.System")
        assertTrue(block.contains("TimeZone.currentSystemDefault()"), "DailyQrViewModel gets TimeZone.currentSystemDefault()")
    }

    // F5-AC12: "CoachKeyRepository and SecretKeyStore are bound in the app Koin module" (decision 13: SqlCoachKeyRepository, InMemorySecretKeyStore)
    @Test
    fun F5_AC12_fakeDataModuleBindsTheCoachKeysAndTheSecretKeyStore() {
        val module = singleMainFile("FakeDataModule.kt").readText().compact()
        assertTrue(Regex("""single<CoachKeyRepository>\{SqlCoachKeyRepository\(""").containsMatchIn(module), "single<CoachKeyRepository> { SqlCoachKeyRepository(…) }")
        assertTrue(Regex("""single<SecretKeyStore>\{InMemorySecretKeyStore\(""").containsMatchIn(module), "single<SecretKeyStore> { InMemorySecretKeyStore() }")
    }

    // F5-AC12 / section 6: "A rememberDailyQr factory beside rememberCoachClasses", used by CoachHomeScreen for the QR tab
    @Test
    fun F5_AC12_coachHomeOpensTheQrTabThroughRememberDailyQr() {
        val di = composeMainFiles().filter { "/di/" in it.unixPath() && "fun rememberDailyQr(" in it.readText() }
        assertEquals(1, di.size, "one rememberDailyQr in composeApp di, found ${di.map { it.path }}")
        assertTrue(di.single().readText().contains("koinViewModel"), "rememberDailyQr falls back to Koin without a test factory")
        val coachHome = singleMainFile("CoachHomeScreen.kt").readText()
        assertTrue(coachHome.contains("rememberDailyQr("), "CoachHomeScreen uses rememberDailyQr")
        assertTrue(coachHome.contains("DailyQrScreen("), "CoachHomeScreen shows DailyQrScreen")
        assertTrue(!coachHome.contains("qrRoot") && !coachHome.contains("UiCopy.tabQr()"), "the QR placeholder is gone from CoachHomeScreen")
    }

    // Decision 15 / F5-AC13: "IosNetworkMonitor’s pre-callback ONLINE … is not a real reading"
    @Test
    fun F5_AC13_theIosMonitorSaysItsStartingValueIsNotARealReading() {
        val files = SourceScan.sourcesIn("iosMain").filter { it.name == "IosNetworkMonitor.kt" }
        assertEquals(1, files.size, "one IosNetworkMonitor.kt")
        assertTrue(files.single().readText().contains("hasRealReading"), "IosNetworkMonitor reports hasRealReading")
    }

    // F5-AC14 / C4: "Add shared/src/commonMain/sqldelight/ph/project/active/data/db/1.sqm"
    @Test
    fun F5_AC14_theSchemaHasAOneSqmMigration() {
        val migration = File(repoRoot(), "shared/src/commonMain/sqldelight/ph/project/active/data/db/1.sqm")
        assertTrue(migration.isFile, "missing ${migration.path}")
    }

    // F5-AC14 / ruling 1: the code-side v1 → v2 step is the supported path, so both phone drivers open the database with it
    @Test
    fun F5_AC14_androidAndIosOpenTheDatabaseWithTheMigratingSchema() {
        for (sourceSet in listOf("androidMain", "iosMain")) {
            val modules = SourceScan.sourcesIn(sourceSet).filter { it.name == "PlatformModule.kt" && "/composeApp/" in it.unixPath() }
            assertEquals(1, modules.size, "one composeApp $sourceSet PlatformModule.kt")
            val source = modules.single().readText()
            assertTrue(source.contains("TrxSchema"), "$sourceSet PlatformModule opens the database with TrxSchema")
        }
    }

    // Ruling 4: "raise() turns brightness up to full and keeps the screen awake, and restore() undoes both".
    // The fake can't see a real screen, so this checks the Android version by its source.
    @Test
    fun F5_AC5_androidScreenBrightnessKeepsTheScreenAwakeAndUndoesIt() {
        val source = platformScreenBrightness("androidMain").compact()
        assertTrue("screenBrightness" in source, "the Android ScreenBrightness sets the window's screenBrightness")
        assertTrue("addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)" in source || "keepScreenOn=true" in source,
            "raise() keeps the screen awake (FLAG_KEEP_SCREEN_ON)")
        assertTrue("clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)" in source || "keepScreenOn=false" in source,
            "restore() lets the screen sleep again")
    }

    // Ruling 4: the same rule on iOS, where keep-awake is the app's idle timer.
    @Test
    fun F5_AC5_iosScreenBrightnessKeepsTheScreenAwakeAndUndoesIt() {
        val source = platformScreenBrightness("iosMain").compact()
        assertTrue("brightness" in source, "the iOS ScreenBrightness sets the screen brightness")
        assertTrue("idleTimerDisabled=true" in source, "raise() keeps the screen awake (idleTimerDisabled = true)")
        assertTrue("idleTimerDisabled=false" in source, "restore() lets the screen sleep again")
    }

    private fun platformScreenBrightness(sourceSet: String): String {
        val files = SourceScan.sourcesIn(sourceSet)
            .filter { "/composeApp/" in it.unixPath() && Regex(":\\s*ScreenBrightness\\b").containsMatchIn(it.readText()) }
        assertEquals(1, files.size, "expected one ScreenBrightness in composeApp $sourceSet, found ${files.map { it.name }}")
        return files.single().readText()
    }

    private fun composeMainFiles(): List<File> = listOf("commonMain", "androidMain", "iosMain", "jvmMain")
        .flatMap { SourceScan.sourcesIn(it) }
        .filter { "/composeApp/" in it.unixPath() }

    private fun singleMainFile(name: String): File {
        val matches = composeMainFiles().filter { it.name == name }
        assertEquals(1, matches.size, "expected one $name under composeApp/src/*Main, found ${matches.map { it.path }}")
        return matches.single()
    }

    private fun dailyQrScreenSource(): String = singleMainFile("DailyQrScreen.kt").readText()

    /** The `viewModel { … }` block that contains [marker], or "" when there is none. */
    private fun blockAround(source: String, marker: String): String {
        val at = source.indexOf(marker)
        if (at < 0) return ""
        val open = source.lastIndexOf("viewModel", at)
        return if (open < 0) "" else source.substring(open, minOf(source.length, at + 600))
    }

    private fun File.unixPath(): String = path.replace('\\', '/')

    private fun String.compact(): String = replace(Regex("\\s+"), "")
}
