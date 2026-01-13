package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PersonalDetailsScreen(
    modifier: Modifier = Modifier,
    name: String,
    heightCm: String,
    weightKg: String,
    ageYears: String,
    onNameChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val bg = Color(0xFFF5F6F8)
    val title = Color(0xFF111111)
    val hint = Color(0xFF6F747C)

    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDir = LocalLayoutDirection.current

    val startPad = insets.calculateStartPadding(layoutDir)
    val endPad = insets.calculateEndPadding(layoutDir)
    val topPad = insets.calculateTopPadding()
    val bottomPad = insets.calculateBottomPadding()

    val minDim = 390.dp
    val uiScale = 1f

    val cardShape = RoundedCornerShape(22.dp * uiScale)
    val fieldShape = RoundedCornerShape(16.dp * uiScale)
    val buttonShape = RoundedCornerShape(18.dp * uiScale)
    val buttonH = (54.dp * uiScale).coerceIn(48.dp, 60.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(start = 16.dp + startPad, end = 16.dp + endPad)
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp * uiScale))

        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp * uiScale),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp * uiScale)) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = title,
                    modifier = Modifier.size(22.dp * uiScale)
                )
            }
            Spacer(Modifier.height(0.dp))
            Text(
                text = "Personal Details",
                fontSize = (18.sp * uiScale),
                fontWeight = FontWeight.SemiBold,
                color = title
            )
        }

        Spacer(Modifier.height(10.dp * uiScale))

        Text(
            text = "Your profile data",
            fontSize = (14.sp * uiScale),
            color = hint,
            modifier = Modifier.padding(start = 6.dp * uiScale, bottom = 12.dp * uiScale)
        )

        Surface(
            shape = cardShape,
            color = Color.White,
            shadowElevation = 6.dp * uiScale,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp * uiScale)) {
                DetailField(
                    scale = uiScale,
                    shape = fieldShape,
                    title = "Name",
                    value = name,
                    placeholder = "Enter your name",
                    leading = { Icon(Icons.Outlined.Person, null) },
                    keyboardType = KeyboardType.Text,
                    onValueChange = onNameChange
                )

                DividerSoft(scale = uiScale)

                DetailField(
                    scale = uiScale,
                    shape = fieldShape,
                    title = "Height",
                    value = heightCm,
                    placeholder = "e.g. 182",
                    suffix = "cm",
                    leading = { Icon(Icons.Outlined.Height, null) },
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onHeightChange(it.onlyDigits(3)) }
                )

                DividerSoft(scale = uiScale)

                DetailField(
                    scale = uiScale,
                    shape = fieldShape,
                    title = "Weight",
                    value = weightKg,
                    placeholder = "e.g. 78",
                    suffix = "kg",
                    leading = { Icon(Icons.Outlined.Scale, null) },
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onWeightChange(it.onlyDigits(3)) }
                )

                DividerSoft(scale = uiScale)

                DetailField(
                    scale = uiScale,
                    shape = fieldShape,
                    title = "Age",
                    value = ageYears,
                    placeholder = "e.g. 20",
                    suffix = "years",
                    leading = { Icon(Icons.Outlined.Cake, null) },
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onAgeChange(it.onlyDigits(3)) }
                )
            }
        }

        Spacer(Modifier.height(16.dp * uiScale))

        Button(
            onClick = onSave,
            shape = buttonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonH),
            colors = ButtonDefaults.buttonColors(
                containerColor = title,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Save",
                fontSize = (16.sp * uiScale),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp * uiScale + bottomPad))
    }
}

@Composable
private fun DetailField(
    scale: Float,
    shape: RoundedCornerShape,
    title: String,
    value: String,
    placeholder: String,
    suffix: String? = null,
    leading: @Composable () -> Unit,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = (12.sp * scale),
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8E9097),
            modifier = Modifier.padding(start = 2.dp * scale, bottom = 8.dp * scale)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            leadingIcon = {
                CompositionLocalProvider(LocalContentColor provides Color(0xFF111111)) {
                    leading()
                }
            },
            trailingIcon = {
                if (!suffix.isNullOrBlank()) {
                    Text(
                        text = suffix,
                        color = Color(0xFF8E9097),
                        fontSize = (13.sp * scale),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color(0xFFF7F8FB),
                focusedContainerColor = Color(0xFFF7F8FB),
                unfocusedBorderColor = Color(0xFFE6E8EE),
                focusedBorderColor = Color(0xFF111111),
                cursorColor = Color(0xFF111111)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DividerSoft(scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp * scale)
            .height((1.dp * scale).coerceAtLeast(1.dp))
            .background(Color(0xFFE9EAEE))
    )
}

private fun String.onlyDigits(maxLen: Int): String {
    val digits = filter { it.isDigit() }
    return if (digits.length <= maxLen) digits else digits.take(maxLen)
}
