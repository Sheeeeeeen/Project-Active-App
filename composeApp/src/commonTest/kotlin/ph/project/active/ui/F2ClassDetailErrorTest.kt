package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.model.Visibility
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.Flow2Harness
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowClassDetail
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F2ClassDetailErrorTest {
    @Test
    fun F2_AC23_requestErrorsShowAboveTheButtonAndClearOnTheNextTap() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            val viewModel = world.detail(trxClass.id, student)
            world.network.set(NetworkStatus.OFFLINE)
            setContent { ShowClassDetail(viewModel) }
            tag("class_detail_request_button").performClick()
            tag("class_detail_form_error").assertTextContains("You\u2019re offline. Nothing was saved.")
            world.network.set(NetworkStatus.ONLINE)
            tag("class_detail_request_button").performClick()
            waitForIdle()
            tag("class_detail_form_error").assertDoesNotExist()
        }
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            val student = world.account(world.student())
            setContent { ShowClassDetail(world.detail(trxClass.id, student)) }
            val saved = world.classes.findClass(trxClass.id)!!
            world.classes.saveClass(saved.copy(visibility = Visibility.PRIVATE))
            tag("class_detail_request_button").performClick()
            tag("class_detail_form_error").assertTextContains("This class is private. Joining is by coach invite only.")
        }
    }

    @Test
    fun F2_AC24_backPopsOneScreen() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            tag("guest_list_card_${trxClass.id}").performClick()
            waitForIdle()
            tag("class_detail_back").assertTextContains("Classes")
            tag("class_detail_back").performClick()
            waitForIdle()
            assertEquals(listOf("GuestHome"), Flow1Compose.screenRoutes(nav))
        }
    }
}
