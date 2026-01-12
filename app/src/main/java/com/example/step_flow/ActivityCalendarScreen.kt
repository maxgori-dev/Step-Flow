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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min

private const val DAYS_IN_WEEK = 7
private const val GRID_CELLS = 42 // 7*6

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActivityCalendarScreen(
    modifier: Modifier = Modifier,
    month: YearMonth = YearMonth.now(),
    initialSelectedDate: LocalDate = LocalDate.now(),
    dayData: Map<LocalDate, DayMetrics> = demoDayData(month),
    onBack: () -> Unit = {},
    onPickMonth: () -> Unit = {},
    onPickDay: (LocalDate) -> Unit = {}
) {
    var pickedDate by rememberSaveable { mutableStateOf(initialSelectedDate) }
    var showDetails by rememberSaveable { mutableStateOf(true) }

    val cells = remember(month) { buildMonthGrid(month) }
    val pickedMetrics = dayData[pickedDate] ?: DayMetrics.empty()

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenPadH, vertical = screenPadV)
        ) {
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
                    onPickMonth = onPickMonth
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

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp * uiScale)
                        ) {
                            WeekHeaderAligned(
                                cellSize = cellSize,
                                scale = uiScale,
                                modifier = Modifier.padding(horizontal = gridPad)
                            )

                            Spacer(Modifier.height(12.dp * uiScale))

                            SummaryBlockLight(
                                scale = uiScale,
                                monthTitle = monthTitleEn(month),
                                goalText = "Goal achieved 1/10 days",
                                leftStat = "82,090 steps",
                                rightStat = "794 minutes",
                                bottomStat = "3,389 kcal"
                            )

                            Spacer(Modifier.height(10.dp * uiScale))

                            CalendarGridAligned(
                                cells = cells,
                                cellSize = cellSize,
                                scale = uiScale,
                                pickedDate = pickedDate,
                                progressForDay = { date ->
                                    (dayData[date]?.overallProgress ?: 0f).coerceIn(0f, 1f)
                                },
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = detailsBottomPad)
            )
        }
    }
}

@Composable
private fun TopBarLight(
    title: String,
    scale: Float,
    onBack: () -> Unit,
    onPickMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "←",
            color = Color(0xFF111111),
            fontSize = (22.sp * scale),
            modifier = Modifier
                .padding(end = 10.dp * scale)
                .clickable { onBack() }
        )

        Text(
            text = title,
            color = Color(0xFF111111),
            fontSize = (22.sp * scale),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "🗓️",
            modifier = Modifier
                .padding(end = 14.dp * scale)
                .clickable { onPickMonth() },
            fontSize = (18.sp * scale)
        )
        Text(text = "⋮", color = Color(0xFF111111), fontSize = (22.sp * scale))
    }
}

@Composable
private fun WeekHeaderAligned(
    cellSize: Dp,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        labels.forEachIndexed { index, s ->
            Box(
                modifier = Modifier.size(cellSize),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = s,
                    color = if (index == 6) Color(0xFFE05A5A) else Color(0xFF9EA0A6),
                    fontSize = (14.sp * scale),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp * scale))
    Box(
        Modifier
            .fillMaxWidth()
            .height((1.dp * scale).coerceAtLeast(1.dp))
            .background(Color(0xFFE7E8EC))
    )
}

