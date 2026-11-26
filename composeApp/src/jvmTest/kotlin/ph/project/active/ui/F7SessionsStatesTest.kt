package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.testing.FakeQrScanner
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertNothingClickableUnder
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.chooseAndScan
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.mountFlow7
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.openSessions
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.paoloScan
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.scanText
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.sessionsRowTag
import ph.project.active.ui.testing.signInAs
import ph.project.active.ui.testing.tap
import ph.project.active.ui.testing.textsUnder
import ph.project.active.ui.testing.topOf

/** Flow 7 Sessions tab states: pending, Absent, empty, offline, no click and live updates (F7-AC40 to F7-AC43). */
@OptIn(ExperimentalTestApi::class)
class F7SessionsStatesTest {

    // F7-AC40: "A PENDING Core Burn record shows "Present" and "Upload pending"; once SYNCED, the Upload pending chip is gone."
    @Test
    fun F7_AC40_aPendingRecordShowsUploadPendingUntilSynced() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.network.set(NetworkStatus.OFFLINE)
            val record = world.paoloScan(example.coreBurn, example.paolo, MON_28_SEP_2026, 17, 31, UploadState.PENDING)
            openSessions(world, example)
            val row = sessionsRowTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))
            assertTextUnder(row, "Present")
            assertTextUnder(row, "Upload pending")

            world.attendance.update(record.copy(uploadState = UploadState.SYNCED))
            waitForIdle()

            assertTextUnder(row, "Present")
            assertNoTextUnder(row, "Upload pending")
        }
    }

    // F7-AC40: "After the clock moves to Tue 29, unscanned Mon 28 rows show "Absent" in Earlier."
    @Test
    fun F7_AC40_onTuesdayUnscannedMondayRowsAreAbsentInEarlier() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.setNow(LocalDate(2026, 9, 29), 9, 0)
            openSessions(world, example)

            assertTextUnder("sessions_group_earlier", "Earlier")
            val earlierTop = topOf("sessions_group_earlier")
            for (trxClass in listOf(example.mobilityFlow, example.coreBurn)) {
                val row = sessionsRowTag(world.sessionIdOn(trxClass, MON_28_SEP_2026))
                assertTrue(topOf(row) > earlierTop, "$row sits in Earlier")
                assertTextUnder(row, "Absent")
            }
            val sunrise = sessionsRowTag(world.sessionIdOn(example.sunrise, MON_28_SEP_2026))
            assertTextUnder(sunrise, "Present")
            assertNoTextUnder(sunrise, "Absent")
        }
    }

    // F7-AC41: "A student on no class sees sessions_empty: "No sessions yet" / "When you join a class, its dates show up here.", and no groups."
    @Test
    fun F7_AC41_aStudentOnNoClassSeesTheEmptyState() = runFlow7UiTest {
        Flow4Harness().use { world ->
            world.seedFlow7()
            val ana = world.student("Ana Cruz", "EMP-30001")
            world.signInAs(ana)
            mountFlow7(world)
            openTab("sessions")

            assertTextUnder("sessions_empty", "No sessions yet")
            assertTextUnder("sessions_empty", "When you join a class, its dates show up here.")
            for (group in listOf("sessions_group_upcoming", "sessions_group_today", "sessions_group_earlier")) {
                assertEquals(0, countTagged(group), "no $group")
            }
            assertEquals(emptyList<String>(), cardIdsTopToBottom("sessions_row_"))
        }
    }

    // F7-AC42: "Offline, offline_banner shows under the title, and rows still show."
    @Test
    fun F7_AC42_offlineShowsTheBannerAndStillShowsRows() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.network.set(NetworkStatus.OFFLINE)
            openSessions(world, example)

            assertEquals(listOf("No internet connection"), textsUnder("offline_banner").filter { it.isNotBlank() })
            assertTrue(topOf("offline_banner") > topOf(StudentTabBarTestTags.sessionsTitle), "the banner sits under the title")
            assertEquals(14, cardIdsTopToBottom("sessions_row_").size, "10 upcoming rows and 4 today rows still show")
        }
    }

    // F7-AC43: "Rows have no click action."
    @Test
    fun F7_AC43_rowsHaveNoClickAction() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openSessions(world, example)

            for (id in cardIdsTopToBottom("sessions_row_")) assertNothingClickableUnder(sessionsRowTag(id))
        }
    }

    // F7-AC43: "Recording on Attend and then selecting Sessions shows that row as Present."
    @Test
    fun F7_AC43_recordingOnAttendShowsPresentOnSessions() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)
            scanText(scanner, example.marasCode)
            tap("result_done")

            openTab("sessions")

            val row = sessionsRowTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))
            assertTextUnder(row, "Present")
            assertNoTextUnder(row, "Upcoming")
        }
    }
}
