package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassRequestAgainTest {
    @Test
    fun `a declined request can be sent again`() {
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
        catalog.decline(className = "Morning TRX", employeeId = "104582")

        assertEquals(true, catalog.canRequest(className = "Morning TRX", employeeId = "104582"))

        catalog.request(className = "Morning TRX", employeeId = "104582")

        val pending = catalog.pendingRequests(className = "Morning TRX", employeeId = "104582")
        assertEquals(1, pending.size)
        assertEquals(JoinRequestStatus.Pending, pending.single().status)
    }
}
