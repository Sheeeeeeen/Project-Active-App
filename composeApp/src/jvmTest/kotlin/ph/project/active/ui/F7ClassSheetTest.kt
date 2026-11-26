package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.testing.FakeCameraPermission
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.MON_28_SEP_2026
import ph.project.active.ui.testing.assertTextIs
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.cardIdsTopToBottom
import ph.project.active.ui.testing.choiceTag
import ph.project.active.ui.testing.countTagged
import ph.project.active.ui.testing.countTextUnder
import ph.project.active.ui.testing.openAttend
import ph.project.active.ui.testing.runFlow7UiTest
import ph.project.active.ui.testing.seedFlow7
import ph.project.active.ui.testing.sessionIdOn
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tap

/** Flow 7 C7, C8 and C18: the class sheet (F7-AC17, F7-AC18, F7-AC21). */
@OptIn(ExperimentalTestApi::class)
class F7ClassSheetTest {

    // F7-AC17: "The sheet shows "Which class are you attending?" and "Today · Mon 28 Sep 2026 · choose one""
    @Test
    fun F7_AC17_theSheetTitleAndSubtitle() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())

            assertTextUnder("attend_sheet", "Which class are you attending?")
            assertTextUnder("attend_sheet", "Today \u00B7 Mon 28 Sep 2026 \u00B7 choose one")
        }
    }

    // F7-AC17: "attend_choice_{sessionId} for Sunrise, Core Burn and Mobility Flow in start-time order, and no Lunch Express (cancelled today)"
    @Test
    fun F7_AC17_todaysScheduledClassesInStartTimeOrder() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)

            assertEquals(
                listOf(
                    world.sessionIdOn(example.sunrise, MON_28_SEP_2026),
                    world.sessionIdOn(example.coreBurn, MON_28_SEP_2026),
                    world.sessionIdOn(example.mobilityFlow, MON_28_SEP_2026),
                ),
                cardIdsTopToBottom("attend_choice_"),
            )
            assertEquals(0, countTagged(choiceTag(world.sessionIdOn(example.lunchExpress, MON_28_SEP_2026))))
            assertEquals(0, countTextUnder("attend_sheet", "Lunch Express TRX"))
        }
    }

    // F7-AC17: "row lines like "5:30 PM – 6:30 PM · Coach Mara Santos", with Sunrise still listed"
    @Test
    fun F7_AC17_eachRowShowsTheClassTimeAndCoach() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)

            val coreBurn = choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))
            assertTextUnder(coreBurn, "TRX Core Burn")
            assertTextUnder(coreBurn, "5:30 PM \u2013 6:30 PM \u00B7 Coach Mara Santos")
            val sunrise = choiceTag(world.sessionIdOn(example.sunrise, MON_28_SEP_2026))
            assertTextUnder(sunrise, "Sunrise TRX Strength")
            assertTextUnder(sunrise, "6:00 AM \u2013 6:45 AM \u00B7 Coach Mara Santos")
            val mobility = choiceTag(world.sessionIdOn(example.mobilityFlow, MON_28_SEP_2026))
            assertTextUnder(mobility, "7:00 PM \u2013 8:00 PM \u00B7 Coach Leo Ramirez")
        }
    }

    // F7-AC17: "attend_sheet_scan "Scan coach’s code", disabled until a row is tapped. Then the tapped row has selected = true and the others false"
    @Test
    fun F7_AC17_scanIsDisabledUntilARowIsTappedAndOnlyThatRowIsSelected() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example)
            val sunrise = choiceTag(world.sessionIdOn(example.sunrise, MON_28_SEP_2026))
            val coreBurn = choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026))
            val mobility = choiceTag(world.sessionIdOn(example.mobilityFlow, MON_28_SEP_2026))

            assertTextIs("attend_sheet_scan", "Scan coach\u2019s code")
            tag("attend_sheet_scan").assertIsNotEnabled()
            for (row in listOf(sunrise, coreBurn, mobility)) tag(row).assertIsNotSelected()

            tap(coreBurn)

            tag("attend_sheet_scan").assertIsEnabled()
            tag(coreBurn).assertIsSelected()
            tag(sunrise).assertIsNotSelected()
            tag(mobility).assertIsNotSelected()
        }
    }

    // F7-AC17: "the footnote."
    @Test
    fun F7_AC17_theFootnote() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7())

            assertEquals(
                1,
                countTextUnder("attend_sheet", "Your choice is saved with the scan. The time of the scan doesn\u2019t change it."),
            )
        }
    }

    // F7-AC18: "On Sat 3 Oct 9:00 AM, attend_sheet_empty shows "No classes today." and "Your scheduled classes for today show up here.". There are no rows, and Scan is disabled."
    @Test
    fun F7_AC18_noClassesTodayShowsTheEmptyLinesAndADisabledScan() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            world.setNow(LocalDate(2026, 10, 3), 9, 0)
            openAttend(world, example)

            assertTextUnder("attend_sheet_empty", "No classes today.")
            assertTextUnder("attend_sheet_empty", "Your scheduled classes for today show up here.")
            assertEquals(emptyList<String>(), cardIdsTopToBottom("attend_choice_"))
            tag("attend_sheet_scan").assertIsNotEnabled()
        }
    }

    // F7-AC21: "With fake camera access DENIED_FOR_GOOD and Core Burn selected, the sheet shows camera_denied with a warning icon, "Camera access is off" and "Allow camera access in Settings to scan your coach’s code.", plus camera_open_settings "Open Settings"."
    @Test
    fun F7_AC21_cameraDeniedForGoodShowsTheCalloutAndOpenSettings() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            openAttend(world, example, permission = FakeCameraPermission(CameraAccess.DENIED_FOR_GOOD))

            tap(choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026)))

            assertTextUnder("camera_denied", "Camera access is off")
            assertTextUnder("camera_denied", "Allow camera access in Settings to scan your coach\u2019s code.")
            assertTextIs("camera_open_settings", "Open Settings")
        }
    }

    // F7-AC21: "There is no attend_sheet_scan and no footnote. Tapping Open Settings calls openSettings() once."
    @Test
    fun F7_AC21_theCalloutReplacesScanAndTheFootnote() = runFlow7UiTest {
        Flow4Harness().use { world ->
            val example = world.seedFlow7()
            val permission = FakeCameraPermission(CameraAccess.DENIED_FOR_GOOD)
            openAttend(world, example, permission = permission)
            tap(choiceTag(world.sessionIdOn(example.coreBurn, MON_28_SEP_2026)))

            tag("attend_sheet_scan").assertDoesNotExist()
            assertEquals(
                0,
                countTextUnder("attend_sheet", "Your choice is saved with the scan. The time of the scan doesn\u2019t change it."),
            )
            tap("camera_open_settings")
            assertEquals(1, permission.openSettingsCount)
            assertEquals(0, permission.requestCount, "no system prompt from C18")
        }
    }

    // F7-AC21: "With no row selected, the Scan button shows (disabled), as in C7."
    @Test
    fun F7_AC21_withNoRowSelectedTheDisabledScanStillShows() = runFlow7UiTest {
        Flow4Harness().use { world ->
            openAttend(world, world.seedFlow7(), permission = FakeCameraPermission(CameraAccess.DENIED_FOR_GOOD))

            tag("attend_sheet_scan").assertIsNotEnabled()
            tag("camera_denied").assertDoesNotExist()
        }
    }
}
