package com.example.step_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

enum class HomeTab { Calendar, Home, Profile }

@Composable
fun HomeScreenNow(
    modifier: Modifier = Modifier,
    onRunClick: () -> Unit = {},
    onTileCalendar: () -> Unit = {},
    onTileHistory: () -> Unit = {},
    onTileAchievements: () -> Unit = {},
    onTopProfile: () -> Unit = {},
    onTopSettings: () -> Unit = {},
    onBottomTabChange: (HomeTab) -> Unit = {}
) {
    val bg = Color.White
    val soft = Color(0xFFF4F6F9)
    val textMain = Color(0xFF111111)
    val textSoft = Color(0xFF6F747C)

    val navInactive = Color(0xFF9AA3AD)
    val navActive = Color(0xFF111111)

    val ring1 = Color(0xFFB8E36A)
    val ring2 = Color(0xFF35B25C)

    var tab by remember { mutableStateOf(HomeTab.Home) }
    val haptics = LocalHapticFeedback.current
    val pageBg = Brush.verticalGradient(listOf(bg, soft))

    // ✅ локализованные строки (один раз тут)
    val appTitle = stringResource(R.string.home_app_title)
    val quickActions = stringResource(R.string.home_quick_actions)

    val tileCalendar = stringResource(R.string.home_tile_calendar)
    val tileHistory = stringResource(R.string.home_tile_history)
    val tileAwards = stringResource(R.string.home_tile_awards)

    val runText = stringResource(R.string.home_run)

    val navCalendar = stringResource(R.string.home_nav_calendar)
    val navHome = stringResource(R.string.home_nav_home)
    val navProfile = stringResource(R.string.home_nav_profile)

    val cdProfile = stringResource(R.string.cd_profile)
    val cdSettings = stringResource(R.string.cd_settings)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val minDim = minOf(maxWidth, maxHeight)
        val uiScale = (minDim / 390.dp).coerceIn(0.82f, 1.20f)

        val padH = 18.dp * uiScale
        val gapS = 10.dp * uiScale
        val gapM = 18.dp * uiScale
        val gapL = 26.dp * uiScale

        val topBarH = (56.dp * uiScale).coerceIn(48.dp, 64.dp)
        val navH = (76.dp * uiScale).coerceIn(64.dp, 90.dp)

        val ringWrap = (minDim * 0.60f).coerceIn(210.dp, 300.dp)
        val ringCore = (ringWrap * 0.74f).coerceIn(150.dp, 230.dp)

        val scroll = rememberScrollState()

        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = padH)
                    .padding(bottom = navH + gapM)
            ) {
                Spacer(Modifier.height(gapS))

                // ===== TOP BAR =====
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topBarH),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = appTitle,
                        fontSize = (22.sp * uiScale),
                        fontWeight = FontWeight.SemiBold,
                        color = textMain
                    )

                    Spacer(Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp * uiScale)) {

                        // ✅ Baseline: top profile button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTopProfile()
                            },
                            modifier = Modifier
                                .size(40.dp * uiScale)
                                .semantics { contentDescription = "bp_top_profile" }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = cdProfile,
                                tint = textMain,
                                modifier = Modifier.size(22.dp * uiScale)
                            )
                        }

                        // ✅ Baseline: top settings button
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTopSettings()
                            },
                            modifier = Modifier
                                .size(40.dp * uiScale)
                                .semantics { contentDescription = "bp_top_settings" }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = cdSettings,
                                tint = textMain,
                                modifier = Modifier.size(22.dp * uiScale)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(gapM))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    RunRing(
                        wrapSize = ringWrap,
                        coreSize = ringCore,
                        scale = uiScale,
                        ring1 = ring1,
                        ring2 = ring2,
                        runText = runText,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRunClick()
                        }
                    )
                }

                Spacer(Modifier.height(gapL))

                Text(
                    text = quickActions,
                    fontSize = (13.sp * uiScale),
                    color = textSoft,
                    modifier = Modifier.padding(start = 6.dp * uiScale, bottom = 10.dp * uiScale)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp * uiScale)
                ) {
                    // ✅ Baseline: Calendar tile
                    QuickTile(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.05f)
                            .semantics { contentDescription = "bp_tile_calendar" },
                        scale = uiScale,
                        icon = Icons.Outlined.CalendarToday,
                        label = tileCalendar,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTileCalendar()
                        }
                    )

                    QuickTile(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.05f),
                        scale = uiScale,
                        icon = Icons.Outlined.History,
                        label = tileHistory,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTileHistory()
                        }
                    )

                    QuickTile(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.05f),
                        scale = uiScale,
                        icon = Icons.Outlined.EmojiEvents,
                        label = tileAwards,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTileAchievements()
                        }
                    )
                }

                Spacer(Modifier.height(gapM))
            }

            BottomNav(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = padH)
                    .padding(bottom = 10.dp * uiScale),
                height = navH,
                scale = uiScale,
                activeTab = tab,
                inactiveColor = navInactive,
                activeColor = navActive,
                labels = BottomLabels(
                    calendar = navCalendar,
                    home = navHome,
                    profile = navProfile
                ),
                onSelect = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    tab = it
                    onBottomTabChange(it)
                }
            )
        }
    }
}

