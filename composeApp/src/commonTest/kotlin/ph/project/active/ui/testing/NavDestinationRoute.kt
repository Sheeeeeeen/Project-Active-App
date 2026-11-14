package ph.project.active.ui.testing

import androidx.navigation.NavDestination
import androidx.navigation.serialization.generateHashCode
import kotlin.reflect.KClass
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer

/**
 * Navigation 2.9 exposes this check as a companion extension, and the flow 1 tests call
 * `destination.hasRoute(Route::class)` without importing it. Same-package extension so that call
 * resolves. It uses the same hash the NavHost assigns to a type-safe route.
 */
@OptIn(InternalSerializationApi::class)
fun <T : Any> NavDestination.hasRoute(route: KClass<T>): Boolean =
    route.serializer().generateHashCode() == id
