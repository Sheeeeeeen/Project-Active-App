package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassCoachRequestTest {
    @Test
    fun `a coach has no request action`() {
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

        val opened = catalog.open(name = "Morning TRX", coach = CoachAccount())

        assertEquals("Morning TRX", opened.listed.name)
        assertEquals(false, opened.canRequest)
    }
}
