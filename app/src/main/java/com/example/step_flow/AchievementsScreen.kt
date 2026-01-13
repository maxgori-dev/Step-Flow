package com.example.step_flow

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.step_flow.data.RunModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min

private data class AchievementUi(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val pointsG: Int,
    val isUnlocked: Boolean,
    val progress: Int,
    val target: Int
)

@Composable
private fun rememberUiScale(): Float {
    val cfg = LocalConfiguration.current
    val sw = cfg.screenWidthDp.toFloat() / 390f
    val sh = cfg.screenHeightDp.toFloat() / 840f
    return min(sw, sh).coerceIn(0.85f, 1.08f)
}

private fun dpScaled(base: Float, s: Float, minDp: Float, maxDp: Float): Dp {
    return (base * s).coerceIn(minDp, maxDp).dp
}

private fun spScaled(base: Float, s: Float, minSp: Float, maxSp: Float): TextUnit {
    return (base * s).coerceIn(minSp, maxSp).sp
}

@Composable
private fun IosAppear(
    modifier: Modifier = Modifier,
    fromDp: Float = 14f,
    scaleFrom: Float = 0.985f,
    duration: Int = 520,
    content: @Composable () -> Unit
) {
    val iosEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = duration, easing = iosEase),
        label = "appearAlpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else scaleFrom,
        animationSpec = tween(durationMillis = duration, easing = iosEase),
        label = "appearScale"
    )

    val density = LocalDensity.current
    val fromPx = remember(fromDp, density) { with(density) { fromDp.dp.toPx() } }

    val ty by animateFloatAsState(
        targetValue = if (shown) 0f else fromPx,
        animationSpec = tween(durationMillis = duration, easing = iosEase),
        label = "appearTy"
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = ty
            this.scaleX = scale
            this.scaleY = scale
        }
    ) { content() }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    runs: List<RunModel>,
    name: String,
    avatarUriString: String,
    onBack: () -> Unit,
    streakTargetDays: Int = 7,
    flowTargetRuns: Int = 10,
    weeklyDistanceGoalKm: Float = 20f
) {
    val uiScale = rememberUiScale()

    val bg = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onSurface

    val achievements = remember(runs) {
        buildAchievements(
            runs = runs,
            streakTargetDays = streakTargetDays,
            flowTargetRuns = flowTargetRuns,
            weeklyDistanceGoalKm = weeklyDistanceGoalKm
        )
    }

    val totalPoints = remember(achievements) {
        achievements.filter { it.isUnlocked }.sumOf { it.pointsG }
    }

    val avatarBitmap: ImageBitmap? by rememberAvatarBitmap(avatarUriString)
    val displayName = name.trim().ifBlank { "Maxgori" }

    var selected by remember { mutableStateOf<AchievementUi?>(null) }
    if (selected != null) BackHandler { selected = null }

    val topTitleSize = spScaled(base = 18f, s = uiScale, minSp = 16f, maxSp = 20f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Achievements", fontWeight = FontWeight.Bold, fontSize = topTitleSize) },
                navigationIcon = {
                    IconButton(onClick = { if (selected != null) selected = null else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = textPrimary
                )
            )
        },
        containerColor = bg
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                IosAppear(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = dpScaled(14f, uiScale, 12f, 18f),
                            vertical = dpScaled(12f, uiScale, 10f, 16f)
                        )
                ) {
                    AchievementsHeader(
                        uiScale = uiScale,
                        displayName = displayName,
                        avatarBitmap = avatarBitmap,
                        totalPoints = totalPoints,
                        unlockedCount = achievements.count { it.isUnlocked },
                        totalCount = achievements.size
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = dpScaled(14f, uiScale, 12f, 18f),
                        end = dpScaled(14f, uiScale, 12f, 18f),
                        bottom = dpScaled(14f, uiScale, 12f, 18f)
                    ),
                    verticalArrangement = Arrangement.spacedBy(dpScaled(10f, uiScale, 8f, 14f))
                ) {
                    items(items = achievements, key = { it.id }) { a ->
                        IosAppear {
                            XboxAchievementCard(
                                a = a,
                                uiScale = uiScale,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selected = a }
                            )
                        }
                    }
                }
            }

            AchievementCenterOverlay(
                a = selected,
                uiScale = uiScale,
                onDismiss = { selected = null }
            )
        }
    }
}

