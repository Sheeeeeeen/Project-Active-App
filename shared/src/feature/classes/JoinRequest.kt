package ph.com.sheen.projectactive.feature.classes

data class JoinRequest(
    val className: String,
    val employeeId: String,
    val status: JoinRequestStatus,
)
