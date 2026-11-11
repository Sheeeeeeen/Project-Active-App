package ph.project.active.domain

import kotlinx.datetime.LocalTime

data class TimeSlot(val start: LocalTime, val end: LocalTime) {
    init {
        require(start < end) { "A time slot must start before it ends" }
    }
}
