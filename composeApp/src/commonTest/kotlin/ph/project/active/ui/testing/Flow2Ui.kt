package ph.project.active.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import ph.project.active.presentation.classdetail.ClassDetailUiState
import ph.project.active.presentation.classdetail.ClassDetailViewModel
import ph.project.active.presentation.guestclasses.GuestClassListUiState
import ph.project.active.presentation.guestclasses.GuestClassListViewModel
import ph.project.active.ui.classdetail.ClassDetailScreen
import ph.project.active.ui.guestclasses.GuestClassListScreen

object Flow2Ui {
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
    fun run(block: suspend ComposeUiTest.() -> Unit) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        try {
            runComposeUiTest(block = block)
        } finally {
            Dispatchers.resetMain()
        }
    }
}

@Composable
fun ShowGuestList(
    viewModel: GuestClassListViewModel,
    onClassTapped: (String) -> Unit = viewModel::onClassTapped,
    onRegisterOrSignIn: () -> Unit = viewModel::onRegisterOrSignIn,
) {
    val state by viewModel.state.collectAsState()
    GuestClassListScreen(
        state = state,
        onClassTapped = onClassTapped,
        onRegisterOrSignIn = onRegisterOrSignIn,
    )
}

@Composable
fun ShowGuestListState(
    state: GuestClassListUiState,
    onClassTapped: (String) -> Unit = {},
    onRegisterOrSignIn: () -> Unit = {},
) {
    GuestClassListScreen(
        state = state,
        onClassTapped = onClassTapped,
        onRegisterOrSignIn = onRegisterOrSignIn,
    )
}

@Composable
fun ShowClassDetail(
    viewModel: ClassDetailViewModel,
    onRequestToJoin: () -> Unit = viewModel::onRequestToJoin,
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    ClassDetailScreen(
        state = state,
        onRequestToJoin = onRequestToJoin,
        onBack = onBack,
    )
}

@Composable
fun ShowClassDetailState(
    state: ClassDetailUiState,
    onRequestToJoin: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    ClassDetailScreen(
        state = state,
        onRequestToJoin = onRequestToJoin,
        onBack = onBack,
    )
}
