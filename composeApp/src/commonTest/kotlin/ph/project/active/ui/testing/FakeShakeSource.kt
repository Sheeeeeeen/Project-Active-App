package ph.project.active.ui.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ph.project.active.ui.platform.ShakeSource

class FakeShakeSource : ShakeSource {
    private val events = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    override val shakes: Flow<Unit> = events

    fun emitShake() {
        check(events.tryEmit(Unit)) { "shake was dropped; RegistrationRoute is not collecting shakes" }
    }
}
