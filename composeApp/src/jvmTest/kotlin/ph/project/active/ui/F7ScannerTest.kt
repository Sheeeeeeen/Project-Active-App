package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.testing.FakeCameraPermission
import ph.project.active.ui.testing.FakeQrScanner
import ph.project.active.ui.testing.FakeScanHaptics
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.SystemBack
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertNoTabBar
import ph.project.active.ui.testing.assertTabBarShown
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.choiceTag
import ph.project.active.ui.testing.chooseAndScan
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.pressSystemBack
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.scanText
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tap

/** Flow 7 C10 and C11: the scanner (F7-AC22 to F7-AC27), with camera access GRANTED and the fake scanner. */
@OptIn(ExperimentalTestApi::class)
class F7ScannerTest {

    // F7-AC22: "With access GRANTED, choose Core Burn and tap Scan: scanner shows and the tab bar is hidden"
    @Test
    fun F7_AC22_scanOpensTheScannerWithoutTheTabBar() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)

            chooseAndScan(world, example.coreBurn)

            tag("scanner").assertExists()
            assertNoTabBar()
        }
    }

    // F7-AC22: "scanner_title reads "Scan coach’s code", scanner_recording_label "RECORDING FOR", scanner_recording_for "TRX Core Burn · 5:30 PM""
    @Test
    fun F7_AC22_theScannerHeaderAndRecordingForPill() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)
            chooseAndScan(world, example.coreBurn)

            assertTextIs("scanner_title", "Scan coach\u2019s code")
            assertTextIs("scanner_recording_label", "RECORDING FOR")
            assertTextIs("scanner_recording_for", "TRX Core Burn \u00B7 5:30 PM")
        }
    }

    // F7-AC22: "scanner_change reads "Change" and returns to the sheet with Core Burn selected"
    @Test
    fun F7_AC22_changeGoesBackToTheSheetWithTheClassStillSelected() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)
            chooseAndScan(world, example.coreBurn)

            assertTextIs("scanner_change", "Change")
            tap("scanner_change")

            tag("scanner").assertDoesNotExist()
            tag("attend_sheet").assertExists()
            tag(choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))).assertIsSelected()
        }
    }

    // F7-AC22: "scanner_hint reads "Point at the QR code on your coach’s phone" and scanner_hint_detail "It scans automatically."; fake_qr_preview shows."
    @Test
    fun F7_AC22_theHintsAndThePreview() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)
            chooseAndScan(world, example.coreBurn)

            assertTextIs("scanner_hint", "Point at the QR code on your coach\u2019s phone")
            assertTextIs("scanner_hint_detail", "It scans automatically.")
            tag("fake_qr_preview").assertExists()
        }
    }

    // F7-AC23: "Offline, the scanner shows offline_banner with "No internet connection" and "Scans are saved on this phone and upload when you are back online.", and scanner_hint_detail reads "No connection needed. The scan is kept on this phone.""
    @Test
    fun F7_AC23_offlineTheScannerShowsTheBannerAndTheOfflineHint() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.network.set(NetworkStatus.OFFLINE)
            openAttend(world, example)
            chooseAndScan(world, example.coreBurn)

            assertTextIs("offline_banner", "No internet connection")
            assertTextUnder("scanner", "Scans are saved on this phone and upload when you are back online.")
            assertTextIs("scanner_hint_detail", "No connection needed. The scan is kept on this phone.")
        }
    }

    // F7-AC24: "scanner_torch exists only when the fake has hasTorch = true. It toggles the fake's torchOn on and off"
    @Test
    fun F7_AC24_theTorchShowsOnlyWithATorchAndToggles() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner(hasTorch = true)
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)

            assertFalse(scanner.torchOn)
            tap("scanner_torch")
            assertTrue(scanner.torchOn, "the first tap turns the torch on")
            tap("scanner_torch")
            assertFalse(scanner.torchOn, "the second tap turns it off")
        }
    }

    // F7-AC24: "scanner_torch exists only when the fake has hasTorch = true."
    @Test
    fun F7_AC24_noTorchButtonWithoutATorch() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example, scanner = FakeQrScanner(hasTorch = false))
            chooseAndScan(world, example.coreBurn)

            tag("scanner").assertExists()
            assertEquals(0, countTagged("scanner_torch"))
        }
    }

    // F7-AC25: "scanner_close … return[s] to the Attend home with the sheet closed, no record, and the tab bar back."
    @Test
    fun F7_AC25_closeReturnsToAttendHome() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)
            chooseAndScan(world, example.coreBurn)

            tap("scanner_close")

            tag("scanner").assertDoesNotExist()
            tag("attend_sheet").assertDoesNotExist()
            tag(StudentTabBarTestTags.attendRoot).assertExists()
            assertTabBarShown()
            assertEquals(1, world.attendance.all().size, "no record")
        }
    }

    // F7-AC25: "… and a system back press, return to the Attend home with the sheet closed, no record, and the tab bar back."
    @Test
    fun F7_AC25_systemBackReturnsToAttendHome() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val back = SystemBack()
            openAttend(world, example, back = back)
            chooseAndScan(world, example.coreBurn)

            pressSystemBack(back)

            tag("scanner").assertDoesNotExist()
            tag("attend_sheet").assertDoesNotExist()
            tag(StudentTabBarTestTags.attendRoot).assertExists()
            assertTabBarShown()
            assertEquals(1, world.attendance.all().size, "no record")
        }
    }

    // F7-AC26: "The fake emits "https://example.com": the scanner stays, with no result and no record. It then emits Mara's code: C12."
    @Test
    fun F7_AC26_anotherQrIsIgnoredAndMarasCodeIsAccepted() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, "https://example.com")

            tag("scanner").assertExists()
            for (result in listOf("result_present", "result_saved_offline", "result_already", "result_rejected")) {
                assertEquals(0, countTagged(result), "no $result")
            }
            assertEquals(1, world.attendance.all().size, "no record")

            scanText(scanner, example.marasCode)

            tag("result_present").assertExists()
            assertEquals(2, world.attendance.all().size)
        }
    }

    // F7-AC27: "The fake emits Mara's code twice in one frame: one record, one C12, and success() called once."
    @Test
    fun F7_AC27_twoScansInOneFrameGiveOneRecordAndOneVibration() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            val code = example.marasCode
            runOnIdle {
                scanner.emit(code)
                scanner.emit(code)
            }
            waitForIdle()

            assertEquals(1, world.attendance.all().count { it.classId == example.coreBurn.id }, "one Core Burn record")
            assertEquals(1, countTagged("result_present"))
            assertEquals(0, countTagged("result_already"), "the second text is dropped, not recorded as a repeat")
            assertEquals(1, haptics.successCount)
        }
    }

    // F7-AC24: "… and it is absent on C9." (with a torch, before camera access is granted)
    @Test
    fun F7_AC24_noTorchOnTheCameraPermissionScreen() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(
                world,
                example,
                scanner = FakeQrScanner(hasTorch = true),
                permission = FakeCameraPermission(CameraAccess.NOT_ASKED),
            )
            chooseAndScan(world, example.coreBurn)

            tag("camera_permission").assertExists()
            assertEquals(0, countTagged("scanner_torch"))
        }
    }
}
