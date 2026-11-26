package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.usecase.RecordAttendance
import ph.project.active.domain.usecase.VerifyDailyCode
import ph.project.active.presentation.attend.AttendViewModel
import ph.project.active.presentation.attend.RejectReason
import ph.project.active.presentation.attend.ScanResult
import ph.project.active.testkit.Flow7Example
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.ThrowingCoachKeyRepository
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.runVm

/** Flow 7 spec S5 to S8: AttendViewModel closes the scanner, handles a new day and reloads on new records. */
class F7AttendViewModelTest {

    // F7-AC3: "… Neither the record path nor the ViewModel path touches the key repository."
    @Test
    fun F7_AC3_theViewModelPathNeverTouchesTheKeyRepository() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val throwing = ThrowingCoachKeyRepository()
            val viewModel = AttendViewModel(
                account = example.account,
                classes = world.classes,
                memberships = world.memberships,
                attendance = world.attendance,
                people = world.people,
                recordAttendance = RecordAttendance(
                    classes = world.classes,
                    memberships = world.memberships,
                    attendance = world.attendance,
                    verifyDailyCode = VerifyDailyCode(keys = throwing, clock = world.clock, timeZone = world.timeZone),
                    clock = world.clock,
                    timeZone = world.timeZone,
                    networkStatus = { world.network.status.value },
                ),
                outbox = world.outbox,
                clock = world.clock,
                timeZone = world.timeZone,
                network = world.network,
            )
            viewModel.onOpened()
            viewModel.onChoose(example.sessionOf(example.coreBurn).id)
            viewModel.onScanCoachCode()

            viewModel.onCodeScanned(example.marasCode)

            assertIs<ScanResult.Present>(viewModel.state.value.result)
            assertEquals(0, throwing.calls, "the key repository is never called")
        }
    }

    // F7-AC4: "… then record with Mara's Tue 29 code → NoSessionToday (Rejected(NoSessionToday) in the ViewModel). Nothing is stored."
    @Test
    fun F7_AC4_aChosenSessionThatIsNoLongerTodayIsRejectedNoSessionToday() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.attend(example.account)
            viewModel.onOpened()
            viewModel.onChoose(example.sessionOf(example.coreBurn).id)
            viewModel.onScanCoachCode()
            world.setLocal(TUE_29_SEP_2026, 9, 0)

            viewModel.onCodeScanned(example.code(example.mara, TUE_29_SEP_2026))

            assertEquals(ScanResult.Rejected(RejectReason.NoSessionToday), viewModel.state.value.result)
            assertEquals(1, world.attendance.all().size, "only the Sunrise record")
        }
    }

    // F7-AC8: "onCloseScanner() after onScanCoachCode(): scannerOpen = false, sheetOpen = false, no selection, canScan = false, recordingFor = null, no record."
    @Test
    fun F7_AC8_closingTheScannerClearsEverythingAndSavesNothing() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.attend(example.account)
            viewModel.onOpened()
            viewModel.onChoose(example.sessionOf(example.coreBurn).id)
            viewModel.onScanCoachCode()
            assertTrue(viewModel.state.value.scannerOpen)

            viewModel.onCloseScanner()

            val state = viewModel.state.value
            assertFalse(state.scannerOpen)
            assertFalse(state.sheetOpen)
            assertNull(state.selectedSessionId)
            assertFalse(state.canScan)
            assertNull(state.recordingFor)
            assertNull(state.result)
            assertEquals(1, world.attendance.all().size, "no record")
            viewModel.onScanCoachCode()
            assertFalse(viewModel.state.value.scannerOpen, "the chosen session is cleared too")
        }
    }

    // F7-AC9: "Scanner open for Core Burn; the clock moves to Tue 29 Sep 9:00 AM; refresh(). Then today = Tue 29, the scanner is closed, the sheet is open, there is no selection, and choices = Lunch Express Session 2 only."
    @Test
    fun F7_AC9_aNewDayReopensTheSheetWithTheNewDaysChoices() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.attend(example.account)
            viewModel.onOpened()
            viewModel.onChoose(example.sessionOf(example.coreBurn).id)
            viewModel.onScanCoachCode()
            world.setLocal(TUE_29_SEP_2026, 9, 0)

            viewModel.refresh()

            val state = viewModel.state.value
            assertEquals(TUE_29_SEP_2026, state.today)
            assertFalse(state.scannerOpen)
            assertTrue(state.sheetOpen)
            assertNull(state.selectedSessionId)
            assertFalse(state.canScan)
            assertNull(state.recordingFor)
            val lunch = example.sessionOf(example.lunchExpress, TUE_29_SEP_2026)
            assertEquals(listOf(lunch.id), state.choices.map { it.sessionId })
            assertEquals("Lunch Express TRX", state.choices.single().className)
            assertEquals(2, lunch.sessionNumber)
        }
    }

    // F7-AC9: "With a Present result showing, the same steps leave the result until onDone()."
    @Test
    fun F7_AC9_aNewDayLeavesAShowingResultUntilDone() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.attend(example.account)
            viewModel.onOpened()
            viewModel.onChoose(example.sessionOf(example.coreBurn).id)
            viewModel.onScanCoachCode()
            viewModel.onCodeScanned(example.marasCode)
            assertIs<ScanResult.Present>(viewModel.state.value.result)
            world.setLocal(TUE_29_SEP_2026, 9, 0)

            viewModel.refresh()

            assertIs<ScanResult.Present>(viewModel.state.value.result, "the result stays")
            viewModel.onDone()
            assertNull(viewModel.state.value.result)
        }
    }

    // F7-AC10: "Adding a PENDING Core Burn record straight to the store adds a recordedToday row (uploadPending = true)."
    @Test
    fun F7_AC10_aNewRecordInTheStoreShowsInRecordedTodayWithoutRefresh() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.attend(example.account)
            assertEquals(listOf("Sunrise TRX Strength"), viewModel.state.value.recordedToday.map { it.className })

            example.scan(example.coreBurn, MON_28_SEP_2026, 17, 31, UploadState.PENDING)

            val rows = viewModel.state.value.recordedToday
            assertEquals(listOf("Sunrise TRX Strength", "TRX Core Burn"), rows.map { it.className })
            assertEquals(listOf(false, true), rows.map { it.uploadPending })
        }
    }

    // F7-AC10: "An already-created StudentSessionsViewModel shows that row as PRESENT with uploadPending = true, without refresh()."
    @Test
    fun F7_AC10_anOpenSessionsListShowsTheNewRecordWithoutRefresh() = runVm {
        Phase2World().use { world ->
            val example = Flow7Example(world)
            val viewModel = world.studentSessionsVm(example.account)
            val coreBurnId = example.sessionOf(example.coreBurn).id

            example.scan(example.coreBurn, MON_28_SEP_2026, 17, 31, UploadState.PENDING)

            val row = viewModel.state.value.today.single { it.sessionId == coreBurnId }
            assertEquals(ph.project.active.domain.model.StudentSessionStatus.PRESENT, row.status)
            assertTrue(row.uploadPending)
        }
    }
}
