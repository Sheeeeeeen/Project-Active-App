package ph.project.active.ui.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import ph.project.active.presentation.classdetail.JoinOutcome

object JoinOutcomeNavType : NavType<JoinOutcome?>(isNullableAllowed = true) {
    override fun put(bundle: SavedState, key: String, value: JoinOutcome?) {
        bundle.write {
            if (value == null) putNull(key) else putString(key, value.name)
        }
    }

    override fun get(bundle: SavedState, key: String): JoinOutcome? =
        bundle.read { getStringOrNull(key) }?.let(JoinOutcome::valueOf)

    override fun parseValue(value: String): JoinOutcome? =
        if (value.isEmpty() || value == "null") null else JoinOutcome.valueOf(value)

    override fun serializeAsValue(value: JoinOutcome?): String = value?.name ?: "null"
}
