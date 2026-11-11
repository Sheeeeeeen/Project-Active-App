package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import ph.project.active.testkit.idle
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.repository.current
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.runVm
import ph.project.active.presentation.attend.RejectReason
import ph.project.active.presentation.attend.ScanResult

class AttendViewModelTest {

    @Test
    fun AC17_offline_scan_stores_pending_attendance() = runVm {
        val fixture = readyClass()
        fixture.world.network.set(NetworkStatus.OFFLINE)
        val scanTime = fixture.world.clock.now()
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onOpened()
        viewModel.onChoose(fixture.sessionId)
        viewModel.onScanCoachCode()
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        val row = fixture.world.attendance.all().single()
        assertEquals(UploadState.PENDING, row.uploadState)
        assertEquals(scanTime, row.scannedAt)
        assertEquals(fixture.sessionId, row.sessionId)
        val saved = assertIs<ScanResult.SavedOffline>(viewModel.state.value.result)
        assertEquals(true, saved.uploadPending)
        assertEquals(scanTime, saved.scannedAt)
        fixture.world.close()
    }

    @Test
    fun AC48_choices_are_todays_scheduled_sessions_and_the_sheet_opens() = runVm {
        val fixture = readyClass(twoSlots = true)
        val viewModel = fixture.world.attend(fixture.student)
        assertEquals(fixture.world.today(), viewModel.state.value.today)
        assertFalse(viewModel.state.value.sheetOpen)
        viewModel.onOpened()
        assertTrue(viewModel.state.value.sheetOpen)
        assertEquals(2, viewModel.state.value.choices.size)
        assertFalse(viewModel.state.value.noSessionsToday)
        assertTrue(viewModel.state.value.choices.all { it.coachName == "Coach 1" })
        fixture.world.cancelDate(fixture.classId, MON_28_SEP_2026)
        fixture.world.cancelClass(fixture.classId)
        val empty = fixture.world.attend(fixture.student)
        empty.onOpened()
        assertTrue(empty.state.value.choices.isEmpty())
        assertTrue(empty.state.value.noSessionsToday)
        fixture.world.close()
    }

    @Test
    fun AC49_choose_sets_can_scan_and_closing_the_sheet_writes_nothing() = runVm {
        val fixture = readyClass()
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onOpened()
        assertFalse(viewModel.state.value.canScan)
        viewModel.onChoose(fixture.sessionId)
        assertEquals(fixture.sessionId, viewModel.state.value.selectedSessionId)
        assertTrue(viewModel.state.value.canScan)
        viewModel.onCloseSheet()
        assertFalse(viewModel.state.value.sheetOpen)
        assertNull(viewModel.state.value.selectedSessionId)
        assertFalse(viewModel.state.value.scannerOpen)
        assertTrue(fixture.world.attendance.all().isEmpty())
        fixture.world.close()
    }

    @Test
    fun AC50_scan_opens_the_scanner_only_with_a_selection_and_change_session_returns() = runVm {
        val fixture = readyClass()
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onOpened()
        viewModel.onScanCoachCode()
        assertFalse(viewModel.state.value.scannerOpen)
        viewModel.onChoose(fixture.sessionId)
        viewModel.onScanCoachCode()
        assertTrue(viewModel.state.value.scannerOpen)
        assertFalse(viewModel.state.value.sheetOpen)
        assertEquals("TRX Evening", viewModel.state.value.recordingFor!!.className)
        assertEquals(EVENING, viewModel.state.value.recordingFor!!.slot)
        viewModel.onChangeSession()
        assertFalse(viewModel.state.value.scannerOpen)
        assertTrue(viewModel.state.value.sheetOpen)
        assertEquals(fixture.sessionId, viewModel.state.value.selectedSessionId)
        fixture.world.close()
    }

    @Test
    fun AC51_a_scan_uses_the_chosen_session_and_no_selection_reopens_the_sheet() = runVm {
        val fixture = readyClass(twoSlots = true)
        fixture.world.setLocal(MON_28_SEP_2026, 7, 15)
        val evening = fixture.world.sessionOn(fixture.classId, MON_28_SEP_2026, EVENING)
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        assertTrue(fixture.world.attendance.all().isEmpty())
        assertTrue(viewModel.state.value.sheetOpen)
        assertFalse(viewModel.state.value.scannerOpen)

        viewModel.onChoose(evening.id)
        viewModel.onScanCoachCode()
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        assertEquals(evening.id, fixture.world.attendance.all().single().sessionId)
        fixture.world.close()
    }

    @Test
    fun AC52_online_recorded_is_present_and_the_row_is_synced() = runVm {
        val online = readyClass()
        val viewModel = online.world.attend(online.student)
        viewModel.onChoose(online.sessionId)
        viewModel.onCodeScanned(online.world.todaysCode(online.coachId))
        idle()
        val present = assertIs<ScanResult.Present>(viewModel.state.value.result)
        assertEquals(1, present.sessionNumber)
        assertEquals(MON_28_SEP_2026, present.date)
        assertEquals(EVENING, present.slot)
        assertEquals(UploadState.SYNCED, online.world.attendance.all().single().uploadState)
        online.world.close()
    }

    @Test
    fun AC53_already_recorded_shows_the_first_scan_and_writes_no_second_row() = runVm {
        val fixture = readyClass()
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onChoose(fixture.sessionId)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        val first = fixture.world.attendance.all().single().scannedAt
        viewModel.onScanAgain()
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        val result = assertIs<ScanResult.AlreadyRecorded>(viewModel.state.value.result)
        assertEquals(first, result.firstScannedAt)
        assertEquals(1, result.sessionNumber)
        assertEquals(1, fixture.world.attendance.all().size)
        fixture.world.close()
    }

