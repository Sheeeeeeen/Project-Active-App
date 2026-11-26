package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ph.project.active.domain.model.TrxClass
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.Flow7Example
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.boundsOfTagUnder
import ph.project.active.ui.testing.boundsOfTextUnder
import ph.project.active.ui.testing.openSessions
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.sessionsRowTag

/** Flow 7 C5b layout rule: the TODAY tag sits on its own line under the class name (F7-AC39). */
@OptIn(ExperimentalTestApi::class)
class F7SessionsTodayTagTest {

    // F7-AC39: "On every today row, the sessions_today_tag "TODAY" is entirely below the class name's bounds and does not overlap the status chip's bounds. Check this at 360dp … with the long name "Sunrise TRX Strength"."
    @Test
    fun F7_AC39_theTodayTagSitsUnderTheNameAt360dp() = runFlow7UiTest(width = 360) {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            assertTodayTagLayout(world, example)
        }
    }

    // F7-AC39: "… Check this at … 412dp wide, with the long name "Sunrise TRX Strength"."
    @Test
    fun F7_AC39_theTodayTagSitsUnderTheNameAt412dp() = runFlow7UiTest(width = 412) {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            assertTodayTagLayout(world, example)
        }
    }
}

/** For each of today's rows: TODAY is entirely below the class name and does not touch the status chip text. */
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.assertTodayTagLayout(world: Flow4Harness, example: Flow7Example) {
    val todayRows: List<Pair<TrxClass, String>> = listOf(
        example.mobilityFlow to "Upcoming",
        example.coreBurn to "Upcoming",
        example.sunrise to "Present",
        example.lunchExpress to "Cancelled",
    )
    for ((trxClass, status) in todayRows) {
        val row = sessionsRowTag(world.sessionIdOn(trxClass, MON_28_SEP_2026))
        val tag = boundsOfTagUnder(row, "sessions_today_tag")
        val name = boundsOfTextUnder(row, trxClass.name)
        val chip = boundsOfTextUnder(row, status)
        assertTrue(tag.top >= name.bottom, "TODAY ($tag) is entirely below \"${trxClass.name}\" ($name)")
        assertFalse(tag.overlaps(chip), "TODAY ($tag) does not overlap the \"$status\" chip ($chip)")
    }
}
