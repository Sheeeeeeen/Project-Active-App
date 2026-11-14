package ph.project.active.ui.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ph.project.active.presentation.registration.RegistrationViewModel
import ph.project.active.ui.platform.ShakeSource

internal class RegistrationShakeCollection {
    private var job: Job? = null
    private var viewModel: RegistrationViewModel? = null

    fun ensure(scope: CoroutineScope, source: ShakeSource, viewModel: RegistrationViewModel) {
        val existing = job
        if (existing != null && existing.isActive && this.viewModel === viewModel) return
        existing?.cancel()
        this.viewModel = viewModel
        job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
            source.shakes.collect { viewModel.onShake() }
        }
    }

    fun stop(viewModel: RegistrationViewModel) {
        if (this.viewModel !== viewModel) return
        stopAny()
    }

    fun stopAny() {
        job?.cancel()
        job = null
        viewModel = null
    }
}

internal val LocalRegistrationShakes = staticCompositionLocalOf<RegistrationShakeCollection?> { null }
