package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.InviteResult
import ph.project.active.domain.model.JoinResult
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.SessionStatus
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.TUE_29_SEP_2026
import ph.project.active.testkit.WED_30_SEP_2026
import ph.project.active.testkit.runVm
import ph.project.active.testkit.toAccount
import ph.project.active.presentation.manageclass.ManageConfirmation
import ph.project.active.presentation.manageclass.ManageTab
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.attend.RejectReason
import ph.project.active.presentation.attend.ScanResult
import ph.project.active.presentation.common.UiError

class ManageClassViewModelTest {

    @Test
    fun AC67_header_starts_on_schedule_and_tabs_change() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id, name = "Managed")
            val student = world.student()
            world.requestToJoin(trxClass.id, student.id)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            assertEquals("Managed", viewModel.state.value.name)
            assertEquals(trxClass.visibility, viewModel.state.value.visibility)
            assertEquals(trxClass.endDate, viewModel.state.value.endDate)
            assertEquals(10, viewModel.state.value.sessionCount)
            assertEquals(trxClass.slotsByWeekday, viewModel.state.value.slotsByWeekday)
            assertEquals(1, viewModel.state.value.pendingRequestCount)
            assertEquals(ManageTab.SCHEDULE, viewModel.state.value.selectedTab)
            viewModel.onSelectTab(ManageTab.REQUESTS)
            assertEquals(ManageTab.REQUESTS, viewModel.state.value.selectedTab)
            viewModel.onSelectTab(ManageTab.ROSTER)
            viewModel.onSelectTab(ManageTab.INVITE)
            assertEquals(ManageTab.INVITE, viewModel.state.value.selectedTab)
        }
    }

    @Test
    fun AC68_schedule_rows_follow_session_number_and_can_cancel_today_or_later() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            world.setLocal(MON_28_SEP_2026, 12)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            val rows = viewModel.state.value.schedule
            assertEquals((1..10).toList(), rows.map { it.sessionNumber })
            val monday = rows.single { it.date == MON_28_SEP_2026 }
            assertTrue(monday.isToday)
            assertFalse(monday.isPast)
            assertTrue(monday.canCancelDate)
            val wednesday = rows.single { it.date == WED_30_SEP_2026 }
            assertFalse(wednesday.isToday)
            assertFalse(wednesday.isPast)
            assertTrue(wednesday.canCancelDate)
            world.setLocal(TUE_29_SEP_2026, 9)
            val later = world.manage(coach.toAccount(), trxClass.id).state.value.schedule.single { it.date == MON_28_SEP_2026 }
            assertTrue(later.isPast)
            assertFalse(later.canCancelDate)
        }
    }

    @Test
    fun AC69_confirming_a_cancel_date_keeps_other_dates_and_a_past_date_does_not() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createTwoSlotMondayClass(coach.id)
            world.setLocal(MON_28_SEP_2026, 9)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            val target = LocalDate(2026, 10, 5)
            viewModel.onCancelDate(target)
            assertEquals(target, viewModel.state.value.confirmCancelDate)
            viewModel.onKeepDate()
            assertNull(viewModel.state.value.confirmCancelDate)
            assertTrue(world.sessionsOf(trxClass.id).all { it.status == SessionStatus.SCHEDULED })

            viewModel.onCancelDate(target)
            viewModel.onConfirmCancelDate()
            assertEquals(ManageConfirmation.DateCancelled(target), viewModel.state.value.confirmation)
            val sessions = world.sessionsOf(trxClass.id)
            assertTrue(sessions.filter { it.date == target }.all { it.status == SessionStatus.CANCELLED })
            assertEquals(sessions.filter { it.date == target }.map { it.sessionNumber }, world.sessionsOf(trxClass.id).filter { it.date == target }.map { it.sessionNumber })
            assertTrue(sessions.filter { it.date != target }.all { it.status == SessionStatus.SCHEDULED })

            world.setLocal(TUE_29_SEP_2026, 9)
            val past = world.manage(coach.toAccount(), trxClass.id)
            val before = world.sessionsOf(trxClass.id)
            past.onCancelDate(MON_28_SEP_2026)
            past.onConfirmCancelDate()
            assertEquals(UiError.PastDate, past.state.value.error)
            assertEquals(before, world.sessionsOf(trxClass.id))
        }
    }

    @Test
    fun AC70_requests_can_be_approved_or_declined() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            assertTrue(world.manage(coach.toAccount(), trxClass.id).state.value.requestsEmpty)
            val first = world.student(1)
            val second = world.student(2)
            world.requestToJoin(trxClass.id, first.id)
            world.requestToJoin(trxClass.id, second.id)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            viewModel.onSelectTab(ManageTab.REQUESTS)
            assertEquals(listOf(first.id, second.id), viewModel.state.value.requests.map { it.studentId })
            assertEquals(listOf("Student 1", "Student 2"), viewModel.state.value.requests.map { it.name })
            assertEquals(listOf("EMP-1", "EMP-2"), viewModel.state.value.requests.map { it.employeeId })
            viewModel.onApprove(first.id)
            assertEquals(MembershipStatus.ACTIVE, world.statusOf(trxClass.id, first.id))
            assertEquals(listOf(first.id), viewModel.state.value.roster.map { it.studentId })
            assertTrue(viewModel.state.value.requests.none { it.studentId == first.id })
            viewModel.onDecline(second.id)
            assertEquals(MembershipStatus.DECLINED, world.statusOf(trxClass.id, second.id))
            assertTrue(viewModel.state.value.requestsEmpty)
        }
    }

    @Test
    fun AC71_remove_confirms_and_keeps_past_attendance() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createMonWedEveningClass(coach.id)
            val first = world.student(1)
            val second = world.student(2)
            world.makeActive(trxClass.id, first.id)
            world.makeActive(trxClass.id, second.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
            world.recordAttendance(first.id, world.refOf(world.sessionOn(trxClass.id, MON_28_SEP_2026)), world.todaysCode(coach.id))
            val attendance = world.attendance.all()
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            assertEquals(listOf(first.id, second.id), viewModel.state.value.roster.map { it.studentId })
            viewModel.onRemove(first.id)
            assertEquals(first.id, viewModel.state.value.confirmRemove!!.studentId)
            assertEquals("Student 1", viewModel.state.value.confirmRemove!!.name)
            viewModel.onKeep()
            assertNull(viewModel.state.value.confirmRemove)
            assertEquals(listOf(first.id, second.id), viewModel.state.value.roster.map { it.studentId })
            viewModel.onRemove(first.id)
            viewModel.onConfirmRemove()
            assertEquals(MembershipStatus.REMOVED, world.statusOf(trxClass.id, first.id))
            assertEquals(listOf(second.id), viewModel.state.value.roster.map { it.studentId })
            assertEquals(attendance, world.attendance.all())
        }
    }

    @Test
    fun AC72_invite_search_matches_only_an_exact_student_employee_id() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student(1)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            val before = world.memberships.all()
            viewModel.onSearchTextChange("  EMP-1  ")
            viewModel.onSearch()
            assertEquals("Student 1", viewModel.state.value.match!!.name)
            assertEquals("EMP-1", viewModel.state.value.match!!.employeeId)
            assertNull(viewModel.state.value.match!!.currentMembershipStatus)
            assertTrue(viewModel.state.value.canSendInvite)
            assertFalse(viewModel.state.value.noMatch)
            assertEquals(before, world.memberships.all())

            viewModel.onSearchTextChange(coach.email)
            viewModel.onSearch()
            assertTrue(viewModel.state.value.noMatch)
            assertEquals(coach.email, viewModel.state.value.searchedId)
            viewModel.onSearchTextChange(coach.name)
            viewModel.onSearch()
            assertTrue(viewModel.state.value.noMatch)
            viewModel.onSearchTextChange(coach.id)
            viewModel.onSearch()
            assertTrue(viewModel.state.value.noMatch)
            assertEquals(before, world.memberships.all())

            world.requestToJoin(trxClass.id, student.id)
            viewModel.onSearchTextChange("EMP-1")
            viewModel.onSearch()
            assertEquals(MembershipStatus.PENDING_REQUEST, viewModel.state.value.match!!.currentMembershipStatus)
            assertFalse(viewModel.state.value.canSendInvite)
            world.declineRequest(trxClass.id, student.id)
            viewModel.onSearch()
            assertEquals(MembershipStatus.DECLINED, viewModel.state.value.match!!.currentMembershipStatus)
            assertTrue(viewModel.state.value.canSendInvite)
        }
    }

    @Test
    fun AC73_send_invite_updates_the_match_and_the_student_invites_list() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            val trxClass = world.createMonWedEveningClass(coach.id, name = "Invited class")
            val student = world.student()
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            viewModel.onSearchTextChange("EMP-1")
            viewModel.onSearch()
            viewModel.onSendInvite()
            assertEquals(MembershipStatus.INVITED, viewModel.state.value.match!!.currentMembershipStatus)
            assertFalse(viewModel.state.value.canSendInvite)
            assertEquals(ManageConfirmation.InviteSent("Student 1"), viewModel.state.value.confirmation)
            assertEquals(listOf(trxClass.id), world.invites(student.toAccount()).state.value.invites.map { it.classId })

            viewModel.onSendInvite()
            assertEquals(1, world.membershipsOf(trxClass.id, student.id).size)
            assertEquals(InviteResult.AlreadyInvited::class, world.inviteByEmployeeId(trxClass.id, "EMP-1")::class)

            val requested = world.student(2)
            world.requestToJoin(trxClass.id, requested.id)
            viewModel.onSearchTextChange("EMP-2")
            viewModel.onSearch()
            val count = world.memberships.all().size
            viewModel.onSendInvite()
            assertEquals(MembershipStatus.PENDING_REQUEST, viewModel.state.value.match!!.currentMembershipStatus)
            assertEquals(count, world.memberships.all().size)

            viewModel.onSearchTextChange("EMP-404")
            viewModel.onSendInvite()
            assertTrue(viewModel.state.value.noMatch)

            world.cancelClass(trxClass.id)
            viewModel.onSearchTextChange("EMP-1")
            viewModel.onSendInvite()
            assertEquals(UiError.ClassCancelled, viewModel.state.value.error)
        }
    }

    @Test
    fun AC74_cancelling_the_class_goes_back_and_blocks_later_writes() = runVm {
        Phase2World().use { world ->
            val coach = world.coach()
            world.giveKeys(coach.id)
            val trxClass = world.createMonWedEveningClass(coach.id)
            val student = world.student()
            world.makeActive(trxClass.id, student.id)
            world.setLocal(MON_28_SEP_2026, 17, 40)
            assertIsRecorded(world.recordAttendance(student.id, world.refOf(world.sessionOn(trxClass.id, MON_28_SEP_2026)), world.todaysCode(coach.id)))
            val attendance = world.attendance.all()
            world.setLocal(WED_30_SEP_2026, 10)
            val viewModel = world.manage(coach.toAccount(), trxClass.id)
            viewModel.onOpenOptions()
            assertTrue(viewModel.state.value.optionsOpen)
            viewModel.onCancelWholeClass()
            assertTrue(viewModel.state.value.confirmCancelClass)
            viewModel.onKeepClass()
            assertFalse(viewModel.state.value.confirmCancelClass)
            assertTrue(world.sessionsOf(trxClass.id).any { it.status == SessionStatus.SCHEDULED && it.date >= WED_30_SEP_2026 })
            viewModel.onCancelWholeClass()
            viewModel.onConfirmCancelClass()
            assertEquals(Navigation.Back, viewModel.state.value.navigation)
            assertTrue(viewModel.state.value.schedule.all { !it.canCancelDate })
            assertEquals(SessionStatus.SCHEDULED, world.sessionsOf(trxClass.id).single { it.date == MON_28_SEP_2026 }.status)
            assertTrue(world.sessionsOf(trxClass.id).filter { it.date >= WED_30_SEP_2026 }.all { it.status == SessionStatus.CANCELLED })
            assertEquals(attendance, world.attendance.all())
            assertEquals(JoinResult.ClassCancelled, world.requestToJoin(trxClass.id, world.student(2).id))
            assertEquals(InviteResult.ClassCancelled, world.inviteByEmployeeId(trxClass.id, "EMP-1"))
            world.setLocal(WED_30_SEP_2026, 17, 40)
            val attend = world.attend(student.toAccount())
            val sessionId = world.sessionOn(trxClass.id, WED_30_SEP_2026).id
            attend.onChoose(sessionId)
            attend.onCodeScanned(world.todaysCode(coach.id))
            assertEquals(RejectReason.ClassCancelled, (attend.state.value.result as ScanResult.Rejected).reason)
        }
    }
}

private fun assertIsRecorded(result: AttendanceResult) {
    kotlin.test.assertIs<AttendanceResult.Recorded>(result)
}
