package ph.project.active.domain

data class SessionAttendance(val session: Session, val students: List<StudentAttendance>)
