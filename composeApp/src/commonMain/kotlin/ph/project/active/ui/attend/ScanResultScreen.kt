package ph.project.active.ui.attend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.attend.RecordingFor
import ph.project.active.presentation.attend.RejectReason
import ph.project.active.presentation.attend.ScanResult
import ph.project.active.presentation.common.ConnectionUi
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.common.formatSlot
import ph.project.active.presentation.common.formatTime
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.ScanHaptics
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.circle_check
import projectactive.composeapp.generated.resources.hourglass
import projectactive.composeapp.generated.resources.x
import kotlin.time.Instant

@Composable
fun ScanResultScreen(
    result: ScanResult,
    recordingFor: RecordingFor?,
    today: LocalDate,
    connection: ConnectionUi,
    timeZone: TimeZone,
    haptics: ScanHaptics,
    onDone: () -> Unit,
    onRecordAnother: () -> Unit,
    onChooseDifferentClass: () -> Unit,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(result) {
        if (result is ScanResult.Present || result is ScanResult.SavedOffline) haptics.success()
    }
    val close = if (result is ScanResult.Rejected) onDone else onDone
    ScanBackHandler(if (result is ScanResult.Rejected) onDone else onDone)
    val colors = LocalTrxColors.current
    val root = when (result) {
        is ScanResult.Present -> AttendTestTags.resultPresent
        is ScanResult.SavedOffline -> AttendTestTags.resultSavedOffline
        is ScanResult.AlreadyRecorded -> AttendTestTags.resultAlready
        is ScanResult.Rejected -> AttendTestTags.resultRejected
    }
    Column(
        modifier
            .fillMaxSize()
            .background(colors.canvas)
            .verticalScroll(rememberScrollState())
            .testTag(root)
            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s8),
    ) {
        IconButton(onClick = close) {
            Icon(painter = painterResource(Res.drawable.x), contentDescription = UiCopy.close(), tint = colors.textPrimary)
        }
        val banner = connection.banner
        if (result is ScanResult.SavedOffline && banner != null) {
            OfflineBanner(banner, Modifier.padding(bottom = TrxSpacing.s16))
        }
        when (result) {
            is ScanResult.Present -> PresentBody(result, timeZone, onDone, onRecordAnother)
            is ScanResult.SavedOffline -> SavedBody(result, timeZone, onDone)
            is ScanResult.AlreadyRecorded -> AlreadyBody(result, timeZone, onDone, onChooseDifferentClass)
            is ScanResult.Rejected -> RejectedBody(result, recordingFor, today, onScanAgain, onDone)
        }
    }
}

@Composable
private fun PresentBody(
    result: ScanResult.Present,
    timeZone: TimeZone,
    onDone: () -> Unit,
    onRecordAnother: () -> Unit,
) {
    val colors = LocalTrxColors.current
    Text(UiCopy.scanPresentTitle(), color = colors.textPrimary, style = TrxTypography.title)
    Text(
        text = UiCopy.scanPresentBody(),
        color = colors.textSecondary,
        style = TrxTypography.subhead,
        modifier = Modifier.padding(top = TrxSpacing.s8, bottom = TrxSpacing.s16),
    )
    StatusChip(UiCopy.present(), Res.drawable.circle_check, colors.successBg, colors.successFg)
    ResultLine(UiCopy.resultClass(), result.className, null)
    ResultLine(UiCopy.resultSession(), UiCopy.resultSessionLine(result.sessionNumber, result.date), null)
    ResultLine(UiCopy.resultTime(), formatSlot(result.slot), null)
    ResultLine(UiCopy.resultScanned(), formatTime(result.scannedAt.localTime(timeZone)), null)
    TrxLargeButton(
        label = UiCopy.done(),
        kind = TrxButtonKind.Primary,
        tag = AttendTestTags.resultDone,
        onClick = onDone,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s24),
    )
    TrxLargeButton(
        label = UiCopy.recordAnotherClass(),
        kind = TrxButtonKind.Secondary,
        tag = AttendTestTags.resultRecordAnother,
        onClick = onRecordAnother,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s12),
    )
}

