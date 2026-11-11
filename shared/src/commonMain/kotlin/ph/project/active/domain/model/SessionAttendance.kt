package ph.project.active.domain.model

data class SessionAttendance(val session: Session, val students: List<StudentAttendance>)
