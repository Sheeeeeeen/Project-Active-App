package ph.project.active.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.MembershipStatus
import ph.project.active.domain.model.User
import ph.project.active.domain.repository.currentStatus
import ph.project.active.ui.testing.Flow4Example
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextShown
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openManageTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.submitInviteSearch
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F4ManageInviteTest {
    @Test
    fun F4_AC21_searchFindsFelixAndSendInviteWorksOnce() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openInvite(world, d1)

            assertTextShown("Search a student by employee ID to invite them to this class.")
            assertTextShown("Employee ID")
            submitInviteSearch("EMP-20733")

            assertTextShown("1 match")
            assertTextShown("Felix Ong")
            assertTextShown("EMP-20733 \u00B7 Student \u00B7 Not on this class")
            assertTextUnder("manage_invite_send", "Send invite")

            tag("manage_invite_send").performClick()
            waitForIdle()
            assertTextUnder("manage_toast", "Invite sent to Felix Ong. They\u2019ll see it in Invites.")
            assertTextUnder("manage_invite_send", "Invite sent")
            tag("manage_invite_send").assertIsNotEnabled()
            assertEquals(MembershipStatus.INVITED, status(world, d1, d1.felix))
            assertEquals(1, membershipCount(world, d1, d1.felix))

            tag("manage_invite_send").performClick()
            waitForIdle()
            assertEquals(1, membershipCount(world, d1, d1.felix), "Felix cannot be invited again")

            mainClock.advanceTimeBy(4_100)
            waitForIdle()
            tag("manage_toast").assertDoesNotExist()
        }
    }

    @Test
    fun F4_AC21_anUnknownIdShowsNoMatchAndSendsNothing() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openInvite(world, d1)
            val before = world.memberships.all()

            submitInviteSearch("EMP-99999")

            assertTextUnder("manage_invite_empty", "No student found")
            assertTextUnder("manage_invite_empty", "No account uses EMP-99999. Nothing was sent.")
            tag("manage_invite_send").assertDoesNotExist()
            assertEquals(before, world.memberships.all())
        }
    }

    @Test
    fun F4_AC21_aBlankQueryDoesNotSearch() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            openInvite(world, d1)

            submitInviteSearch("")
            tag("manage_invite_empty").assertDoesNotExist()
            tag("manage_invite_send").assertDoesNotExist()

            submitInviteSearch("   ")
            tag("manage_invite_empty").assertDoesNotExist()
            tag("manage_invite_send").assertDoesNotExist()
            tag("manage_form_error").assertDoesNotExist()
        }
    }

    @Test
    fun F4_AC21_sendingToAStudentWhoIsAlreadyInvitedRequestedOrOnTheRosterShowsTheQ8Line() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            val ivy = world.student("Ivy Ramos", "EMP-20901")
            val jon = world.student("Jon Bautista", "EMP-20902")
            val kai = world.student("Kai Mendoza", "EMP-20903")
            openInvite(world, d1)

            // Each student matches as "Not on this class"; then, before Send invite is tapped,
            // their membership changes elsewhere (another device), so the send is refused.
            submitInviteSearch("EMP-20901")
            world.invite(d1.coreBurn.id, ivy)
            assertSendRefused(world, d1, ivy, "This student already has an invite.")

            submitInviteSearch("EMP-20902")
            world.request(d1.coreBurn.id, jon)
            assertSendRefused(world, d1, jon, "This student already requested this class.")

            submitInviteSearch("EMP-20903")
            world.joinByRequest(d1.coreBurn.id, kai)
            assertSendRefused(world, d1, kai, "This student is already on the roster.")
        }
    }

    private fun ComposeUiTest.assertSendRefused(world: Flow4Harness, d1: Flow4Example, student: User, line: String) {
        val before = world.memberships.all()
        tag("manage_invite_send").performClick()
        waitForIdle()
        assertTextUnder("manage_form_error", line)
        assertEquals(before, world.memberships.all(), "nothing is created for ${student.name}")
        tag("manage_toast").assertDoesNotExist()
    }

    private fun ComposeUiTest.openInvite(world: Flow4Harness, d1: Flow4Example) {
        world.signInAsMara()
        mountFlow4(world)
        openCoachCard(d1.coreBurn.id)
        openManageTab("invite")
    }

    private fun status(world: Flow4Harness, d1: Flow4Example, student: User): MembershipStatus? =
        world.memberships.currentStatus(d1.coreBurn.id, student.id)

    private fun membershipCount(world: Flow4Harness, d1: Flow4Example, student: User): Int =
        world.memberships.forClass(d1.coreBurn.id).count { it.studentId == student.id }
}
