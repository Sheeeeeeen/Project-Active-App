package ph.project.active.domain.model

import kotlinx.datetime.LocalDate

fun TrxClass.hasEnded(today: LocalDate): Boolean = endDate != null && endDate < today
