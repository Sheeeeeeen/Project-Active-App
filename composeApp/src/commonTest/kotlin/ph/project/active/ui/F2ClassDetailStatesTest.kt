package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithText
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.Role
import ph.project.active.domain.model.Visibility
import ph.project.active.ui.testing.Flow2Harness
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowClassDetail
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F2ClassDetailStatesTest {
    @Test
    fun F2_AC18_invitedStudentSeesTheInviteAndNoButton() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            world.invite(trxClass.id, student.employeeId!!)
            setContent { ShowClassDetail(world.detail(trxClass.id, student)) }
            tag("class_detail_chip").assertTextContains("Invited")
            tag("class_detail_status_body").assertTextContains(
                "Coach Mara Santos invited you. Accept or decline it in Invites.",
            )
            tag("class_detail_request_button").assertDoesNotExist()
        }
    }

    @Test
    fun F2_AC19_coachSeesTheClassWithNoButton() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val coach = ph.project.active.data.model.SignedInAccount(
                "coach-mara",
                "Mara Santos",
                "mara.santos@acme.test",
                Role.COACH,
                null,
            )
            setContent { ShowClassDetail(world.detail(trxClass.id, coach)) }
            tag("class_detail_name").assertTextContains("TRX Core Burn")
            tag("class_detail_request_button").assertDoesNotExist()
        }
    }

    @Test
    fun F2_AC20_cancelledClassShowsTheCancelledMessageAndNoButton() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            world.cancel(trxClass.id)
            setContent { ShowClassDetail(world.detail(trxClass.id)) }
            tag("class_detail_chip").assertTextContains("Cancelled")
            tag("class_detail_status_body").assertTextContains("This class has been cancelled.")
            tag("class_detail_request_button").assertDoesNotExist()
        }
    }

    @Test
    fun F2_AC21_endedYesterdayHidesTheButtonAndTheEndDateStillAllowsIt() = Flow2Ui.run {
        Flow2Harness().use { world ->
            world.setToday(LocalDate(2026, 10, 31))
            val endingThatDay = world.coreBurn(endDate = LocalDate(2026, 10, 31))
            setContent { ShowClassDetail(world.detail(endingThatDay.id)) }
            tag("class_detail_request_button").assertExists()
            world.setToday(LocalDate(2026, 11, 1))
            setContent { ShowClassDetail(world.detail(endingThatDay.id)) }
            tag("class_detail_status_body").assertTextContains("This class ended on Sat 31 Oct 2026.")
            tag("class_detail_request_button").assertDoesNotExist()
        }
    }

    @Test
    fun F2_AC22_privateOrUnknownClassShowsUnavailableAndOnlyBack() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val privateClass = world.saveClass(name = "Hidden", visibility = Visibility.PRIVATE)
            setContent { ShowClassDetail(world.detail(privateClass.id)) }
            tag("class_detail_unavailable").assertTextContains("Class not available")
            tag("class_detail_unavailable").assertTextContains("It may be private or no longer listed.")
            tag("class_detail_back").assertTextContains("Classes")
            tag("class_detail_request_button").assertDoesNotExist()
            onNodeWithText("Request to join").assertDoesNotExist()
            setContent { ShowClassDetail(world.detail("missing-class")) }
            tag("class_detail_unavailable").assertTextContains("Class not available")
            tag("class_detail_request_button").assertDoesNotExist()
        }
    }
}
