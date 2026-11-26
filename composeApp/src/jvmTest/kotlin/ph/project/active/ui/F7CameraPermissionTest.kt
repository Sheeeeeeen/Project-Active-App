package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.testing.FakeCameraPermission
import ph.project.active.ui.testing.FakeQrScanner
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.HostLifecycle
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.choiceTag
import ph.project.active.ui.testing.chooseAndScan
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tap

/** Flow 7 decision 4, C9 and C18: asking for the camera (F7-AC28 to F7-AC30). */
@OptIn(ExperimentalTestApi::class)
class F7CameraPermissionTest {

    // F7-AC28: "Access NOT_ASKED: Scan shows camera_permission with "Allow camera to scan", the body, and camera_allow "Allow camera". There is no camera_open_settings, no preview and no torch."
    @Test
    fun F7_AC28_notAskedShowsTheAllowCameraScreen() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val permission = FakeCameraPermission(CameraAccess.NOT_ASKED)
            openAttend(world, example, scanner = FakeQrScanner(hasTorch = true), permission = permission)

            chooseAndScan(world, example.coreBurn)

            assertTextUnder("camera_permission", "Allow camera to scan")
            assertTextUnder(
                "camera_permission",
                "The camera is only used to read your coach\u2019s QR code. Nothing is recorded until a valid code is scanned.",
            )
            assertTextIs("camera_allow", "Allow camera")
            assertEquals(0, countTagged("camera_open_settings"))
            assertEquals(0, countTagged("fake_qr_preview"))
            assertEquals(0, countTagged("scanner_torch"))
            assertEquals(0, permission.requestCount, "the system prompt waits for the Allow camera tap")
        }
    }

    // F7-AC28: "camera_allow calls request() once. Answer GRANTED → the scanner with preview."
    @Test
    fun F7_AC28_allowCameraAsksOnceAndGrantedShowsThePreview() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val permission = FakeCameraPermission(CameraAccess.NOT_ASKED)
            permission.nextAnswer = CameraAccess.GRANTED
            openAttend(world, example, permission = permission)
            chooseAndScan(world, example.coreBurn)

            tap("camera_allow")

            assertEquals(1, permission.requestCount)
            tag("scanner").assertExists()
            tag("fake_qr_preview").assertExists()
            assertEquals(0, countTagged("camera_permission"))
        }
    }

    // F7-AC28: "Answer DENIED_FOR_GOOD → the scanner closes, and the sheet shows C18 with Core Burn still selected."
    @Test
    fun F7_AC28_deniedForGoodClosesTheScannerAndTheSheetShowsC18() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val permission = FakeCameraPermission(CameraAccess.NOT_ASKED)
            permission.nextAnswer = CameraAccess.DENIED_FOR_GOOD
            openAttend(world, example, permission = permission)
            chooseAndScan(world, example.coreBurn)

            tap("camera_allow")

            tag("scanner").assertDoesNotExist()
            tag("attend_sheet").assertExists()
            tag("camera_denied").assertExists()
            tag(choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))).assertIsSelected()
        }
    }

    // F7-AC29: "Answer DENIED_CAN_ASK → camera_permission stays, with both camera_allow and camera_open_settings "Open Settings". Open Settings calls openSettings() once."
    @Test
    fun F7_AC29_deniedCanAskKeepsC9WithOpenSettings() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val permission = FakeCameraPermission(CameraAccess.NOT_ASKED)
            permission.nextAnswer = CameraAccess.DENIED_CAN_ASK
            openAttend(world, example, permission = permission)
            chooseAndScan(world, example.coreBurn)

            tap("camera_allow")

            tag("camera_permission").assertExists()
            assertTextIs("camera_allow", "Allow camera")
            assertTextIs("camera_open_settings", "Open Settings")
            tap("camera_open_settings")
            assertEquals(1, permission.openSettingsCount)
        }
    }

    // F7-AC30: "On C18, set the fake to GRANTED and bring the app to the foreground (HostLifecycle): the callout goes away, and attend_sheet_scan shows and is enabled."
    @Test
    fun F7_AC30_comingBackFromSettingsWithAccessShowsScanAgain() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val permission = FakeCameraPermission(CameraAccess.DENIED_FOR_GOOD)
            val lifecycle = HostLifecycle()
            openAttend(world, example, permission = permission, lifecycle = lifecycle)
            tap(choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026)))
            tag("camera_denied").assertExists()

            lifecycle.background()
            waitForIdle()
            permission.access = CameraAccess.GRANTED
            lifecycle.foreground()
            waitForIdle()

            tag("camera_denied").assertDoesNotExist()
            tag("attend_sheet_scan").assertExists()
            tag("attend_sheet_scan").assertIsEnabled()
        }
    }
}
