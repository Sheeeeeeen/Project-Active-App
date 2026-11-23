package ph.project.active.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import kotlin.test.assertEquals
import ph.project.active.ui.testing.Flow4Harness
import ph.project.active.ui.testing.assertTextUnder
import ph.project.active.ui.testing.clickTag
import ph.project.active.ui.testing.coachRoutes
import ph.project.active.ui.testing.mountFlow4
import ph.project.active.ui.testing.openCoachCard
import ph.project.active.ui.testing.openCoachTab
import ph.project.active.ui.testing.runFlow4UiTest
import ph.project.active.ui.testing.tag
import ph.project.active.ui.testing.tapTwiceQuickly
import ph.project.active.ui.testing.typeInto

@OptIn(ExperimentalTestApi::class)
class F4CoachNavTest {
    @Test
    fun F4_AC8_aCardOpensManageOnceAndBackReturnsToD1() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)

            tapTwiceQuickly("coach_class_card_${d1.coreBurn.id}")
            assertEquals(listOf("CoachHome", "ManageClass(${d1.coreBurn.id})"), coachRoutes(nav))

            assertTextUnder("manage_class_back", "Classes")
            tag("manage_class_back").performClick()
            waitForIdle()
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            tag("coach_tab_classes").assertIsSelected()
            tag("coach_class_card_${d1.coreBurn.id}").assertExists()
        }
    }

    @Test
    fun F4_AC26_aSavedCoachSessionLandsOnCoachHomeClassesTab() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.signInAsMara()
            val nav = mountFlow4(world)
            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            tag("coach_home").assertExists()
            tag("coach_tab_classes").assertIsSelected()
            tag("coach_classes_empty").assertExists()
        }
    }

    @Test
    fun F4_AC26_signingInAsACoachLandsOnCoachHomeClassesTab() = runFlow4UiTest {
        Flow4Harness().use { world ->
            world.seedD1()
            val nav = mountFlow4(world)
            tag("guest_home_register_button").performClick()
            waitForIdle()
            typeInto("registration_signin_email_field", "mara@company.com")
            typeInto("registration_signin_password_field", "password1")
            clickTag("registration_signin_button")
            waitForIdle()

            assertEquals(listOf("CoachHome"), coachRoutes(nav))
            tag("coach_tab_classes").assertIsSelected()
            tag("coach_classes_title").assertExists()
        }
    }

    @Test
    fun F4_AC26_createAndManageSitAboveCoachHomeAndTabsLeaveTheBackStackAlone() = runFlow4UiTest {
        Flow4Harness().use { world ->
            val d1 = world.seedD1()
            world.signInAsMara()
            val nav = mountFlow4(world)

            tapTwiceQuickly("coach_classes_add")
            assertEquals(listOf("CoachHome", "CreateClass"), coachRoutes(nav), "two quick taps add one CreateClass")
            tag("create_class_cancel").performClick()
            waitForIdle()

            openCoachCard(d1.coreBurn.id)
            assertEquals(listOf("CoachHome", "ManageClass(${d1.coreBurn.id})"), coachRoutes(nav))
            tag("manage_class_back").performClick()
            waitForIdle()

            for (tab in listOf("qr", "attendance", "classes")) {
                openCoachTab(tab)
                assertEquals(listOf("CoachHome"), coachRoutes(nav), "switching to $tab keeps the back stack")
                tag("coach_tab_$tab").assertIsSelected()
            }
        }
    }
}
