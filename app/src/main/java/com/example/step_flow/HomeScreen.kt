package com.example.step_flow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

enum class HomeTab { Calendar, Home, Profile }

@Composable
fun HomeScreenNow(
    modifier: Modifier = Modifier,
    onRunClick: () -> Unit = {},
    onTileCalendar: () -> Unit = {},
    onTileHistory: () -> Unit = {},
    onTileAchievements: () -> Unit = {},
    onTopProfile: () -> Unit = {},
    onTopSettings: () -> Unit = {},
    onBottomTabChange: (HomeTab) -> Unit = {}
) {
    val bg = Color.White
    val soft = Color(0xFFF4F6F9)
    val textMain = Color(0xFF111111)
    val textSoft = Color(0xFF6F747C)

    val navInactive = Color(0xFF9AA3AD)
    val navActive = Color(0xFF111111)

    val ring1 = Color(0xFFB8E36A)
    val ring2 = Color(0xFF35B25C)

    var tab by remember { mutableStateOf(HomeTab.Home) }
    val haptics = LocalHapticFeedback.current

    val pageBg = Brush.verticalGradient(listOf(bg, soft))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBg)
            .statusBarsPadding()
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // ===== TOP BAR =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "StepFlow",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textMain
                )

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTopProfile()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile",
                            tint = textMain,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTopSettings()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = textMain,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RunRing(
                    wrapSize = 236.dp,
                    coreSize = 174.dp,
                    ring1 = ring1,
                    ring2 = ring2,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRunClick()
                    }
                )
            }

            Spacer(Modifier.height(26.dp))

            Text(
                text = "Quick actions",
                fontSize = 13.sp,
                color = textSoft,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QuickTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CalendarToday,
                    label = "Calendar",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTileCalendar()
                    }
                )

                QuickTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.History,
                    label = "History",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTileHistory()
                    }
                )

                QuickTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.EmojiEvents,
                    label = "Awards",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTileAchievements()
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            BottomNav(
                activeTab = tab,
                inactiveColor = navInactive,
                activeColor = navActive,
                onSelect = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    tab = it
                    onBottomTabChange(it)
                }
            )

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RunRing(
    wrapSize: Dp,
    coreSize: Dp,
    ring1: Color,
    ring2: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(wrapSize)
            .shadow(14.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val d = min(size.width, size.height)
            val pivot = Offset(size.width / 2f, size.height / 2f)

            val inset = d * 0.12f
            val arcSize = d - inset * 2f
            val strokeW = d * 0.030f

            val sweep = Brush.sweepGradient(
                colors = listOf(ring1, ring2, ring1),
                center = pivot
            )

            rotate(degrees = 300f, pivot = pivot) {
                drawArc(
                    brush = sweep,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            val pillW = d * 0.15f
            val pillH = d * 0.08f
            val pillX = (size.width - pillW) / 2f
            val pillY = inset - pillH * 0.35f

            drawRoundRect(
                color = Color(0xFFF2F4F7),
                topLeft = Offset(pillX, pillY),
                size = Size(pillW, pillH),
                cornerRadius = CornerRadius(pillH, pillH)
            )
        }

        Box(
            modifier = Modifier
                .size(coreSize)
                .shadow(10.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color(0xFFF3F5F8))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "RUN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = Color(0xFF7C848F)
            )
        }
    }
}

@Composable
private fun QuickTile(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    val textMain = Color(0xFF111111)
    val textSoft = Color(0xFF6F747C)

    Surface(
        modifier = modifier
            .height(104.dp)
            .clickable(onClick = onClick),
        shape = shape,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3F5F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textMain,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSoft
            )
        }
    }
}

@Composable
private fun BottomNav(
    activeTab: HomeTab,
    inactiveColor: Color,
    activeColor: Color,
    onSelect: (HomeTab) -> Unit
) {
    val shape = RoundedCornerShape(26.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = shape,
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomItem(
                label = "Calendar",
                icon = Icons.Outlined.CalendarToday,
                selected = activeTab == HomeTab.Calendar,
                inactive = inactiveColor,
                active = activeColor
            ) { onSelect(HomeTab.Calendar) }

            BottomItem(
                label = "Home",
                icon = Icons.Outlined.Home,
                selected = activeTab == HomeTab.Home,
                inactive = inactiveColor,
                active = activeColor
            ) { onSelect(HomeTab.Home) }

            BottomItem(
                label = "Profile",
                icon = Icons.Outlined.Person,
                selected = activeTab == HomeTab.Profile,
                inactive = inactiveColor,
                active = activeColor
            ) { onSelect(HomeTab.Profile) }
        }
    }
}

@Composable
private fun BottomItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    inactive: Color,
    active: Color,
    onClick: () -> Unit
) {
    val textColor = if (selected) active else inactive
    val iconAlpha = if (selected) 1f else 0.70f
    val weight = if (selected) FontWeight.SemiBold else FontWeight.Medium

    Column(
        modifier = Modifier
            .width(96.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier
                .size(22.dp)
                .offset(y = (-1).dp)
                .alpha(iconAlpha)
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = weight
        )

        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(3.dp)
                .width(if (selected) 22.dp else 8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(textColor.copy(alpha = if (selected) 0.55f else 0.18f))
        )
    }
}