private data class BottomLabels(
    val calendar: String,
    val home: String,
    val profile: String
)

@Composable
private fun RunRing(
    wrapSize: Dp,
    coreSize: Dp,
    scale: Float,
    ring1: Color,
    ring2: Color,
    runText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(wrapSize)
            .shadow(14.dp * scale, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = min(size.width, size.height)
            val pivot = Offset(size.width / 2f, size.height / 2f)

            val inset = d * 0.12f
            val arcSize = d - inset * 2f
            val strokeW = d * 0.030f

            val sweep = Brush.sweepGradient(
                colors = listOf(ring1, ring2, ring1),
                center = pivot
            )

            rotate(degrees = 300f, pivot = pivot) {
                drawArc(
                    brush = sweep,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            val pillW = d * 0.15f
            val pillH = d * 0.08f
            val pillX = (size.width - pillW) / 2f
            val pillY = inset - pillH * 0.35f

            drawRoundRect(
                color = Color(0xFFF2F4F7),
                topLeft = Offset(pillX, pillY),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(pillH, pillH)
            )
        }

        // ✅ Baseline: RUN button target
        Box(
            modifier = Modifier
                .size(coreSize)
                .shadow(10.dp * scale, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color(0xFFF3F5F8))
                .semantics { contentDescription = "bp_run" }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = runText,
                fontSize = (18.sp * scale),
                fontWeight = FontWeight.Bold,
                letterSpacing = (3.sp * scale),
                color = Color(0xFF7C848F)
            )
        }
    }
}

@Composable
private fun QuickTile(
    modifier: Modifier,
    scale: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp * scale)
    val textMain = Color(0xFF111111)
    val textSoft = Color(0xFF6F747C)

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = shape,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp * scale
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp * scale, vertical = 12.dp * scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp * scale)
                    .clip(RoundedCornerShape(14.dp * scale))
                    .background(Color(0xFFF3F5F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textMain,
                    modifier = Modifier.size(22.dp * scale)
                )
            }

            Spacer(Modifier.height(10.dp * scale))

            Text(
                text = label,
                fontSize = (13.sp * scale),
                fontWeight = FontWeight.SemiBold,
                color = textSoft
            )
        }
    }
}

@Composable
private fun BottomNav(
    modifier: Modifier,
    height: Dp,
    scale: Float,
    activeTab: HomeTab,
    inactiveColor: Color,
    activeColor: Color,
    labels: BottomLabels,
    onSelect: (HomeTab) -> Unit
) {
    val shape = RoundedCornerShape(26.dp * scale)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = shape,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp * scale
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp * scale),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                modifier = Modifier.weight(1f),
                scale = scale,
                label = labels.calendar,
                icon = Icons.Outlined.CalendarToday,
                selected = activeTab == HomeTab.Calendar,
                inactive = inactiveColor,
                active = activeColor,
                baselineId = "bp_tab_calendar"
            ) { onSelect(HomeTab.Calendar) }

            BottomItem(
                modifier = Modifier.weight(1f),
                scale = scale,
                label = labels.home,
                icon = Icons.Outlined.Home,
                selected = activeTab == HomeTab.Home,
                inactive = inactiveColor,
                active = activeColor,
                baselineId = "bp_tab_home"
            ) { onSelect(HomeTab.Home) }

            BottomItem(
                modifier = Modifier.weight(1f),
                scale = scale,
                label = labels.profile,
                icon = Icons.Outlined.Person,
                selected = activeTab == HomeTab.Profile,
                inactive = inactiveColor,
                active = activeColor,
                baselineId = "bp_tab_profile"
            ) { onSelect(HomeTab.Profile) }
        }
    }
}

@Composable
private fun BottomItem(
    modifier: Modifier,
    scale: Float,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    inactive: Color,
    active: Color,
    baselineId: String,
    onClick: () -> Unit
) {
    val textColor = if (selected) active else inactive
    val iconAlpha = if (selected) 1f else 0.70f
    val weight = if (selected) FontWeight.SemiBold else FontWeight.Medium

    Column(
        modifier = modifier
            .semantics { contentDescription = baselineId } // ✅ не локализуем (id для тестов)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp * scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier
                .size(22.dp * scale)
                .offset(y = (-1).dp * scale)
                .alpha(iconAlpha)
        )

        Spacer(Modifier.height(6.dp * scale))

        Text(
            text = label,
            fontSize = (12.sp * scale),
            color = textColor,
            fontWeight = weight
        )

        Box(
            modifier = Modifier
                .padding(top = 6.dp * scale)
                .height(3.dp * scale)
                .width((if (selected) 22.dp else 8.dp) * scale)
                .clip(RoundedCornerShape(99.dp))
                .background(textColor.copy(alpha = if (selected) 0.55f else 0.18f))
        )
    }
}
