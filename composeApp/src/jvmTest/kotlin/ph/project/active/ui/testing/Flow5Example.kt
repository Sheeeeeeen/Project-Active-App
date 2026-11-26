package ph.project.active.ui.testing

import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User

/**
 * Flow 5 D19 example data (spec section 7): coach Mara Santos on Mon 28 Sep 2026. Today's sessions in
 * start-time order are Sunrise (Scheduled), Lunch Express (Cancelled on this date) and Core Burn (Scheduled).
 */
data class Flow5Example(
    val mara: User,
    val sunrise: TrxClass,
    val lunchExpress: TrxClass,
    val coreBurn: TrxClass,
)
