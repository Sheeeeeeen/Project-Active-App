package ph.project.active.structure

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Flow 7 source scans: Koin wiring, platform files, the scan path's logging, and the new files (F7-AC44 to F7-AC47). */
class F7StructureTest {

    // F7-AC44: "AttendViewModel and StudentSessionsViewModel are bound in ViewModelModule" (A6: "with Clock.System and TimeZone.currentSystemDefault()")
    @Test
    fun F7_AC44_viewModelModuleBindsBothViewModelsWithTheSystemClock() {
        val module = singleMainFile("ViewModelModule.kt").readText()
        val problems = mutableListOf<String>()
        for (type in listOf("AttendViewModel", "StudentSessionsViewModel")) {
            val block = blockAround(module, "$type(")
            if (block.isEmpty()) {
                problems += "ViewModelModule does not construct $type"
                continue
            }
            if (!block.contains("Clock.System")) problems += "$type does not get Clock.System"
            if (!block.contains("TimeZone.currentSystemDefault()")) problems += "$type does not get TimeZone.currentSystemDefault()"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F7-AC44 / A6: "rememberAttend(account, factory) in di/AttendFactory.kt and rememberStudentSessions(account, factory) in di/StudentSessionsFactory.kt, keyed "attend:${account.id}" and "student-sessions:${account.id}""
    @Test
    fun F7_AC44_theTwoFactoriesLiveInDiWithTheirKeys() {
        val expected = listOf(
            Triple("AttendFactory.kt", "fun rememberAttend(", "\"attend:\${account.id}\""),
            Triple("StudentSessionsFactory.kt", "fun rememberStudentSessions(", "\"student-sessions:\${account.id}\""),
        )
        val problems = mutableListOf<String>()
        for ((name, function, key) in expected) {
            val matches = composeMainFiles().filter { it.name == name }
            if (matches.size != 1) {
                problems += "expected one $name under composeApp/src/*Main, found ${matches.map { it.path }}"
                continue
            }
            val file = matches.single()
            val source = file.readText()
            if ("/di/" !in file.unixPath()) problems += "$name should live in di"
            if (!source.contains(function)) problems += "$name does not declare ${function.removePrefix("fun ")}"
            if (!source.contains(key)) problems += "$name does not key its ViewModel $key"
            if (!source.contains("koinViewModel")) problems += "$name does not fall back to Koin without a test factory"
            if (NestedTypes.names(source).isNotEmpty()) problems += "$name has nested types"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F7-AC44 / A7: "StudentHomeScreen … uses" the factories; "StudentHomeScreen and AppNavHost gain last optional parameters: attendViewModel, studentSessionsViewModel …, and qrScanner, cameraPermission, scanHaptics …, all = null."
    @Test
    fun F7_AC44_studentHomeAndAppNavHostTakeTheFactoriesAndSeams() {
        val problems = mutableListOf<String>()
        val studentHome = singleMainFile("StudentHomeScreen.kt").readText()
        if (!studentHome.contains("rememberAttend(")) problems += "StudentHomeScreen does not use rememberAttend"
        if (!studentHome.contains("rememberStudentSessions(")) problems += "StudentHomeScreen does not use rememberStudentSessions"
        for ((name, source) in listOf("StudentHomeScreen.kt" to studentHome, "AppNavHost.kt" to singleMainFile("AppNavHost.kt").readText())) {
            for (parameter in listOf("attendViewModel", "studentSessionsViewModel", "qrScanner", "cameraPermission", "scanHaptics")) {
                if (!Regex("""\b$parameter\s*:[^=\n]*\?\s*=\s*null""").containsMatchIn(source)) {
                    problems += "$name has no optional parameter $parameter = null"
                }
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F7-AC45: "the manifest has CAMERA, and android.hardware.camera.any with required="false""
    @Test
    fun F7_AC45_theManifestAsksForTheCameraWithoutRequiringOne() {
        val manifest = File(repoRoot(), "androidApp/src/main/AndroidManifest.xml").readText()
        assertTrue(
            Regex("""<uses-permission\s+android:name="android\.permission\.CAMERA"\s*/>""").containsMatchIn(manifest),
            "the manifest has <uses-permission android:name=\"android.permission.CAMERA\" />",
        )
        assertTrue(
            Regex("""<uses-feature\s+android:name="android\.hardware\.camera\.any"\s+android:required="false"\s*/>""").containsMatchIn(manifest),
            "the manifest has <uses-feature android:name=\"android.hardware.camera.any\" android:required=\"false\" />",
        )
    }

    // F7-AC45: "Info.plist has NSCameraUsageDescription with the exact string"
    @Test
    fun F7_AC45_infoPlistHasTheCameraUsageText() {
        val plist = File(repoRoot(), "iosApp/iosApp/Info.plist").readText()
        assertTrue(
            Regex("""<key>NSCameraUsageDescription</key>\s*<string>The camera is only used to read your coach’s QR code\.</string>""").containsMatchIn(plist),
            "Info.plist has NSCameraUsageDescription = \"The camera is only used to read your coach’s QR code.\"",
        )
    }

    // F7-AC45: "composeApp androidMain uses com.google.mlkit:barcode-scanning and not play-services-mlkit-barcode-scanning"
    @Test
    fun F7_AC45_androidMainUsesTheBundledMlKitBarcodeScanner() {
        val build = File(repoRoot(), "composeApp/build.gradle.kts").readText()
        val catalog = File(repoRoot(), "gradle/libs.versions.toml").readText()
        assertTrue("play-services-mlkit-barcode-scanning" !in build + catalog, "play-services-mlkit-barcode-scanning must not be used")
        val androidMain = androidMainDependencies(build)
        val direct = "com.google.mlkit:barcode-scanning" in androidMain
        val alias = catalogAliasesFor(catalog, "com.google.mlkit", "barcode-scanning")
            .any { "libs.${it.replace('-', '.').replace('_', '.')}" in androidMain }
        assertTrue(direct || alias, "composeApp androidMain.dependencies uses com.google.mlkit:barcode-scanning")
    }

    // F7-AC45: "nothing under presentation/attend, ui/attend or RecordAttendance.kt references CoachKeyRepository or calls VerifyDailyCode."
    @Test
    fun F7_AC45_theStudentScanPathUsesNoCoachKeys() {
        val files = studentScanPath()
        assertTrue(files.any { it.name == "RecordAttendance.kt" }, "RecordAttendance.kt is scanned")
        assertTrue(files.any { "/ui/attend/" in it.unixPath() }, "ui/attend has sources")
        val problems = mutableListOf<String>()
        for (file in files) {
            val source = file.readText().withoutComments()
            if ("CoachKeyRepository" in source) problems += "${file.path} references CoachKeyRepository"
            if (Regex("""\bverifyDailyCode\s*[.(]|\bVerifyDailyCode\s*\(""").containsMatchIn(source)) problems += "${file.path} calls VerifyDailyCode"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F7-AC46: "no println(, print(, Log. or NSLog( call in ui/attend, presentation/attend, RecordAttendance.kt or the scanner / permission actual files"
    @Test
    fun F7_AC46_nothingInTheScanPathLogs() {
        val actuals = listOf("androidMain", "iosMain", "jvmMain")
            .flatMap { SourceScan.sourcesIn(it) }
            .filter { "/composeApp/" in it.unixPath() && "/ui/platform/" in it.unixPath() }
            .filter { "QrScanner" in it.readText() || "CameraPermission" in it.readText() }
        for (sourceSet in listOf("androidMain", "iosMain", "jvmMain")) {
            assertTrue(actuals.any { "/$sourceSet/" in it.unixPath() }, "$sourceSet has scanner / permission actual files")
        }
        val problems = mutableListOf<String>()
        for (file in studentScanPath() + actuals) {
            val source = file.readText().withoutComments()
            for (call in listOf("""\bprintln\s*\(""", """\bprint\s*\(""", """\bLog\.""", """\bNSLog\s*\(""")) {
                if (Regex(call).containsMatchIn(source)) problems += "${file.path} has a ${call.replace("\\b", "").replace("\\s*", "").replace("\\", "")} call"
            }
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F7-AC46: "AndroidSecretKeyStore.kt contains Log.w("TrxKeys" and IosSecretKeyStore.kt contains NSLog("TrxKeys, with no key or coach-id argument."
    @Test
    fun F7_AC46_theKeyStoresLogOneLineWithoutTheKeyOrCoach() {
        val problems = mutableListOf<String>()
        for ((name, marker) in listOf("AndroidSecretKeyStore.kt" to "Log.w(\"TrxKeys\"", "IosSecretKeyStore.kt" to "NSLog(\"TrxKeys")) {
            val source = singleMainFile(name).readText()
            val at = source.indexOf(marker)
            if (at < 0) {
                problems += "$name has no $marker"
                continue
            }
            val call = source.substring(at, source.indexOf(')', at) + 1)
            if ('$' in call) problems += "$name logs a template value: $call"
            // A11: the line is fixed text only ("Could not save signing key for coach"), so no argument may be a value.
            val arguments = call.substringAfter('(').substringBeforeLast(')').replace(Regex("\"[^\"]*\""), "")
            if (Regex("[A-Za-z_]").containsMatchIn(arguments)) problems += "$name logs a value besides fixed text: $call"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // F7-AC47: "one public type per file, no nested classes" for the new files (A1, A2, A6 and section 9)
    @Test
    fun F7_AC47_newFilesHaveOnePublicTypeAndNoNestedClasses() {
        val problems = mutableListOf<String>()
        // Screens and factories hold composable functions, so they may declare no type at all; they still must not nest one.
        val functionFiles = mapOf(
            "AttendScreen.kt" to "/ui/attend/",
            "ClassSheet.kt" to "/ui/attend/",
            "ScannerScreen.kt" to "/ui/attend/",
            "ScanResultScreen.kt" to "/ui/attend/",
            "StudentSessionsScreen.kt" to "/ui/studentsessions/",
        )
        for ((name, folder) in functionFiles) {
            val files = composeMainFiles().filter { it.name == name && folder in it.unixPath() }
            if (files.size != 1) {
                problems += "expected one $name under composeApp/src/*Main/…$folder, found ${files.map { it.path }}"
                continue
            }
            val source = files.single().readText()
            val types = KotlinPublicTypes.publicTopLevelTypeNames(source)
            if (types.size > 1) problems += "$name has ${types.size} public types: $types"
            if (NestedTypes.names(source).isNotEmpty()) problems += "$name has nested types: ${NestedTypes.names(source)}"
        }
        val typeFiles = mapOf(
            "QrScanner" to "composeApp/src/commonMain/kotlin/ph/project/active/ui/platform/QrScanner.kt",
            "CameraAccess" to "composeApp/src/commonMain/kotlin/ph/project/active/ui/platform/CameraAccess.kt",
            "CameraPermission" to "composeApp/src/commonMain/kotlin/ph/project/active/ui/platform/CameraPermission.kt",
            "ScanHaptics" to "composeApp/src/commonMain/kotlin/ph/project/active/ui/platform/ScanHaptics.kt",
            "DailyCodeLink" to "shared/src/commonMain/kotlin/ph/project/active/domain/model/DailyCodeLink.kt",
        )
        for ((type, path) in typeFiles) {
            val file = File(repoRoot(), path)
            if (!file.isFile) {
                problems += "missing $path"
                continue
            }
            problems += SourceScan.onePublicTypeProblems(file)
            if (type !in KotlinPublicTypes.publicTopLevelTypeNames(file.readText())) problems += "$path does not declare $type"
        }
        val scanText = File(repoRoot(), "shared/src/commonMain/kotlin/ph/project/active/presentation/attend/ScanText.kt")
        if (!scanText.isFile) {
            problems += "missing ${scanText.path}"
        } else {
            if (!Regex("""\bfun\s+looksLikeAttendanceCode\s*\(\s*text\s*:\s*String\s*\)\s*:\s*Boolean""").containsMatchIn(scanText.readText())) {
                problems += "ScanText.kt does not declare fun looksLikeAttendanceCode(text: String): Boolean"
            }
            if (NestedTypes.names(scanText.readText()).isNotEmpty()) problems += "ScanText.kt has nested types"
        }
        assertTrue(problems.isEmpty(), problems.distinct().joinToString("\n"))
    }

    // F7-AC47: "StudentTabPlaceholder is gone." (A1: "Delete StudentTabPlaceholder and its stale "flow 5" comment.")
    @Test
    fun F7_AC47_studentTabPlaceholderIsGone() {
        val using = composeMainFiles().filter { "StudentTabPlaceholder" in it.readText() || it.name == "StudentTabPlaceholder.kt" }
        assertTrue(using.isEmpty(), "StudentTabPlaceholder is still in ${using.map { it.path }}")
    }

    // F7-AC15 / F7-AC36: "no placeholder_tag" on Attend or Sessions; section 5: StudentHomeScreen shows the real screens
    @Test
    fun F7_AC47_studentHomeShowsTheRealAttendAndSessionsScreens() {
        val studentHome = singleMainFile("StudentHomeScreen.kt").readText()
        assertTrue(studentHome.contains("AttendScreen("), "StudentHomeScreen shows AttendScreen")
        assertTrue(studentHome.contains("StudentSessionsScreen("), "StudentHomeScreen shows StudentSessionsScreen")
    }

    // Section 8: "AttendTestTags in ui.attend, StudentSessionsTestTags in ui.studentsessions, one object per file" with the new tags
    @Test
    fun F7_AC47_theTestTagObjectsHoldTheNewTags() {
        val attendTags = listOf(
            "attend_overline", "attend_subtitle", "attend_card_title", "attend_card_body", "attend_choose_class",
            "attend_recorded_header", "attend_recorded_row_", "attend_sheet", "attend_sheet_close", "attend_choice_",
            "attend_sheet_scan", "attend_sheet_empty", "camera_denied", "scanner", "scanner_close", "scanner_title",
            "scanner_torch", "scanner_recording_label", "scanner_recording_for", "scanner_change", "scanner_hint",
            "scanner_hint_detail", "camera_permission", "camera_allow", "camera_open_settings", "result_present",
            "result_saved_offline", "result_already", "result_rejected", "result_message", "result_you_chose",
            "result_today", "result_code_date", "result_done", "result_record_another", "result_choose_different",
            "result_scan_again", "result_close",
        )
        val sessionsTags = listOf(
            "sessions_overline", "sessions_subtitle", "sessions_group_upcoming", "sessions_group_today",
            "sessions_group_earlier", "sessions_row_", "sessions_today_tag", "sessions_empty",
        )
        val problems = mutableListOf<String>()
        for ((type, folder, tags) in listOf(
            Triple("AttendTestTags", "/ui/attend/", attendTags),
            Triple("StudentSessionsTestTags", "/ui/studentsessions/", sessionsTags),
        )) {
            val declaring = composeMainFiles().filter { type in KotlinPublicTypes.publicTopLevelTypeNames(it.readText()) }
            if (declaring.size != 1) {
                problems += "expected one $type object, found ${declaring.map { it.path }}"
                continue
            }
            val file = declaring.single()
            if (folder !in file.unixPath()) problems += "$type should live in $folder"
            problems += SourceScan.onePublicTypeProblems(file)
            val source = file.readText()
            val missing = tags.filter { "\"$it" !in source }
            if (missing.isNotEmpty()) problems += "$type is missing $missing"
        }
        assertTrue(problems.isEmpty(), problems.joinToString("\n"))
    }

    // Section 8: "plus the semantics key sessions_row_cancelled (its own file)"
    @Test
    fun F7_AC47_sessionsRowCancelledIsASemanticsKeyInItsOwnFile() {
        val declaring = composeMainFiles().filter { "SemanticsPropertyKey" in it.readText() && "\"sessions_row_cancelled\"" in it.readText() }
        assertEquals(1, declaring.size, "one file declares the sessions_row_cancelled semantics key, found ${declaring.map { it.path }}")
        val file = declaring.single()
        assertTrue("/ui/studentsessions/" in file.unixPath(), "the key lives in ui/studentsessions")
        assertTrue(!file.name.endsWith("Screen.kt") && file.name != "StudentSessionsTestTags.kt", "the key has its own file, not ${file.name}")
        assertTrue(NestedTypes.names(file.readText()).isEmpty(), "${file.name} has nested types")
    }

    /** presentation/attend (shared), ui/attend (composeApp) and RecordAttendance.kt, main source sets only. */
    private fun studentScanPath(): List<File> = mainFiles().filter {
        val path = it.unixPath()
        "/presentation/attend/" in path || "/ui/attend/" in path || it.name == "RecordAttendance.kt"
    }

    private fun mainFiles(): List<File> = listOf("commonMain", "androidMain", "iosMain", "jvmMain").flatMap { SourceScan.sourcesIn(it) }

    private fun composeMainFiles(): List<File> = mainFiles().filter { "/composeApp/" in it.unixPath() }

    private fun singleMainFile(name: String): File {
        val matches = composeMainFiles().filter { it.name == name }
        assertEquals(1, matches.size, "expected one $name under composeApp/src/*Main, found ${matches.map { it.path }}")
        return matches.single()
    }

    /** The `viewModel { … }` block that contains [marker], or "" when there is none. */
    private fun blockAround(source: String, marker: String): String {
        val at = Regex("""\b${Regex.escape(marker)}""").find(source)?.range?.first ?: return ""
        val open = source.lastIndexOf("viewModel", at)
        return if (open < 0) "" else source.substring(open, minOf(source.length, at + 600))
    }

    /** The text of the `androidMain.dependencies { … }` block in composeApp/build.gradle.kts, or "" when there is none. */
    private fun androidMainDependencies(build: String): String {
        val at = build.indexOf("androidMain.dependencies")
        if (at < 0) return ""
        val open = build.indexOf('{', at)
        var depth = 0
        for (i in open until build.length) {
            if (build[i] == '{') depth += 1
            if (build[i] == '}') depth -= 1
            if (depth == 0) return build.substring(open, i + 1)
        }
        return ""
    }

    /** Version-catalog library aliases whose coordinates are [group]:[name]. */
    private fun catalogAliasesFor(catalog: String, group: String, name: String): List<String> =
        catalog.lines().mapNotNull { line ->
            val alias = line.substringBefore('=').trim()
            val value = line.substringAfter('=', "")
            val matches = "\"$group:$name\"" in value ||
                (Regex("""group\s*=\s*"${Regex.escape(group)}"""").containsMatchIn(value) && Regex("""name\s*=\s*"${Regex.escape(name)}"""").containsMatchIn(value))
            if (matches && alias.isNotEmpty()) alias else null
        }

    private fun String.withoutComments(): String =
        replace(Regex("""/\*[\s\S]*?\*/"""), "").replace(Regex("""//[^\n]*"""), "")

    private fun File.unixPath(): String = path.replace('\\', '/')
}
