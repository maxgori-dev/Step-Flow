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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.fillMaxSize()) {

            TopBarLight(
                title = "Activity",
                onBack = onBack,
                onPickMonth = onPickMonth
            )

            Spacer(Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val cellSize = (maxWidth - 24.dp) / DAYS_IN_WEEK // 12dp слева + 12dp справа

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    ) {
                        WeekHeaderAligned(
                            cellSize = cellSize,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        SummaryBlockLight(
                            monthTitle = monthTitleEn(month),
                            goalText = "Goal achieved 1/10 days",
                            leftStat = "82,090 steps",
                            rightStat = "794 minutes",
                            bottomStat = "3,389 kcal"
                        )

                        Spacer(Modifier.height(10.dp))

                        CalendarGridAligned(
                            cells = cells,
                            cellSize = cellSize,
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
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }

        DayDetailsPanel(
            visible = showDetails,
            date = pickedDate,
            metrics = pickedMetrics,
            onClose = { showDetails = false },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }
}


@Composable
private fun TopBarLight(
    title: String,
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
            fontSize = 22.sp,
            modifier = Modifier
                .padding(end = 10.dp)
                .clickable { onBack() }
        )

        Text(
            text = title,
            color = Color(0xFF111111),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "🗓️",
            modifier = Modifier
                .padding(end = 14.dp)
                .clickable { onPickMonth() },
            fontSize = 18.sp
        )
        Text(text = "⋮", color = Color(0xFF111111), fontSize = 22.sp)
    }
}


@Composable
private fun WeekHeaderAligned(
    cellSize: Dp,
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE7E8EC))
    )
}

@Composable
private fun SummaryBlockLight(
    monthTitle: String,
    goalText: String,
    leftStat: String,
    rightStat: String,
    bottomStat: String
) {
    Column(modifier = Modifier.padding(horizontal = 14.dp)) {
        Text(
            text = monthTitle,
            color = Color(0xFF111111),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = goalText,
            color = Color(0xFF6B6E76),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = leftStat,
                color = Color(0xFF111111),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = " | ",
                color = Color(0xFFB5B7BE),
                fontSize = 18.sp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = rightStat,
                color = Color(0xFF111111),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = bottomStat,
            color = Color(0xFF111111),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun CalendarGridAligned(
    cells: List<DayCell>,
    cellSize: Dp,
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

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(6.dp)
            .clip(RoundedCornerShape(16.dp))
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
                size = 34.dp,
                stroke = 4.dp,
                dim = cell.isOutsideMonth
            )

            Text(
                text = dayText,
                color = when {
                    cell.isOutsideMonth -> Color(0xFFB8BBC2)
                    isSunday -> Color(0xFFE05A5A)
                    else -> Color(0xFF34363C)
                },
                fontSize = 13.sp,
                fontWeight = if (isPicked) FontWeight.SemiBold else FontWeight.Medium,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)
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
        enter = fadeIn(tween(180)) + slideInVertically(tween(220, easing = FastOutSlowInEasing)) { it / 2 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(200, easing = FastOutSlowInEasing)) { it / 2 },
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 14.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dayTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111111),
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F1F4))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", fontSize = 18.sp, color = Color(0xFF44474F))
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressRingsLight(
                        progress = metrics.overallProgress,
                        size = 86.dp,
                        stroke = 10.dp,
                        dim = false
                    )

                    Spacer(Modifier.width(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricLine(color = Color(0xFF34C759), label = "${stepsNow} / ${metrics.goalSteps} steps")
                        MetricLine(color = Color(0xFF0A84FF), label = "${minutesNow} / ${metrics.goalMinutes} minutes")
                        MetricLine(color = Color(0xFFFF375F), label = "${kcalNow} / ${metrics.goalKcal} kcal")
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLine(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            color = Color(0xFF111111),
            fontSize = 16.sp,
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

    val firstDayIndex = (first.dayOfWeek.value + 6) % 7

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
