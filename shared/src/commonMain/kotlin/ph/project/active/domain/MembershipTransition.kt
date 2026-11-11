package ph.project.active.domain

/** Moves the student's current membership from [from] to [to]; throws [IllegalStateException] otherwise. */
internal fun MembershipRepository.transition(
    classId: String,
    studentId: String,
    from: MembershipStatus,
    to: MembershipStatus,
): Membership {
    val current = current(classId, studentId)
    check(current != null && current.status == from) {
        "Expected membership $from for student $studentId on class $classId but was ${current?.status}"
    }
    val updated = current.copy(status = to)
    update(updated)
    return updated
}
