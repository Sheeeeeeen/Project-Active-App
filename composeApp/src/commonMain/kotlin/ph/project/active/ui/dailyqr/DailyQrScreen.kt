package ph.project.active.ui.dailyqr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
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
import projectactive.composeapp.generated.resources.shield
import projectactive.composeapp.generated.resources.smartphone
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
                    .padding(start = TrxSpacing.screenHorizontal, end = TrxSpacing.screenHorizontal, bottom = TrxSpacing.s16)
                    .testTag(CoachTabBarTestTags.qrRoot),
            ) {
                // Same row height as the other tabs' top bar, so the title doesn't move between tabs.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = TrxSpacing.s8)
                        .heightIn(min = TrxSpacing.touchTarget),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = UiCopy.dailyQrOverline(),
                        color = colors.accentText,
                        style = TrxTypography.overline,
                        modifier = Modifier.testTag(DailyQrTestTags.overline),
                    )
                }
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
                QrCard(
                    state,
                    Modifier.padding(
                        top = if (state.connection.banner != null) TrxSpacing.s12 else TrxSpacing.s16,
                        start = TrxSpacing.s20,
                        end = TrxSpacing.s20,
                    ),
                )
                Caption(
                    text = UiCopy.brightnessRaised(),
                    icon = Res.drawable.smartphone,
                    color = colors.textSecondary,
                    tag = DailyQrTestTags.brightness,
                    modifier = Modifier.padding(top = TrxSpacing.s12),
                )
                SectionHeader(Modifier.padding(top = TrxSpacing.s16, bottom = TrxSpacing.s12))
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
            .padding(TrxSpacing.s24)
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
        Caption(
            text = UiCopy.validForDate(state.validFor),
            icon = Res.drawable.shield,
            color = TrxColors.Light.textSecondary,
            tag = DailyQrTestTags.valid,
            modifier = Modifier.padding(top = TrxSpacing.s12),
        )
    }
}

@Composable
private fun SectionHeader(modifier: Modifier = Modifier) {
    val colors = LocalTrxColors.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = UiCopy.thisCodeCoversToday(),
            color = colors.textSecondary,
            style = TrxTypography.overline,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(text = UiCopy.oneCodeAllClasses(), color = colors.textTertiary, style = TrxTypography.footnote, maxLines = 1)
    }
}

@Composable
private fun Caption(text: String, icon: DrawableResource, color: Color, tag: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .padding(end = TrxSpacing.s4)
                .size(TrxSpacing.iconSmall),
        )
        Text(text = text, color = color, style = TrxTypography.footnote, modifier = Modifier.testTag(tag))
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
            .padding(horizontal = TrxSpacing.s16, vertical = TrxSpacing.s12),
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
