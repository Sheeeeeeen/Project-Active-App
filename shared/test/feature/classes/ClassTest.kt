package ph.com.sheen.projectactive.feature.classes

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalTime

class ClassTest {
    @Test
    fun `a class records its name, coach, days, time, and visibility`() {
        val trxClass = Class(
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
                ClassDay(
                    name = "Wednesday",
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

        assertEquals("Morning TRX", trxClass.name)
        assertEquals("Ana Reyes", trxClass.coachName)
        assertEquals("Monday", trxClass.days[0].name)
        assertEquals(
            listOf(
                ClassTime(
                    start = LocalTime(hour = 17, minute = 30),
                    end = LocalTime(hour = 18, minute = 30)
                )
            ), trxClass.days[0].schedules
        )
        assertEquals(ClassVisibility.Public, trxClass.visibility)
    }

    @Test
    fun `each day holds its own times`() {
        val trxClass = Class(
            name = "TRX",
            coachName = "Ana Reyes",
            days = listOf(
                ClassDay(
                    name = "Monday",
                    schedules = listOf(
                        ClassTime(
                            start = LocalTime(hour = 17, minute = 30),
                            end = LocalTime(hour = 18, minute = 30)
                        ),
                        ClassTime(
                            start = LocalTime(hour = 18, minute = 30),
                            end = LocalTime(hour = 19, minute = 30)
                        ),
                    ),
                ),
                ClassDay(
                    name = "Wednesday",
                    schedules = listOf(
                        ClassTime(
                            start = LocalTime(hour = 17, minute = 30),
                            end = LocalTime(hour = 18, minute = 30)
                        ),
                    ),
                ),
            ),
            visibility = ClassVisibility.Public,
        )

        assertEquals(
            listOf(
                ClassTime(
                    start = LocalTime(hour = 17, minute = 30),
                    end = LocalTime(hour = 18, minute = 30)
                ),
                ClassTime(
                    start = LocalTime(hour = 18, minute = 30),
                    end = LocalTime(hour = 19, minute = 30)
                ),
            ),
            trxClass.days.first { it.name == "Monday" }.schedules,
        )
        assertEquals(
            listOf(
                ClassTime(
                    start = LocalTime(hour = 17, minute = 30),
                    end = LocalTime(hour = 18, minute = 30)
                )
            ),
            trxClass.days.first { it.name == "Wednesday" }.schedules,
        )
    }
}