@Composable
private fun SummaryBlockLight(
    scale: Float,
    monthTitle: String,
    goalText: String,
    leftStat: String,
    rightStat: String,
    bottomStat: String
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp * scale)) {
        Text(
            text = monthTitle,
            color = Color(0xFF111111),
            fontSize = (18.sp * scale),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp * scale))
        Text(
            text = goalText,
            color = Color(0xFF6B6E76),
            fontSize = (16.sp * scale),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(12.dp * scale))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = leftStat,
                color = Color(0xFF111111),
                fontSize = (18.sp * scale),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(12.dp * scale))
            Text(
                text = " | ",
                color = Color(0xFFB5B7BE),
                fontSize = (18.sp * scale)
            )
            Spacer(Modifier.width(12.dp * scale))
            Text(
                text = rightStat,
                color = Color(0xFF111111),
                fontSize = (18.sp * scale),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(6.dp * scale))

        Text(
            text = bottomStat,
            color = Color(0xFF111111),
            fontSize = (18.sp * scale),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CalendarGridAligned(
    cells: List<DayCell>,
    cellSize: Dp,
    scale: Float,
    pickedDate: LocalDate,
    progressForDay: (LocalDate) -> Float,
    onDayClick: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        for (row in 0 until (GRID_CELLS / DAYS_IN_WEEK)) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until DAYS_IN_WEEK) {
                    val idx = row * DAYS_IN_WEEK + col
                    val cell = cells[idx]
                    DayCellItem(
                        cell = cell,
                        cellSize = cellSize,
                        scale = scale,
                        isPicked = cell.date == pickedDate,
                        progress = cell.date?.let(progressForDay) ?: 0f,
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
    progress: Float,
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

    val ringSize = (cellSize * 0.72f).coerceIn(26.dp, 44.dp)
    val ringStroke = (cellSize * 0.10f).coerceIn(3.dp, 5.dp)

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(pad)
            .clip(RoundedCornerShape(corner))
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (pickedAlpha > 0f) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color(0xFFEFF0F3).copy(alpha = pickedAlpha))
            )
        }

        if (date != null) {
            ProgressRingsLight(
                progress = progress,
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (2.dp * scale).coerceIn(1.dp, 3.dp))
            )
        }
    }
}

@Composable
private fun ProgressRingsLight(
    progress: Float,
    size: Dp,
    stroke: Dp,
    dim: Boolean
) {
    val p1 by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "p1"
    )
    val p2 by animateFloatAsState(
        targetValue = (progress * 0.85f).coerceIn(0f, 1f),
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "p2"
    )
    val p3 by animateFloatAsState(
        targetValue = (progress * 0.7f).coerceIn(0f, 1f),
        animationSpec = tween(590, easing = FastOutSlowInEasing),
        label = "p3"
    )

    val track = if (dim) Color(0xFFE9EAF0) else Color(0xFFE6E7EB)
    val green = if (dim) Color(0xFFBFDCC7) else Color(0xFF34C759)
    val blue = if (dim) Color(0xFFB9D7E8) else Color(0xFF0A84FF)
    val pink = if (dim) Color(0xFFE6B7C1) else Color(0xFFFF375F)

    Canvas(modifier = Modifier.size(size)) {
        fun drawRing(radius: Float, color: Color, p: Float) {
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * p,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = stroke.toPx(), cap = StrokeCap.Round)
            )
        }

        val base = size.toPx() / 2f
        val gap = stroke.toPx() + 2f

        drawRing(radius = base - 2f, color = green, p = p1)
        drawRing(radius = base - gap - 2f, color = blue, p = p2)
        drawRing(radius = base - 2f * gap - 2f, color = pink, p = p3)
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
        "$dow, $mon ${date.dayOfMonth}"
    }

    val stepsNow by animateIntAsState(
        targetValue = metrics.steps,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "stepsNow"
    )
    val minutesNow by animateIntAsState(
        targetValue = metrics.minutes,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "minutesNow"
    )
    val kcalNow by animateIntAsState(
        targetValue = metrics.kcal,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "kcalNow"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically(
            tween(220, easing = FastOutSlowInEasing)
        ) { it / 2 },
        exit = fadeOut(tween(160)) + slideOutVertically(
            tween(200, easing = FastOutSlowInEasing)
        ) { it / 2 },
        modifier = modifier
    ) {
        val padH = 16.dp * scale
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = padH),
            shape = RoundedCornerShape(22.dp * scale),
            color = Color.White,
            shadowElevation = 14.dp * scale
        ) {
            Column(modifier = Modifier.padding(14.dp * scale)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dayTitle,
                        fontSize = (16.sp * scale),
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111111),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp * scale)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F1F4))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", fontSize = (18.sp * scale), color = Color(0xFF44474F))
                    }
                }

                Spacer(Modifier.height(12.dp * scale))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressRingsLight(
                        progress = metrics.overallProgress,
                        size = (86.dp * scale).coerceIn(72.dp, 96.dp),
                        stroke = (10.dp * scale).coerceIn(8.dp, 12.dp),
                        dim = false
                    )

                    Spacer(Modifier.width(14.dp * scale))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp * scale)) {
                        MetricLine(
                            scale = scale,
                            color = Color(0xFF34C759),
                            label = "${stepsNow} / ${metrics.goalSteps} steps"
                        )
                        MetricLine(
                            scale = scale,
                            color = Color(0xFF0A84FF),
                            label = "${minutesNow} / ${metrics.goalMinutes} minutes"
                        )
                        MetricLine(
                            scale = scale,
                            color = Color(0xFFFF375F),
                            label = "${kcalNow} / ${metrics.goalKcal} kcal"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLine(
    scale: Float,
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp * scale)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp * scale))
        Text(
            text = label,
            color = Color(0xFF111111),
            fontSize = (16.sp * scale),
            fontWeight = FontWeight.Medium
        )
    }
}

