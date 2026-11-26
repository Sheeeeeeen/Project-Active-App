package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.usecase.CancelDate
import ph.project.active.domain.usecase.RemoveFromRoster
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.testing.FakeQrScanner
import ph.project.active.ui.testing.FakeScanHaptics
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertTabBarShown
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.chooseAndScan
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.scanText
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tap

/**
 * Flow 7 C15: every rejection (F7-AC35, cases 1 to 8). The setup change in cases 4 to 7 happens while the scanner
 * is open for the chosen class, after the class was chosen on the sheet.
 */
@OptIn(ExperimentalTestApi::class)
class F7RejectedScanTest {

    // F7-AC35 case 1: "Core Burn + old code | "This code is for a different date." (Code date "Mon 21 Sep 2026")"
    @Test
    fun F7_AC35_case1_anOldCodeIsForADifferentDate() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, example.oldCode)

            assertRejected(world, haptics, "This code is for a different date.", "TRX Core Burn \u00B7 5:30 PM", "Mon 28 Sep 2026")
            assertTextIs("result_code_date", "Mon 21 Sep 2026")
            assertTextUnder("result_rejected", "Code date")
        }
    }

    // F7-AC35 case 2: "Mobility Flow + Mara's code | "This code isn’t from your coach for this class.""
    @Test
    fun F7_AC35_case2_marasCodeForLeosClass() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.mobilityFlow)

            scanText(scanner, example.marasCode)

            assertRejected(world, haptics, "This code isn\u2019t from your coach for this class.", "TRX Mobility Flow \u00B7 7:00 PM", "Mon 28 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35 case 3: "Core Burn + trxatt://v1/daily?coach=coach-mara&date=2026-09-28 (no sig) | "This isn’t a valid attendance code.""
    @Test
    fun F7_AC35_case3_aLinkWithNoSignatureIsNotAValidCode() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            scanText(scanner, "trxatt://v1/daily?coach=coach-mara&date=2026-09-28")

            assertRejected(world, haptics, "This isn\u2019t a valid attendance code.", "TRX Core Burn \u00B7 5:30 PM", "Mon 28 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35 case 4: "Core Burn, Paolo removed from the roster | "You’re not on this class roster.""
    @Test
    fun F7_AC35_case4_removedFromTheRoster() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            RemoveFromRoster(world.memberships).invoke(example.coreBurn.id, example.paolo.id)
            scanText(scanner, example.marasCode)

            assertRejected(world, haptics, "You\u2019re not on this class roster.", "TRX Core Burn \u00B7 5:30 PM", "Mon 28 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35 case 5: "Core Burn, Mon 28 cancelled while the sheet is open | "This date is cancelled.""
    @Test
    fun F7_AC35_case5_theDateWasCancelled() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            CancelDate(world.classes, world.clock, world.timeZone).invoke(example.coreBurn.id, MON_28_SEP_2026)
            scanText(scanner, example.marasCode)

            assertRejected(world, haptics, "This date is cancelled.", "TRX Core Burn \u00B7 5:30 PM", "Mon 28 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35 case 6: "Core Burn, CancelClass on Core Burn | "This class has been cancelled.""
    @Test
    fun F7_AC35_case6_theClassWasCancelled() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            world.cancel(example.coreBurn.id)
            scanText(scanner, example.marasCode)

            assertRejected(world, haptics, "This class has been cancelled.", "TRX Core Burn \u00B7 5:30 PM", "Mon 28 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35 case 7: "Offline, chosen session missing from the store | "This class date isn’t on this phone yet.""
    @Test
    fun F7_AC35_case7_theChosenSessionIsNotOnThisPhone() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            world.network.set(NetworkStatus.OFFLINE)
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            val chosen = world.classes.sessionsOf(example.coreBurn.id).single { it.date == MON_28_SEP_2026 }
            world.classes.saveSessions(listOf(chosen.copy(slot = world.slot(9, 0, 10, 0))))
            scanText(scanner, example.marasCode)

            assertRejected(world, haptics, "This class date isn\u2019t on this phone yet.", "TRX Core Burn \u00B7 5:30 PM", "Mon 28 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35 case 8: "Scanner open for Core Burn across midnight (no refresh), Mara's Tue 29 code | "This class has no session today."" with result_today "Tue 29 Sep 2026"
    @Test
    fun F7_AC35_case8_acrossMidnightTheClassHasNoSessionToday() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            val haptics = FakeScanHaptics()
            openAttend(world, example, scanner = scanner, haptics = haptics)
            chooseAndScan(world, example.coreBurn)

            val tuesday = LocalDate(2026, 9, 29)
            world.setNow(tuesday, 0, 5)
            scanText(scanner, example.code(example.mara, tuesday))

            assertRejected(world, haptics, "This class has no session today.", "TRX Core Burn \u00B7 5:30 PM", "Tue 29 Sep 2026")
            assertEquals(0, countTagged("result_code_date"))
        }
    }

    // F7-AC35: "result_scan_again "Scan again" reopens the scanner with the same class."
    @Test
    fun F7_AC35_scanAgainReopensTheScannerForTheSameClass() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)
            scanText(scanner, example.oldCode)

            assertTextIs("result_scan_again", "Scan again")
            tap("result_scan_again")

            tag("result_rejected").assertDoesNotExist()
            tag("scanner").assertExists()
            assertTextIs("scanner_recording_for", "TRX Core Burn \u00B7 5:30 PM")

            scanText(scanner, example.marasCode)
            tag("result_present").assertExists()
        }
    }

    // F7-AC35: "result_close "Close" goes home."
    @Test
    fun F7_AC35_closeGoesHome() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val scanner = FakeQrScanner()
            openAttend(world, example, scanner = scanner)
            chooseAndScan(world, example.coreBurn)
            scanText(scanner, example.oldCode)

            assertTextIs("result_close", "Close")
            tap("result_close")

            tag("result_rejected").assertDoesNotExist()
            tag("scanner").assertDoesNotExist()
            tag("attend_sheet").assertDoesNotExist()
            tag(StudentTabBarTestTags.attendRoot).assertExists()
            assertTabBarShown()
        }
    }
}

/**
 * F7-AC35 "Every case": title "Scan not accepted"; result_message = [line] + " Nothing was saved."; result_you_chose
 * [youChose]; result_today [today]; no new record (only the seeded Sunrise scan); no haptic.
 */
@OptIn(ExperimentalTestApi::class)
private fun ComposeUiTest.assertRejected(
    world: Flow4Harness,
    haptics: FakeScanHaptics,
    line: String,
    youChose: String,
    today: String,
) {
    tag("result_rejected").assertExists()
    assertTextUnder("result_rejected", "Scan not accepted")
    assertTextIs("result_message", "$line Nothing was saved.")
    assertTextIs("result_you_chose", youChose)
    assertTextUnder("result_rejected", "You chose")
    assertTextIs("result_today", today)
    assertTextUnder("result_rejected", "Today")
    assertEquals(1, world.attendance.all().size, "no new record")
    assertEquals(0, haptics.successCount, "no haptic")
}
