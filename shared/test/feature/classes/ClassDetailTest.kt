package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassDetailTest {
    @Test
    fun `opening a public class returns its name, coach, days, and time`() {
        val morning = Class(
            name = "Morning TRX",
            coachName = "Ana Reyes",
            days = listOf(
                ClassDay(
                    name = "Monday",
                    schedules = listOf(
                        ClassTime(
                            start = LocalTime(hour = 17, minute = 30),
                            end = LocalTime(hour = 18, minute = 30)
                        )
                    ),
                ),
            ),
            visibility = ClassVisibility.Public,
        )

        val opened = ClassCatalog(listOf(morning)).open(morning.name)

        assertEquals("Morning TRX", opened.name)
        assertEquals("Ana Reyes", opened.coachName)
        assertEquals("Monday", opened.days.single().name)
        assertEquals(
            listOf(
                ClassTime(
                    start = LocalTime(hour = 17, minute = 30),
                    end = LocalTime(hour = 18, minute = 30)
                )
            ), opened.days.single().schedules
        )
    }
}
