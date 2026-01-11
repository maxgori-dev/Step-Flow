package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AppLanguage(val label: String) {
    System("System"),
    English("English"),
    Russian("Русский"),
    Polish("Polski")
}

enum class Units(val label: String) {
    Metric("Metric (cm/kg)"),
    Imperial("Imperial (ft/lb)")
}

enum class AppTheme(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    language: AppLanguage,
    units: Units,
    theme: AppTheme,
    fontScale: Float, // 0.85..1.25
    notificationsEnabled: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onUnitsChange: (Units) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val bg = Color(0xFFF5F6F8)
    val title = Color(0xFF111111)
    val hint = Color(0xFF6F747C)

    Scaffold(
        containerColor = bg,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
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
            }
        }
    ) { inner ->
        // ✅ Скролл + учёт bottomBar, чтобы нижние элементы были кликабельны
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(bg)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding(),
                bottom = inner.calculateBottomPadding() + 12.dp
            )
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Top bar
            item {
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
                            tint = title
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = title
                    )
                }
            }

            item { Spacer(Modifier.height(10.dp)) }

            item {
                Text(
                    text = "App preferences",
                    fontSize = 14.sp,
                    color = hint,
                    modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
                )
            }

            // ===== GENERAL =====
            item { SectionTitle("GENERAL") }
            item {
                SettingsCard {
                    SettingsMenuRow(
                        title = "Language",
                        subtitle = "Choose app language",
                        icon = Icons.Outlined.Language,
                        valueText = language.label,
                        options = AppLanguage.values().toList(),
                        optionLabel = { it.label },
                        onSelect = onLanguageChange
                    )

                    DividerSoft()

                    SettingsMenuRow(
                        title = "Units",
                        subtitle = "Measurement system",
                        icon = Icons.Outlined.Straighten,
                        valueText = units.label,
                        options = Units.values().toList(),
                        optionLabel = { it.label },
                        onSelect = onUnitsChange
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ===== APPEARANCE =====
            item { SectionTitle("APPEARANCE") }
            item {
                SettingsCard {
                    SettingsMenuRow(
                        title = "Theme",
                        subtitle = "Light / Dark / System",
                        icon = Icons.Outlined.Palette,
                        valueText = theme.label,
                        options = AppTheme.values().toList(),
                        optionLabel = { it.label },
                        onSelect = onThemeChange
                    )

                    DividerSoft()

                    SettingsSliderRow(
                        title = "Font size",
                        subtitle = "Scale the app text",
                        icon = Icons.Outlined.TextFields,
                        value = fontScale,
                        valueLabel = when {
                            fontScale < 0.95f -> "Small"
                            fontScale < 1.05f -> "Default"
                            fontScale < 1.15f -> "Large"
                            else -> "XL"
                        },
                        range = 0.85f..1.25f,
                        onChange = onFontScaleChange
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ===== NOTIFICATIONS =====
            item { SectionTitle("NOTIFICATIONS") }
            item {
                SettingsCard {
                    SettingsSwitchRow(
                        title = "Notifications",
                        subtitle = "Enable reminders & alerts",
                        icon = Icons.Outlined.Notifications,
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsChange
                    )
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF8E9097),
        modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(vertical = 6.dp), content = content)
    }
}

@Composable
private fun <T> SettingsMenuRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    valueText: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF111111))
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF111111),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF6F747C)
            )
        }

        Text(
            text = valueText,
            fontSize = 14.sp,
            color = Color(0xFF8E9097),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFB0B2B8)
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { item ->
            DropdownMenuItem(
                text = { Text(optionLabel(item)) },
                onClick = {
                    onSelect(item)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF111111))
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF111111),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color(0xFF6F747C)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSliderRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    value: Float,
    valueLabel: String,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF111111))
            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color(0xFF111111),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF6F747C)
                )
            }

            Text(
                text = valueLabel,
                fontSize = 14.sp,
                color = Color(0xFF8E9097),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp))

        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = { onChange(it.coerceIn(range.start, range.endInclusive)) },
            valueRange = range
        )
    }
}

@Composable
private fun DividerSoft() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(Color(0xFFE9EAEE))
    )
}
