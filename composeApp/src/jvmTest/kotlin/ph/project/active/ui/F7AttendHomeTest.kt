package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.OFFLINE_SUBTITLES
import ph.project.active.ui.testing.assertNoProfileIcon
import ph.project.active.ui.testing.assertNoSpinner
import ph.project.active.ui.testing.assertNoTextUnder
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextNotShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTaggedUnder
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.paoloScan
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tap
import ph.project.active.ui.testing.textsUnder
import ph.project.active.ui.testing.topOf

/** Flow 7 C6: the student's real Attend tab (F7-AC15, F7-AC16, F7-AC19, F7-AC20). Default clock Mon 28 Sep 2026, 5:31 PM. */
@OptIn(ExperimentalTestApi::class)
class F7AttendHomeTest {

    // F7-AC15: "Signed in as Paolo, selecting Attend shows the real screen under StudentTabBarTestTags.attendRoot: title "Attend", attend_overline "TRX ATTENDANCE", attend_subtitle "Mon 28 Sep 2026""
    @Test
    fun F7_AC15_attendShowsTheRealScreenHeader() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())

            tag(StudentTabBarTestTags.attendRoot).assertExists()
            tag("student_tab_attend").assertIsSelected()
            assertTextIs(StudentTabBarTestTags.attendTitle, "Attend")
            assertTextIs("attend_overline", "TRX ATTENDANCE")
            assertTextIs("attend_subtitle", "Mon 28 Sep 2026")
            assertEquals(1, countTaggedUnder(StudentTabBarTestTags.attendRoot, StudentTabBarTestTags.attendTitle), "the title is under student_attend")
        }
    }

    // F7-AC15: "attend_card_title "Record attendance", the card body, and attend_choose_class "Choose class""
    @Test
    fun F7_AC15_theRecordAttendanceCard() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())

            assertTextIs("attend_card_title", "Record attendance")
            assertTextIs(
                "attend_card_body",
                "First choose the class you\u2019re attending. Then scan the QR code on your coach\u2019s phone.",
            )
            assertTextIs("attend_choose_class", "Choose class")
        }
    }

    // F7-AC15: "no placeholder_tag, no profile icon, no spinner."
    @Test
    fun F7_AC15_noPlaceholderNoProfileIconNoSpinner() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())

            assertEquals(0, countTagged("placeholder_tag"))
            assertNoProfileIcon()
            assertNoSpinner()
        }
    }

    // F7-AC16: "Selecting Attend opens attend_sheet with no tap. attend_sheet_close closes it. attend_choose_class reopens it. Closing writes no record."
    @Test
    fun F7_AC16_theSheetOpensByItselfClosesAndReopens() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())
            tag("attend_sheet").assertExists()

            tap("attend_sheet_close")
            tag("attend_sheet").assertDoesNotExist()
            assertEquals(1, world.attendance.all().size, "closing writes no record")

            tap("attend_choose_class")
            tag("attend_sheet").assertExists()
            tap("attend_sheet_close")
            assertEquals(1, world.attendance.all().size, "closing writes no record")
        }
    }

    // F7-AC19: "attend_recorded_header "Recorded today" and attend_recorded_row_0 show "Sunrise TRX Strength", "Session 1 · scanned 6:02 AM" and "Present", with no "Upload pending"."
    @Test
    fun F7_AC19_recordedTodayListsTheSunriseScan() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())
            tap("attend_sheet_close")

            assertTextIs("attend_recorded_header", "Recorded today")
            assertTextUnder("attend_recorded_row_0", "Sunrise TRX Strength")
            assertTextUnder("attend_recorded_row_0", "Session 1 \u00B7 scanned 6:02 AM")
            assertTextUnder("attend_recorded_row_0", "Present")
            assertNoTextUnder("attend_recorded_row_0", "Upload pending")
            assertEquals(0, countTagged("attend_recorded_row_1"))
        }
    }

    // F7-AC19: "A PENDING Core Burn record adds attend_recorded_row_1 with "Upload pending"."
    @Test
    fun F7_AC19_aPendingRecordAddsARowWithUploadPending() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)
            tap("attend_sheet_close")

            world.paoloScan(example.coreBurn, example.paolo, MON_28_SEP_2026, 17, 31, UploadState.PENDING)
            waitForIdle()

            assertTextUnder("attend_recorded_row_1", "TRX Core Burn")
            assertTextUnder("attend_recorded_row_1", "Session 1 \u00B7 scanned 5:31 PM")
            assertTextUnder("attend_recorded_row_1", "Present")
            assertTextUnder("attend_recorded_row_1", "Upload pending")
        }
    }

    // F7-AC19: "With no records today, neither shows." (Tue 29: the only record is Mon 28's Sunrise)
    @Test
    fun F7_AC19_noRecordsTodayHidesTheHeaderAndRows() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.setNow(LocalDate(2026, 9, 29), 9, 0)
            openAttend(world, example)
            tap("attend_sheet_close")

            assertEquals(0, countTagged("attend_recorded_header"))
            assertEquals(0, countTagged("attend_recorded_row_0"))
        }
    }

    // F7-AC20: "Offline: offline_banner "No internet connection", with no subtitle, under the title. Back online, it hides."
    @Test
    fun F7_AC20_offlineShowsTheBannerUnderTheTitleWithNoSubtitle() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.network.set(NetworkStatus.OFFLINE)
            openAttend(world, example)
            tap("attend_sheet_close")

            assertEquals(listOf("No internet connection"), textsUnder("offline_banner").filter { it.isNotBlank() })
            for (subtitle in OFFLINE_SUBTITLES) assertTextNotShown(subtitle)
            assertTrue(topOf("offline_banner") > topOf(StudentTabBarTestTags.attendTitle), "the banner sits under the title")

            world.network.set(NetworkStatus.ONLINE)
            waitForIdle()
            tag("offline_banner").assertDoesNotExist()
        }
    }
}
