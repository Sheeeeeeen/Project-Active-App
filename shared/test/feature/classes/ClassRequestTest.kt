package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassRequestTest {
    @Test
    fun `a guest request is pending and the request action is gone`() {
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

        val request = catalog.request(className = "Morning TRX", employeeId = "104582")

        assertEquals("Morning TRX", request.className)
        assertEquals("104582", request.employeeId)
        assertEquals(JoinRequestStatus.Pending, request.status)
        assertEquals(false, catalog.canRequest(className = "Morning TRX", employeeId = "104582"))
    }

    @Test
    fun `a second request does not create another pending request`() {
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

        catalog.request(className = "Morning TRX", employeeId = "104582")
        catalog.request(className = "Morning TRX", employeeId = "104582")

        val pending = catalog.pendingRequests(className = "Morning TRX", employeeId = "104582")
        assertEquals(1, pending.size)
        assertEquals(JoinRequestStatus.Pending, pending.single().status)
    }
}
