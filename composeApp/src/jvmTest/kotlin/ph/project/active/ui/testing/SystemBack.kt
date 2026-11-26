package ph.project.active.ui.testing

import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventDispatcherOwner

/**
 * The system back gesture for desktop UI tests (F7-AC25). The desktop test scene has no window, so Escape never
 * reaches the back dispatcher; [mountFlow7] stores the composition's dispatcher owner here, and [press] sends one
 * completed back event through it, the same path Android's back button and iOS's swipe use.
 */
class SystemBack {
    var owner: NavigationEventDispatcherOwner? = null

    fun press() {
        val dispatcher = checkNotNull(owner) { "mount the app first" }.navigationEventDispatcher
        val input = DirectNavigationEventInput()
        dispatcher.addInput(input)
        input.backCompleted()
        dispatcher.removeInput(input)
    }
}
