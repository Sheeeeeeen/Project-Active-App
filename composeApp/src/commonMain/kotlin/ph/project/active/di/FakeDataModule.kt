package ph.project.active.di

import app.cash.sqldelight.db.SqlDriver
import org.koin.dsl.module
import ph.project.active.data.db.TrxDatabase
import ph.project.active.data.fake.FakeAttendanceUploader
import ph.project.active.data.fake.FakeAuthRepository
import ph.project.active.data.fake.FakePasswordHasher
import ph.project.active.data.fake.MutableNetworkMonitor
import ph.project.active.data.repository.AttendanceUploader
import ph.project.active.data.repository.AuthRepository
import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.repository.PersonRepository
import ph.project.active.data.repository.SessionStore
import ph.project.active.data.repository.SqlAttendanceRepository
import ph.project.active.data.repository.SqlClassRepository
import ph.project.active.data.repository.SqlMembershipRepository
import ph.project.active.data.repository.SqlPersonRepository
import ph.project.active.data.repository.SqlSessionStore
import ph.project.active.data.sync.OutboxSync
import ph.project.active.domain.crypto.PasswordHasher
import ph.project.active.domain.model.NetworkStatus
import ph.project.active.domain.repository.AttendanceRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.repository.UserRepository
import ph.project.active.domain.repository.inmemory.InMemoryUserRepository
import ph.project.active.domain.usecase.RequestToJoin

val FakeDataModule = module {
    single { TrxDatabase(get<SqlDriver>()) }
    single<SessionStore> { SqlSessionStore(get()) }
    single<PersonRepository> { SqlPersonRepository(get()) }
    single<ClassRepository> { SqlClassRepository(get()) }
    single<MembershipRepository> { SqlMembershipRepository(get()) }
    single<AttendanceRepository> { SqlAttendanceRepository(get()) }
    single<NetworkMonitor> { MutableNetworkMonitor(NetworkStatus.ONLINE) }
    single<UserRepository> { InMemoryUserRepository() }
    single<PasswordHasher> { FakePasswordHasher() }
    single<AuthRepository> {
        FakeAuthRepository(
            directory = get(),
            hasher = get(),
            network = get(),
            session = get(),
            people = get(),
        )
    }
    single<AttendanceUploader> { FakeAttendanceUploader() }
    single {
        OutboxSync(
            attendance = get(),
            uploader = get(),
            network = get(),
        )
    }
    single { RequestToJoin(classes = get(), memberships = get()) }
}
