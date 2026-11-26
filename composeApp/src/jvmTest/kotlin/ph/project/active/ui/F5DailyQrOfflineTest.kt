package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.DayOfWeek
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.openDailyQr
import ph.project.active.ui.testing.runFlow5UiTest
import ph.project.active.ui.testing.seedD19
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.topOf

private const val OFFLINE_TITLE = "No internet connection"
private const val OFFLINE_SUBTITLE = "Showing today\u2019s code saved on this phone."
private const val CALLOUT_TITLE = "Today\u2019s class list can\u2019t load"
private const val CALLOUT_BODY = "The code still works. Students on the roster can scan it now; their phones upload later."

/** Flow 5 D20 and decisions 9–10: the offline banner, the callout, and the empty list (F5-AC7 to F5-AC10). */
@OptIn(ExperimentalTestApi::class)
class F5DailyQrOfflineTest {

    // F5-AC7: "Offline with the D19 sessions already in the store: offline_banner shows under the title, the title text is "No internet connection", the subtitle … is visible"
    @Test
    fun F5_AC7_offlineWithSavedSessionsShowsTheBannerAndSubtitleUnderTheTitle() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.network.set(NetworkStatus.OFFLINE)
            openDailyQr(world)

            assertTextIs("offline_banner", OFFLINE_TITLE)
            assertTextShown(OFFLINE_SUBTITLE)
            assertTrue(topOf("offline_banner") > topOf("coach_qr_title"), "the banner sits under the title")
        }
    }

    // F5-AC7: "the QR card still shows, and the three session rows still show. The D20 callout is not showing. classListUnavailable is false."
    @Test
    fun F5_AC7_offlineWithSavedSessionsStillShowsTheCodeAndTheRowsWithoutTheCallout() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.seedD19()
            world.network.set(NetworkStatus.OFFLINE)
            openDailyQr(world)

            tag("coach_qr_card").assertExists()
            for (name in listOf("Sunrise TRX Strength", "Lunch Express TRX", "TRX Core Burn")) {
                assertEquals(1, countTextUnder("coach_qr_sessions", name), "$name still shows")
            }
            tag("coach_qr_callout").assertDoesNotExist()
            assertTextNotShown(CALLOUT_TITLE)
            assertFalse(world.lastDailyQr!!.state.value.classListUnavailable)
        }
    }

    // F5-AC8: "Offline with no classes for this coach: the banner and subtitle show, the QR card still shows, and the callout shows classListUnavailableTitle() and classListUnavailableBody()"
    @Test
    fun F5_AC8_offlineWithNoClassesShowsTheCalloutAndStillShowsTheCode() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.mara()
            world.network.set(NetworkStatus.OFFLINE)
            openDailyQr(world)

            assertTextIs("offline_banner", OFFLINE_TITLE)
            assertTextShown(OFFLINE_SUBTITLE)
            tag("coach_qr_card").assertExists()
            assertTextUnder("coach_qr_callout", CALLOUT_TITLE)
            assertTextUnder("coach_qr_callout", CALLOUT_BODY)
        }
    }

    // F5-AC8: "There are no session rows. classListUnavailable is true."
    @Test
    fun F5_AC8_offlineWithNoClassesHasNoRowsAndIsUnavailable() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.mara()
            world.network.set(NetworkStatus.OFFLINE)
            openDailyQr(world)

            assertTextNotShown("Scheduled")
            assertTextNotShown("Cancelled")
            assertTrue(world.lastDailyQr!!.state.value.classListUnavailable)
            assertTrue(world.lastDailyQr!!.state.value.code.isNotBlank())
        }
    }

    // F5-AC9: "Online with no classes for this coach: the banner and the D20 callout are both hidden, the code still shows, the section header still shows, and the session list is empty. classListUnavailable is false."
    @Test
    fun F5_AC9_onlineWithNoClassesShowsTheCodeAndHeaderWithAnEmptyList() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.mara()
            openDailyQr(world)

            tag("offline_banner").assertDoesNotExist()
            tag("coach_qr_callout").assertDoesNotExist()
            assertTextNotShown(CALLOUT_TITLE)
            tag("coach_qr_card").assertExists()
            assertTextShown("This code covers today")
            assertTextShown("One code \u00B7 all classes")
            assertTextNotShown("Scheduled")
            assertTextNotShown("Cancelled")
            assertFalse(world.lastDailyQr!!.state.value.classListUnavailable)
            assertTrue(world.lastDailyQr!!.state.value.code.isNotBlank())
        }
    }

    // F5-AC10: "Online with classes but no sessions today: same as F5-AC9 (no banner, no callout, empty list, classListUnavailable is false)."
    @Test
    fun F5_AC10_onlineWithClassesButNoSessionTodayLooksLikeAc9() = runFlow5UiTest {
        Flow4Harness().use { world ->
            world.saveClass("TRX Tuesday Only", listOf(DayOfWeek.TUESDAY), world.slot(7, 0, 8, 0))
            openDailyQr(world)

            tag("offline_banner").assertDoesNotExist()
            tag("coach_qr_callout").assertDoesNotExist()
            tag("coach_qr_card").assertExists()
            assertTextShown("This code covers today")
            assertTextShown("One code \u00B7 all classes")
            assertEquals(0, countTextUnder("coach_qr_sessions", "TRX Tuesday Only"))
            assertTextNotShown("Scheduled")
            assertFalse(world.lastDailyQr!!.state.value.classListUnavailable)
        }
    }
}
