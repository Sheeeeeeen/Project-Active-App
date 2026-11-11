package ph.project.active.domain

/** Outcome of checking a scanned daily code against an expected coach and today's local date. */
enum class DailyCodeCheck { VALID, INVALID, WRONG_COACH, WRONG_DATE }
