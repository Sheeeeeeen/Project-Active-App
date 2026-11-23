package ph.project.active.ui.testing

import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User

/**
 * Flow 4 example data (spec section 7). D1: Mara's five classes in creation order.
 * Manage (Core Burn): four roster students in join order, three requests, and Felix, who is not on it.
 */
data class Flow4Example(
    val mara: User,
    val coreBurn: TrxClass,
    val lunchExpress: TrxClass,
    val sunrise: TrxClass,
    val privateClass: TrxClass,
    val cancelledClass: TrxClass,
    val paolo: User,
    val dana: User,
    val gia: User,
    val hana: User,
    val ella: User,
    val bea: User,
    val carlo: User,
    val felix: User,
) {
    val classIds: List<String> get() = listOf(coreBurn, lunchExpress, sunrise, privateClass, cancelledClass).map { it.id }
}
