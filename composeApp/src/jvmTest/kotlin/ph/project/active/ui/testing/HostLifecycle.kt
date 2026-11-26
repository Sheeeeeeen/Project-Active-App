package ph.project.active.ui.testing

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * The app's lifecycle in a UI test. Moving it to CREATED sends the app to the background;
 * moving it back to RESUMED brings it to the foreground (flow 5 decisions 5 and 11).
 */
class HostLifecycle : LifecycleOwner {
    private val registry = LifecycleRegistry.createUnsafe(this)
    override val lifecycle: Lifecycle get() = registry

    init {
        registry.currentState = Lifecycle.State.RESUMED
    }

    fun background() {
        registry.currentState = Lifecycle.State.CREATED
    }

    fun foreground() {
        registry.currentState = Lifecycle.State.RESUMED
    }
}
