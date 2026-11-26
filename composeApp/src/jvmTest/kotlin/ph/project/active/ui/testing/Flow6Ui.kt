package ph.project.active.ui.testing

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.navigation.NavHostController
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import ph.project.active.domain.model.AttendanceRecord
import ph.project.active.domain.model.TrxClass
import ph.project.active.domain.model.UploadState
import ph.project.active.domain.model.User

/**
 * Builds the flow 6 D22 picture on top of [seedD19]: rosters in join order (Sunrise = Paolo, Dana, Hana;
 * Core Burn = Paolo, Dana, Gia; Lunch Express empty), and Mon 28 Sep scans for Paolo on Sunrise at 6:02 AM,
 * Dana on Sunrise at 6:05 AM and Paolo on Core Burn at 5:31 PM. Ends on the D22 clock: Mon 28 Sep 2026, 5:40 PM.
 */
fun Flow4Harness.seedD22(): Flow6Example {
    val d19 = seedD19()
    val paolo = student("Paolo Reyes", "EMP-20417")
    val dana = student("Dana Uy", "EMP-20155")
    val hana = student("Hana Cho", "EMP-20877")
    val gia = student("Gia Torres", "EMP-20249")
    joinByRequest(d19.sunrise.id, paolo)
    joinByRequest(d19.sunrise.id, dana)
    joinByRequest(d19.sunrise.id, hana)
    joinByRequest(d19.coreBurn.id, paolo)
    joinByRequest(d19.coreBurn.id, dana)
    joinByRequest(d19.coreBurn.id, gia)
    scanAt(d19.sunrise, paolo, MON_28_SEP_2026, 6, 2)
    scanAt(d19.sunrise, dana, MON_28_SEP_2026, 6, 5)
    scanAt(d19.coreBurn, paolo, MON_28_SEP_2026, 17, 31)
    setNow(MON_28_SEP_2026, 17, 40)
    return Flow6Example(
        mara = d19.mara,
        sunrise = d19.sunrise,
        lunchExpress = d19.lunchExpress,
        coreBurn = d19.coreBurn,
        paolo = paolo,
        dana = dana,
        hana = hana,
        gia = gia,
        sunriseSessionId = sessionIdOn(d19.sunrise, MON_28_SEP_2026),
        lunchExpressSessionId = sessionIdOn(d19.lunchExpress, MON_28_SEP_2026),
        coreBurnSessionId = sessionIdOn(d19.coreBurn, MON_28_SEP_2026),
    )
}

/** The D21 picture: D22 plus Dana on Core Burn at 5:33 PM on Mon 28 Sep, on the clock Tue 29 Sep 2026, 9:41 AM. */
fun Flow4Harness.seedD21(): Flow6Example {
    val d22 = seedD22()
    scanAt(d22.coreBurn, d22.dana, MON_28_SEP_2026, 17, 33)
    setNow(TUE_29_SEP_2026, 9, 41)
    return d22
}

/** Saves a scan straight to the phone's store (already uploaded), at [hour]:[minute] Asia/Manila on [date]. */
fun Flow4Harness.scanAt(trxClass: TrxClass, student: User, date: LocalDate, hour: Int, minute: Int): AttendanceRecord {
    val record = AttendanceRecord(
        id = attendance.nextId(),
        sessionId = sessionIdOn(trxClass, date),
        classId = trxClass.id,
        studentId = student.id,
        scannedAt = LocalDateTime(date.year, date.month, date.day, hour, minute).toInstant(timeZone),
        uploadState = UploadState.SYNCED,
    )
    attendance.add(record)
    return record
}

fun Flow4Harness.sessionIdOn(trxClass: TrxClass, date: LocalDate): String =
    classes.sessionsOf(trxClass.id).single { it.date == date }.id

fun sectionTag(sessionId: String): String = "coach_attendance_section_$sessionId"

fun rowTag(sessionId: String, student: User): String = "coach_attendance_row_${sessionId}_${student.id}"

/** Signs in as Mara, mounts the app (flow 5 mount, which passes the Attendance factory), and opens Attendance. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.openCoachAttendance(
    world: Flow4Harness,
    lifecycle: HostLifecycle = HostLifecycle(),
): NavHostController {
    world.signInAsMara()
    val nav = mountFlow5(world, lifecycle)
    openCoachTab("attendance")
    return nav
}

/** True when some node showing exactly [text] sits on the same line as the node tagged [tagName] (a chip beside a count). */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.assertTextBeside(text: String, tagName: String) {
    val anchor = tag(tagName).fetchSemanticsNode().boundsInRoot
    val beside = onAllNodes(hasText(text), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false)
        .any { it.boundsInRoot.overlapsVertically(anchor) }
    assertTrue(beside, "expected a \"$text\" chip on the same line as $tagName")
}

/** Nodes whose tag starts with [prefix] inside the node tagged [parentTag]. */
@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.countTagPrefixUnder(parentTag: String, prefix: String): Int =
    onAllNodes(hasTestTagPrefix(prefix) and hasAnyAncestor(hasTestTag(parentTag)), useUnmergedTree = true)
        .fetchSemanticsNodes(atLeastOneRootRequired = false).size

private fun Rect.overlapsVertically(other: Rect): Boolean = top < other.bottom && other.top < bottom
