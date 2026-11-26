package ph.project.active.testkit

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toInstant
import ph.project.active.data.model.SignedInAccount
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.Session
import ph.project.active.domain.model.TimeSlot
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.User
import ph.project.active.domain.model.Visibility

/**
 * Flow 6 example data (spec section 7), on top of the flow 5 D19 classes. All three classes are created on
 * Mon 28 Sep 2026 at 9:00 AM, so each Mon 28 Sep session is "Session 1". Lunch Express is cancelled on
 * Mon 28 Sep only and has no roster. Rosters in join order: Sunrise = Paolo, Dana, Hana; Core Burn = Paolo,
 * Dana, Gia. Mon 28 Sep records: Paolo on Sunrise at 6:02 AM, Dana on Sunrise at 6:05 AM, Paolo on Core Burn
 * at 5:31 PM. The clock ends on the D22 clock: Mon 28 Sep 2026, 5:40 PM, Asia/Manila.
 */
class Flow6Example(val world: Phase2World) {
    val mara: User
    val sunrise: TrxClass
    val lunchExpress: TrxClass
    val coreBurn: TrxClass
    val paolo: User
    val dana: User
    val hana: User
    val gia: User

    init {
        world.setLocal(MON_28_SEP_2026, 9)
        mara = world.registerCoach(name = "Mara Santos", email = "mara@company.com", password = "password1")
            .also { world.remember(it) }
        coreBurn = save("TRX Core Burn", listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), slot(17, 30, 18, 30))
        lunchExpress = save(
            "Lunch Express TRX",
            listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            slot(12, 15, 12, 45),
        )
        sunrise = save(
            "Sunrise TRX Strength",
            listOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            slot(6, 0, 6, 45),
        )
        world.cancelDate(lunchExpress.id, MON_28_SEP_2026)

        paolo = student("Paolo Reyes", "EMP-20417")
        dana = student("Dana Uy", "EMP-20155")
        hana = student("Hana Cho", "EMP-20877")
        gia = student("Gia Torres", "EMP-20249")
        world.makeActive(sunrise.id, paolo.id)
        world.makeActive(sunrise.id, dana.id)
        world.makeActive(sunrise.id, hana.id)
        world.makeActive(coreBurn.id, paolo.id)
        world.makeActive(coreBurn.id, dana.id)
        world.makeActive(coreBurn.id, gia.id)

        scan(paolo, sunrise, MON_28_SEP_2026, 6, 2)
        scan(dana, sunrise, MON_28_SEP_2026, 6, 5)
        scan(paolo, coreBurn, MON_28_SEP_2026, 17, 31)
        world.setLocal(MON_28_SEP_2026, 17, 40)
    }

    val account: SignedInAccount get() = mara.toAccount()

    /** The class's session on [date] (each example class has one slot per day). */
    fun sessionOf(trxClass: TrxClass, date: LocalDate = MON_28_SEP_2026): Session =
        world.classes.sessionsOf(trxClass.id).single { it.date == date }

    /** Saves a scan straight to the store (already uploaded), at [hour]:[minute] on [date] in Asia/Manila. */
    fun scan(student: User, trxClass: TrxClass, date: LocalDate, hour: Int, minute: Int): AttendanceRecord {
        val session = sessionOf(trxClass, date)
        val record = AttendanceRecord(
            id = world.attendance.nextId(),
            sessionId = session.id,
            classId = trxClass.id,
            studentId = student.id,
            scannedAt = LocalDateTime(date, LocalTime(hour, minute)).toInstant(world.timeZone),
            uploadState = UploadState.SYNCED,
        )
        world.attendance.add(record)
        return record
    }

    /** The D21 picture: Dana also scanned into Core Burn at 5:33 PM, and it is now Tue 29 Sep 2026, 9:41 AM. */
    fun moveToD21() {
        scan(dana, coreBurn, MON_28_SEP_2026, 17, 33)
        world.setLocal(TUE_29_SEP_2026, 9, 41)
    }

    private fun save(name: String, weekdays: List<DayOfWeek>, time: TimeSlot): TrxClass = world.createClass(
        coachId = mara.id,
        name = name,
        visibility = Visibility.PUBLIC,
        slotsByWeekday = weekdays.associateWith { listOf(time) },
        endDate = null,
    )

    private fun student(name: String, employeeId: String): User = world.registerStudent(
        name = name,
        email = "${name.lowercase().replace(' ', '.')}@acme.test",
        password = "password1",
        employeeId = employeeId,
        requestToJoinClassId = null,
    ).also { world.remember(it) }

    private fun slot(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimeSlot =
        TimeSlot(LocalTime(startHour, startMinute), LocalTime(endHour, endMinute))
}
