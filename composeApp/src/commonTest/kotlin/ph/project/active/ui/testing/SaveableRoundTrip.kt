package ph.project.active.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry

class SaveableRoundTrip {
    private var registry = SaveableStateRegistry(restoredValues = null, canBeSaved = { true })
    var savedEntryCount: Int = -1

    @Composable
    fun Wrap(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalSaveableStateRegistry provides registry, content = content)
    }

    fun saveForNextComposition() {
        val saved = registry.performSave()
        savedEntryCount = saved.size
        registry = SaveableStateRegistry(restoredValues = saved, canBeSaved = { true })
    }
}
