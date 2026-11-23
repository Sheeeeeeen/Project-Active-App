package ph.project.active.data.network

import ph.project.active.domain.model.NetworkStatus

/** Online only when the default network has INTERNET and VALIDATED. A null signal means no network. */
fun networkStatusOf(signal: NetworkSignal?): NetworkStatus =
    if (signal != null && signal.hasInternet && signal.isValidated) NetworkStatus.ONLINE else NetworkStatus.OFFLINE
