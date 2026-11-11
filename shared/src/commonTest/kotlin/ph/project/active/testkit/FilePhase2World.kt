package ph.project.active.testkit

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import ph.project.active.data.fake.InMemorySecretKeyStore
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.fileDb
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.model.NetworkStatus

fun phase2WorldOnFile(
    path: String,
    start: LocalDateTime = FRI_25_SEP_2026.atTime(9, 0),
    timeZone: TimeZone = PHONE_TZ,
    network: MutableNetworkMonitor = MutableNetworkMonitor(NetworkStatus.ONLINE),
    secrets: InMemorySecretKeyStore = InMemorySecretKeyStore(),
    directory: InMemoryUserRepository = InMemoryUserRepository(),
): Phase2World = Phase2World(
    start = start,
    timeZone = timeZone,
    testDb = fileDb(path),
    network = network,
    secrets = secrets,
    directory = directory,
)
