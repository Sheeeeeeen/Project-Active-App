package ph.project.active.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry

class SaveableRegistryHost(
    val registry: SaveableStateRegistry,
) {
    @Composable
    fun Wrap(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalSaveableStateRegistry provides registry, content = content)
    }
}
