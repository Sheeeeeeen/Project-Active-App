package ph.project.active.ui.coachhome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ph.project.active.data.model.SignedInAccount
import ph.project.active.di.rememberCoachClasses
import ph.project.active.di.rememberDailyQr
import ph.project.active.presentation.coachclasses.CoachClassesViewModel
import ph.project.active.presentation.common.Navigation
import ph.project.active.presentation.dailyqr.DailyQrViewModel
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.coachclasses.CoachClassesScreen
import ph.project.active.ui.dailyqr.DailyQrScreen
import ph.project.active.ui.platform.ScreenBrightness
import ph.project.active.ui.platform.rememberScreenBrightness

/** The signed-in coach root: three tabs inside one back-stack entry, so tab switches never touch the back stack. */
@Composable
fun CoachHomeScreen(
    account: SignedInAccount,
    coachClassesViewModel: ((SignedInAccount) -> CoachClassesViewModel)?,
    onOpenCreate: () -> Unit,
    onOpenManage: (String) -> Unit,
    dailyQrViewModel: ((SignedInAccount) -> DailyQrViewModel)? = null,
    screenBrightness: ScreenBrightness? = null,
) {
    val classes = rememberCoachClasses(account, coachClassesViewModel)
    var selected by rememberSaveable { mutableStateOf(CoachTab.Classes) }
    LifecycleStartEffect(selected) {
        if (selected == CoachTab.Classes) classes.refresh()
        onStopOrDispose {}
    }
    val state by classes.state.collectAsStateWithLifecycle()
    val follow = {
        when (val event = classes.state.value.navigation) {
            Navigation.OpenCreateClass -> onOpenCreate()
            is Navigation.OpenManage -> onOpenManage(event.classId)
            else -> Unit
        }
        classes.onNavigationHandled()
    }
    val tabBar = @Composable { CoachTabBar(selected = selected, onSelect = { selected = it }) }
    when (selected) {
        CoachTab.Classes -> CoachClassesScreen(
            coachName = account.name,
            state = state,
            onCreateClass = {
                classes.onCreateClass()
                follow()
            },
            onClassTapped = { id ->
                classes.onClassTapped(id)
                follow()
            },
            tabBar = tabBar,
        )
        CoachTab.Qr -> {
            val dailyQr = rememberDailyQr(account, dailyQrViewModel)
            val brightness = screenBrightness ?: rememberScreenBrightness()
            // Each show or return to the foreground picks up a new day; leaving or backgrounding restores the screen.
            LifecycleStartEffect(dailyQr, brightness) {
                dailyQr.refresh()
                brightness.raise()
                onStopOrDispose { brightness.restore() }
            }
            val qrState by dailyQr.state.collectAsStateWithLifecycle()
            DailyQrScreen(state = qrState, tabBar = tabBar)
        }
        CoachTab.Attendance -> CoachTabPlaceholder(
            title = UiCopy.tabAttendance(),
            rootTag = CoachTabBarTestTags.attendanceRoot,
            titleTag = CoachTabBarTestTags.attendanceTitle,
            accountTag = CoachTabBarTestTags.attendanceAccount,
            tabBar = tabBar,
        )
    }
}