@Composable
private fun AchievementsHeader(
    uiScale: Float,
    displayName: String,
    avatarBitmap: ImageBitmap?,
    totalPoints: Int,
    unlockedCount: Int,
    totalCount: Int
) {
    val cardShape = RoundedCornerShape(dpScaled(26f, uiScale, 22f, 30f))

    val cardBg = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.secondaryContainer
    val textMain = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val green = Color(0xFF86D400)

    val ratio =
        if (totalCount > 0) (unlockedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (ratio * 100f).toInt()

    val headerPadH = dpScaled(18f, uiScale, 14f, 22f)
    val headerPadV = dpScaled(18f, uiScale, 14f, 22f)

    val avatarFrame = dpScaled(90f, uiScale, 72f, 110f)
    val avatarSize = dpScaled(82f, uiScale, 64f, 102f)

    val titleSize = spScaled(18f, uiScale, 15f, 20f)
    val labelSize = spScaled(12f, uiScale, 10f, 13f)
    val scoreSize = spScaled(34f, uiScale, 26f, 40f)
    val chipText = spScaled(12f, uiScale, 10f, 13f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = dpScaled(2f, uiScale, 1f, 3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, cardShape)
                .padding(horizontal = headerPadH, vertical = headerPadV)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(avatarFrame)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                        .border(dpScaled(1.5f, uiScale, 1f, 2f), border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarBitmap != null) {
                        Image(
                            painter = BitmapPainter(avatarBitmap),
                            contentDescription = null,
                            modifier = Modifier
                                .size(avatarSize)
                                .clip(CircleShape)
                        )
                    } else {
                        AvatarPlaceholder(modifier = Modifier.size(avatarSize))
                    }
                }

                Spacer(Modifier.width(dpScaled(14f, uiScale, 10f, 18f)))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        fontSize = titleSize,
                        fontWeight = FontWeight.SemiBold,
                        color = textMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(dpScaled(6f, uiScale, 4f, 8f)))

                    Text(
                        text = "TOTAL SCORE",
                        fontSize = labelSize,
                        fontWeight = FontWeight.Black,
                        color = textMuted,
                        letterSpacing = spScaled(0.6f, uiScale, 0.4f, 0.9f)
                    )

                    Spacer(Modifier.height(dpScaled(6f, uiScale, 4f, 8f)))

                    Text(
                        text = "${totalPoints}G",
                        fontSize = scoreSize,
                        fontWeight = FontWeight.Black,
                        color = green
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, border, RoundedCornerShape(999.dp))
                        .padding(
                            horizontal = dpScaled(12f, uiScale, 10f, 16f),
                            vertical = dpScaled(8f, uiScale, 6f, 10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percent%  $unlockedCount/$totalCount",
                        fontSize = chipText,
                        fontWeight = FontWeight.Bold,
                        color = textMuted,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(dpScaled(14f, uiScale, 10f, 18f)))

            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dpScaled(12f, uiScale, 9f, 14f))
                    .clip(RoundedCornerShape(999.dp)),
                trackColor = border,
                color = green
            )
        }
    }
}

