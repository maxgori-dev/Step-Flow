package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ProfileSetupScreen(
    heightCm: String,
    weightKg: String,
    ageYears: String,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val defaultWeight = 68
    val defaultHeight = 172
    val defaultAge = 34

    val weight = weightKg.toIntOrNull() ?: defaultWeight
    val height = heightCm.toIntOrNull() ?: defaultHeight
    val age = ageYears.toIntOrNull() ?: defaultAge

    val bg = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val onBg = MaterialTheme.colorScheme.onBackground
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer
    val outlineSoft = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Profile setup",
                modifier = Modifier.align(Alignment.TopStart),
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = onBg
            )

            ResetPill(
                modifier = Modifier.align(Alignment.TopEnd),
                container = secondaryContainer,
                content = onSecondaryContainer,
                onClick = {
                    onWeightChange(defaultWeight.toString())
                    onHeightChange(defaultHeight.toString())
                    onAgeChange(defaultAge.toString())
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Select your current weight, height and age.",
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = onBg.copy(alpha = 0.60f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 22.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = surface,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CenterMetricBlock(
                    label = "Weight",
                    valueText = "$weight kg",
                    labelColor = onSurfaceVar,
                    valueColor = onSurface,
                    divider = outlineSoft
                ) {
                    MinimalSlider(
                        value = weight,
                        min = 40,
                        max = 120,
                        widthFraction = 1f,
                        track = secondaryContainer,
                        dot = onSurfaceVar.copy(alpha = 0.45f),
                        thumb = primary,
                        onThumb = MaterialTheme.colorScheme.onPrimary
                    ) { newValue ->
                        if (newValue != weight) {
                            onWeightChange(newValue.toString())
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                CenterMetricBlock(
                    label = "Height (cm)",
                    valueText = "$height cm",
                    labelColor = onSurfaceVar,
                    valueColor = onSurface,
                    divider = outlineSoft
                ) {
                    MinimalSlider(
                        value = height,
                        min = 140,
                        max = 210,
                        widthFraction = 1f,
                        track = secondaryContainer,
                        dot = onSurfaceVar.copy(alpha = 0.45f),
                        thumb = primary,
                        onThumb = MaterialTheme.colorScheme.onPrimary
                    ) { newValue ->
                        if (newValue != height) {
                            onHeightChange(newValue.toString())
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                CenterMetricBlock(
                    label = "Age",
                    valueText = "$age",
                    labelColor = onSurfaceVar,
                    valueColor = onSurface,
                    divider = Color.Transparent
                ) {
                    MinimalSlider(
                        value = age,
                        min = 14,
                        max = 80,
                        widthFraction = 1f,
                        track = secondaryContainer,
                        dot = onSurfaceVar.copy(alpha = 0.45f),
                        thumb = primary,
                        onThumb = MaterialTheme.colorScheme.onPrimary
                    ) { newValue ->
                        if (newValue != age) {
                            onAgeChange(newValue.toString())
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CenterMetricBlock(
    label: String,
    valueText: String,
    labelColor: Color,
    valueColor: Color,
    divider: Color,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = labelColor,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = valueText,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        content()

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(divider)
        )
    }
}

@Composable
private fun ResetPill(
    modifier: Modifier = Modifier,
    container: Color,
    content: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        color = container,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("↻", fontSize = 14.sp, color = content.copy(alpha = 0.85f))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Reset",
                fontSize = 14.sp,
                color = content.copy(alpha = 0.85f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MinimalSlider(
    value: Int,
    min: Int,
    max: Int,
    widthFraction: Float = 0.82f,
    height: Dp = 56.dp,
    track: Color,
    dot: Color,
    thumb: Color,
    onThumb: Color,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val clamped = value.coerceIn(min, max)
    val progress = (clamped - min).toFloat() / (max - min).toFloat()

    val horizontalPadding = 22.dp
    val thumbSize = 38.dp
    val dotsCount = 20

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(track)
            .pointerInput(min, max) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.pressed) {
                            val w = size.width.toFloat().coerceAtLeast(1f)
                            val p = (change.position.x / w).coerceIn(0f, 1f)
                            val newValue = min + (p * (max - min)).roundToInt()
                            onValueChange(newValue.coerceIn(min, max))
                            change.consume()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(dotsCount) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(dot)
                    )
                }
            }
        }

        val padPx = with(density) { horizontalPadding.toPx() }
        val thumbPx = with(density) { thumbSize.toPx() }
        val wPx = with(density) { maxWidth.toPx() }
        val usable = (wPx - padPx * 2f - thumbPx).coerceAtLeast(0f)
        val xPx = padPx + usable * progress

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(xPx.roundToInt(), 0) }
                .size(thumbSize),
            shape = CircleShape,
            color = thumb,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = clamped.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = onThumb
                )
            }
        }
    }
}
