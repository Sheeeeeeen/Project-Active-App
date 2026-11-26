package ph.project.active.domain

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.AttendanceResult
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.SessionRef
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.inmemory.InMemoryCoachKeyRepository
import ph.project.active.domain.usecase.GenerateDailyCode

/**
 * Flow 7 example data (spec section 7) on the Phase 1 in-memory world.
 * Coaches Mara Santos and Leo Ramirez, student Paolo Reyes (EMP-20417). Four classes, all created on
 * Mon 28 Sep 2026, Paolo ACTIVE on all four: Sunrise (Mara, Mon/Wed/Fri 6:00 AM), Lunch Express (Mara,
 * Mon/Tue/Thu 12:15 PM, cancelled on Mon 28 Sep only), Core Burn (Mara, Mon/Wed 5:30 PM) and Mobility Flow
 * (Leo, Mon/Thu 7:00 PM). One record: Paolo on Sunrise Mon 28 at 6:02 AM, SYNCED.
 * Default clock: Mon 28 Sep 2026, 5:31 PM, Asia/Manila, online.
 *
 * The daily codes are made on the coach side, with keys in [coachSideKeys] only. The student's world
 * ([world].coachKeys) holds no coach keys.
 */
class Flow7Example(val world: TestWorld = TestWorld(start = MON_28_SEP_2026.atTime(5, 0))) {
    val coachSideKeys = InMemoryCoachKeyRepository()
    private val coachSide = GenerateDailyCode(keys = coachSideKeys)

    val mara: User = world.registerCoach(name = "Mara Santos", email = "mara@company.com", password = "password1")
    val leo: User = world.registerCoach(name = "Leo Ramirez", email = "leo@company.com", password = "password1")
    val paolo: User = world.registerStudent(
        name = "Paolo Reyes",
        email = "paolo@company.com",
        password = "password1",
        employeeId = "EMP-20417",
        requestToJoinClassId = null,
    )
    val sunrise: TrxClass = save(mara, "Sunrise TRX Strength", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), slot(6, 0, 6, 45))
    val lunchExpress: TrxClass = save(mara, "Lunch Express TRX", listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), slot(12, 15, 12, 45))
    val coreBurn: TrxClass = save(mara, "TRX Core Burn", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30))
    val mobilityFlow: TrxClass = save(leo, "TRX Mobility Flow", listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), slot(19, 0, 20, 0))

    init {
        world.cancelDate(lunchExpress.id, MON_28_SEP_2026)
        for (trxClass in listOf(sunrise, lunchExpress, coreBurn, mobilityFlow)) world.makeActive(trxClass.id, paolo.id)
        coachSideKeys.save(mara.id, Ed25519KeyPair.generate())
        coachSideKeys.save(leo.id, Ed25519KeyPair.generate())
        world.attendance.add(
            AttendanceRecord(
                id = world.attendance.nextId(),
                sessionId = sessionOf(sunrise).id,
                classId = sunrise.id,
                studentId = paolo.id,
                scannedAt = MON_28_SEP_2026.atTime(6, 2).toInstant(world.timeZone),
                uploadState = UploadState.SYNCED,
            ),
        )
        world.setLocal(MON_28_SEP_2026, 17, 31)
    }

    /** "Mara's code": Mara's daily code for Mon 28 Sep 2026. */
    val marasCode: String get() = code(mara, MON_28_SEP_2026)

    /** "Old code": Mara's code for Mon 21 Sep 2026. */
    val oldCode: String get() = code(mara, MON_21_SEP_2026)

    /** "Leo's code": Leo's code for Mon 28 Sep 2026. */
    val leosCode: String get() = code(leo, MON_28_SEP_2026)

    fun code(coach: User, date: LocalDate): String = coachSide(coachId = coach.id, date = date)

    fun sessionOf(trxClass: TrxClass, date: LocalDate = MON_28_SEP_2026): Session =
        world.classes.sessionsOf(trxClass.id).single { it.date == date }

    fun refOf(trxClass: TrxClass, date: LocalDate = MON_28_SEP_2026): SessionRef = world.refOf(sessionOf(trxClass, date))

    /** Paolo chooses [trxClass] on [date] and scans [code]. */
    fun record(trxClass: TrxClass, code: String, date: LocalDate = MON_28_SEP_2026): AttendanceResult =
        world.recordAttendance(studentId = paolo.id, session = refOf(trxClass, date), code = code)

    private fun save(coach: User, name: String, weekdays: List<DayOfWeek>, time: TimeSlot): TrxClass = world.createClass(
        coachId = coach.id,
        name = name,
        visibility = Visibility.PUBLIC,
        slotsByWeekday = weekdays.associateWith { listOf(time) },
        endDate = null,
    )

    private fun slot(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot =
        TimeSlot(LocalTime(startHour, startMinute), LocalTime(endHour, endMinute))
}

val MON_21_SEP_2026 = LocalDate(2026, 9, 21)
