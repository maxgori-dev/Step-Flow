package com.example.step_flow

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.abs

data class FaqItem(
    val question: String,
    val answer: String
)

private const val LOOP_MULTIPLIER = 400

@Composable
fun FaqScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    // ✅ Theme Colors
    val bg = MaterialTheme.colorScheme.background
    val title = MaterialTheme.colorScheme.onBackground
    val hint = MaterialTheme.colorScheme.onSurfaceVariant

    val faqs = remember {
        listOf(
            FaqItem(
                "How do I start a run?",
                "Go to Home and tap the RUN circle. Allow GPS permissions for accurate tracking. Your session is saved automatically."
            ),
            FaqItem(
                "Where can I see my activity calendar?",
                "On Home, tap the Calendar tile or switch to the Calendar tab in the bottom bar."
            ),
            FaqItem(
                "How do I edit my personal data?",
                "Profile → Personal Details. You can change name, height, weight and age."
            ),
            FaqItem(
                "How do I change language and units?",
                "Profile → Settings. Choose Language and Units (Metric/Imperial), then press Save."
            ),
            FaqItem(
                "Does StepFlow work offline?",
                "Profile & settings work offline. Tracking needs location services. Sync features may require internet later."
            ),
            FaqItem(
                "Why is GPS accuracy sometimes poor?",
                "GPS can drift near tall buildings/trees. Enable High Accuracy and wait a few seconds before starting."
            ),
            FaqItem(
                "How do notifications work?",
                "In Settings you can enable/disable notifications. Later we’ll add time-based reminders and streak alerts."
            ),
            FaqItem(
                "Will my data be saved if I close the app?",
                "Yes. Profile/settings are stored locally. Activity data remains on device and persists after restart."
            )
        )
    }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    val totalCount = remember(faqs) { faqs.size * LOOP_MULTIPLIER }
    val centerStart = remember(faqs) { (totalCount / 2) - ((totalCount / 2) % faqs.size) }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (showAnswer) showAnswer = false else onBack()
    }

    LaunchedEffect(faqs.size) {
        if (faqs.isNotEmpty()) listState.scrollToItem(centerStart)
    }

    fun realIndex(virtualIndex: Int): Int {
        val n = faqs.size
        if (n == 0) return 0
        val m = virtualIndex % n
        return if (m < 0) m + n else m
    }

    val centeredVirtualIndex by remember {
        derivedStateOf { findCenteredIndex(listState) ?: listState.firstVisibleItemIndex }
    }

    LaunchedEffect(listState) {
        snapshotFlow { findCenteredIndex(listState) }
            .filter { it != null }
            .distinctUntilChanged()
            .collect { idx ->
                val r = realIndex(idx!!)
                if (r != selectedIndex) {
                    selectedIndex = r
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
    }

    val snapFling = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { inProgress -> !inProgress }
            .collect {
                val delta = computeCenteringDeltaPx(listState) ?: return@collect
                if (abs(delta) > 1) {
                    scope.launch { listState.animateScrollBy(delta.toFloat(), animationSpec = tween(320)) }
                }
                val idx = findCenteredIndex(listState) ?: return@collect
                val n = faqs.size
                if (n == 0) return@collect
                if (idx < n || idx > totalCount - n) {
                    val keepReal = realIndex(idx)
                    val target = centerStart + keepReal
                    scope.launch { listState.scrollToItem(target) }
                }
            }
    }

    Scaffold(
        containerColor = bg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { inner ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(bg)
                .padding(inner)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .blurIfAvailable(enabled = showAnswer, radiusPx = 18f)
                    .alpha(if (showAnswer) 0.96f else 1f)
                    .statusBarsPadding()
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FAQ",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = title, // ✅ Adaptive
                    letterSpacing = 1.2.sp
                )

                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = snapFling,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 120.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(totalCount) { vIndex ->
                            val rIndex = realIndex(vIndex)
                            val item = faqs[rIndex]
                            val isActive = vIndex == centeredVirtualIndex

                            val a by animateFloatAsState(
                                targetValue = if (isActive) 1f else 0.18f,
                                animationSpec = tween(280),
                                label = "pillAlpha"
                            )
                            val s by animateFloatAsState(
                                targetValue = if (isActive) 1f else 0.93f,
                                animationSpec = tween(280),
                                label = "pillScale"
                            )

                            FaqPillFullscreen(
                                text = item.question,
                                active = isActive,
                                alpha = a,
                                scale = s,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch { listState.animateScrollToItem(vIndex) }
                                }
                            )
                        }
                    }

                    Text(
                        text = "Scroll to pick a question",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = hint, // ✅ Adaptive
                        letterSpacing = 0.6.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 86.dp)
                    )
                }

                AnswerButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAnswer = true
                    }
                )

                Spacer(Modifier.height(18.dp))
            }

            AnswerOverlayFullscreen(
                visible = showAnswer,
                question = faqs[selectedIndex].question,
                answer = faqs[selectedIndex].answer,
                onClose = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showAnswer = false
                }
            )
        }
    }
}

