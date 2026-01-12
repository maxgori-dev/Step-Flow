package com.example.step_flow

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.step_flow.data.RunModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

private const val DAYS_IN_WEEK = 7
private const val GRID_CELLS = 42 // 7*6

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCalendarScreen(
    modifier: Modifier = Modifier,
    runs: List<RunModel>,
    goalSteps: Int,
    goalMinutes: Int,
    goalKcal: Int,
    initialSelectedDate: LocalDate = LocalDate.now(),
    onBack: () -> Unit = {},
    onPickDay: (LocalDate) -> Unit = {}
) {
    var currentMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var pickedDate by rememberSaveable { mutableStateOf(initialSelectedDate) }
    var showDetails by rememberSaveable { mutableStateOf(true) }

    val dayData = remember(runs, currentMonth, goalSteps, goalMinutes, goalKcal) {
        val map = mutableMapOf<LocalDate, DayMetrics>()

        val grouped = runs.groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }

        grouped.forEach { (date, dailyRuns) ->
            val totalSteps = dailyRuns.sumOf { it.steps }
            val totalSeconds = dailyRuns.sumOf { it.durationSeconds }
            val totalKcal = dailyRuns.sumOf { it.calories.toDouble() }.toInt()

            // ✅ ИСПРАВЛЕНИЕ 1: Убираем .coerceIn(0f, 1f)
            // Это позволяет прогрессу быть больше 1.0 (например, 1.5 = 150%)
            // Также сохраняем прогресс для каждого кольца отдельно.
            val pSteps = (totalSteps.toFloat() / goalSteps)
            val pMin = ((totalSeconds / 60f) / goalMinutes)
            val pKcal = (totalKcal.toFloat() / goalKcal)

            // overallProgress оставляем ограниченным для логики "достиг цели или нет"
            val overall = ((pSteps.coerceAtMost(1f) + pMin.coerceAtMost(1f) + pKcal.coerceAtMost(1f)) / 3f)

            map[date] = DayMetrics(
                steps = totalSteps,
                minutes = (totalSeconds / 60).toInt(),
                kcal = totalKcal,
                goalSteps = goalSteps,
                goalMinutes = goalMinutes,
                goalKcal = goalKcal,
                overallProgress = overall,
                // ✅ Сохраняем "сырой" прогресс > 100%
                pSteps = pSteps,
                pMinutes = pMin,
                pKcal = pKcal
            )
        }
        map
    }

    val cells = remember(currentMonth) { buildMonthGrid(currentMonth) }
    val pickedMetrics = dayData[pickedDate] ?: DayMetrics.empty(goalSteps, goalMinutes, goalKcal)

    val monthStats = remember(dayData, currentMonth) {
        val entriesInMonth = dayData.filterKeys {
            it.year == currentMonth.year && it.month == currentMonth.month
        }
        val totalSteps = entriesInMonth.values.sumOf { it.steps }
        val totalMinutes = entriesInMonth.values.sumOf { it.minutes }
        val totalKcal = entriesInMonth.values.sumOf { it.kcal }
        val goalAchievedDays = entriesInMonth.values.count { it.overallProgress >= 1f }

        MonthStats(totalSteps, totalMinutes, totalKcal, goalAchievedDays)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val minDim = minOf(maxWidth, maxHeight)
        val uiScale = (minDim / 390.dp).coerceIn(0.82f, 1.20f)

        val screenPadH = 16.dp * uiScale
        val screenPadV = 12.dp * uiScale
        val detailsBottomPad = 10.dp * uiScale
        val detailsReservedSpace = (170.dp * uiScale).coerceIn(140.dp, 210.dp)
        val scroll = rememberScrollState()

        Box(modifier = Modifier.fillMaxSize().padding(horizontal = screenPadH, vertical = screenPadV)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(bottom = detailsReservedSpace)
            ) {
                TopBarLight(
                    title = "Activity",
                    scale = uiScale,
                    onBack = onBack,
                    onToday = {
                        currentMonth = YearMonth.now()
                        pickedDate = LocalDate.now()
                    }
                )

                Spacer(Modifier.height(12.dp * uiScale))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp * uiScale),
                    color = Color.White,
                    shadowElevation = 12.dp * uiScale
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val gridPad = 12.dp * uiScale
                        val cellSize = (maxWidth - gridPad * 2f) / DAYS_IN_WEEK

                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp * uiScale)) {
                            WeekHeaderAligned(cellSize = cellSize, scale = uiScale, modifier = Modifier.padding(horizontal = gridPad))
                            Spacer(Modifier.height(12.dp * uiScale))
                            SummaryBlockLight(
                                scale = uiScale,
                                month = currentMonth,
                                onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
                                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                                stats = monthStats
                            )
                            Spacer(Modifier.height(10.dp * uiScale))

                            CalendarGridAligned(
                                cells = cells,
                                cellSize = cellSize,
                                scale = uiScale,
                                pickedDate = pickedDate,
                                dayData = dayData, // Передаем всю карту данных
                                onDayClick = { date ->
                                    if (date != null) {
                                        pickedDate = date
                                        showDetails = true
                                        onPickDay(date)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = gridPad)
                            )
                            Spacer(Modifier.height(10.dp * uiScale))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp * uiScale))
            }

            DayDetailsPanel(
                visible = showDetails,
                date = pickedDate,
                metrics = pickedMetrics,
                scale = uiScale,
                onClose = { showDetails = false },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = detailsBottomPad)
            )
        }
    }
}

