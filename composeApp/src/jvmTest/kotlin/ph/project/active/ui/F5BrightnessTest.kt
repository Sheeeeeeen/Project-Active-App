package ph.project.active.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.HostLifecycle
import ph.project.active.ui.testing.mountFlow5
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD19

/** Flow 5 decision 5 and F5-AC5: full brightness and keep-awake only while the QR tab is visible (fake, no system APIs). */
@OptIn(ExperimentalTestApi::class)
class F5BrightnessTest {

    // F5-AC5: "when the QR tab becomes visible, brightness is raised to full and keep-awake is on"
    @Test
    fun F5_AC5_showingTheQrTabRaisesBrightnessAndKeepsTheScreenAwake() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.signInAsMara()
            mountFlow5(world)
            assertEquals(emptyList(), world.brightness.calls, "nothing changes on the Classes tab")

            openCoachTab("qr")

            assertTrue(world.brightness.isRaised)
            assertEquals(listOf("raise"), world.brightness.calls)
        }
    }

    // F5-AC5: "when the coach selects Classes … brightness is restored and keep-awake is off"
    @Test
    fun F5_AC5_selectingClassesRestoresBrightnessAndSleep() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.signInAsMara()
            mountFlow5(world)
            openCoachTab("qr")

            openCoachTab("classes")

            assertFalse(world.brightness.isRaised)
            assertEquals(listOf("raise", "restore"), world.brightness.calls)
        }
    }

    // F5-AC5: "or the screen is disposed, brightness is restored and keep-awake is off"
    @Test
    fun F5_AC5_disposingTheScreenRestoresBrightnessAndSleep() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.signInAsMara()
            val shown = mutableStateOf(true)
            mountFlow5(world, shown = shown)
            openCoachTab("qr")

            shown.value = false
            waitForIdle()

            assertFalse(world.brightness.isRaised)
            assertEquals(listOf("raise", "restore"), world.brightness.calls)
        }
    }

    // Decision 5: "Restore the previous brightness, and allow the screen to sleep again, when the coach … backgrounds the app"
    @Test
    fun F5_AC5_backgroundingRestoresAndComingBackRaisesAgain() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.signInAsMara()
            val lifecycle = HostLifecycle()
            mountFlow5(world, lifecycle)
            openCoachTab("qr")

            runOnIdle { lifecycle.background() }
            waitForIdle()
            assertFalse(world.brightness.isRaised, "restored in the background")

            runOnIdle { lifecycle.foreground() }
            waitForIdle()
            assertTrue(world.brightness.isRaised, "raised again when the QR tab is back in front")
        }
    }

    // Decision 5: leaving the QR tab for Attendance also restores brightness and sleep
    @Test
    fun F5_AC5_selectingAttendanceAlsoRestores() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.signInAsMara()
            mountFlow5(world)
            openCoachTab("qr")

            openCoachTab("attendance")

            assertFalse(world.brightness.isRaised)
        }
    }
}
