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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.step_flow.data.RunModel
import java.util.Calendar
import kotlin.math.min

enum class HomeTab { Calendar, Home, Profile }

@Composable
fun HomeScreenNow(
    modifier: Modifier = Modifier,
    runs: List<RunModel>,
    stepsGoal: Int = 8000,
    onRunClick: () -> Unit = {},
    onTileCalendar: () -> Unit = {},
    onTileHistory: () -> Unit = {},
    onTileAchievements: () -> Unit = {},
    onTopProfile: () -> Unit = {},
    onTopSettings: () -> Unit = {},
    onBottomTabChange: (HomeTab) -> Unit = {}
) {
    val bg = MaterialTheme.colorScheme.background
    val soft = MaterialTheme.colorScheme.surface

    val textMain = MaterialTheme.colorScheme.onBackground
    val textSoft = MaterialTheme.colorScheme.onSurfaceVariant

    val navInactive = MaterialTheme.colorScheme.onSurfaceVariant
    val navActive = MaterialTheme.colorScheme.primary

    val ring1 = Color(0xFFB8E36A)
    val ring2 = Color(0xFF35B25C)

    var tab by remember { mutableStateOf(HomeTab.Home) }
    val haptics = LocalHapticFeedback.current
    val pageBg = Brush.verticalGradient(listOf(bg, soft))

    val appTitle = stringResource(R.string.home_app_title)
    val tileCalendar = stringResource(R.string.home_tile_calendar)
    val tileHistory = stringResource(R.string.home_tile_history)
    val tileAwards = stringResource(R.string.home_tile_awards)
    val runText = stringResource(R.string.home_run)
    val navCalendar = stringResource(R.string.home_nav_calendar)
    val navHome = stringResource(R.string.home_nav_home)
    val navProfile = stringResource(R.string.home_nav_profile)
    val cdProfile = stringResource(R.string.cd_profile)
    val cdSettings = stringResource(R.string.cd_settings)
    val todayTitle = stringResource(R.string.home_today_title)
    val todaySteps = stringResource(R.string.home_today_steps)
    val todayActive = stringResource(R.string.home_today_active)
    val todayCalories = stringResource(R.string.home_today_calories)

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

        val topBarH = (56.dp * uiScale).coerceIn(48.dp, 64.dp)
        val navH = (76.dp * uiScale).coerceIn(64.dp, 90.dp)

        val ringWrap = (minDim * 0.60f).coerceIn(210.dp, 300.dp)
        val ringCore = (ringWrap * 0.74f).coerceIn(150.dp, 230.dp)

        val scroll = rememberScrollState()

        val (startOfDay, endOfDay) = remember {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.DAY_OF_MONTH, 1)
            val end = cal.timeInMillis
            start to end
        }

        val todayStats by remember(runs, stepsGoal, startOfDay, endOfDay) {
            derivedStateOf {
                val todayRuns = runs.filter { it.timestamp in startOfDay until endOfDay }
                val steps = todayRuns.sumOf { it.steps }
                val activeMin = (todayRuns.sumOf { it.durationSeconds } / 60L).toInt()
                val calories = todayRuns.sumOf { it.calories.toDouble() }.toInt()
                TodayStats(
                    steps = steps,
                    goal = stepsGoal.coerceAtLeast(1),
                    activeMin = activeMin,
                    calories = calories
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(horizontal = padH)
                    .padding(bottom = navH + gapM)
            ) {
                Spacer(Modifier.height(gapS))

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

                Spacer(Modifier.height(14.dp * uiScale))

                TodaySummary(
                    modifier = Modifier.fillMaxWidth(),
                    scale = uiScale,
                    title = todayTitle,
                    stepsLabel = todaySteps,
                    activeLabel = todayActive,
                    caloriesLabel = todayCalories,
                    steps = todayStats.steps,
                    stepsGoal = todayStats.goal,
                    activeMin = todayStats.activeMin,
                    calories = todayStats.calories
                )

                Spacer(Modifier.height(16.dp * uiScale))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp * uiScale)
                ) {
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
private fun TodaySummary(
    modifier: Modifier,
    scale: Float,
    title: String,
    stepsLabel: String,
    activeLabel: String,
    caloriesLabel: String,
    steps: Int,
    stepsGoal: Int,
    activeMin: Int,
    calories: Int
) {
    val textMain = MaterialTheme.colorScheme.onSurface
    val textSoft = MaterialTheme.colorScheme.onSurfaceVariant
    val card = MaterialTheme.colorScheme.surface
    val chipBg = MaterialTheme.colorScheme.secondaryContainer

    val progress = (steps.toFloat() / stepsGoal.toFloat()).coerceIn(0f, 1f)

    val stepsValue = "${formatInt(steps)} / ${formatInt(stepsGoal)}"
    val activeValue = "${activeMin} min"
    val caloriesValue = "${calories} kcal"

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp * scale),
        color = card,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp * scale
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = (14.sp * scale),
                    fontWeight = FontWeight.SemiBold,
                    color = textMain
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = (13.sp * scale),
                    fontWeight = FontWeight.SemiBold,
                    color = textSoft
                )
            }

            Spacer(Modifier.height(10.dp * scale))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp * scale)
                    .clip(RoundedCornerShape(99.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFB8E36A), Color(0xFF35B25C))
                            )
                        )
                )
            }

            Spacer(Modifier.height(12.dp * scale))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp * scale)
            ) {
                StatChip(modifier = Modifier.weight(1f), scale = scale, bg = chipBg, label = stepsLabel, value = stepsValue)
                StatChip(modifier = Modifier.weight(1f), scale = scale, bg = chipBg, label = activeLabel, value = activeValue)
                StatChip(modifier = Modifier.weight(1f), scale = scale, bg = chipBg, label = caloriesLabel, value = caloriesValue)
            }
        }
    }
}