@Composable
private fun AchievementCenterOverlay(
    a: AchievementUi?,
    uiScale: Float,
    onDismiss: () -> Unit
) {
    val visible = a != null
    val iosEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (visible) 220 else 180, easing = iosEase),
        label = "scrim"
    )

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = tween(durationMillis = if (visible) 520 else 220, easing = iosEase),
        label = "centerScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (visible) 240 else 160, easing = iosEase),
        label = "centerAlpha"
    )

    if (scrimAlpha > 0.001f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f * scrimAlpha))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            if (a != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .graphicsLayer {
                            alpha = contentAlpha
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable(enabled = false) {}
                ) {
                    XboxAchievementCard(
                        a = a,
                        uiScale = uiScale * 1.08f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun XboxAchievementCard(
    a: AchievementUi,
    uiScale: Float,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(dpScaled(18f, uiScale, 16f, 22f))

    val cardBg = MaterialTheme.colorScheme.surface
    val border = MaterialTheme.colorScheme.secondaryContainer
    val textMain = MaterialTheme.colorScheme.onSurface
    val textMuted = MaterialTheme.colorScheme.onSurfaceVariant
    val green = Color(0xFF86D400)

    val statusText = if (a.isUnlocked) "ACHIEVEMENT UNLOCKED" else "ACHIEVEMENT LOCKED"
    val statusColor = if (a.isUnlocked) green else textMuted

    val showProgress = !a.isUnlocked && a.target > 1
    val ratio = if (a.target > 0) (a.progress.toFloat() / a.target.toFloat()).coerceIn(0f, 1f) else 0f
    val percent = (ratio * 100f).toInt()

    Card(
        modifier = modifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = dpScaled(1f, uiScale, 1f, 2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, border, cardShape)
                .padding(
                    horizontal = dpScaled(14f, uiScale, 12f, 18f),
                    vertical = dpScaled(12f, uiScale, 10f, 16f)
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(dpScaled(44f, uiScale, 40f, 54f))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        .border(2.dp, border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(dpScaled(32f, uiScale, 28f, 40f))
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            .border(2.dp, green.copy(alpha = 0.65f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(a.iconRes),
                            contentDescription = a.title,
                            modifier = Modifier.size(dpScaled(20f, uiScale, 18f, 26f))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(dpScaled(12f, uiScale, 10f, 16f)))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusText,
                        fontSize = spScaled(12f, uiScale, 11f, 14f),
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        letterSpacing = spScaled(0.5f, uiScale, 0.3f, 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dpScaled(2f, uiScale, 2f, 6f)))

                    val rightPart = if (a.isUnlocked) a.description else "Progress ${a.progress}/${a.target}"
                    Text(
                        text = "${a.pointsG}G  -  $rightPart",
                        fontSize = spScaled(13f, uiScale, 12f, 15f),
                        fontWeight = FontWeight.SemiBold,
                        color = textMain,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(dpScaled(2f, uiScale, 2f, 6f)))

                    Text(
                        text = a.title,
                        fontSize = spScaled(12f, uiScale, 11f, 14f),
                        color = textMuted,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(dpScaled(8f, uiScale, 6f, 12f)))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (a.isUnlocked) green.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background)
                        .border(
                            1.dp,
                            if (a.isUnlocked) green.copy(alpha = 0.35f) else border,
                            RoundedCornerShape(999.dp)
                        )
                        .padding(
                            horizontal = dpScaled(10f, uiScale, 8f, 14f),
                            vertical = dpScaled(6f, uiScale, 5f, 10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (a.isUnlocked) "DONE" else "IN PROGRESS",
                        fontSize = spScaled(11f, uiScale, 10f, 13f),
                        fontWeight = FontWeight.Bold,
                        color = if (a.isUnlocked) green else textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (showProgress) {
                Spacer(modifier = Modifier.height(dpScaled(10f, uiScale, 8f, 14f)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .weight(1f)
                            .height(dpScaled(8f, uiScale, 7f, 12f))
                            .clip(RoundedCornerShape(999.dp)),
                        trackColor = border,
                        color = green
                    )
                    Spacer(modifier = Modifier.width(dpScaled(10f, uiScale, 8f, 14f)))
                    Text(
                        text = "$percent%  ${a.progress}/${a.target}",
                        fontSize = spScaled(12f, uiScale, 11f, 14f),
                        color = textMuted,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildAchievements(
    runs: List<RunModel>,
    streakTargetDays: Int,
    flowTargetRuns: Int,
    weeklyDistanceGoalKm: Float
): List<AchievementUi> {

    val totalRuns = runs.size

    val dates = runs.map { tsToLocalDate(it.timestamp) }.distinct().sorted()
    val maxStreak = maxConsecutiveDays(dates)
    val last7DaysDistanceKm = distanceKmLastDays(runs, days = 7)

    val firstStepUnlocked = totalRuns >= 1
    val streakUnlocked = maxStreak >= streakTargetDays
    val weeklyGoalReached = last7DaysDistanceKm >= weeklyDistanceGoalKm
    val flowUnlocked = totalRuns >= flowTargetRuns

    return listOf(
        AchievementUi(
            id = "first_step",
            title = "First Step",
            description = "Completed your first activity",
            iconRes = R.drawable.firststep,
            pointsG = 50,
            isUnlocked = firstStepUnlocked,
            progress = min(totalRuns, 1),
            target = 1
        ),
        AchievementUi(
            id = "streak_7",
            title = "7-Day Streak",
            description = "Stayed active for $streakTargetDays days",
            iconRes = R.drawable.ic_award_streak_7,
            pointsG = 150,
            isUnlocked = streakUnlocked,
            progress = min(maxStreak, streakTargetDays),
            target = streakTargetDays
        ),
        AchievementUi(
            id = "goal_reached",
            title = "Goal Reached",
            description = "Hit ${weeklyDistanceGoalKm.toInt()} km in 7 days",
            iconRes = R.drawable.ic_award_goal_reached,
            pointsG = 200,
            isUnlocked = weeklyGoalReached,
            progress = min(last7DaysDistanceKm.toInt(), weeklyDistanceGoalKm.toInt().coerceAtLeast(1)),
            target = weeklyDistanceGoalKm.toInt().coerceAtLeast(1)
        ),
        AchievementUi(
            id = "flow_state",
            title = "Flow State",
            description = "Completed $flowTargetRuns activities",
            iconRes = R.drawable.ic_award_flow_state,
            pointsG = 300,
            isUnlocked = flowUnlocked,
            progress = min(totalRuns, flowTargetRuns),
            target = flowTargetRuns
        )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun tsToLocalDate(timestampMs: Long): LocalDate {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(timestampMs).atZone(zone).toLocalDate()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun maxConsecutiveDays(sortedDates: List<LocalDate>): Int {
    if (sortedDates.isEmpty()) return 0
    var best = 1
    var cur = 1
    for (i in 1 until sortedDates.size) {
        val prev = sortedDates[i - 1]
        val now = sortedDates[i]
        if (prev.plusDays(1) == now) {
            cur++
            best = maxOf(best, cur)
        } else {
            cur = 1
        }
    }
    return best
}

@RequiresApi(Build.VERSION_CODES.O)
private fun distanceKmLastDays(runs: List<RunModel>, days: Int): Float {
    if (runs.isEmpty()) return 0f
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val from = today.minusDays((days - 1).toLong())

    val meters = runs.sumOf { run ->
        val d = tsToLocalDate(run.timestamp)
        if (!d.isBefore(from) && !d.isAfter(today)) run.distanceMeters.toDouble() else 0.0
    }
    return (meters / 1000.0).toFloat()
}

@Composable
private fun rememberAvatarBitmap(uriString: String): State<ImageBitmap?> {
    val context = LocalContext.current

    return produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        if (uriString.isBlank()) {
            value = null
            return@produceState
        }

        try {
            val uri = Uri.parse(uriString)
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            value = bitmap.asImageBitmap()
        } catch (_: Throwable) {
            value = null
        }
    }
}

@Composable
private fun AvatarPlaceholder(modifier: Modifier = Modifier) {
    val bg = MaterialTheme.colorScheme.secondaryContainer
    val fg = MaterialTheme.colorScheme.onSurfaceVariant

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
    ) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = fg,
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.42f)
        )
        drawCircle(
            color = fg,
            radius = w * 0.28f,
            center = Offset(w * 0.5f, h * 0.82f)
        )
    }
}