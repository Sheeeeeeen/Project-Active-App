package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassStudentRequestTest {
    @Test
    fun `a signed-in student request is pending immediately`() {
        val student = StudentAccount(employeeId = "104582")
        val catalog = ClassCatalog(
            listOf(
                Class(
                    name = "Morning TRX",
                    coachName = "Ana Reyes",
                    days = listOf(
                        ClassDay(
                            name = "Monday",
                            schedules = listOf(
                                ClassTime(
                                    start = LocalTime(hour = 17, minute = 30),
                                    end = LocalTime(hour = 18, minute = 30),
                                ),
                            ),
                        ),
                    ),
                    visibility = ClassVisibility.Public,
                ),
            ),
        )

        val request = catalog.request(className = "Morning TRX", student = student)

        assertEquals("104582", request.employeeId)
        assertEquals(JoinRequestStatus.Pending, request.status)
        assertEquals(1, catalog.pendingRequests(className = "Morning TRX", employeeId = "104582").size)
        assertEquals(false, catalog.canRequest(className = "Morning TRX", employeeId = "104582"))
    }
}