    @Test
    fun AC54_other_results_are_rejected_and_class_cancelled_wins() = runVm {
        val fixture = readyClass()
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onChoose(fixture.sessionId)
        viewModel.onCodeScanned("not a code")
        assertEquals(RejectReason.InvalidCode, (viewModel.state.value.result as ScanResult.Rejected).reason)
        assertTrue(fixture.world.attendance.all().isEmpty())

        val sunday = fixture.world.generateDailyCode(fixture.coachId, LocalDate(2026, 9, 27))
        viewModel.onCodeScanned(sunday)
        val wrongDate = (viewModel.state.value.result as ScanResult.Rejected).reason
        assertEquals(RejectReason.WrongDate(LocalDate(2026, 9, 27)), wrongDate)

        val other = fixture.world.coach(2)
        fixture.world.giveKeys(other.id)
        viewModel.onCodeScanned(fixture.world.todaysCode(other.id))
        assertEquals(RejectReason.WrongCoach, (viewModel.state.value.result as ScanResult.Rejected).reason)

        fixture.world.cancelDate(fixture.classId, MON_28_SEP_2026)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        assertEquals(RejectReason.SessionCancelled, (viewModel.state.value.result as ScanResult.Rejected).reason)

        val cancelled = readyClass()
        cancelled.world.cancelClass(cancelled.classId)
        val cancelledVm = cancelled.world.attend(cancelled.student)
        cancelledVm.onChoose(cancelled.sessionId)
        cancelledVm.onCodeScanned(cancelled.world.todaysCode(cancelled.coachId))
        assertEquals(RejectReason.ClassCancelled, (cancelledVm.state.value.result as ScanResult.Rejected).reason)
        assertTrue(cancelled.world.attendance.all().isEmpty())
        cancelled.world.close()
        fixture.world.close()
    }

    @Test
    fun AC55_record_another_and_scan_again_follow_the_sheet_and_scanner() = runVm {
        val fixture = readyClass(twoSlots = true)
        val viewModel = fixture.world.attend(fixture.student)
        val morning = fixture.world.sessionOn(fixture.classId, MON_28_SEP_2026, MORNING)
        viewModel.onChoose(morning.id)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        viewModel.onRecordAnother()
        assertNull(viewModel.state.value.result)
        assertNull(viewModel.state.value.selectedSessionId)
        assertTrue(viewModel.state.value.sheetOpen)
        val evening = fixture.world.sessionOn(fixture.classId, MON_28_SEP_2026, EVENING)
        viewModel.onChoose(evening.id)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        assertEquals(2, fixture.world.attendance.all().size)

        viewModel.onChooseDifferentClass()
        assertNull(viewModel.state.value.selectedSessionId)
        viewModel.onChoose(morning.id)
        viewModel.onScanAgain()
        assertTrue(viewModel.state.value.scannerOpen)
        assertEquals(morning.id, viewModel.state.value.selectedSessionId)
        viewModel.onDone()
        assertNull(viewModel.state.value.result)
        fixture.world.close()
    }

    @Test
    fun AC56_recorded_today_lists_only_this_students_rows_for_today() = runVm {
        val fixture = readyClass(twoSlots = true)
        fixture.world.network.set(NetworkStatus.OFFLINE)
        val viewModel = fixture.world.attend(fixture.student)
        val morning = fixture.world.sessionOn(fixture.classId, MON_28_SEP_2026, MORNING)
        viewModel.onChoose(morning.id)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        idle()
        viewModel.refresh()
        val row = viewModel.state.value.recordedToday.single()
        assertEquals(morning.sessionNumber, row.sessionNumber)
        assertEquals(true, row.uploadPending)
        assertEquals("TRX Double Monday", row.className)
        assertEquals(1, viewModel.state.value.recordedToday.size)
        fixture.world.close()
    }

    @Test
    fun AC57_offline_cache_misses_and_midnight_refresh() = runVm {
        val fixture = readyClass()
        fixture.world.network.set(NetworkStatus.OFFLINE)
        val viewModel = fixture.world.attend(fixture.student)
        viewModel.onChoose(fixture.sessionId)
        val chosen = fixture.world.sessionOn(fixture.classId, MON_28_SEP_2026)
        fixture.world.classes.saveSessions(listOf(chosen.copy(slot = MORNING)))
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        assertEquals(RejectReason.SessionUnknown, (viewModel.state.value.result as ScanResult.Rejected).reason)
        assertTrue(fixture.world.attendance.all().isEmpty())

        val restored = chosen.copy(slot = EVENING)
        fixture.world.classes.saveSessions(listOf(restored))
        val membership = fixture.world.memberships.current(fixture.classId, fixture.student.id)!!
        fixture.world.memberships.update(membership.copy(studentId = "someone-else"))
        viewModel.onChoose(fixture.sessionId)
        viewModel.onCodeScanned(fixture.world.todaysCode(fixture.coachId))
        assertEquals(RejectReason.NotOnRoster, (viewModel.state.value.result as ScanResult.Rejected).reason)
        assertTrue(fixture.world.attendance.all().isEmpty())

        fixture.world.clock.current = fixture.world.clock.now() + 1.days
        viewModel.refresh()
        assertEquals(TUE_29_SEP_2026, viewModel.state.value.today)
        assertTrue(viewModel.state.value.choices.isEmpty())
        fixture.world.close()
    }
}
