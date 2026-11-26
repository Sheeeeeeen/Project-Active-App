package ph.project.active.testkit

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.crypto.Ed25519KeyPair
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility
import ph.project.active.domain.repository.inmemory.InMemoryCoachKeyRepository
import ph.project.active.domain.usecase.GenerateDailyCode

/**
 * Flow 7 example data (spec section 7) on one SQL world. Coaches Mara Santos and Leo Ramirez, student
 * Paolo Reyes (EMP-20417). Four classes created on Mon 28 Sep 2026, Paolo ACTIVE on all four:
 * Sunrise (Mara, Mon/Wed/Fri 6:00–6:45 AM), Lunch Express (Mara, Mon/Tue/Thu 12:15–12:45 PM, cancelled on
 * Mon 28 Sep only), Core Burn (Mara, Mon/Wed 5:30–6:30 PM) and Mobility Flow (Leo, Mon/Thu 7:00–8:00 PM).
 * One record: Paolo on Sunrise Mon 28 at 6:02 AM, SYNCED. Clock: Mon 28 Sep 2026, 5:31 PM, Asia/Manila, online.
 * Codes are made on the coach side ([coachSideKeys]); the world's own key store stays empty.
 */
class Flow7Example(val world: Phase2World) {
    val coachSideKeys = InMemoryCoachKeyRepository()
    private val coachSide = GenerateDailyCode(keys = coachSideKeys)
    val mara: User
    val leo: User
    val paolo: User
    val sunrise: TrxClass
    val lunchExpress: TrxClass
    val coreBurn: TrxClass
    val mobilityFlow: TrxClass

    init {
        world.setLocal(MON_28_SEP_2026, 5)
        mara = world.registerCoach(name = "Mara Santos", email = "mara@company.com", password = "password1")
            .also { world.remember(it) }
        leo = world.registerCoach(name = "Leo Ramirez", email = "leo@company.com", password = "password1")
            .also { world.remember(it) }
        paolo = world.registerStudent(
            name = "Paolo Reyes",
            email = "paolo@company.com",
            password = "password1",
            employeeId = "EMP-20417",
            requestToJoinClassId = null,
        ).also { world.remember(it) }
        sunrise = save(mara, "Sunrise TRX Strength", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), slot(6, 0, 6, 45))
        lunchExpress = save(mara, "Lunch Express TRX", listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), slot(12, 15, 12, 45))
        coreBurn = save(mara, "TRX Core Burn", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30))
        mobilityFlow = save(leo, "TRX Mobility Flow", listOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), slot(19, 0, 20, 0))
        world.cancelDate(lunchExpress.id, MON_28_SEP_2026)
        for (trxClass in listOf(sunrise, lunchExpress, coreBurn, mobilityFlow)) world.makeActive(trxClass.id, paolo.id)
        coachSideKeys.save(mara.id, Ed25519KeyPair.generate())
        coachSideKeys.save(leo.id, Ed25519KeyPair.generate())
        scan(sunrise, MON_28_SEP_2026, 6, 2, UploadState.SYNCED)
        world.setLocal(MON_28_SEP_2026, 17, 31)
    }

    val account: SignedInAccount get() = paolo.toAccount()

    /** "Mara's code": Mara's daily code for Mon 28 Sep 2026. */
    val marasCode: String get() = code(mara, MON_28_SEP_2026)

    fun code(coach: User, date: LocalDate): String = coachSide(coachId = coach.id, date = date)

    fun sessionOf(trxClass: TrxClass, date: LocalDate = MON_28_SEP_2026): Session =
        world.classes.sessionsOf(trxClass.id).single { it.date == date }

    /** Saves Paolo's scan straight to the store at [hour]:[minute] Asia/Manila on [date]. */
    fun scan(trxClass: TrxClass, date: LocalDate, hour: Int, minute: Int, uploadState: UploadState): AttendanceRecord {
        val record = AttendanceRecord(
            id = world.attendance.nextId(),
            sessionId = sessionOf(trxClass, date).id,
            classId = trxClass.id,
            studentId = paolo.id,
            scannedAt = LocalDateTime(date, LocalTime(hour, minute)).toInstant(world.timeZone),
            uploadState = uploadState,
        )
        world.attendance.add(record)
        return record
    }

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
