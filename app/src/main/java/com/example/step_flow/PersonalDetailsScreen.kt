package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
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
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF111111)
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Personal Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Your profile data",
            fontSize = 14.sp,
            color = Color(0xFF6F747C),
            modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
        )

        // Card with fields
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {

                DetailField(
                    title = "Name",
                    value = name,
                    placeholder = "Enter your name",
                    leading = { Icon(Icons.Outlined.Person, null) },
                    keyboardType = KeyboardType.Text,
                    onValueChange = { onNameChange(it) }
                )

                DividerSoft()

                DetailField(
                    title = "Height",
                    value = heightCm,
                    placeholder = "e.g. 182",
                    suffix = "cm",
                    leading = { Icon(Icons.Outlined.Height, null) },
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onHeightChange(it.onlyDigits(3)) }
                )

                DividerSoft()

                DetailField(
                    title = "Weight",
                    value = weightKg,
                    placeholder = "e.g. 78",
                    suffix = "kg",
                    leading = { Icon(Icons.Outlined.Scale, null) },
                    keyboardType = KeyboardType.Number,
                    onValueChange = { onWeightChange(it.onlyDigits(3)) }
                )

                DividerSoft()

                DetailField(
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

        Spacer(Modifier.height(16.dp))

        // Optional block
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Coming next",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "• Goals (fat loss / endurance / strength)\n• Activity level\n• Units (cm/kg ↔ ft/lb)",
                    fontSize = 13.sp,
                    color = Color(0xFF6F747C),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Save button
        Button(
            onClick = onSave,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF111111),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Save",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailField(
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8E9097),
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(16.dp),
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
private fun DividerSoft() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp)
            .height(1.dp)
            .background(Color(0xFFE9EAEE))
    )
}

private fun String.onlyDigits(maxLen: Int): String {
    val digits = filter { it.isDigit() }
    return if (digits.length <= maxLen) digits else digits.take(maxLen)
}
