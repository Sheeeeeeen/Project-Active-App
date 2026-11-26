package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Flow 6 source scans: the Attendance tab files and wiring, the signing-key stores, and the iOS idle timer. */
class F6StructureTest {

    // F6-AC24: "one public type per file, no nested classes … New files are included"
    @Test
    fun F6_AC24_newAttendanceFilesHaveOnePublicTypeAndNoNestedClasses() {
        val problems = mutableListOf<String>()
        // CoachAttendanceScreen.kt holds composable functions; its tags live in CoachAttendanceTestTags, so it may declare no type at all.
        val screens = composeMainFiles().filter { it.name == "CoachAttendanceScreen.kt" && "/ui/coachattendance/" in it.unixPath() }
        if (screens.isEmpty()) problems += "CoachAttendanceScreen.kt is missing under composeApp/src/*Main/…/ui/coachattendance"
        for (file in screens) {
            val source = file.readText()
            val types = KotlinPublicTypes.publicTopLevelTypeNames(source)
            if (types.size > 1) problems += "${file.path} has ${types.size} public types: $types"
            val nested = NestedTypes.names(source)
            if (nested.isNotEmpty()) problems += "${file.path} has nested types: $nested"
        }
        val factories = composeMainFiles().filter { it.name == "CoachAttendanceFactory.kt" && "/di/" in it.unixPath() }
        if (factories.isEmpty()) problems += "CoachAttendanceFactory.kt is missing under composeApp/src/*Main/…/di"
        for (file in factories) {
            if (NestedTypes.names(file.readText()).isNotEmpty()) problems += "${file.path} has nested types"
        }
        for (type in listOf("CoachAttendanceTestTags", "AndroidSecretKeyStore", "IosSecretKeyStore")) {
            val declaring = composeMainFiles().filter { type in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
            if (declaring.isEmpty()) problems += "public type $type is missing under composeApp/src/*Main"
            declaring.forEach { problems += SourceScan.onePublicTypeProblems(it) }
        }
        assertTrue(problems.isEmpty(), problems.distinct().joinToString("\n"))
    }

    // Spec section 8 test tags: "New: coach_attendance_overline, … coach_attendance_section_{sessionId}, coach_attendance_row_{sessionId}_{studentId}" in CoachAttendanceTestTags
    @Test
    fun F6_AC24_coachAttendanceTestTagsHoldTheNewTags() {
        val declaring = composeMainFiles().filter { "CoachAttendanceTestTags" in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
        assertEquals(1, declaring.size, "one CoachAttendanceTestTags object, found ${declaring.map { it.path }}")
        assertTrue("/ui/coachattendance/" in declaring.single().unixPath(), "CoachAttendanceTestTags lives in ui/coachattendance")
        val source = declaring.single().readText()
        val tags = listOf(
            "coach_attendance_overline",
            "coach_attendance_prev_day",
            "coach_attendance_next_day",
            "coach_attendance_date",
            "coach_attendance_day_state",
            "coach_attendance_present_count",
            "coach_attendance_absent_count",
            "coach_attendance_footnote",
            "coach_attendance_empty",
            "coach_attendance_section_",
            "coach_attendance_row_",
        )
        val missing = tags.filter { it !in source }
        assertTrue(missing.isEmpty(), "CoachAttendanceTestTags is missing $missing")
    }

    // Spec section 6: "a new circle_x drawable (Lucide "circle-x") for Absent"
    @Test
    fun F6_AC24_theCircleXDrawableIsAddedForAbsent() {
        val drawable = File(repoRoot(), "composeApp/src/commonMain/composeResources/drawable/circle_x.xml")
        assertTrue(drawable.isFile, "missing ${drawable.path}")
    }

    // F6-AC1 / decision 3: "PlaceholderTag leaves this tab, so coach_attendance_account goes away"; section 5: CoachHomeScreen shows CoachAttendanceScreen
    @Test
    fun F6_AC1_coachHomeShowsCoachAttendanceScreenNotThePlaceholder() {
        val coachHome = singleMainFile("CoachHomeScreen.kt").readText()
        assertTrue(coachHome.contains("CoachAttendanceScreen("), "CoachHomeScreen shows CoachAttendanceScreen")
        assertTrue(!coachHome.contains("attendanceAccount"), "CoachHomeScreen no longer passes coach_attendance_account")
        assertTrue(!coachHome.contains("CoachTabPlaceholder("), "the Attendance tab no longer uses CoachTabPlaceholder")
        val stillUsingAccountTag = composeMainFiles().filter { "\"coach_attendance_account\"" in it.readText() && "TestTags" !in it.name }
        assertTrue(stillUsingAccountTag.isEmpty(), "coach_attendance_account is still used in ${stillUsingAccountTag.map { it.path }}")
    }

    // F6-AC16: "CoachAttendanceViewModel is bound in ViewModelModule" (section 6: with Clock.System and TimeZone.currentSystemDefault())
    @Test
    fun F6_AC16_viewModelModuleBindsCoachAttendanceViewModelWithTheSystemClock() {
        val module = singleMainFile("ViewModelModule.kt").readText()
        val block = blockAround(module, "CoachAttendanceViewModel(")
        assertTrue(block.isNotEmpty(), "ViewModelModule constructs CoachAttendanceViewModel")
        assertTrue(block.contains("Clock.System"), "CoachAttendanceViewModel gets Clock.System")
        assertTrue(block.contains("TimeZone.currentSystemDefault()"), "CoachAttendanceViewModel gets TimeZone.currentSystemDefault()")
    }

    // F6-AC16 / section 6: "rememberCoachAttendance(account, factory) in ph.project.active.di (CoachAttendanceFactory.kt), key "coach-attendance:${account.id}", same shape as rememberDailyQr"
    @Test
    fun F6_AC16_coachHomeOpensTheTabThroughRememberCoachAttendance() {
        val factory = singleMainFile("CoachAttendanceFactory.kt")
        assertTrue("/di/" in factory.unixPath(), "CoachAttendanceFactory.kt lives in di")
        val source = factory.readText()
        assertTrue(source.contains("fun rememberCoachAttendance("), "CoachAttendanceFactory.kt declares rememberCoachAttendance")
        assertTrue(source.contains("\"coach-attendance:\${account.id}\""), "the ViewModel key is \"coach-attendance:\${account.id}\"")
        assertTrue(source.contains("koinViewModel"), "rememberCoachAttendance falls back to Koin without a test factory")
        val coachHome = singleMainFile("CoachHomeScreen.kt").readText()
        assertTrue(coachHome.contains("rememberCoachAttendance("), "CoachHomeScreen uses rememberCoachAttendance")
    }

    // F6-AC16: "UI tests pass a factory and never start Koin."
    @Test
    fun F6_AC16_uiTestsNeverStartKoin() {
        val uiTests = listOf("commonTest", "jvmTest")
            .flatMap { SourceScan.sourcesIn(it) }
            .filter { "/composeApp/" in it.unixPath() && "/ui/" in it.unixPath() }
        val starting = uiTests.filter { "startKoin" in it.readText() || "koinApplication" in it.readText() }
        assertTrue(starting.isEmpty(), "UI tests start Koin: ${starting.map { it.path }}")
    }

    // F6-AC18: "FakeDataModule no longer binds InMemorySecretKeyStore, and its "Placeholder: the private key stays in memory" comment is gone."
    @Test
    fun F6_AC18_fakeDataModuleNoLongerBindsTheInMemoryKeyStore() {
        val module = singleMainFile("FakeDataModule.kt").readText()
        assertTrue("InMemorySecretKeyStore" !in module, "FakeDataModule still mentions InMemorySecretKeyStore")
        assertTrue("Placeholder: the private key stays in memory" !in module, "the placeholder comment is still in FakeDataModule")
    }

    // F6-AC18: "The Android platformModule() binds SecretKeyStore to AndroidSecretKeyStore, the iOS one to IosSecretKeyStore, and the desktop (jvmMain) one to InMemorySecretKeyStore"
    @Test
    fun F6_AC18_eachPlatformModuleBindsItsOwnSecretKeyStore() {
        val expected = mapOf(
            "androidMain" to """single<SecretKeyStore>\{AndroidSecretKeyStore\(""",
            "iosMain" to """single<SecretKeyStore>\{IosSecretKeyStore\(""",
            "jvmMain" to """single<SecretKeyStore>\{InMemorySecretKeyStore\(""",
        )
        val problems = mutableListOf<String>()
        for ((sourceSet, binding) in expected) {
            val module = platformModule(sourceSet).compact()
            if (!Regex(binding).containsMatchIn(module)) problems += "$sourceSet PlatformModule should contain ${binding.replace("\\", "")}"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // Section 9: "ph.project.active.data.secure.AndroidSecretKeyStore(context) in composeApp/src/androidMain, and ph.project.active.data.secure.IosSecretKeyStore() in composeApp/src/iosMain. Both implement the shared SecretKeyStore."
    @Test
    fun F6_AC18_thePlatformKeyStoresLiveInDataSecureAndImplementSecretKeyStore() {
        val problems = mutableListOf<String>()
        for ((sourceSet, name) in listOf("androidMain" to "AndroidSecretKeyStore", "iosMain" to "IosSecretKeyStore")) {
            val files = SourceScan.sourcesIn(sourceSet).filter { "/composeApp/" in it.unixPath() && it.name == "$name.kt" }
            if (files.size != 1) {
                problems += "expected one $name.kt in composeApp $sourceSet, found ${files.map { it.path }}"
                continue
            }
            val file = files.single()
            val source = file.readText()
            if ("/ph/project/active/data/secure/" !in file.unixPath()) problems += "$name.kt should be under ph/project/active/data/secure"
            if (!source.contains("package ph.project.active.data.secure")) problems += "$name is not in package ph.project.active.data.secure"
            if (!Regex("""class\s+$name\b[^{]*:\s*SecretKeyStore\b""").containsMatchIn(source)) problems += "$name does not implement SecretKeyStore"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F6-AC21: "IosScreenBrightness saves idleTimerDisabled on the first raise() and puts that saved value back on restore(). A source scan finds no idleTimerDisabled = false in IosScreenBrightness.kt."
    @Test
    fun F6_AC21_iosScreenBrightnessPutsTheSavedIdleTimerBack() {
        val files = SourceScan.sourcesIn("iosMain").filter { "/composeApp/" in it.unixPath() && it.name == "IosScreenBrightness.kt" }
        assertEquals(1, files.size, "one IosScreenBrightness.kt in composeApp iosMain")
        val source = files.single().readText()
        assertTrue(!Regex("""idleTimerDisabled\s*=\s*false\b""").containsMatchIn(source), "IosScreenBrightness must not set idleTimerDisabled = false")
        assertTrue(Regex("""idleTimerDisabled\s*=\s*true\b""").containsMatchIn(source), "raise() still keeps the screen awake (idleTimerDisabled = true)")
        // The value before raise() is read into a field: `saved = UIApplication.sharedApplication.idleTimerDisabled`.
        assertTrue(Regex("""=\s*[\w.()]*\bidleTimerDisabled\b(?!\s*=)""").containsMatchIn(source), "raise() saves the current idleTimerDisabled")
        // restore() writes that saved value back, not a literal.
        assertTrue(Regex("""idleTimerDisabled\s*=\s*(?!true\b|false\b)[A-Za-z_(]""").containsMatchIn(source), "restore() puts the saved value back")
    }

    private fun platformModule(sourceSet: String): String {
        val modules = SourceScan.sourcesIn(sourceSet).filter { it.name == "PlatformModule.kt" && "/composeApp/" in it.unixPath() }
        assertEquals(1, modules.size, "one composeApp $sourceSet PlatformModule.kt")
        return modules.single().readText()
    }

    private fun composeMainFiles(): List<File> = listOf("commonMain", "androidMain", "iosMain", "jvmMain")
        .flatMap { SourceScan.sourcesIn(it) }
        .filter { "/composeApp/" in it.unixPath() }

    private fun singleMainFile(name: String): File {
        val matches = composeMainFiles().filter { it.name == name }
        assertEquals(1, matches.size, "expected one $name under composeApp/src/*Main, found ${matches.map { it.path }}")
        return matches.single()
    }

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
