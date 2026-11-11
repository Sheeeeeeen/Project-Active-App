package ph.project.active.domain.usecase

import ph.project.active.domain.model.Session

internal val SESSION_ORDER: Comparator<Session> =
    compareBy<Session>({ it.date }, { it.slot.start }, { it.classId }, { it.sessionNumber })
