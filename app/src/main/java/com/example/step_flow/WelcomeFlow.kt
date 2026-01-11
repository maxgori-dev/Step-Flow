package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    onContinue: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val defaultWeight = 68
    val defaultHeight = 172
    val defaultAge = 34

    var weight by remember { mutableIntStateOf(defaultWeight) }
    var height by remember { mutableIntStateOf(defaultHeight) }
    var age by remember { mutableIntStateOf(defaultAge) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Header
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Profile setup",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2C2C2E)
            )

            ResetPill(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = {
                    weight = defaultWeight
                    height = defaultHeight
                    age = defaultAge
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }

        Spacer(Modifier.height(36.dp))

        CenterMetricBlock(
            label = "Weight",
            valueText = "$weight kg"
        ) {
            MinimalSlider(
                value = weight,
                min = 40,
                max = 120
            ) {
                weight = it
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }

        Spacer(Modifier.height(36.dp))

        CenterMetricBlock(
            label = "Height (cm)",
            valueText = "$height cm"
        ) {
            MinimalSlider(
                value = height,
                min = 140,
                max = 210
            ) {
                height = it
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }

        Spacer(Modifier.height(36.dp))

        CenterMetricBlock(
            label = "Age",
            valueText = "$age"
        ) {
            MinimalSlider(
                value = age,
                min = 14,
                max = 80
            ) {
                age = it
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                containerColor = Color(0xFFF3F3F3),
                contentColor = Color.Black
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CenterMetricBlock(
    label: String,
    valueText: String,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2C2C2E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = valueText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C2C2E),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        content()

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEDEDED))
        )
    }
}

@Composable
private fun ResetPill(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        color = Color(0xFFF2F2F2),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("↻", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.width(6.dp))
            Text("Reset", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun MinimalSlider(
    value: Int,
    min: Int,
    max: Int,
    height: Dp = 52.dp,
    onValueChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val clamped = value.coerceIn(min, max)
    val progress = (clamped - min).toFloat() / (max - min).toFloat()

    val horizontalPadding = 20.dp
    val thumbSize = 36.dp
    val dotsCount = 20

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFF1F1F1))
            .pointerInput(min, max) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: continue
                        if (change.pressed) {
                            val w = size.width.toFloat()
                            val p = (change.position.x / w).coerceIn(0f, 1f)
                            val new = min + (p * (max - min)).roundToInt()
                            onValueChange(new)
                            change.consume()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(dotsCount) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFCCCCCC))
                )
            }
        }

        val padPx = with(density) { horizontalPadding.toPx() }
        val thumbPx = with(density) { thumbSize.toPx() }
        val wPx = with(density) { maxWidth.toPx() }
        val usable = (wPx - padPx * 2 - thumbPx).coerceAtLeast(0f)
        val xPx = padPx + usable * progress

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(xPx.roundToInt(), 0) }
                .size(thumbSize),
            shape = CircleShape,
            color = Color.Black,
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = clamped.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
