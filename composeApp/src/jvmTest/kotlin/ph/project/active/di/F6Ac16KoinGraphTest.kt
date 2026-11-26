package ph.project.active.di

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.test.KoinTest
import org.koin.test.get
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.Role
import ph.project.active.presentation.coachattendance.CoachAttendanceViewModel

/** Flow 6 F6-AC16: the production Koin graph can build the coach Attendance ViewModel without a test factory. */
@OptIn(ExperimentalCoroutinesApi::class)
class F6Ac16KoinGraphTest : KoinTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    // F6-AC16: "CoachAttendanceViewModel is bound in ViewModelModule, and a signed-in coach can open the Attendance tab in the production graph without a custom factory (checked by the Koin graph test …)"
    @Test
    fun F6_AC16_appModulesResolveCoachAttendanceViewModelForASignedInCoach() {
        startKoin { modules(appModules()) }
        val coach = SignedInAccount("coach-1", "Mara Santos", "mara@company.com", Role.COACH, null)

        val viewModel = get<CoachAttendanceViewModel> { parametersOf(coach) }

        assertEquals(emptyList(), viewModel.state.value.sections, "a new coach has no sessions yet")
    }
}
