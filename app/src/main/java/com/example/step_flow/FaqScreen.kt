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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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

@Composable
fun FaqScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    // ✅ Colors from Theme
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

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (showAnswer) showAnswer = false else onBack()
    }

    val centeredIndex by remember {
        derivedStateOf { findCenteredIndex(listState) ?: selectedIndex }
    }

    LaunchedEffect(listState) {
        snapshotFlow { findCenteredIndex(listState) }
            .filter { it != null }
            .distinctUntilChanged()
            .collect { idx ->
                val safe = idx!!.coerceIn(0, faqs.lastIndex)
                if (safe != selectedIndex) {
                    selectedIndex = safe
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { inProgress -> !inProgress }
            .collect {
                val delta = computeCenteringDeltaPx(listState) ?: return@collect
                if (abs(delta) > 1) {
                    scope.launch {
                        listState.animateScrollBy(
                            delta.toFloat(),
                            animationSpec = tween(420, easing = FastOutSlowInEasing)
                        )
                    }
                }
            }
    }

    Scaffold(containerColor = bg) { inner ->
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = 120.dp,
                            bottom = 120.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(faqs) { index, item ->
                            val isActive = index == centeredIndex

                            val alphaTarget = if (isActive) 1f else 0.18f
                            val scaleTarget = if (isActive) 1f else 0.93f

                            val a by animateFloatAsState(
                                targetValue = alphaTarget,
                                animationSpec = tween(380, easing = FastOutSlowInEasing),
                                label = "pillAlpha"
                            )
                            val s by animateFloatAsState(
                                targetValue = scaleTarget,
                                animationSpec = tween(380, easing = FastOutSlowInEasing),
                                label = "pillScale"
                            )

                            FaqPillFullscreen(
                                text = item.question,
                                active = isActive,
                                alpha = a,
                                scale = s,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    scope.launch { listState.animateScrollToItem(index) }
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
                    text = "View answer",
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
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .height(54.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.primary) // ✅ Adaptive Primary
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
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
            .graphicsLayer(
                alpha = alpha,
                scaleX = scale,
                scaleY = scale
            )
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
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        label = "overlayProgress"
    )

    if (!visible && progress <= 0.001f) return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // Allow drag to dismiss
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
    ) {
        val heightPx = with(density) { maxHeight.toPx() }
        val offsetY = (1f - progress) * heightPx

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = offsetY }
        ) {

            // Scrim (stays black usually, but adjusted alpha)
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

                Text(
                    text = answer,
                    color = onSheetVar, // ✅
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(18.dp))

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
    } else {
        this
    }
}