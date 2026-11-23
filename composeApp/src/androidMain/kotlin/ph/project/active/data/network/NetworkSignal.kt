package ph.project.active.data.network

/** The two capabilities of the default network that decide whether the app is online. */
data class NetworkSignal(val hasInternet: Boolean, val isValidated: Boolean)
