package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
    val bg = MaterialTheme.colorScheme.background
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

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
                    tint = textPrimary
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Personal Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Your profile data",
            fontSize = 14.sp,
            color = textSecondary,
            modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
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

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Coming next",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "• Goals (fat loss / endurance / strength)\n• Activity level\n• Units (cm/kg ↔ ft/lb)",
                    fontSize = 13.sp,
                    color = textSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSave,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
    val fieldBg = MaterialTheme.colorScheme.background
    val fieldBorder = MaterialTheme.colorScheme.secondaryContainer
    val primary = MaterialTheme.colorScheme.primary
    val textLabel = MaterialTheme.colorScheme.onSurfaceVariant

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textLabel,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            leadingIcon = {
                CompositionLocalProvider(LocalContentColor provides primary) {
                    leading()
                }
            },
            trailingIcon = {
                if (!suffix.isNullOrBlank()) {
                    Text(
                        text = suffix,
                        color = textLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            placeholder = { Text(placeholder, color = textLabel.copy(alpha = 0.5f)) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldBg,
                focusedContainerColor = fieldBg,
                unfocusedBorderColor = fieldBorder,
                focusedBorderColor = primary,
                cursorColor = primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
            .background(MaterialTheme.colorScheme.secondaryContainer)
    )
}

private fun String.onlyDigits(maxLen: Int): String {
    val digits = filter { it.isDigit() }
    return if (digits.length <= maxLen) digits else digits.take(maxLen)
}