@Composable
private fun AnswerButton(
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 14.dp)
            .height(54.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary) // ✅ Adaptive Primary
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "View answer",
            color = MaterialTheme.colorScheme.onPrimary, // ✅ Adaptive OnPrimary
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.2.sp
        )
    }
}

@Composable
private fun FaqPillFullscreen(
    text: String,
    active: Boolean,
    alpha: Float,
    scale: Float,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)

    // ✅ Adaptive colors for pills
    // Active: Primary, Inactive: SecondaryContainer
    val bg = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.70f)
    val fg = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 15.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun AnswerOverlayFullscreen(
    visible: Boolean,
    question: String,
    answer: String,
    onClose: () -> Unit
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(320),
        label = "overlayProgress"
    )

    if (!visible && progress <= 0.001f) return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(visible) {
                val thresholdPx = with(density) { 80.dp.toPx() }
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { _, dragAmount -> totalDrag += dragAmount },
                    onDragEnd = {
                        if (totalDrag > thresholdPx) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClose()
                        }
                    }
                )
            }
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val heightPx = with(density) { maxHeight.toPx() }
        val offsetY = (1f - progress) * heightPx

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = offsetY }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )

            val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

            // ✅ Sheet Background adapted to theme
            val sheetBg = MaterialTheme.colorScheme.surface
            val onSheet = MaterialTheme.colorScheme.onSurface
            val onSheetVar = MaterialTheme.colorScheme.onSurfaceVariant

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter)
                    .clip(shape)
                    .background(sheetBg.copy(alpha = 0.96f)) // Slightly opaque surface
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .statusBarsPadding()
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = onSheet // ✅
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Answer",
                        color = onSheet, // ✅
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClose()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = onSheet // ✅
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = question,
                    color = onSheet, // ✅
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = answer,
                        color = onSheetVar, // ✅
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(Modifier.height(18.dp))
                }

                Text(
                    text = "Swipe down to close",
                    color = onSheetVar, // ✅
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun findCenteredIndex(state: LazyListState): Int? {
    val layout = state.layoutInfo
    val items = layout.visibleItemsInfo
    if (items.isEmpty()) return null
    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    val closest = items.minByOrNull { it.distanceToCenter(viewportCenter) } ?: return null
    return closest.index
}

private fun computeCenteringDeltaPx(state: LazyListState): Int? {
    val layout = state.layoutInfo
    val items = layout.visibleItemsInfo
    if (items.isEmpty()) return null
    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    val closest = items.minByOrNull { it.distanceToCenter(viewportCenter) } ?: return null
    val itemCenter = closest.offset + closest.size / 2
    return itemCenter - viewportCenter
}

private fun LazyListItemInfo.distanceToCenter(viewportCenter: Int): Int {
    val itemCenter = offset + size / 2
    return abs(itemCenter - viewportCenter)
}

@Composable
private fun Modifier.blurIfAvailable(enabled: Boolean, radiusPx: Float): Modifier {
    if (!enabled) return this
    val radiusDp = with(LocalDensity.current) { radiusPx.toDp() }
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.blur(
            radius = radiusDp,
            edgeTreatment = androidx.compose.ui.draw.BlurredEdgeTreatment.Unbounded
        )
    } else this
}