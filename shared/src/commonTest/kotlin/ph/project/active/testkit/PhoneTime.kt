package ph.project.active.testkit

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import ph.project.active.domain.model.TimeSlot

val PHONE_TZ: TimeZone = TimeZone.of("Asia/Manila")

val FRI_25_SEP_2026: LocalDate = LocalDate(2026, 9, 25)
val MON_28_SEP_2026: LocalDate = LocalDate(2026, 9, 28)
val TUE_29_SEP_2026: LocalDate = LocalDate(2026, 9, 29)
val WED_30_SEP_2026: LocalDate = LocalDate(2026, 9, 30)
val SAT_31_OCT_2026: LocalDate = LocalDate(2026, 10, 31)
val THU_31_DEC_2026: LocalDate = LocalDate(2026, 12, 31)

val MORNING: TimeSlot = TimeSlot(start = LocalTime(7, 0), end = LocalTime(8, 0))
val EVENING: TimeSlot = TimeSlot(start = LocalTime(17, 30), end = LocalTime(18, 30))

val MON_WED_EVENING: Map<DayOfWeek, List<TimeSlot>> = mapOf(
    DayOfWeek.MONDAY to listOf(EVENING),
    DayOfWeek.WEDNESDAY to listOf(EVENING),
)
