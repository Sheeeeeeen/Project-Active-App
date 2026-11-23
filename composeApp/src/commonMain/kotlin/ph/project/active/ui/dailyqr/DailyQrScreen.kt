package ph.project.active.ui.dailyqr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import ph.project.active.domain.model.SessionStatus
import ph.project.active.presentation.attend.TodaySessionRow
import ph.project.active.presentation.common.UiCopy
import ph.project.active.presentation.common.formatSlot
import ph.project.active.presentation.dailyqr.DailyQrUiState
import ph.project.active.ui.coachhome.CoachTabBarTestTags
import ph.project.active.ui.components.InfoCallout
import ph.project.active.ui.components.InfoCalloutStyle
import ph.project.active.ui.components.LargeTitle
import ph.project.active.ui.components.OfflineBanner
import ph.project.active.ui.components.StatusChip
import ph.project.active.ui.components.TrxCard
import ph.project.active.ui.platform.LightStatusBarIcons
import ph.project.active.ui.theme.LocalTrxColors
import ph.project.active.ui.theme.TrxColors
import ph.project.active.ui.theme.TrxSpacing
import ph.project.active.ui.theme.TrxTypography
import projectactive.composeapp.generated.resources.Res
import projectactive.composeapp.generated.resources.ban
import projectactive.composeapp.generated.resources.calendar_check
import projectactive.composeapp.generated.resources.wifi_off

/** D19–D20: today's code for every class, always dark, with today's sessions under it. */
@Composable
fun DailyQrScreen(
    state: DailyQrUiState,
    tabBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons(lightIcons = true)
    // Always dark, in both app modes; the tab bar slot is drawn inside so it turns dark too.
    CompositionLocalProvider(LocalTrxColors provides TrxColors.Dark) {
        val colors = TrxColors.Dark
        Scaffold(
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            containerColor = colors.canvas,
            contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom),
            bottomBar = tabBar,
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = TrxSpacing.screenHorizontal, vertical = TrxSpacing.s16)
                    .testTag(CoachTabBarTestTags.qrRoot),
            ) {
                Text(
                    text = UiCopy.dailyQrOverline(),
                    color = colors.textSecondary,
                    style = TrxTypography.overline,
                    modifier = Modifier.testTag(DailyQrTestTags.overline),
                )
                LargeTitle(title = UiCopy.todaysCode(), tag = CoachTabBarTestTags.qrTitle)
                Text(
                    text = UiCopy.dailyQrSubtitle(state.today, state.coachName),
                    color = colors.textSecondary,
                    style = TrxTypography.subhead,
                    modifier = Modifier.testTag(DailyQrTestTags.subtitle),
                )
                state.connection.banner?.let { banner ->
                    OfflineBanner(
                        message = banner,
                        modifier = Modifier
                            .padding(top = TrxSpacing.s16)
                            .clip(RoundedCornerShape(TrxSpacing.radiusMd)),
                        subtitle = UiCopy.qrOfflineSubtitle(),
                    )
                }
                QrCard(state, Modifier.padding(top = TrxSpacing.s24))
                Text(
                    text = UiCopy.brightnessRaised(),
                    color = colors.textTertiary,
                    style = TrxTypography.footnote,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = TrxSpacing.s12)
                        .testTag(DailyQrTestTags.brightness),
                )
                SectionHeader(Modifier.padding(top = TrxSpacing.s32, bottom = TrxSpacing.s8))
                if (state.classListUnavailable) {
                    InfoCallout(
                        title = UiCopy.classListUnavailableTitle(),
                        body = UiCopy.classListUnavailableBody(),
                        tag = DailyQrTestTags.callout,
                        icon = Res.drawable.wifi_off,
                        style = InfoCalloutStyle.Neutral,
                    )
                } else {
                    Sessions(state.todaysSessions)
                }
            }
        }
    }
}

@Composable
private fun QrCard(state: DailyQrUiState, modifier: Modifier = Modifier) {
    // The code is black on white in both modes, so a camera reads it the same way (decision 4).
    val paper = TrxColors.Light.surface
    val ink = TrxColors.Light.textPrimary
    val matrix = remember(state.code) { QrEncoder.encode(state.code) }
    Column(
        modifier
            .fillMaxWidth()
            .background(paper, RoundedCornerShape(TrxSpacing.radiusLg))
            .padding(TrxSpacing.s16)
            .testTag(DailyQrTestTags.card),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            // Whole-pixel modules, centred, so every module edge is sharp.
            val module = (minOf(size.width, size.height) / matrix.size).toInt().coerceAtLeast(1)
            val origin = ((size.width - module * matrix.size) / 2).toInt().toFloat()
            for (y in 0 until matrix.size) {
                for (x in 0 until matrix.size) {
                    if (matrix.isDark(x, y)) {
                        drawRect(
                            color = ink,
                            topLeft = Offset(origin + x * module, origin + y * module),
                            size = Size(module.toFloat(), module.toFloat()),
                        )
                    }
                }
            }
        }
        Text(
            text = UiCopy.validForDate(state.validFor),
            color = ink,
            style = TrxTypography.subheadStrong,
            modifier = Modifier
                .padding(top = TrxSpacing.s8)
                .testTag(DailyQrTestTags.valid),
        )
    }
}

@Composable
private fun SectionHeader(modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = UiCopy.thisCodeCoversToday(),
            color = colors.textPrimary,
            style = TrxTypography.sectionTitle,
            modifier = Modifier.weight(1f),
        )
        Text(text = UiCopy.oneCodeAllClasses(), color = colors.textTertiary, style = TrxTypography.footnote)
    }
}

@Composable
private fun Sessions(rows: List<TodaySessionRow>) {
    Column(Modifier.fillMaxWidth().testTag(DailyQrTestTags.sessions)) {
        if (rows.isNotEmpty()) {
            TrxCard(contentPadding = TrxSpacing.s0) {
                rows.forEachIndexed { index, row ->
                    if (index > 0) HorizontalDivider(color = LocalTrxColors.current.borderDefault)
                    SessionLine(row)
                }
            }
        }
    }
}

@Composable
private fun SessionLine(row: TodaySessionRow) {
    val colors = LocalTrxColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(TrxSpacing.s16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrxSpacing.s12),
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.className, color = colors.textPrimary, style = TrxTypography.subheadStrong)
            Text(formatSlot(row.slot), color = colors.textSecondary, style = TrxTypography.subhead)
        }
        if (row.status == SessionStatus.CANCELLED) {
            StatusChip(UiCopy.cancelled(), Res.drawable.ban, colors.surfaceAlt, colors.textSecondary)
        } else {
            StatusChip(UiCopy.scheduled(), Res.drawable.calendar_check, colors.infoBg, colors.infoFg)
        }
    }
}
