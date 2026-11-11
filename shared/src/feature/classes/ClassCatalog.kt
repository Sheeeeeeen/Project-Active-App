package ph.com.sheen.projectactive.feature.classes

class ClassCatalog(private val classes: List<Class>) {
    private val requests = mutableListOf<JoinRequest>()

    fun publicClasses(): List<Class> = classes.filter { it.visibility == ClassVisibility.Public }

    fun open(name: String): Class = classes.first { it.name == name }

    fun open(name: String, coach: CoachAccount): ClassDetail =
        ClassDetail(listed = open(name), canRequest = false)

    fun request(className: String, student: StudentAccount): JoinRequest =
        request(className, student.employeeId)

    fun request(className: String, employeeId: String): JoinRequest {
        pendingRequests(className, employeeId).singleOrNull()?.let { return it }
        val request = JoinRequest(
            className = className,
            employeeId = employeeId,
            status = JoinRequestStatus.Pending,
        )
        requests += request
        return request
    }

    fun decline(className: String, employeeId: String) {
        val index = requests.indexOfFirst {
            it.className == className &&
                it.employeeId == employeeId &&
                it.status == JoinRequestStatus.Pending
        }
        if (index < 0) return
        requests[index] = requests[index].copy(status = JoinRequestStatus.Declined)
    }

    fun pendingRequests(className: String, employeeId: String): List<JoinRequest> =
        requests.filter {
            it.className == className &&
                it.employeeId == employeeId &&
                it.status == JoinRequestStatus.Pending
        }

    fun canRequest(className: String, employeeId: String): Boolean =
        pendingRequests(className, employeeId).isEmpty()
}
