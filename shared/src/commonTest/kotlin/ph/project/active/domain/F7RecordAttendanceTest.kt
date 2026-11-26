package ph.project.active.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.usecase.RecordAttendance
import ph.project.active.domain.usecase.VerifyDailyCode

/**
 * Flow 7 spec S2 and decision 1: RecordAttendance reads the link (format, coach, date) and never checks
 * the signature. F7-AC2 to F7-AC4. Paolo's world holds no coach keys.
 */
class F7RecordAttendanceTest {

    // F7-AC2: "Core Burn + Mara's code → Recorded, with no coach keys on the phone."
    @Test
    fun F7_AC2_coreBurnWithMarasCodeIsRecordedWithNoCoachKeysOnThePhone() {
        val example = Flow7Example()
        assertNull(example.world.coachKeys.find(example.mara.id), "Paolo's phone has no key for Mara")

        val result = example.record(example.coreBurn, example.marasCode)

        val recorded = assertIs<AttendanceResult.Recorded>(result)
        assertEquals(example.sessionOf(example.coreBurn).id, recorded.attendance.sessionId)
        assertEquals(2, example.world.attendance.all().size, "the Sunrise record plus the new Core Burn record")
    }

    // F7-AC2: "Core Burn + Mara's code with a changed signature character → Recorded (the server checks later)."
    @Test
    fun F7_AC2_aChangedSignatureCharacterIsStillRecorded() {
        val example = Flow7Example()

        val result = example.record(example.coreBurn, withChangedLastSigCharacter(example.marasCode))

        assertIs<AttendanceResult.Recorded>(result)
        assertEquals(2, example.world.attendance.all().size)
    }

    // F7-AC2: "Mobility Flow + Mara's code → WrongCoach." / "Only Recorded writes a record."
    @Test
    fun F7_AC2_mobilityFlowWithMarasCodeIsWrongCoach() {
        val example = Flow7Example()

        assertEquals(AttendanceResult.WrongCoach, example.record(example.mobilityFlow, example.marasCode))
        assertEquals(1, example.world.attendance.all().size, "nothing new is stored")
    }

    // F7-AC2: "Core Burn + Leo's code → WrongCoach." / "Only Recorded writes a record."
    @Test
    fun F7_AC2_coreBurnWithLeosCodeIsWrongCoach() {
        val example = Flow7Example()

        assertEquals(AttendanceResult.WrongCoach, example.record(example.coreBurn, example.leosCode))
        assertEquals(1, example.world.attendance.all().size, "nothing new is stored")
    }

    // F7-AC2: "Core Burn + old code → WrongDate." / "Only Recorded writes a record."
    @Test
    fun F7_AC2_coreBurnWithTheOldCodeIsWrongDate() {
        val example = Flow7Example()

        assertEquals(AttendanceResult.WrongDate, example.record(example.coreBurn, example.oldCode))
        assertEquals(1, example.world.attendance.all().size, "nothing new is stored")
    }

    // F7-AC2: "Core Burn + "trxatt://v1/daily?coach=coach-mara&date=2026-09-28" → InvalidCode." / "Only Recorded writes a record."
    @Test
    fun F7_AC2_aLinkWithoutSigIsInvalidCode() {
        val example = Flow7Example()

        assertEquals(
            AttendanceResult.InvalidCode,
            example.record(example.coreBurn, "trxatt://v1/daily?coach=coach-mara&date=2026-09-28"),
        )
        assertEquals(
            AttendanceResult.InvalidCode,
            example.record(example.coreBurn, example.marasCode.substringBefore("&sig=")),
            "the same, with Mara's own coach id",
        )
        assertEquals(1, example.world.attendance.all().size, "nothing new is stored")
    }

    // F7-AC2: "Paolo removed from Core Burn → NotOnRoster, even with a broken code (the roster check comes first)." / "Only Recorded writes a record."
    @Test
    fun F7_AC2_offTheRosterIsNotOnRosterEvenWithABrokenCode() {
        val example = Flow7Example()
        example.world.removeFromRoster(classId = example.coreBurn.id, studentId = example.paolo.id)

        assertEquals(AttendanceResult.NotOnRoster, example.record(example.coreBurn, "not a code"))
        assertEquals(AttendanceResult.NotOnRoster, example.record(example.coreBurn, example.marasCode))
        assertEquals(1, example.world.attendance.all().size, "nothing new is stored")
    }

    // F7-AC3: "RecordAttendance built with a VerifyDailyCode whose CoachKeyRepository throws on every call still records Core Burn + Mara's code. Neither the record path … touches the key repository."
    @Test
    fun F7_AC3_aKeyStoreThatThrowsIsNeverTouchedWhenRecording() {
        val example = Flow7Example()
        val world = example.world
        val throwing = ThrowingCoachKeyRepository()
        val recordAttendance = RecordAttendance(
            classes = world.classes,
            memberships = world.memberships,
            attendance = world.attendance,
            verifyDailyCode = VerifyDailyCode(keys = throwing, clock = world.clock, timeZone = world.timeZone),
            clock = world.clock,
            timeZone = world.timeZone,
            networkStatus = { world.network },
        )

        val result = recordAttendance(studentId = example.paolo.id, session = example.refOf(example.coreBurn), code = example.marasCode)

        assertIs<AttendanceResult.Recorded>(result)
        assertEquals(0, throwing.calls, "the key repository is never called")
    }

    // F7-AC4: "Choose Core Burn for Mon 28. Move the clock to Tue 29 Sep 9:00 AM …, then record with Mara's Tue 29 code → NoSessionToday. … Nothing is stored."
    @Test
    fun F7_AC4_aChosenSessionThatIsNoLongerTodayIsNoSessionToday() {
        val example = Flow7Example()
        val chosen = example.refOf(example.coreBurn, MON_28_SEP_2026)
        example.world.setLocal(TUE_29_SEP_2026, 9, 0)

        val result = example.world.recordAttendance(
            studentId = example.paolo.id,
            session = chosen,
            code = example.code(example.mara, TUE_29_SEP_2026),
        )

        assertEquals(AttendanceResult.NoSessionToday, result)
        assertEquals(1, example.world.attendance.all().size, "nothing new is stored")
        assertTrue(example.world.attendance.all().none { it.classId == example.coreBurn.id })
    }
}