@Composable
private fun SavedBody(result: ScanResult.SavedOffline, timeZone: TimeZone, onDone: () -> Unit) {
    val colors = LocalTrxColors.current
    Text(UiCopy.scanSavedOfflineTitle(), color = colors.textPrimary, style = TrxTypography.title)
    Text(
        text = UiCopy.scanSavedOfflineBody(),
        color = colors.textSecondary,
        style = TrxTypography.subhead,
        modifier = Modifier.padding(top = TrxSpacing.s8, bottom = TrxSpacing.s16),
    )
    Row {
        StatusChip(UiCopy.present(), Res.drawable.circle_check, colors.successBg, colors.successFg)
        StatusChip(
            UiCopy.uploadPending(),
            Res.drawable.hourglass,
            colors.warningBg,
            colors.warningFg,
            Modifier.padding(start = TrxSpacing.s8),
        )
    }
    ResultLine(UiCopy.resultClass(), result.className, null)
    ResultLine(UiCopy.resultSession(), UiCopy.resultSessionLine(result.sessionNumber, result.date), null)
    ResultLine(UiCopy.resultScanned(), UiCopy.recordedTimeLine(result.scannedAt.localTime(timeZone)), null)
    ResultLine(UiCopy.resultUpload(), UiCopy.waitingForConnection(), null)
    TrxLargeButton(
        label = UiCopy.done(),
        kind = TrxButtonKind.Primary,
        tag = AttendTestTags.resultDone,
        onClick = onDone,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s24),
    )
}

@Composable
private fun AlreadyBody(
    result: ScanResult.AlreadyRecorded,
    timeZone: TimeZone,
    onDone: () -> Unit,
    onChooseDifferent: () -> Unit,
) {
    val colors = LocalTrxColors.current
    Text(UiCopy.scanAlreadyRecordedTitle(), color = colors.textPrimary, style = TrxTypography.title)
    Text(
        text = UiCopy.scanAlreadyRecordedBody(result.className, result.date),
        color = colors.textSecondary,
        style = TrxTypography.subhead,
        modifier = Modifier.padding(top = TrxSpacing.s8, bottom = TrxSpacing.s16),
    )
    StatusChip(UiCopy.present(), Res.drawable.circle_check, colors.successBg, colors.successFg)
    ResultLine(UiCopy.resultClass(), result.className, null)
    ResultLine(UiCopy.resultSession(), UiCopy.resultSessionLine(result.sessionNumber, result.date), null)
    ResultLine(UiCopy.resultFirstScan(), formatTime(result.firstScannedAt.localTime(timeZone)), null)
    TrxLargeButton(
        label = UiCopy.done(),
        kind = TrxButtonKind.Primary,
        tag = AttendTestTags.resultDone,
        onClick = onDone,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s24),
    )
    TrxLargeButton(
        label = UiCopy.chooseDifferentClass(),
        kind = TrxButtonKind.Secondary,
        tag = AttendTestTags.resultChooseDifferent,
        onClick = onChooseDifferent,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s12),
    )
}

@Composable
private fun RejectedBody(
    result: ScanResult.Rejected,
    recordingFor: RecordingFor?,
    today: LocalDate,
    onScanAgain: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = LocalTrxColors.current
    Text(UiCopy.scanRejectedTitle(), color = colors.textPrimary, style = TrxTypography.title)
    Text(
        text = UiCopy.rejectLine(result.reason) + UiCopy.scanRejectedTail(),
        color = colors.textSecondary,
        style = TrxTypography.subhead,
        modifier = Modifier.padding(top = TrxSpacing.s8, bottom = TrxSpacing.s16).testTag(AttendTestTags.resultMessage),
    )
    if (recordingFor != null) {
        ResultLine(UiCopy.resultYouChose(), UiCopy.recordingForLine(recordingFor.className, recordingFor.slot.start), AttendTestTags.resultYouChose)
    }
    ResultLine(UiCopy.resultToday(), formatDate(today), AttendTestTags.resultToday)
    val codeDate = (result.reason as? RejectReason.WrongDate)?.codeDate
    if (codeDate != null) {
        ResultLine(UiCopy.resultCodeDate(), formatDate(codeDate), AttendTestTags.resultCodeDate)
    }
    TrxLargeButton(
        label = UiCopy.scanAgain(),
        kind = TrxButtonKind.Primary,
        tag = AttendTestTags.resultScanAgain,
        onClick = onScanAgain,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s24),
    )
    TrxLargeButton(
        label = UiCopy.close(),
        kind = TrxButtonKind.Secondary,
        tag = AttendTestTags.resultClose,
        onClick = onClose,
        enabled = true,
        modifier = Modifier.padding(top = TrxSpacing.s12),
    )
}

@Composable
private fun ResultLine(label: String, value: String, valueTag: String?) {
    val colors = LocalTrxColors.current
    Column(Modifier.fillMaxWidth().padding(top = TrxSpacing.s16)) {
        Text(label, color = colors.textTertiary, style = TrxTypography.footnote)
        Text(
            text = value,
            color = colors.textPrimary,
            style = TrxTypography.headline,
            modifier = if (valueTag == null) Modifier else Modifier.testTag(valueTag),
        )
    }
}

private fun Instant.localTime(timeZone: TimeZone) = toLocalDateTime(timeZone).time
