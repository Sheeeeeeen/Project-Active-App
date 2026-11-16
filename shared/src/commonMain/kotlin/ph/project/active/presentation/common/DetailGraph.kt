package ph.project.active.presentation.common

import ph.project.active.data.repository.NetworkMonitor
import ph.project.active.data.repository.PersonRepository
import ph.project.active.domain.repository.ClassRepository
import ph.project.active.domain.repository.MembershipRepository
import ph.project.active.domain.usecase.RequestToJoin

/**
 * The navigation tests build screens without starting Koin, so the list and
 * registration view models publish the repositories the detail screen reads.
 */
object DetailGraph {
    var classes: ClassRepository? = null
    var people: PersonRepository? = null
    var network: NetworkMonitor? = null
    var memberships: MembershipRepository? = null
    var requestToJoin: RequestToJoin? = null
}
