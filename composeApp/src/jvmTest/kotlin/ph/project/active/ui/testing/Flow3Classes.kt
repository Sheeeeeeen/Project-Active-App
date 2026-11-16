package ph.project.active.ui.testing

import ph.project.active.domain.model.TrxClass

/** The six example classes from the flow 3 spec, in creation order. */
data class Flow3Classes(
    val coreBurn: TrxClass,
    val lunchExpress: TrxClass,
    val mobilityFlow: TrxClass,
    val everyDay20: TrxClass,
    val bootcamp: TrxClass,
    val sunrise: TrxClass,
)
