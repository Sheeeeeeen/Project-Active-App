package ph.project.active.data.fake

import ph.project.active.data.fake.FakeAuthRepository
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.repository.SqlPersonRepository
import ph.project.active.data.repository.SqlSessionStore
import ph.project.active.data.inMemoryDb
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.testkit.TestPasswordHasher

class AuthFixture(
    val auth: FakeAuthRepository,
    val users: InMemoryUserRepository,
    val session: SqlSessionStore,
    val people: SqlPersonRepository,
    val db: ph.project.active.data.TestDb,
) : AutoCloseable {
    override fun close() {
        db.close()
    }
}

fun authFixture(status: NetworkStatus = NetworkStatus.ONLINE): AuthFixture {
    val db = inMemoryDb()
    val users = InMemoryUserRepository()
    val network = MutableNetworkMonitor(status)
    val session = SqlSessionStore(db.database)
    val people = SqlPersonRepository(db.database)
    val auth = FakeAuthRepository(
        directory = users,
        hasher = TestPasswordHasher(),
        network = network,
        session = session,
        people = people,
    )
    return AuthFixture(auth, users, session, people, db)
}
