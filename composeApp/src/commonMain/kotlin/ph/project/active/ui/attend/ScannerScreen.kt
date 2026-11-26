package ph.project.active.ui.attend

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import ph.project.active.ui.theme.TrxColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import projectactive.composeapp.generated.resources.flashlight
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
    // The scanner and the permission prompt are always dark, in both themes (C9, C10).
    CompositionLocalProvider(LocalTrxColors provides TrxColors.Dark) {
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
    Column(modifier.fillMaxSize().background(colors.canvas).testTag(AttendTestTags.scanner)) {
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
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            scanner.Preview(
                torchOn = torchOn,
                onText = { text ->
                    if (taken[0] || !looksLikeAttendanceCode(text)) return@Preview
                    taken[0] = true
                    onCodeScanned(text)
                },
                modifier = Modifier.fillMaxSize(),
            )
            ScanFrame()
        }
        Text(
            text = UiCopy.scannerHint(),
            color = colors.textPrimary,
            style = TrxTypography.headline,
            modifier = Modifier.padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16).testTag(AttendTestTags.scannerHint),
        )
        Text(
            text = if (offline) UiCopy.scannerHintOffline() else UiCopy.scannerHintDetail(),
            color = colors.textPrimary,
            style = TrxTypography.subhead,
            modifier = Modifier
                .padding(horizontal = TrxSpacing.screenHorizontal)
                .padding(bottom = TrxSpacing.s24)
                .testTag(AttendTestTags.scannerHintDetail),
        )
    }
}

/** Four yellow corner brackets, 268dp square, 4dp stroke, 16dp corner radius, 40dp arms (Figma C10). */
@Composable
private fun ScanFrame(modifier: Modifier = Modifier) {
    val color = LocalTrxColors.current.accentDefault
    Canvas(modifier.size(268.dp)) {
        val arm = 40.dp.toPx()
        val r = 16.dp.toPx()
        val sw = 4.dp.toPx()
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = sw, cap = StrokeCap.Round)
        fun corner(path: Path) = drawPath(path, color, style = stroke)
        corner(Path().apply { moveTo(0f, arm); lineTo(0f, r); arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false); lineTo(arm, 0f) })
        corner(Path().apply { moveTo(w - arm, 0f); lineTo(w - r, 0f); arcTo(Rect(w - 2 * r, 0f, w, 2 * r), 270f, 90f, false); lineTo(w, arm) })
        corner(Path().apply { moveTo(w, h - arm); lineTo(w, h - r); arcTo(Rect(w - 2 * r, h - 2 * r, w, h), 0f, 90f, false); lineTo(w - arm, h) })
        corner(Path().apply { moveTo(arm, h); lineTo(r, h); arcTo(Rect(0f, h - 2 * r, 2 * r, h), 90f, 90f, false); lineTo(0f, h - arm) })
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
    Column(modifier.fillMaxSize().background(colors.canvas).testTag(AttendTestTags.cameraPermission)) {
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
                tint = colors.textPrimary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = UiCopy.allowCameraTitle(),
                color = colors.textPrimary,
                style = TrxTypography.sectionTitle,
                modifier = Modifier.padding(top = TrxSpacing.s16),
            )
            Text(
                text = UiCopy.allowCameraBody(),
                color = colors.textPrimary,
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
    val content = if (light) colors.textPrimary else colors.textPrimary
    Column(Modifier.fillMaxWidth().padding(horizontal = TrxSpacing.s8, vertical = TrxSpacing.s8)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose, modifier = Modifier.size(TrxSpacing.touchTarget).testTag(AttendTestTags.scannerClose)) {
                Icon(painter = painterResource(Res.drawable.x), contentDescription = UiCopy.close(), tint = content)
            }
            Text(
                text = UiCopy.scanCoachCode(),
                color = content,
                style = TrxTypography.headline,
                modifier = Modifier.weight(1f).testTag(AttendTestTags.scannerTitle),
            )
            if (showTorch) {
                IconButton(
                    onClick = onTorch,
                    modifier = Modifier
                        .size(TrxSpacing.touchTarget)
                        .background(if (torchOn) colors.accentDefault else colors.surfaceAlt, CircleShape)
                        .testTag(AttendTestTags.scannerTorch),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.flashlight),
                        contentDescription = UiCopy.torch(),
                        tint = if (torchOn) colors.textOnAccent else colors.textPrimary,
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
