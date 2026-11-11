package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassListTest {
    @Test
    fun `a guest sees only public classes`() {
        val publicClass = Class(
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
        val privateClass = Class(
            name = "Private TRX",
            coachName = "Ana Reyes",
            days = listOf(
                ClassDay(
                    name = "Friday",
                    schedules = listOf(
                        ClassTime(
                            start = LocalTime(hour = 19, minute = 0),
                            end = LocalTime(hour = 20, minute = 0)
                        )
                    ),
                ),
            ),
            visibility = ClassVisibility.Private,
        )

        val visible = ClassCatalog(listOf(publicClass, privateClass)).publicClasses()

        assertEquals(listOf(publicClass), visible)
    }
}
