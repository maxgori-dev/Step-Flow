package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    language: AppLanguage,
    units: Units,
    theme: AppTheme,
    fontScale: Float, 
    notificationsEnabled: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onUnitsChange: (Units) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onGoalsClick: () -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    
    val bg = MaterialTheme.colorScheme.background
    val titleColor = MaterialTheme.colorScheme.onBackground
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant

    var languageDraft by remember(language) { mutableStateOf(language) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val minDim = if (maxWidth < maxHeight) maxWidth else maxHeight
        val uiScale = (minDim / 390.dp).coerceIn(0.82f, 1.20f)

        val padH = 16.dp * uiScale
        val padV = 12.dp * uiScale
        val bottomBarPad = 12.dp * uiScale
        val buttonH = (54.dp * uiScale).coerceIn(48.dp, 60.dp)

        Scaffold(
            containerColor = bg,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = padH, vertical = bottomBarPad)
                ) {
                    Button(
                        onClick = {
                            if (languageDraft != language) onLanguageChange(languageDraft)
                            onSave()
                        },
                        shape = RoundedCornerShape(18.dp * uiScale),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonH),
                        colors = ButtonDefaults.buttonColors(
                            
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Save",
                            fontSize = (16.sp * uiScale),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        ) { inner ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(horizontal = padH),
                contentPadding = PaddingValues(
                    top = inner.calculateTopPadding() + padV,
                    bottom = inner.calculateBottomPadding() + (12.dp * uiScale)
                )
            ) {
                item { Spacer(Modifier.height(8.dp * uiScale)) }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp * uiScale),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(44.dp * uiScale)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = titleColor, 
                                modifier = Modifier.size(22.dp * uiScale)
                            )
                        }
                        Spacer(Modifier.width(6.dp * uiScale))
                        Text(
                            text = "Settings",
                            fontSize = (20.sp * uiScale),
                            fontWeight = FontWeight.SemiBold,
                            color = titleColor 
                        )
                    }
                }

                item { Spacer(Modifier.height(10.dp * uiScale)) }

                item {
                    Text(
                        text = "App preferences",
                        fontSize = (14.sp * uiScale),
                        color = hintColor, 
                        modifier = Modifier.padding(start = 6.dp * uiScale, bottom = 12.dp * uiScale)
                    )
                }

                item { SectionTitle(text = "GOALS", scale = uiScale) }
                item {
                    SettingsCard(scale = uiScale) {
                        SettingsActionRow(
                            scale = uiScale,
                            title = "Daily Goals",
                            subtitle = "Steps, duration & calories",
                            icon = Icons.Outlined.Flag,
                            onClick = onGoalsClick
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp * uiScale)) }

                item { SectionTitle(text = "GENERAL", scale = uiScale) }
                item {
                    SettingsCard(scale = uiScale) {
                        SettingsMenuRow(
                            scale = uiScale,
                            title = "Language",
                            subtitle = "Choose app language",
                            icon = Icons.Outlined.Language,
                            valueText = languageDraft.label,
                            options = AppLanguage.entries.toList(),
                            optionLabel = { it.label },
                            onSelect = { picked -> languageDraft = picked }
                        )

                        DividerSoft(scale = uiScale)

                        SettingsMenuRow(
                            scale = uiScale,
                            title = "Units",
                            subtitle = "Measurement system",
                            icon = Icons.Outlined.Straighten,
                            valueText = units.label,
                            options = Units.entries.toList(),
                            optionLabel = { it.label },
                            onSelect = onUnitsChange
                        )
                    }
                }

                item { Spacer(Modifier.height(16.dp * uiScale)) }

                item { SectionTitle(text = "APPEARANCE", scale = uiScale) }
                item {
                    SettingsCard(scale = uiScale) {
                        SettingsMenuRow(
                            scale = uiScale,
                            title = "Theme",
                            subtitle = "Light / Dark / System",
                            icon = Icons.Outlined.Palette,
                            valueText = theme.label,
                            options = AppTheme.entries.toList(),
                            optionLabel = { it.label },
                            onSelect = onThemeChange
                        )

                        DividerSoft(scale = uiScale)

                        SettingsSliderRow(
                            scale = uiScale,
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

                item { Spacer(Modifier.height(16.dp * uiScale)) }

                item { SectionTitle(text = "NOTIFICATIONS", scale = uiScale) }
                item {
                    SettingsCard(scale = uiScale) {
                        SettingsSwitchRow(
                            scale = uiScale,
                            title = "Notifications",
                            subtitle = "Enable reminders & alerts",
                            icon = Icons.Outlined.Notifications,
                            checked = notificationsEnabled,
                            onCheckedChange = onNotificationsChange
                        )
                    }
                }

                item { Spacer(Modifier.height(12.dp * uiScale)) }
            }
        }
    }
}





@Composable
private fun SectionTitle(text: String, scale: Float) {
    Text(
        text = text,
        fontSize = (12.sp * scale),
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant, 
        modifier = Modifier.padding(start = 8.dp * scale, bottom = 8.dp * scale)
    )
}

@Composable
private fun SettingsCard(
    scale: Float,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp * scale),
        color = MaterialTheme.colorScheme.surface, 
        shadowElevation = 6.dp * scale,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(vertical = 6.dp * scale), content = content)
    }
}

@Composable
private fun SettingsActionRow(
    scale: Float,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp * scale))
        Spacer(Modifier.width(14.dp * scale))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = (16.sp * scale),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp * scale))
            Text(
                text = subtitle,
                fontSize = (13.sp * scale),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp * scale)
        )
    }
}

@Composable
private fun <T> SettingsMenuRow(
    scale: Float,
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
            .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp * scale))
        Spacer(Modifier.width(14.dp * scale))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = (16.sp * scale),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp * scale))
            Text(
                text = subtitle,
                fontSize = (13.sp * scale),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = valueText,
            fontSize = (14.sp * scale),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.width(10.dp * scale))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp * scale)
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MaterialTheme.colorScheme.surface 
    ) {
        options.forEach { item ->
            DropdownMenuItem(
                text = { Text(optionLabel(item), color = MaterialTheme.colorScheme.onSurface) },
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
    scale: Float,
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp * scale))
        Spacer(Modifier.width(14.dp * scale))

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = (16.sp * scale),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp * scale))
            Text(
                text = subtitle,
                fontSize = (13.sp * scale),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsSliderRow(
    scale: Float,
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
            .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp * scale))
            Spacer(Modifier.width(14.dp * scale))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = (16.sp * scale),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp * scale))
                Text(
                    text = subtitle,
                    fontSize = (13.sp * scale),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = valueLabel,
                fontSize = (14.sp * scale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(10.dp * scale))

        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = { onChange(it.coerceIn(range.start, range.endInclusive)) },
            valueRange = range,
            colors = SliderDefaults.colors(
                
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun DividerSoft(scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp * scale)
            .height((1.dp * scale).coerceAtLeast(1.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer) 
    )
}