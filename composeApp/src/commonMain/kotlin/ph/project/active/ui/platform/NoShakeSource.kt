package ph.project.active.ui.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object NoShakeSource : ShakeSource {
    override val shakes: Flow<Unit> = emptyFlow()
}
