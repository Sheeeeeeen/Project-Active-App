package ph.project.active.ui.attend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import ph.project.active.presentation.attend.AttendUiState
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatDate
import ph.project.active.presentation.common.formatTime
import ph.project.active.ui.components.LargeTitle
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxButtonKind
import ph.project.active.ui.components.TrxLargeButton
import ph.project.active.ui.platform.CameraPermission
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.platform.QrScanner
import ph.project.active.ui.platform.ScanHaptics
import ph.project.active.ui.studenthome.StudentTabBarTestTags
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.circle_check
import projectactive.composeapp.generated.resources.hourglass
import projectactive.composeapp.generated.resources.qr_code

@Composable
fun AttendScreen(
    state: AttendUiState,
    timeZone: TimeZone,
    scanner: QrScanner,
    permission: CameraPermission,
    haptics: ScanHaptics,
    onOpened: () -> Unit,
    onChoose: (String) -> Unit,
    onCloseSheet: () -> Unit,
    onScanCoachCode: () -> Unit,
    onChangeSession: () -> Unit,
    onCloseScanner: () -> Unit,
    onCodeScanned: (String) -> Unit,
    onRecordAnother: () -> Unit,
    onChooseDifferentClass: () -> Unit,
    onScanAgain: () -> Unit,
    onDone: () -> Unit,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var access by remember(permission) { mutableStateOf(permission.current()) }
    DisposableEffect(lifecycleOwner, permission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) access = permission.current()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        if (state.result == null && !state.scannerOpen) onOpened()
    }
    val result = state.result
    val recordingFor = state.recordingFor
    if (result != null) {
        ScanResultScreen(
            result = result,
            recordingFor = recordingFor,
            today = state.today,
            connection = state.connection,
            timeZone = timeZone,
            haptics = haptics,
            onDone = onDone,
            onRecordAnother = onRecordAnother,
            onChooseDifferentClass = onChooseDifferentClass,
            onScanAgain = onScanAgain,
            modifier = modifier,
        )
        return
    }
    if (state.scannerOpen && recordingFor != null) {
        ScannerScreen(
            recordingFor = recordingFor,
            connection = state.connection,
            scanner = scanner,
            permission = permission,
            access = access,
            onAccess = { access = it },
            onChange = onChangeSession,
            onClose = onCloseScanner,
            onCodeScanned = onCodeScanned,
            modifier = modifier,
        )
        return
    }
    val colors = LocalTrxColors.current
    LightStatusBarIcons(lightIcons = false)
    Scaffold(
        modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        containerColor = colors.canvas,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom),
        bottomBar = { tabBar() },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding).testTag(StudentTabBarTestTags.attendRoot)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Column(Modifier.padding(horizontal = TrxSpacing.screenHorizontal)) {
                    Text(
                        text = UiCopy.largeTitleEyebrow(),
                        color = colors.accentText,
                        style = TrxTypography.overline,
                        modifier = Modifier.padding(top = TrxSpacing.s8).testTag(AttendTestTags.overline),
                    )
                    LargeTitle(title = UiCopy.tabAttend(), tag = StudentTabBarTestTags.attendTitle)
                    Text(
                        text = formatDate(state.today),
                        color = colors.textSecondary,
                        style = TrxTypography.subhead,
                        modifier = Modifier.testTag(AttendTestTags.subtitle),
                    )
                }
                state.connection.banner?.let { banner ->
                    OfflineBanner(banner, Modifier.padding(top = TrxSpacing.s16))
                }
                RecordCard(onChooseClass = onOpened, modifier = Modifier.padding(TrxSpacing.screenHorizontal))
                if (state.recordedToday.isNotEmpty()) {
                    Text(
                        text = UiCopy.recordedToday(),
                        color = colors.textPrimary,
                        style = TrxTypography.sectionTitle,
                        modifier = Modifier
                            .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s8)
                            .testTag(AttendTestTags.recordedHeader),
                    )
                    state.recordedToday.forEachIndexed { index, row ->
                        Column(
                            Modifier
                                .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s8)
                                .testTag(AttendTestTags.recordedRow(index)),
                        ) {
                            Text(row.className, color = colors.textPrimary, style = TrxTypography.headline)
                            Text(
                                text = UiCopy.recordedTodayLine(row.sessionNumber, row.scannedAt.toLocalDateTime(timeZone).time),
                                color = colors.textSecondary,
                                style = TrxTypography.subhead,
                                modifier = Modifier.padding(top = TrxSpacing.s4),
                            )
                            Row(Modifier.padding(top = TrxSpacing.s8)) {
                                StatusChip(UiCopy.present(), Res.drawable.circle_check, colors.successBg, colors.successFg)
                                if (row.uploadPending) {
                                    StatusChip(
                                        UiCopy.uploadPending(),
                                        Res.drawable.hourglass,
                                        colors.warningBg,
                                        colors.warningFg,
                                        Modifier.padding(start = TrxSpacing.s8),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (state.sheetOpen) {
                Column(Modifier.align(Alignment.BottomCenter)) {
                    ClassSheet(
                        today = state.today,
                        choices = state.choices,
                        selectedSessionId = state.selectedSessionId,
                        canScan = state.canScan,
                        cameraAccess = access,
                        onChoose = onChoose,
                        onClose = onCloseSheet,
                        onScan = onScanCoachCode,
                        onOpenSettings = permission::openSettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordCard(onChooseClass: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    val shape = RoundedCornerShape(TrxSpacing.radiusLg)
    Column(
        modifier
            .fillMaxWidth()
            .padding(top = TrxSpacing.s16)
            .clip(shape)
            .background(colors.surfaceInverse)
            .padding(TrxSpacing.s16),
    ) {
        // The icon sits on a yellow tile so it keeps 12:1 contrast in dark mode, where surfaceInverse is white.
        Box(
            Modifier.size(56.dp).background(colors.accentDefault, RoundedCornerShape(TrxSpacing.radiusLg)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.qr_code),
                contentDescription = null,
                tint = colors.textOnAccent,
                modifier = Modifier.size(TrxSpacing.s32),
            )
        }
        Text(
            text = UiCopy.recordAttendanceTitle(),
            color = colors.textInverse,
            style = TrxTypography.sectionTitle,
            modifier = Modifier.padding(top = TrxSpacing.s12).testTag(AttendTestTags.cardTitle),
        )
        Text(
            text = UiCopy.recordAttendanceBody(),
            color = colors.textInverse,
            style = TrxTypography.subhead,
            modifier = Modifier.padding(top = TrxSpacing.s8).testTag(AttendTestTags.cardBody),
        )
        TrxLargeButton(
            label = UiCopy.chooseClass(),
            kind = TrxButtonKind.Primary,
            tag = AttendTestTags.chooseClass,
            onClick = onChooseClass,
            enabled = true,
            modifier = Modifier.padding(top = TrxSpacing.s16),
        )
    }
}
