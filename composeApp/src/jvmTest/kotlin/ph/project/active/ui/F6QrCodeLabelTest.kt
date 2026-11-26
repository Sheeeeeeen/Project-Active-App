package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.HostLifecycle
import ph.project.active.ui.testing.TUE_29_SEP_2026
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD19

/** Flow 6 decision 17 and F6-AC20: screen readers read the QR image as today's attendance code. */
@OptIn(ExperimentalTestApi::class)
class F6QrCodeLabelTest {

    // F6-AC20: "the QR image (under coach_qr_card) has the spoken label "Today’s attendance code, valid Mon 28 Sep 2026 only""
    @Test
    fun F6_AC20_theQrImageHasASpokenLabelNamingTodaysDate() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            openDailyQr(world)

            assertEquals(1, qrImagesLabelled("Today\u2019s attendance code, valid Mon 28 Sep 2026 only"))
        }
    }

    // F6-AC20: "After the F5-AC11 date change it reads "…valid Tue 29 Sep 2026 only"."
    @Test
    fun F6_AC20_theLabelFollowsTheNewDayAfterTheForeground() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            val lifecycle = HostLifecycle()
            openDailyQr(world, lifecycle)

            runOnIdle { lifecycle.background() }
            waitForIdle()
            world.setToday(TUE_29_SEP_2026)
            runOnIdle { lifecycle.foreground() }
            waitForIdle()

            assertEquals(1, qrImagesLabelled("Today\u2019s attendance code, valid Tue 29 Sep 2026 only"))
            assertEquals(0, qrImagesLabelled("Today\u2019s attendance code, valid Mon 28 Sep 2026 only"))
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.qrImagesLabelled(label: String): Int =
        onAllNodes(hasContentDescription(label) and hasAnyAncestor(hasTestTag("coach_qr_card")), useUnmergedTree = true)
            .fetchSemanticsNodes(atLeastOneRootRequired = false).size
}
