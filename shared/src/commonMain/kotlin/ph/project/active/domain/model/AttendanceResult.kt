package ph.project.active.domain.model

sealed interface AttendanceResult {
    data class Recorded(val attendance: AttendanceRecord) : AttendanceResult
    data object AlreadyRecorded : AttendanceResult
    data object NoSessionChosen : AttendanceResult
    /** The chosen class/date/slot is not in the local store. */
    data object SessionUnknown : AttendanceResult
    /** The whole class was cancelled (wins over [SessionCancelled]). */
    data object ClassCancelled : AttendanceResult
    /** This date was cancelled while the class is still open. */
    data object SessionCancelled : AttendanceResult
    /** The student's current membership is not ACTIVE (or not in the local store). */
    data object NotOnRoster : AttendanceResult
    data object InvalidCode : AttendanceResult
    data object WrongCoach : AttendanceResult
    /** The code is for another date, or the chosen session is not today. */
    data object WrongDate : AttendanceResult
}
