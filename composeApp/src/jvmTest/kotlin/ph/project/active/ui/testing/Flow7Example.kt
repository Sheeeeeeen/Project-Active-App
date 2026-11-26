package ph.project.active.ui.testing

import kotlinx.datetime.LocalDate
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.User
import ph.project.active.domain.usecase.GenerateDailyCode

/**
 * Flow 7 example data (spec section 7). Coaches Mara Santos and Leo Ramirez, student Paolo Reyes (EMP-20417),
 * four classes created on Mon 28 Sep 2026 with Paolo ACTIVE on all four. [codes] signs on the coach side only:
 * the student phone's own key store holds no coach keys.
 */
class Flow7Example(
    val mara: User,
    val leo: User,
    val paolo: User,
    val sunrise: TrxClass,
    val lunchExpress: TrxClass,
    val coreBurn: TrxClass,
    val mobilityFlow: TrxClass,
    private val codes: GenerateDailyCode,
) {
    /** "Mara's code": Mara's daily code for Mon 28 Sep 2026. */
    val marasCode: String get() = code(mara, MON_28_SEP_2026)

    /** "Old code": Mara's code for Mon 21 Sep 2026. */
    val oldCode: String get() = code(mara, LocalDate(2026, 9, 21))

    fun code(coach: User, date: LocalDate): String = codes(coachId = coach.id, date = date)
}
