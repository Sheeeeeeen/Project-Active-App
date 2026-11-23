package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import ph.project.active.domain.model.DailyCodeCheck
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.HostLifecycle
import ph.project.active.ui.testing.TUE_29_SEP_2026
import ph.project.active.ui.testing.assertNoDialogShown
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD19

/** Flow 5 decision 11 and F5-AC11: a new day's code on foreground or tab show, with no timer and no dialog. */
@OptIn(ExperimentalTestApi::class)
class F5CodeRolloverTest {

    // F5-AC11: "move the clock to Tue 29 Sep 2026 and return the tab to the foreground … The subtitle and the "Valid … only" line show "Tue 29 Sep 2026", state.code is not the old value, and the old value checks as WRONG_DATE. No dialog is shown."
    @Test
    fun F5_AC11_returningToTheForegroundOnANewDayShowsTheNewCode() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d19 = world.seedD19()
            val lifecycle = HostLifecycle()
            openDailyQr(world, lifecycle)
            val oldCode = world.lastDailyQr!!.state.value.code

            runOnIdle { lifecycle.background() }
            waitForIdle()
            world.setToday(TUE_29_SEP_2026)
            runOnIdle { lifecycle.foreground() }
            waitForIdle()

            assertTextIs("coach_qr_subtitle", "Tue 29 Sep 2026 \u00B7 Coach Mara Santos")
            assertTextIs("coach_qr_valid", "Valid Tue 29 Sep 2026 only")
            assertNotEquals(oldCode, world.lastDailyQr!!.state.value.code)
            assertEquals(DailyCodeCheck.WRONG_DATE, world.verifyDailyCode().check(oldCode, d19.mara.id))
            assertNoDialogShown()
        }
    }

    // Decision 11: "Call refresh() whenever the QR tab is shown" — coming back to the tab on a new day also rolls the code
    @Test
    fun F5_AC11_showingTheQrTabAgainOnANewDayShowsTheNewCode() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val d19 = world.seedD19()
            openDailyQr(world)
            val oldCode = world.lastDailyQr!!.state.value.code
            openCoachTab("classes")

            world.setToday(TUE_29_SEP_2026)
            openCoachTab("qr")

            assertTextIs("coach_qr_valid", "Valid Tue 29 Sep 2026 only")
            assertNotEquals(oldCode, world.lastDailyQr!!.state.value.code)
            assertEquals(DailyCodeCheck.WRONG_DATE, world.verifyDailyCode().check(oldCode, d19.mara.id))
            assertNoDialogShown()
        }
    }

    // Decision 11 / ruling 3: "never on a timer" — the date does not change while the tab just stays open
    @Test
    fun F5_AC11_theCodeDoesNotChangeWhileTheTabJustStaysOpen() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)
            val oldCode = world.lastDailyQr!!.state.value.code

            world.setToday(TUE_29_SEP_2026)
            mainClock.advanceTimeBy(60_000)
            waitForIdle()

            assertTextIs("coach_qr_valid", "Valid Mon 28 Sep 2026 only")
            assertEquals(oldCode, world.lastDailyQr!!.state.value.code)
        }
    }
}
