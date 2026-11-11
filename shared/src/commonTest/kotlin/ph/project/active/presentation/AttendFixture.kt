package ph.project.active.presentation

import ph.project.active.data.model.SignedInAccount
import ph.project.active.testkit.EVENING
import ph.project.active.testkit.MON_28_SEP_2026
import ph.project.active.testkit.MORNING
import ph.project.active.testkit.Phase2World
import ph.project.active.testkit.toAccount

class AttendFixture(
    val world: Phase2World,
    val student: SignedInAccount,
    val coachId: String,
    val classId: String,
    val sessionId: String,
)

fun readyClass(twoSlots: Boolean = false): AttendFixture {
    val world = Phase2World()
    val coach = world.coach()
    world.giveKeys(coach.id)
    val trxClass = if (twoSlots) world.createTwoSlotMondayClass(coach.id) else world.createMonWedEveningClass(coach.id)
    val student = world.student()
    world.makeActive(trxClass.id, student.id)
    world.setLocal(MON_28_SEP_2026, 17, 40)
    val session = world.sessionOn(trxClass.id, MON_28_SEP_2026, if (twoSlots) MORNING else EVENING)
    return AttendFixture(world, student.toAccount(), coach.id, trxClass.id, session.id)
}
