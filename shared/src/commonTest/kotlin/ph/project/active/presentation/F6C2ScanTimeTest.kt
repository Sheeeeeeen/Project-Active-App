package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalTime
import ph.project.active.testkit.Flow6Example
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.runVm

/** Flow 6 spec C2: a Present row carries its scan time in the app's time zone. */
class F6C2ScanTimeTest {

    // C2: "a record scanned at 6:02 AM Asia/Manila gives scannedTime = 06:02"
    @Test
    fun F6_C2_aPresentRowHasItsScanTimeInAsiaManila() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)

            val sections = world.coachAttendance(d22.account).state.value.sections
            val sunrise = sections.single { it.className == "Sunrise TRX Strength" }
            val coreBurn = sections.single { it.className == "TRX Core Burn" }

            assertEquals(LocalTime(6, 2), sunrise.students.single { it.studentId == d22.paolo.id }.scannedTime)
            assertEquals(LocalTime(6, 5), sunrise.students.single { it.studentId == d22.dana.id }.scannedTime)
            assertEquals(LocalTime(17, 31), coreBurn.students.single { it.studentId == d22.paolo.id }.scannedTime)
        }
    }

    // C2: "null otherwise" (rows that are not Present)
    @Test
    fun F6_C2_rowsThatAreNotPresentHaveNoScanTime() = runVm {
        Phase2World().use { world ->
            val d22 = Flow6Example(world)

            val sunrise = world.coachAttendance(d22.account).state.value.sections
                .single { it.className == "Sunrise TRX Strength" }

            assertNull(sunrise.students.single { it.studentId == d22.hana.id }.scannedTime)
        }
    }
}
