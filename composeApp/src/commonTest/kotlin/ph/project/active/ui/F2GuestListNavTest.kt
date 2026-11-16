package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow1Compose
import ph.project.active.ui.testing.Flow1Harness
import ph.project.active.ui.testing.Flow2Harness
import ph.project.active.ui.testing.Flow2Ui
import ph.project.active.ui.testing.ShowGuestList
import ph.project.active.ui.testing.mergedTag
import ph.project.active.ui.testing.mountNav
import ph.project.active.ui.testing.tag

@OptIn(ExperimentalTestApi::class)
class F2GuestListNavTest {
    @Test
    fun F2_AC7_returningFromRegistrationShowsAClassAddedWhileAway() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val nav = mountNav(world)
            tag("guest_home_register_button").performClick()
            world.coreBurn()
            nav.popBackStack()
            waitForIdle()
            tag("guest_list_card_${world.classes.allClasses().single().id}").assertTextContains("TRX Core Burn")
        }
    }

    @Test
    fun F2_AC8_tappingACardOpensOneClassDetail() = Flow2Ui.run {
        Flow1Harness().use { world ->
            val trxClass = world.coreBurn()
            val nav = mountNav(world)
            val card = tag("guest_list_card_${trxClass.id}")
            val click = card.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsActions.OnClick].action
            checkNotNull(click).invoke()
            checkNotNull(click).invoke()
            waitForIdle()
            val details = Flow1Compose.screenRoutes(nav).filter { it.startsWith("ClassDetail(") }
            assertEquals(listOf("ClassDetail(${trxClass.id})"), details)
        }
    }

    @Test
    fun F2_AC9_eachCardIsOneButtonWithTheLongDescription() = Flow2Ui.run {
        Flow2Harness().use { world ->
            val trxClass = world.coreBurn()
            setContent { ShowGuestList(world.guestList()) }
            val merged = onAllNodesWithTag("guest_list_card_${trxClass.id}").fetchSemanticsNodes()
            assertEquals(1, merged.size)
            val card = mergedTag("guest_list_card_${trxClass.id}")
            card.assertContentDescriptionEquals(
                "TRX Core Burn, Coach Mara Santos, Monday and Wednesday, 5:30 PM to 6:30 PM",
            )
            card.assertHeightIsAtLeast(48.dp)
            card.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        }
    }
}
