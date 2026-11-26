package ph.project.active.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import ph.project.active.domain.model.TimeSlot
import ph.project.active.presentation.attend.RejectReason
import ph.project.active.presentation.common.UiCopy

/** Flow 7 spec section 10 copy table (typographic apostrophe U+2019, en dash U+2013, middle dot U+00B7). F7-AC14. */
class F7AttendCopyTest {
    private val mon28 = LocalDate(2026, 9, 28)
    private val coreBurnSlot = TimeSlot(LocalTime(17, 30), LocalTime(18, 30))

    // F7-AC14: "UiCopy returns exactly the copy-table strings." (Attend home, sheet and C18)
    @Test
    fun F7_AC14_attendHomeSheetAndCameraOffCopy() {
        assertEquals("Record attendance", UiCopy.recordAttendanceTitle())
        assertEquals(
            "First choose the class you\u2019re attending. Then scan the QR code on your coach\u2019s phone.",
            UiCopy.recordAttendanceBody(),
        )
        assertEquals("Choose class", UiCopy.chooseClass())
        assertEquals("Recorded today", UiCopy.recordedToday())
        assertEquals("Session 1 \u00B7 scanned 6:02 AM", UiCopy.recordedTodayLine(1, LocalTime(6, 2)))
        assertEquals("Which class are you attending?", UiCopy.chooseClassTitle())
        assertEquals("Today \u00B7 Mon 28 Sep 2026 \u00B7 choose one", UiCopy.chooseClassSubtitle(mon28))
        assertEquals("5:30 PM \u2013 6:30 PM \u00B7 Coach Mara Santos", UiCopy.choiceLine(coreBurnSlot, "Mara Santos"))
        assertEquals("Scan coach\u2019s code", UiCopy.scanCoachCode())
        assertEquals(
            "Your choice is saved with the scan. The time of the scan doesn\u2019t change it.",
            UiCopy.choiceFootnote(),
        )
        assertEquals("No classes today.", UiCopy.noClassesToday())
        assertEquals("Your scheduled classes for today show up here.", UiCopy.noClassesTodayBody())
        assertEquals("Camera access is off", UiCopy.cameraOffTitle())
        assertEquals("Allow camera access in Settings to scan your coach\u2019s code.", UiCopy.cameraOffBody())
        assertEquals("Open Settings", UiCopy.openSettings())
    }

    // F7-AC14: "UiCopy returns exactly the copy-table strings." (scanner and camera permission)
    @Test
    fun F7_AC14_scannerAndPermissionCopy() {
        assertEquals("RECORDING FOR", UiCopy.recordingFor())
        assertEquals("TRX Core Burn \u00B7 5:30 PM", UiCopy.recordingForLine("TRX Core Burn", LocalTime(17, 30)))
        assertEquals("Change", UiCopy.change())
        assertEquals("Point at the QR code on your coach\u2019s phone", UiCopy.scannerHint())
        assertEquals("It scans automatically.", UiCopy.scannerHintDetail())
        assertEquals("No connection needed. The scan is kept on this phone.", UiCopy.scannerHintOffline())
        assertEquals("Allow camera to scan", UiCopy.allowCameraTitle())
        assertEquals(
            "The camera is only used to read your coach\u2019s QR code. Nothing is recorded until a valid code is scanned.",
            UiCopy.allowCameraBody(),
        )
        assertEquals("Allow camera", UiCopy.allowCamera())
        assertEquals("The camera is only used to read your coach\u2019s QR code.", UiCopy.cameraUsageDescription())
    }

    // F7-AC14: "UiCopy returns exactly the copy-table strings." (results C12 to C15)
    @Test
    fun F7_AC14_resultCopy() {
        assertEquals("Your coach will see you on this session.", UiCopy.scanPresentBody())
        assertEquals(
            "Your attendance is stored now and uploads automatically when you\u2019re back online. The recorded time is the scan time.",
            UiCopy.scanSavedOfflineBody(),
        )
        assertEquals("Class", UiCopy.resultClass())
        assertEquals("Session", UiCopy.resultSession())
        assertEquals("Time", UiCopy.resultTime())
        assertEquals("Scanned", UiCopy.resultScanned())
        assertEquals("Upload", UiCopy.resultUpload())
        assertEquals("First scan", UiCopy.resultFirstScan())
        assertEquals("Session 1 \u00B7 Mon 28 Sep 2026", UiCopy.resultSessionLine(1, mon28))
        assertEquals("5:31 PM (recorded time)", UiCopy.recordedTimeLine(LocalTime(17, 31)))
        assertEquals("Waiting for connection", UiCopy.waitingForConnection())
        assertEquals("Done", UiCopy.done())
        assertEquals("Record another class", UiCopy.recordAnotherClass())
        assertEquals("Choose a different class", UiCopy.chooseDifferentClass())
        assertEquals("You chose", UiCopy.resultYouChose())
        assertEquals("Today", UiCopy.resultToday())
        assertEquals("Code date", UiCopy.resultCodeDate())
        assertEquals("Scan again", UiCopy.scanAgain())
        assertEquals("Close", UiCopy.close())
    }

