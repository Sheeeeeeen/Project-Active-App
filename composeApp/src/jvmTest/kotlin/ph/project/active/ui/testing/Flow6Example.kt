package ph.project.active.ui.testing

import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User

/**
 * Flow 6 D21 / D22 example data (spec section 7), on top of the flow 5 D19 classes. The session ids are
 * Mon 28 Sep 2026's sessions, so tests can build `coach_attendance_section_{sessionId}` and
 * `coach_attendance_row_{sessionId}_{studentId}` tags.
 */
data class Flow6Example(
    val mara: User,
    val sunrise: TrxClass,
    val lunchExpress: TrxClass,
    val coreBurn: TrxClass,
    val paolo: User,
    val dana: User,
    val hana: User,
    val gia: User,
    val sunriseSessionId: String,
    val lunchExpressSessionId: String,
    val coreBurnSessionId: String,
)
