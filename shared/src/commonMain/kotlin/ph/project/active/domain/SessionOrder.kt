package ph.project.active.domain

internal val SESSION_ORDER: Comparator<Session> =
    compareBy<Session>({ it.date }, { it.slot.start }, { it.classId }, { it.sessionNumber })
