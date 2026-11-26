package ph.project.active.di

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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
import ph.project.active.presentation.attend.AttendViewModel
import ph.project.active.presentation.studentsessions.StudentSessionsViewModel

/** Flow 7 F7-AC44: the production Koin graph builds the student Attend and Sessions ViewModels without a test factory. */
@OptIn(ExperimentalCoroutinesApi::class)
class F7Ac44KoinGraphTest : KoinTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    // F7-AC44: "AttendViewModel and StudentSessionsViewModel are bound in ViewModelModule. A signed-in student opens both tabs in the production graph without custom factories (Koin graph test …)"
    @Test
    fun F7_AC44_appModulesResolveAttendViewModelForASignedInStudent() {
        startKoin { modules(appModules()) }
        val student = SignedInAccount("student-new", "Ana Cruz", "ana@company.com", Role.STUDENT, "EMP-30001")

        val viewModel = get<AttendViewModel> { parametersOf(student) }

        assertTrue(viewModel.state.value.recordedToday.isEmpty(), "a student with no records has nothing recorded today")
    }

    // F7-AC44: "… A signed-in student opens both tabs in the production graph without custom factories"
    @Test
    fun F7_AC44_appModulesResolveStudentSessionsViewModelForASignedInStudent() {
        startKoin { modules(appModules()) }
        val student = SignedInAccount("student-new", "Ana Cruz", "ana@company.com", Role.STUDENT, "EMP-30001")

        val viewModel = get<StudentSessionsViewModel> { parametersOf(student) }

        assertEquals(emptyList(), viewModel.state.value.today, "a student on no class has no sessions today")
    }
}
