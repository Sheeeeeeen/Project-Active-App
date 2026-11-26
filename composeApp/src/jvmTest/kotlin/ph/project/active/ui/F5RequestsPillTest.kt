package ph.project.active.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.math.abs
import kotlin.test.assertTrue
import ph.project.active.presentation.manageclass.ManageClassUiState
import ph.project.active.ui.manageclass.ManageClassScreen
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.noManageActions
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTheme
import ph.project.active.ui.theme.TrxTypography

/** Flow 5 decision 18 and F5-AC16: the Requests count pill has a 20dp minimum width and caps at "99+". */
@OptIn(ExperimentalTestApi::class)
class F5RequestsPillTest {

    // F5-AC16: "the pill tagged manage_tab_requests_badge is hidden when pendingRequestCount is 0"
    @Test
    fun F5_AC16_thePillIsHiddenAtZero() = runFlow5UiTest {
        Flow4Harness().use { world ->
            showManage(world, pendingRequestCount = 0)
            tag("manage_tab_requests_badge").assertDoesNotExist()
        }
    }

    // F5-AC16: "At 1 it shows "1"; at 99 it shows "99""
    @Test
    fun F5_AC16_oneTo99ShowTheNumber() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val count = mutableIntStateOf(1)
            showManage(world, count)
            assertTextIs("manage_tab_requests_badge", "1")

            count.intValue = 99
            waitForIdle()
            assertTextIs("manage_tab_requests_badge", "99")
        }
    }

    // F5-AC16: "at 100 and at 1000 it shows "99+""
    @Test
    fun F5_AC16_100AndMoreShow99Plus() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val count = mutableIntStateOf(100)
            showManage(world, count)
            assertTextIs("manage_tab_requests_badge", "99+")

            count.intValue = 1000
            waitForIdle()
            assertTextIs("manage_tab_requests_badge", "99+")
        }
    }

    // F5-AC16: "The pill’s height is 20.dp and its width is at least 20.dp (TrxSpacing.s20)."
    @Test
    fun F5_AC16_thePillIs20dpHighAndAtLeast20dpWide() = runFlow5UiTest {
        Flow4Harness().use { world ->
            val count = mutableIntStateOf(1)
            showManage(world, count)
            assertEquals(20.dp, TrxSpacing.s20)

            for (n in listOf(1, 99, 100)) {
                count.intValue = n
                waitForIdle()
                val size = tag("manage_tab_requests_badge").fetchSemanticsNode().size
                val height = with(density) { size.height.toDp() }
                val width = with(density) { size.width.toDp() }
                assertTrue(abs(height.value - 20f) < 0.5f, "height is 20dp at $n, got $height")
                assertTrue(width.value >= 20f - 0.5f, "width is at least 20dp at $n, got $width")
            }
        }
    }

    // F5-AC16 / decision 18: "so three-digit text is not clipped … It can grow wider than 20.dp to fit "99+""
    @Test
    fun F5_AC16_thePillGrowsToFit99Plus() = runFlow5UiTest {
        Flow4Harness().use { world ->
            var labelWidthPx = 0
            showManage(world, mutableIntStateOf(100)) {
                val measurer = rememberTextMeasurer()
                labelWidthPx = measurer.measure("99+", TrxTypography.label).size.width
            }
            val pillWidthPx = tag("manage_tab_requests_badge").fetchSemanticsNode().size.width
            assertTrue(pillWidthPx >= labelWidthPx, "pill ${pillWidthPx}px fits \"99+\" (${labelWidthPx}px)")
            assertTrue(with(density) { pillWidthPx.toDp() } > 20.dp, "the pill is wider than 20dp for \"99+\"")
        }
    }

    private fun ComposeUiTest.showManage(world: Flow4Harness, pendingRequestCount: Int) =
        showManage(world, mutableIntStateOf(pendingRequestCount))

    /** Shows the stateless Manage screen for Core Burn, with the request count the test sets. */
    private fun ComposeUiTest.showManage(
        world: Flow4Harness,
        pendingRequestCount: MutableIntState,
        extra: @Composable () -> Unit = {},
    ) {
        val d1 = world.seedD1()
        val base: ManageClassUiState = world.manageClass(world.maraAccount(), d1.coreBurn.id).state.value
        setContent {
            TrxTheme(dark = false) {
                extra()
                ManageClassScreen(
                    state = base.copy(pendingRequestCount = pendingRequestCount.intValue),
                    actions = noManageActions,
                    onBack = {},
                )
            }
        }
        waitForIdle()
    }
}
