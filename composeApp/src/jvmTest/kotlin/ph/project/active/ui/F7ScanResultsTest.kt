package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.testing.FakeQrScanner
import ph.project.active.ui.testing.FakeScanHaptics
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertNoTabBar
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.choiceTag
import ph.project.active.ui.testing.chooseAndScan
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.openTab
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.scanText
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.sessionsRowTag
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tap

/** Flow 7 C12, C13 and C14: accepted and repeat scans (F7-AC32 to F7-AC34). Default clock Mon 28 Sep 2026, 5:31 PM. */
@OptIn(ExperimentalTestApi::class)
class F7ScanResultsTest {

    // F7-AC32: "result_present shows "You’re marked Present", "Your coach will see you on this session.", "Present", and rows Class "TRX Core Burn", Session "Session 1 · Mon 28 Sep 2026", Time "5:30 PM – 6:30 PM", Scanned "5:31 PM""
    @Test
    fun F7_AC32_presentShowsTheTitleBodyChipAndRows() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, example.marasCode)

            for (text in listOf(
                "You\u2019re marked Present",
                "Your coach will see you on this session.",
                "Present",
                "Class", "TRX Core Burn",
                "Session", "Session 1 \u00B7 Mon 28 Sep 2026",
                "Time", "5:30 PM \u2013 6:30 PM",
                "Scanned", "5:31 PM",
            )) assertEquals(1, countTextUnder("result_present", text), "one \"$text\" on C12")
            assertNoTabBar()
        }
    }

    // F7-AC32: "success() is called once"
    @Test
    fun F7_AC32_presentVibratesOnce() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, example.marasCode)

            assertEquals(1, haptics.successCount)
        }
    }

    // F7-AC32: "result_done goes home with Core Burn in Recorded today"
    @Test
    fun F7_AC32_doneGoesHomeWithCoreBurnInRecordedToday() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)
            scanText(scanner, example.marasCode)

            assertTextIs("result_done", "Done")
            tap("result_done")

            tag("result_present").assertDoesNotExist()
            tag(StudentTabBarTestTags.attendRoot).assertExists()
            tag("attend_sheet").assertDoesNotExist()
            assertTextUnder("attend_recorded_row_1", "TRX Core Burn")
            assertTextUnder("attend_recorded_row_1", "Session 1 \u00B7 scanned 5:31 PM")
            assertTextUnder("attend_recorded_row_1", "Present")
        }
    }

    // F7-AC32: "result_record_another "Record another class" opens the sheet with no selection."
    @Test
    fun F7_AC32_recordAnotherClassOpensTheSheetWithNoSelection() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)
            scanText(scanner, example.marasCode)

            assertTextIs("result_record_another", "Record another class")
            tap("result_record_another")

            tag("attend_sheet").assertExists()
            tag(choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))).assertIsNotSelected()
            tag(choiceTag(world.sessionIdOn(example.mobilityFlow, MON_28_SEP_2026))).assertIsNotSelected()
            tag("attend_sheet_scan").assertIsNotEnabled()
        }
    }

    // F7-AC33: "result_saved_offline shows the banner (title only), "Saved on this phone", the body, the "Present" and "Upload pending" chips, and rows Class, Session, Scanned "5:31 PM (recorded time)", Upload "Waiting for connection", plus one result_done"
    @Test
    fun F7_AC33_offlineShowsSavedOnThisPhone() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            world.network.set(NetworkStatus.OFFLINE)
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, example.marasCode)

            assertTextIs("offline_banner", "No internet connection")
            assertTextNotShown("Scans are saved on this phone and upload when you are back online.")
            for (text in listOf(
                "Saved on this phone",
                "Your attendance is stored now and uploads automatically when you\u2019re back online. The recorded time is the scan time.",
                "Present",
                "Upload pending",
                "Class", "TRX Core Burn",
                "Session", "Session 1 \u00B7 Mon 28 Sep 2026",
                "Scanned", "5:31 PM (recorded time)",
                "Upload", "Waiting for connection",
            )) assertEquals(1, countTextUnder("result_saved_offline", text), "one \"$text\" on C13")
            assertEquals(1, countTagged("result_done"))
            assertEquals(0, countTagged("result_record_another"))
        }
    }

    // F7-AC33: "success() is called once, and the record is PENDING"
    @Test
    fun F7_AC33_offlineVibratesOnceAndTheRecordIsPending() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            world.network.set(NetworkStatus.OFFLINE)
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, example.marasCode)

            assertEquals(1, haptics.successCount)
            val record = world.attendance.find(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026), example.paolo.id)
            assertEquals(UploadState.PENDING, record?.uploadState)
        }
    }

    // F7-AC33: "a real ONLINE reading makes RootViewModel run OutboxSync. The row becomes SYNCED, and the Sessions row loses "Upload pending" without leaving the tab."
    @Test
    fun F7_AC33_backOnlineTheRowSyncsAndSessionsDropsUploadPending() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            world.network.set(NetworkStatus.OFFLINE)
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)
            scanText(scanner, example.marasCode)
            tap("result_done")
            openTab("sessions")
            val row = sessionsRowTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))
            assertTextUnder(row, "Upload pending")

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()

            val record = world.attendance.find(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026), example.paolo.id)
            assertEquals(UploadState.SYNCED, record?.uploadState)
            tag(StudentTabBarTestTags.sessionsRoot).assertExists()
            assertTextUnder(row, "Present")
            assertNoTextUnder(row, "Upload pending")
        }
    }

    // F7-AC34: "result_already shows "You already have attendance", the body, "Present", and rows Class, Session "Session 1 · Mon 28 Sep 2026", First scan "6:02 AM""
    @Test
    fun F7_AC34_aRepeatScanShowsYouAlreadyHaveAttendance() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.sunrise)

            scanText(scanner, example.marasCode)

            for (text in listOf(
                "You already have attendance",
                "Sunrise TRX Strength on Mon 28 Sep 2026 is already recorded. No new record was added.",
                "Present",
                "Class", "Sunrise TRX Strength",
                "Session", "Session 1 \u00B7 Mon 28 Sep 2026",
                "First scan", "6:02 AM",
            )) assertEquals(1, countTextUnder("result_already", text), "one \"$text\" on C14")
        }
    }

    // F7-AC34: "result_done "Done" and result_choose_different "Choose a different class" show; there is still one Sunrise record, and no haptic."
    @Test
    fun F7_AC34_aRepeatScanKeepsOneRecordAndDoesNotVibrate() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.sunrise)

            scanText(scanner, example.marasCode)

            assertTextIs("result_done", "Done")
            assertTextIs("result_choose_different", "Choose a different class")
            assertEquals(1, world.attendance.all().count { it.classId == example.sunrise.id })
            assertEquals(0, haptics.successCount)
        }
    }
}
