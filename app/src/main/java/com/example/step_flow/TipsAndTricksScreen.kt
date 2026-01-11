package com.example.step_flow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class TipItem(
    val title: String,
    val subtitle: String,
    val body: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun TipsAndTricksScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val bg = Color.White
    val titleColor = Color(0xFF111111)
    val hint = Color(0xFF6F747C)
    val cardBg = Color(0xFFF3F5F8)
    val divider = Color(0xFFE6E9EF)

    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // какой элемент открыт (один за раз)
    var expandedIndex by rememberSaveable { mutableIntStateOf(-1) }

    // контент tips (под StepFlow)
    val tips = remember {
        listOf(
            TipItem(
                title = "Start runs faster",
                subtitle = "Warm up GPS before pressing RUN",
                body = "Open Home and wait 5–10 seconds before starting a run. "
                        + "If accuracy feels off, switch Location to High accuracy and make sure battery saver is off for StepFlow.",
                icon = Icons.Outlined.LocationOn
            ),
            TipItem(
                title = "Cleaner progress",
                subtitle = "Keep your personal details updated",
                body = "Go to Profile → Personal Details and fill height/weight/age. "
                        + "This helps us improve stats and future recommendations.",
                icon = Icons.Outlined.Person
            ),
            TipItem(
                title = "Use the calendar like a dashboard",
                subtitle = "Quick view of your month",
                body = "On Home, tap Calendar tile or use the Calendar tab. "
                        + "Pick any date to review activity (we’ll expand details later).",
                icon = Icons.Outlined.Timeline
            ),
            TipItem(
                title = "Don’t miss reminders",
                subtitle = "Enable notifications",
                body = "Profile → Settings → Notifications. "
                        + "Turn them on to receive future streak alerts and scheduled reminders.",
                icon = Icons.Outlined.Notifications
            ),
            TipItem(
                title = "Make it feel like your app",
                subtitle = "Language, units and font size",
                body = "In Settings you can choose Language, Units (Metric/Imperial), Theme and Font size. "
                        + "Try slightly larger font if you use StepFlow during movement.",
                icon = Icons.Outlined.Settings
            ),
            TipItem(
                title = "Smooth tracking",
                subtitle = "Avoid background restrictions",
                body = "If your device kills background tasks, allow StepFlow to run unrestricted: "
                        + "Settings → Apps → StepFlow → Battery → Unrestricted (names may vary by phone).",
                icon = Icons.Outlined.Speed
            ),
            TipItem(
                title = "Simple habit trick",
                subtitle = "Make the first step tiny",
                body = "If motivation drops, open StepFlow and only do this: tap Home and look at the RUN circle. "
                        + "Small friction-free actions build consistency.",
                icon = Icons.Outlined.Lightbulb
            )
        )
    }

    // Back always returns
    BackHandler(enabled = true) { onBack() }

    Scaffold(containerColor = bg) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(bg)
                .padding(inner)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = titleColor
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tips & Tricks",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Tap a card to expand • Smooth, simple and practical",
                fontSize = 13.sp,
                color = hint,
                modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(tips) { index, item ->
                    val expanded = expandedIndex == index

                    TipCard(
                        item = item,
                        expanded = expanded,
                        cardBg = cardBg,
                        divider = divider,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            expandedIndex = if (expanded) -1 else index

                            // докрутка, чтобы раскрытый текст не уплыл под низ
                            scope.launch {
                                // чуть подождём анимацию (субъективно приятнее)
                                kotlinx.coroutines.delay(60)
                                listState.animateScrollToItem(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TipCard(
    item: TipItem,
    expanded: Boolean,
    cardBg: Color,
    divider: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "chevronRotation"
    )

    Surface(
        shape = shape,
        color = cardBg,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = Color(0xFF111111),
                    modifier = Modifier.alpha(0.95f)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111111)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.subtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF6F747C)
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF111111).copy(alpha = 0.60f),
                    modifier = Modifier
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(220)) + expandVertically(tween(260, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(180)) + shrinkVertically(tween(220, easing = FastOutSlowInEasing))
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(divider)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = item.body,
                        fontSize = 14.sp,
                        color = Color(0xFF2A2D33),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
