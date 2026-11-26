package ph.project.active.ui.attend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.attend.RecordingFor
import ph.project.active.presentation.attend.looksLikeAttendanceCode
import ph.project.active.presentation.common.UiCopy
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxCompactButton
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.CameraAccess
import ph.project.active.ui.platform.CameraPermission
import ph.project.active.ui.platform.QrScanner
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.qr_code
import projectactive.composeapp.generated.resources.x
import ph.project.active.presentation.common.ConnectionUi

@Composable
fun ScannerScreen(
    recordingFor: RecordingFor,
    connection: ConnectionUi,
    scanner: QrScanner,
    permission: CameraPermission,
    access: CameraAccess,
    onAccess: (CameraAccess) -> Unit,
    onChange: () -> Unit,
    onClose: () -> Unit,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScanBackHandler(onClose)
    val granted = access == CameraAccess.GRANTED
    if (granted) {
        ScannerPreview(
            recordingFor = recordingFor,
            connection = connection,
            scanner = scanner,
            onChange = onChange,
            onClose = onClose,
            onCodeScanned = onCodeScanned,
            modifier = modifier,
        )
    } else {
        CameraPermissionPrompt(
            recordingFor = recordingFor,
            access = access,
            permission = permission,
            onAccess = onAccess,
            onChange = onChange,
            onClose = onClose,
            modifier = modifier,
        )
    }
}

@Composable
private fun ScannerPreview(
    recordingFor: RecordingFor,
    connection: ConnectionUi,
    scanner: QrScanner,
    onChange: () -> Unit,
    onClose: () -> Unit,
    onCodeScanned: (String) -> Unit,
    modifier: Modifier,
) {
    val colors = LocalTrxColors.current
    var torchOn by remember { mutableStateOf(false) }
    val taken = remember { booleanArrayOf(false) }
    val offline = connection.isOffline
    Column(modifier.fillMaxSize().background(colors.surfaceInverse).testTag(AttendTestTags.scanner)) {
        ScannerChrome(
            recordingFor = recordingFor,
            showTorch = scanner.hasTorch,
            torchOn = torchOn,
            onTorch = { torchOn = !torchOn },
            onChange = onChange,
            onClose = onClose,
            light = true,
        )
        if (offline) {
            OfflineBanner(
                message = UiCopy.offlineBanner(),
                subtitle = UiCopy.scannerOfflineSubtitle(),
            )
        }
        scanner.Preview(
            torchOn = torchOn,
            onText = { text ->
                if (taken[0] || !looksLikeAttendanceCode(text)) return@Preview
                taken[0] = true
                onCodeScanned(text)
            },
            modifier = Modifier.fillMaxWidth().height(280.dp),
        )
        Text(
            text = UiCopy.scannerHint(),
            color = colors.textInverse,
            style = TrxTypography.headline,
            modifier = Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16).testTag(AttendTestTags.scannerHint),
        )
        Text(
            text = if (offline) UiCopy.scannerHintOffline() else UiCopy.scannerHintDetail(),
            color = colors.textInverse,
            style = TrxTypography.subhead,
            modifier = Modifier.padding(horizontal = TrxSpacing.screenHorizontal).testTag(AttendTestTags.scannerHintDetail),
        )
    }
}

@Composable
private fun CameraPermissionPrompt(
    recordingFor: RecordingFor,
    access: CameraAccess,
    permission: CameraPermission,
    onAccess: (CameraAccess) -> Unit,
    onChange: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier,
) {
    val colors = LocalTrxColors.current
    Column(modifier.fillMaxSize().background(colors.surfaceInverse).testTag(AttendTestTags.cameraPermission)) {
        ScannerChrome(
            recordingFor = recordingFor,
            showTorch = false,
            torchOn = false,
            onTorch = {},
            onChange = onChange,
            onClose = onClose,
            light = true,
        )
        Column(
            Modifier.fillMaxWidth().padding(TrxSpacing.screenHorizontal).padding(top = TrxSpacing.s32),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(Res.drawable.qr_code),
                contentDescription = null,
                tint = colors.textInverse,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = UiCopy.allowCameraTitle(),
                color = colors.textInverse,
                style = TrxTypography.sectionTitle,
                modifier = Modifier.padding(top = TrxSpacing.s16),
            )
            Text(
                text = UiCopy.allowCameraBody(),
                color = colors.textInverse,
                style = TrxTypography.subhead,
                modifier = Modifier.padding(top = TrxSpacing.s8),
            )
            TrxLargeButton(
                label = UiCopy.allowCamera(),
                kind = TrxButtonKind.Primary,
                tag = AttendTestTags.cameraAllow,
                onClick = {
                    permission.request { answer ->
                        onAccess(answer)
                        if (answer == CameraAccess.DENIED_FOR_GOOD) onChange()
                    }
                },
                enabled = true,
                modifier = Modifier.padding(top = TrxSpacing.s24),
            )
            if (access == CameraAccess.DENIED_CAN_ASK) {
                TrxCompactButton(
                    label = UiCopy.openSettings(),
                    kind = TrxButtonKind.Secondary,
                    tag = AttendTestTags.cameraOpenSettings,
                    onClick = permission::openSettings,
                    modifier = Modifier.padding(top = TrxSpacing.s12),
                )
            }
        }
    }
}

@Composable
private fun ScannerChrome(
    recordingFor: RecordingFor,
    showTorch: Boolean,
    torchOn: Boolean,
    onTorch: () -> Unit,
    onChange: () -> Unit,
    onClose: () -> Unit,
    light: Boolean,
) {
    val colors = LocalTrxColors.current
    val content = if (light) colors.textInverse else colors.textPrimary
    Column(Modifier.fillMaxWidth().padding(horizontal = TrxSpacing.s8, vertical = TrxSpacing.s8)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.testTag(AttendTestTags.scannerClose)) {
                Icon(painter = painterResource(Res.drawable.x), contentDescription = UiCopy.close(), tint = content)
            }
            Text(
                text = UiCopy.scanCoachCode(),
                color = content,
                style = TrxTypography.headline,
                modifier = Modifier.weight(1f).testTag(AttendTestTags.scannerTitle),
            )
            if (showTorch) {
                IconButton(onClick = onTorch, modifier = Modifier.testTag(AttendTestTags.scannerTorch)) {
                    Box(
                        Modifier
                            .size(TrxSpacing.iconMedium)
                            .clip(RoundedCornerShape(TrxSpacing.radiusSm))
                            .background(if (torchOn) colors.accentDefault else colors.surfaceAlt),
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = TrxSpacing.s8, vertical = TrxSpacing.s8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = UiCopy.recordingFor(),
                    color = colors.accentText,
                    style = TrxTypography.overline,
                    modifier = Modifier.testTag(AttendTestTags.scannerRecordingLabel),
                )
                Text(
                    text = UiCopy.recordingForLine(recordingFor.className, recordingFor.slot.start),
                    color = content,
                    style = TrxTypography.headline,
                    modifier = Modifier.testTag(AttendTestTags.scannerRecordingFor),
                )
            }
            TrxCompactButton(
                label = UiCopy.change(),
                kind = TrxButtonKind.Secondary,
                tag = AttendTestTags.scannerChange,
                onClick = onChange,
            )
        }
    }
}