data class MonthStats(val steps: Int, val minutes: Int, val kcal: Int, val goalDays: Int)

// ... (TopBarLight, WeekHeaderAligned, SummaryBlockLight - без изменений) ...
@Composable
private fun TopBarLight(title: String, scale: Float, onBack: () -> Unit, onToday: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("←", color = Color(0xFF111111), fontSize = (22.sp * scale), modifier = Modifier.padding(end = 10.dp * scale).clickable { onBack() })
        Text(title, color = Color(0xFF111111), fontSize = (22.sp * scale), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text("Today", color = Color(0xFF0A84FF), fontSize = (16.sp * scale), fontWeight = FontWeight.Medium, modifier = Modifier.padding(end = 14.dp * scale).clickable { onToday() })
    }
}

@Composable
private fun WeekHeaderAligned(cellSize: Dp, scale: Float, modifier: Modifier = Modifier) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        labels.forEachIndexed { index, s ->
            Box(modifier = Modifier.size(cellSize), contentAlignment = Alignment.Center) {
                Text(s, color = if (index == 6) Color(0xFFE05A5A) else Color(0xFF9EA0A6), fontSize = (14.sp * scale), fontWeight = FontWeight.Medium)
            }
        }
    }
    Spacer(Modifier.height(8.dp * scale))
    Box(Modifier.fillMaxWidth().height((1.dp * scale).coerceAtLeast(1.dp)).background(Color(0xFFE7E8EC)))
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SummaryBlockLight(scale: Float, month: YearMonth, onPrevMonth: () -> Unit, onNextMonth: () -> Unit, stats: MonthStats) {
    val monthTitle = month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.year
    Column(modifier = Modifier.padding(horizontal = 14.dp * scale)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onPrevMonth, modifier = Modifier.size(24.dp * scale)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color.Gray) }
            Text(monthTitle, color = Color(0xFF111111), fontSize = (18.sp * scale), fontWeight = FontWeight.SemiBold)
            IconButton(onClick = onNextMonth, modifier = Modifier.size(24.dp * scale)) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray) }
        }
        Spacer(Modifier.height(6.dp * scale))
        Text("Goal achieved ${stats.goalDays} days", color = Color(0xFF6B6E76), fontSize = (16.sp * scale), fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp * scale))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("${stats.steps} steps", color = Color(0xFF111111), fontSize = (16.sp * scale), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(12.dp * scale))
            Text("|", color = Color(0xFFB5B7BE), fontSize = (16.sp * scale))
            Spacer(Modifier.width(12.dp * scale))
            Text("${stats.minutes} min", color = Color(0xFF111111), fontSize = (16.sp * scale), fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp * scale))
        Text("${stats.kcal} kcal", color = Color(0xFF111111), fontSize = (16.sp * scale), fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun CalendarGridAligned(
    cells: List<DayCell>,
    cellSize: Dp,
    scale: Float,
    pickedDate: LocalDate,
    dayData: Map<LocalDate, DayMetrics>,
    onDayClick: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        for (row in 0 until (GRID_CELLS / DAYS_IN_WEEK)) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until DAYS_IN_WEEK) {
                    val idx = row * DAYS_IN_WEEK + col
                    val cell = cells[idx]
                    // Получаем метрики для конкретного дня или пустые, если данных нет
                    val metrics = cell.date?.let { dayData[it] } ?: DayMetrics.empty()

                    DayCellItem(
                        cell = cell,
                        cellSize = cellSize,
                        scale = scale,
                        isPicked = cell.date == pickedDate,
                        metrics = metrics, // Передаем метрики
                        onClick = { onDayClick(cell.date) }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayCellItem(
    cell: DayCell,
    cellSize: Dp,
    scale: Float,
    isPicked: Boolean,
    metrics: DayMetrics, // Принимаем объект DayMetrics
    onClick: () -> Unit
) {
    val date = cell.date
    val dayText = date?.dayOfMonth?.toString() ?: ""
    val isSunday = date?.dayOfWeek == DayOfWeek.SUNDAY

    val pickedAlpha by animateFloatAsState(
        targetValue = if (isPicked) 1f else 0f,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "pickedAlpha"
    )

    val pad = (6.dp * scale).coerceIn(4.dp, 8.dp)
    val corner = (16.dp * scale).coerceIn(12.dp, 18.dp)

    // Увеличили размер колец, чтобы они были видны
    val ringSize = (cellSize * 0.85f).coerceIn(26.dp, 44.dp)
    val ringStroke = (cellSize * 0.08f).coerceIn(2.5.dp, 4.dp)

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(pad)
            .clip(RoundedCornerShape(corner))
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (pickedAlpha > 0f) {
            Box(Modifier.matchParentSize().background(Color(0xFFEFF0F3).copy(alpha = pickedAlpha)))
        }

        if (date != null) {
            // ✅ Теперь передаем РЕАЛЬНЫЕ данные для каждого кольца
            ProgressRingsLight(
                pSteps = metrics.pSteps,
                pTime = metrics.pMinutes,
                pKcal = metrics.pKcal,
                size = ringSize,
                stroke = ringStroke,
                dim = cell.isOutsideMonth
            )

            Text(
                text = dayText,
                color = when {
                    cell.isOutsideMonth -> Color(0xFFB8BBC2)
                    isSunday -> Color(0xFFE05A5A)
                    else -> Color(0xFF34363C)
                },
                fontSize = (13.sp * scale),
                fontWeight = if (isPicked) FontWeight.SemiBold else FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center) // Центрируем текст поверх колец
            )
        }
    }
}

// ✅ ОБНОВЛЕННЫЙ КОМПОНЕНТ КОЛЕЦ
// Теперь он принимает 3 параметра и разрешает переполнение (overfill)
@Composable
private fun ProgressRingsLight(
    pSteps: Float,
    pTime: Float,
    pKcal: Float,
    size: Dp,
    stroke: Dp,
    dim: Boolean
) {
    // Анимация без ограничения coerceIn(0,1), чтобы разрешить > 100%
    val p1 by animateFloatAsState(targetValue = pSteps, animationSpec = tween(450), label = "p1")
    val p2 by animateFloatAsState(targetValue = pTime, animationSpec = tween(520), label = "p2")
    val p3 by animateFloatAsState(targetValue = pKcal, animationSpec = tween(590), label = "p3")

    val track = if (dim) Color(0xFFE9EAF0) else Color(0xFFE6E7EB)
    // Зеленый (Шаги), Синий (Время), Розовый (Ккал)
    val green = if (dim) Color(0xFFBFDCC7) else Color(0xFF34C759)
    val blue = if (dim) Color(0xFFB9D7E8) else Color(0xFF0A84FF)
    val pink = if (dim) Color(0xFFE6B7C1) else Color(0xFFFF375F)

    Canvas(modifier = Modifier.size(size)) {
        val baseRadius = size.toPx() / 2f
        val strokePx = stroke.toPx()
        // Расстояние между кольцами (немного плотнее, чем раньше)
        val gap = strokePx + 1.5f

        fun drawRing(index: Int, color: Color, progress: Float) {
            val radius = baseRadius - (index * gap) - (strokePx / 2f)
            if (radius <= 0) return

            // 1. Рисуем серый трек (фон)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Рисуем прогресс
            // Если progress > 1.0, угол будет > 360, что создаст эффект наложения (Apple Watch style)
            if (progress > 0.01f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        drawRing(0, green, p1) // Внешнее (шаги)
        drawRing(1, blue, p2)  // Среднее (минуты)
        drawRing(2, pink, p3)  // Внутреннее (ккал)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DayDetailsPanel(
    visible: Boolean,
    date: LocalDate,
    metrics: DayMetrics,
    scale: Float,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayTitle = remember(date) {
        val dow = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        val mon = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        "$dow, $mon ${date.dayOfMonth}, ${date.year}"
    }

    val stepsNow by animateIntAsState(targetValue = metrics.steps, animationSpec = tween(350))
    val minutesNow by animateIntAsState(targetValue = metrics.minutes, animationSpec = tween(350))
    val kcalNow by animateIntAsState(targetValue = metrics.kcal, animationSpec = tween(350))

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { it / 2 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { it / 2 },
        modifier = modifier
    ) {
        val padH = 16.dp * scale
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = padH),
            shape = RoundedCornerShape(22.dp * scale),
            color = Color.White,
            shadowElevation = 14.dp * scale
        ) {
            Column(modifier = Modifier.padding(14.dp * scale)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text = dayTitle, fontSize = (16.sp * scale), fontWeight = FontWeight.SemiBold, color = Color(0xFF111111), modifier = Modifier.weight(1f))
                    Box(modifier = Modifier.size(30.dp * scale).clip(CircleShape).background(Color(0xFFF0F1F4)).clickable { onClose() }, contentAlignment = Alignment.Center) {
                        Text("×", fontSize = (18.sp * scale), color = Color(0xFF44474F))
                    }
                }
                Spacer(Modifier.height(12.dp * scale))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    // Большие кольца в панели деталей
                    ProgressRingsLight(
                        pSteps = metrics.pSteps,
                        pTime = metrics.pMinutes,
                        pKcal = metrics.pKcal,
                        size = (86.dp * scale).coerceIn(72.dp, 96.dp),
                        stroke = (10.dp * scale).coerceIn(8.dp, 12.dp),
                        dim = false
                    )
                    Spacer(Modifier.width(14.dp * scale))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp * scale)) {
                        MetricLine(scale = scale, color = Color(0xFF34C759), label = "$stepsNow / ${metrics.goalSteps} steps")
                        MetricLine(scale = scale, color = Color(0xFF0A84FF), label = "$minutesNow / ${metrics.goalMinutes} minutes")
                        MetricLine(scale = scale, color = Color(0xFFFF375F), label = "$kcalNow / ${metrics.goalKcal} kcal")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLine(scale: Float, color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp * scale).clip(CircleShape).background(color))
        Spacer(Modifier.width(10.dp * scale))
        Text(text = label, color = Color(0xFF111111), fontSize = (16.sp * scale), fontWeight = FontWeight.Medium)
    }
}