    // F7-AC14 / C15 table: "rejectLine(reason) + scanRejectedTail()", one line per reason
    @Test
    fun F7_AC14_oneRejectLinePerReason() {
        assertEquals("This isn\u2019t a valid attendance code.", UiCopy.invalidCode())
        assertEquals("This class has no session today.", UiCopy.noSessionToday())
        assertEquals("This class date isn\u2019t on this phone yet.", UiCopy.sessionNotOnPhone())
        val lines = mapOf(
            RejectReason.InvalidCode to "This isn\u2019t a valid attendance code.",
            RejectReason.WrongCoach to "This code isn\u2019t from your coach for this class.",
            RejectReason.WrongDate(LocalDate(2026, 9, 21)) to "This code is for a different date.",
            RejectReason.WrongDate(null) to "This code is for a different date.",
            RejectReason.NoSessionToday to "This class has no session today.",
            RejectReason.NotOnRoster to "You\u2019re not on this class roster.",
            RejectReason.SessionCancelled to "This date is cancelled.",
            RejectReason.ClassCancelled to "This class has been cancelled.",
            RejectReason.SessionUnknown to "This class date isn\u2019t on this phone yet.",
        )
        for ((reason, line) in lines) {
            assertEquals(line, UiCopy.rejectLine(reason), "rejectLine($reason)")
            assertEquals("$line Nothing was saved.", UiCopy.rejectLine(reason) + UiCopy.scanRejectedTail())
        }
    }

    // F7-AC14: "UiCopy returns exactly the copy-table strings." (Sessions C5 / C5b)
    @Test
    fun F7_AC14_sessionsCopy() {
        assertEquals("Your dates for joined classes", UiCopy.sessionsSubtitle())
        assertEquals("Today \u00B7 Mon 28 Sep", UiCopy.sessionsTodayHeader(mon28))
        assertEquals("Earlier", UiCopy.earlier())
        assertEquals("Session 2 \u00B7 12:15 PM", UiCopy.sessionRowLine(2, LocalTime(12, 15)))
        assertEquals("MON", UiCopy.dateTileWeekday(mon28))
        assertEquals("SEP", UiCopy.dateTileMonth(mon28))
        assertEquals("WED", UiCopy.dateTileWeekday(LocalDate(2026, 9, 30)))
        assertEquals("OCT", UiCopy.dateTileMonth(LocalDate(2026, 10, 5)))
        assertEquals("No sessions yet", UiCopy.noSessionsYetTitle())
        assertEquals("When you join a class, its dates show up here.", UiCopy.noSessionsYetBody())
    }

    // F7-AC14: "Existing strings are unchanged." (section 10, "Existing, unchanged and used here")
    @Test
    fun F7_AC14_existingStringsAreUnchanged() {
        assertEquals("TRX ATTENDANCE", UiCopy.largeTitleEyebrow())
        assertEquals("Attend", UiCopy.tabAttend())
        assertEquals("Sessions", UiCopy.tabSessions())
        assertEquals("Upcoming", UiCopy.upcoming())
        assertEquals("Present", UiCopy.present())
        assertEquals("Absent", UiCopy.absent())
        assertEquals("Cancelled", UiCopy.cancelled())
        assertEquals("Upload pending", UiCopy.uploadPending())
        assertEquals("TODAY", UiCopy.today())
        assertEquals("No internet connection", UiCopy.offlineBanner())
        assertEquals("Scans are saved on this phone and upload when you are back online.", UiCopy.scannerOfflineSubtitle())
        assertEquals("You\u2019re marked Present", UiCopy.scanPresentTitle())
        assertEquals("Saved on this phone", UiCopy.scanSavedOfflineTitle())
        assertEquals("You already have attendance", UiCopy.scanAlreadyRecordedTitle())
        assertEquals(
            "Sunrise TRX Strength on Mon 28 Sep 2026 is already recorded. No new record was added.",
            UiCopy.scanAlreadyRecordedBody("Sunrise TRX Strength", mon28),
        )
        assertEquals("Scan not accepted", UiCopy.scanRejectedTitle())
        assertEquals(" Nothing was saved.", UiCopy.scanRejectedTail())
        assertEquals("This code is for a different date.", UiCopy.wrongDate())
        assertEquals("This code isn\u2019t from your coach for this class.", UiCopy.wrongCoach())
        assertEquals("You\u2019re not on this class roster.", UiCopy.notOnRoster())
        assertEquals("This date is cancelled.", UiCopy.sessionCancelled())
        assertEquals("This class has been cancelled.", UiCopy.classCancelledBody())
    }
}
