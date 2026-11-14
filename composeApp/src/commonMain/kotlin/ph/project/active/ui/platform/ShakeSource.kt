package ph.project.active.ui.platform

import kotlinx.coroutines.flow.Flow

interface ShakeSource {
    val shakes: Flow<Unit>
}