@Immutable
private data class DayCell(val date: LocalDate?, val isOutsideMonth: Boolean)

@Immutable
data class DayMetrics(
    val steps: Int,
    val minutes: Int,
    val kcal: Int,
    val goalSteps: Int,
    val goalMinutes: Int,
    val goalKcal: Int,
    val overallProgress: Float, // 0..1 (для общей статистики)
    // ✅ Новые поля для точного отображения колец > 100%
    val pSteps: Float,
    val pMinutes: Float,
    val pKcal: Float
) {
    companion object {
        fun empty(gSteps: Int = 6000, gMin: Int = 60, gKcal: Int = 500) = DayMetrics(
            steps = 0, minutes = 0, kcal = 0,
            goalSteps = gSteps, goalMinutes = gMin, goalKcal = gKcal,
            overallProgress = 0f,
            pSteps = 0f, pMinutes = 0f, pKcal = 0f
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildMonthGrid(month: YearMonth): List<DayCell> {
    val first = month.atDay(1)
    val last = month.atEndOfMonth()
    val firstDayIndex = (first.dayOfWeek.value + 6) % 7
    val cells = ArrayList<DayCell>(GRID_CELLS)
    val prevMonth = month.minusMonths(1)
    val prevLast = prevMonth.atEndOfMonth()
    for (i in firstDayIndex - 1 downTo 0) cells += DayCell(date = prevLast.minusDays(i.toLong()), isOutsideMonth = true)
    var d = first
    while (!d.isAfter(last)) { cells += DayCell(date = d, isOutsideMonth = false); d = d.plusDays(1) }
    val nextMonth = month.plusMonths(1)
    var nd = nextMonth.atDay(1)
    while (cells.size < GRID_CELLS) { cells += DayCell(date = nd, isOutsideMonth = true); nd = nd.plusDays(1) }
    return cells
}