@Composable
private fun StatChip(modifier: Modifier, scale: Float, bg: Color, label: String, value: String) {
    val textMain = MaterialTheme.colorScheme.onSurface
    val textSoft = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp * scale))
            .background(bg)
            .padding(horizontal = 10.dp * scale, vertical = 10.dp * scale)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = label, fontSize = (11.sp * scale), fontWeight = FontWeight.Medium, color = textSoft)
            Spacer(Modifier.height(4.dp * scale))
            Text(text = value, fontSize = (12.sp * scale), fontWeight = FontWeight.SemiBold, color = textMain)
        }
    }
}

@Composable
private fun RunRing(
    wrapSize: Dp, coreSize: Dp, scale: Float, ring1: Color, ring2: Color, runText: String, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(wrapSize)
            .shadow(14.dp * scale, CircleShape, clip = false)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = min(size.width, size.height)
            val pivot = Offset(size.width / 2f, size.height / 2f)
            val inset = d * 0.12f
            val arcSize = d - inset * 2f
            val strokeW = d * 0.030f
            val sweep = Brush.sweepGradient(colors = listOf(ring1, ring2, ring1), center = pivot)

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

        Box(
            modifier = Modifier
                .size(coreSize)
                .shadow(10.dp * scale, CircleShape, clip = false)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .semantics { contentDescription = "bp_run" }
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = runText,
                fontSize = (18.sp * scale),
                fontWeight = FontWeight.Bold,
                letterSpacing = (3.sp * scale),
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val textMain = MaterialTheme.colorScheme.onSurface
    val textSoft = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp * scale
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp * scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp * scale)
                    .clip(RoundedCornerShape(14.dp * scale))
                    .background(MaterialTheme.colorScheme.secondaryContainer),
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
        color = MaterialTheme.colorScheme.surface,
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
                Modifier.weight(1f),
                scale,
                labels.calendar,
                Icons.Outlined.CalendarToday,
                activeTab == HomeTab.Calendar,
                inactiveColor,
                activeColor,
                "bp_tab_calendar"
            ) { onSelect(HomeTab.Calendar) }
            BottomItem(
                Modifier.weight(1f),
                scale,
                labels.home,
                Icons.Outlined.Home,
                activeTab == HomeTab.Home,
                inactiveColor,
                activeColor,
                "bp_tab_home"
            ) { onSelect(HomeTab.Home) }
            BottomItem(
                Modifier.weight(1f),
                scale,
                labels.profile,
                Icons.Outlined.Person,
                activeTab == HomeTab.Profile,
                inactiveColor,
                activeColor,
                "bp_tab_profile"
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
            .semantics { contentDescription = baselineId }
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

private data class TodayStats(
    val steps: Int,
    val goal: Int,
    val activeMin: Int,
    val calories: Int
)

private fun formatInt(v: Int): String {
    val s = v.toString()
    val sb = StringBuilder()
    for (i in s.indices) {
        if (i > 0 && (s.length - i) % 3 == 0) sb.append(' ')
        sb.append(s[i])
    }
    return sb.toString()
}
