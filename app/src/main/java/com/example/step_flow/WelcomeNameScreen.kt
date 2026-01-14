package com.example.step_flow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlin.math.max
import kotlin.math.min

private const val MAX_NAME_LEN = 18
private val NotLetterOrSpace = Regex("[^\\p{L} ]")

@Composable
fun WelcomeNameScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val density = LocalDensity.current
    var bottomBlockHeightDp by remember { mutableStateOf(0.dp) }

    // ✅ Цвета темы
    val bg = MaterialTheme.colorScheme.background
    val textMain = MaterialTheme.colorScheme.onBackground
    val textSub = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg) // ✅ Явный фон
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {

        NameBadgeInput(
            name = name,
            onNameChange = onNameChange,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -(bottomBlockHeightDp / 2f))
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    scaleX = 1.25f
                    scaleY = 1.25f
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
                .onSizeChanged { size ->
                    bottomBlockHeightDp = with(density) { size.height.toDp() }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome!",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = textMain // ✅
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Enter your name",
                fontSize = 16.sp,
                color = textSub // ✅
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onContinue,
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "bp_continue" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary, // ✅
                    contentColor = MaterialTheme.colorScheme.onPrimary // ✅
                )
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun NameBadgeInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1.35f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.hello_my_name_is),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(80.dp)
                .offset(y = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            val maxW = maxWidth
            val maxH = maxHeight

            val displayText = if (name.isBlank()) "Your name" else name

            val valueFontSize = rememberAutoFontSize(
                text = if (name.isBlank()) "W" else name,
                maxWidth = maxW,
                maxHeight = maxH,
                maxFontSize = 34.sp,
                minFontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            val placeholderFontSize = rememberAutoFontSize(
                text = "Your name",
                maxWidth = maxW,
                maxHeight = maxH,
                maxFontSize = 26.sp,
                minFontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            BasicTextField(
                value = name,
                onValueChange = { raw ->
                    val filtered = raw
                        .replace(NotLetterOrSpace, "")
                        .replace(Regex("\\s+"), " ")
                        .trimStart()
                        .take(MAX_NAME_LEN)

                    onNameChange(filtered)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = valueFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black, // Оставляем черным, так как фон картинки всегда белый
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = false
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "bp_name_input" },
                decorationBox = { inner ->
                    if (name.isBlank()) {
                        Text(
                            text = displayText,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = placeholderFontSize,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black.copy(alpha = 0.25f), // Тоже черным для контраста с белым стикером
                            maxLines = 1
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
private fun rememberAutoFontSize(
    text: String,
    maxWidth: Dp,
    maxHeight: Dp,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    fontWeight: FontWeight
): TextUnit {
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()

    val d = density.density
    val fs = density.fontScale

    return remember(text, maxWidth, maxHeight, maxFontSize, minFontSize, fontWeight, d, fs) {
        val safeText = if (text.isBlank()) " " else text

        val maxWpx = with(density) { maxWidth.roundToPx() }
        val maxHpx = with(density) { maxHeight.roundToPx() }

        var lowPx = with(density) { minFontSize.toPx() }
        var highPx = with(density) { maxFontSize.toPx() }
        var bestPx = lowPx

        repeat(18) {
            val midPx = (lowPx + highPx) / 2f
            val midSp = with(density) { midPx.toSp() }

            val layout = measurer.measure(
                text = AnnotatedString(safeText),
                style = TextStyle(fontSize = midSp, fontWeight = fontWeight),
                constraints = Constraints(maxWidth = maxWpx, maxHeight = maxHpx),
                maxLines = 1
            )

            val fits = layout.size.width <= maxWpx && layout.size.height <= maxHpx

            if (fits) {
                bestPx = midPx
                lowPx = midPx + 0.5f
            } else {
                highPx = midPx - 0.5f
            }
        }

        val clamped = min(
            max(bestPx, with(density) { minFontSize.toPx() }),
            with(density) { maxFontSize.toPx() }
        )

        with(density) { clamped.toSp() }
    }
}