@Immutable
private data class DayCell(
    val date: LocalDate?,
    val isOutsideMonth: Boolean
)

@Immutable
data class DayMetrics(
    val steps: Int,
    val minutes: Int,
    val kcal: Int,
    val goalSteps: Int,
    val goalMinutes: Int,
    val goalKcal: Int,
    val overallProgress: Float // 0..1
) {
    companion object {
        fun empty() = DayMetrics(
            steps = 0, minutes = 0, kcal = 0,
            goalSteps = 6000, goalMinutes = 90, goalKcal = 500,
            overallProgress = 0f
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildMonthGrid(month: YearMonth): List<DayCell> {
    val first = month.atDay(1)
    val last = month.atEndOfMonth()

    val firstDayIndex = (first.dayOfWeek.value + 6) % 7 // Monday-first

    val cells = ArrayList<DayCell>(GRID_CELLS)

    val prevMonth = month.minusMonths(1)
    val prevLast = prevMonth.atEndOfMonth()
    for (i in firstDayIndex - 1 downTo 0) {
        val d = prevLast.minusDays(i.toLong())
        cells += DayCell(date = d, isOutsideMonth = true)
    }

    var d = first
    while (!d.isAfter(last)) {
        cells += DayCell(date = d, isOutsideMonth = false)
        d = d.plusDays(1)
    }

    val nextMonth = month.plusMonths(1)
    var nd = nextMonth.atDay(1)
    while (cells.size < GRID_CELLS) {
        cells += DayCell(date = nd, isOutsideMonth = true)
        nd = nd.plusDays(1)
    }

    return cells
}

@RequiresApi(Build.VERSION_CODES.O)
private fun monthTitleEn(month: YearMonth): String {
    val m = month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).uppercase(Locale.ENGLISH)
    return "$m."
}

@RequiresApi(Build.VERSION_CODES.O)
private fun demoDayData(month: YearMonth): Map<LocalDate, DayMetrics> {
    val map = mutableMapOf<LocalDate, DayMetrics>()
    val goals = Triple(6000, 90, 500)

    for (day in 1..month.lengthOfMonth()) {
        val date = month.atDay(day)

        val steps = when (day) {
            1 -> 8159
            2 -> 5200
            3 -> 6100
            4 -> 7200
            5 -> 3000
            6 -> 6500
            7 -> 1800
            8 -> 7400
            9 -> 9000
            10 -> 12000
            else -> 0
        }
        val minutes = when (day) {
            1 -> 80
            2 -> 40
            3 -> 55
            4 -> 90
            5 -> 20
            6 -> 70
            7 -> 10
            8 -> 60
            9 -> 95
            10 -> 110
            else -> 0
        }
        val kcal = when (day) {
            1 -> 333
            2 -> 210
            3 -> 290
            4 -> 480
            5 -> 120
            6 -> 360
            7 -> 80
            8 -> 310
            9 -> 520
            10 -> 610
            else -> 0
        }

        val pSteps = (steps.toFloat() / goals.first).coerceIn(0f, 1f)
        val pMin = (minutes.toFloat() / goals.second).coerceIn(0f, 1f)
        val pKcal = (kcal.toFloat() / goals.third).coerceIn(0f, 1f)
        val overall = (pSteps + pMin + pKcal) / 3f

        map[date] = DayMetrics(
            steps = steps,
            minutes = minutes,
            kcal = kcal,
            goalSteps = goals.first,
            goalMinutes = goals.second,
            goalKcal = goals.third,
            overallProgress = overall
        )
    }
    return map
}
