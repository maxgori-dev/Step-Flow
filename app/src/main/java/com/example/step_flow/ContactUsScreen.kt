package com.example.step_flow

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ContactUsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val cfg = LocalConfiguration.current
    val compactH = cfg.screenHeightDp < 700
    val compactW = cfg.screenWidthDp < 390

    val pageHPad = if (compactW) 14.dp else 16.dp
    val topGap = if (compactH) 6.dp else 8.dp
    val sectionGap = if (compactH) 14.dp else 16.dp
    val titleSize = if (compactW) 19.sp else 20.sp
    val hintSize = if (compactW) 12.sp else 13.sp
    val cardShadow = if (compactH) 4.dp else 6.dp
    val cardRadius = if (compactW) 20.dp else 22.dp

    // ✅ Theme Colors
    val bg = MaterialTheme.colorScheme.background
    val title = MaterialTheme.colorScheme.onBackground
    val hint = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBg = MaterialTheme.colorScheme.surface
    val stroke = MaterialTheme.colorScheme.secondaryContainer

    val email = "stepflow@gmail.com"
    val phone = "+48 500 123 456"

    val baseMessage = remember {
        """
        Hello StepFlow team,

        I need help with:
        - (describe your issue here)

        Device:
        - Android version:
        - Phone model:
        - StepFlow version:

        Steps to reproduce:
        1)
        2)
        3)

        Expected result:
        Actual result:

        Thanks!
        """.trimIndent()
    }

    val ctx = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }

    fun strongFeedback(message: String) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch { snack.showSnackbar(message) }
    }

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(hostState = snack) },
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.ime)
    ) { inner ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(bg)
                .padding(inner),
            contentPadding = WindowInsets.systemBars
                .union(WindowInsets.ime)
                .asPaddingValues(),
        ) {
            item { Spacer(Modifier.height(topGap)) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = pageHPad)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            strongFeedback("Back")
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = title
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Contact us",
                        fontSize = titleSize,
                        fontWeight = FontWeight.SemiBold,
                        color = title
                    )
                }
            }

            item { Spacer(Modifier.height(4.dp)) }

            item {
                Text(
                    text = "We usually reply within 24–48 hours.",
                    fontSize = hintSize,
                    color = hint,
                    modifier = Modifier
                        .padding(horizontal = pageHPad)
                        .padding(start = 6.dp, bottom = 12.dp)
                )
            }

            item {
                SoftCard(
                    bg = cardBg,
                    stroke = stroke,
                    radius = cardRadius,
                    shadow = cardShadow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = pageHPad)
                ) {
                    SectionTitle("CONTACT", hint)

                    ContactRow(
                        icon = Icons.Outlined.Email,
                        title = "Email",
                        value = email,
                        onClick = {
                            strongFeedback("Opening email…")
                            openEmail(ctx, email, "StepFlow — Support", baseMessage)
                        }
                    )

                    DividerLine(stroke)

                    ContactRow(
                        icon = Icons.Outlined.Phone,
                        title = "Phone",
                        value = phone,
                        onClick = {
                            strongFeedback("Opening dialer…")
                            openDialer(ctx, phone)
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(sectionGap)) }

            item {
                SoftCard(
                    bg = cardBg,
                    stroke = stroke,
                    radius = cardRadius,
                    shadow = cardShadow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = pageHPad)
                ) {
                    SectionTitle("DEVELOPERS", hint)

                    DevRow(name = "Yehor Myroshnychenko")
                    DividerLine(stroke)
                    DevRow(name = "Ivan Pashyn")
                }
            }

            item { Spacer(Modifier.height(sectionGap)) }

            item {
                val expanded = rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

                SoftCard(
                    bg = cardBg,
                    stroke = stroke,
                    radius = cardRadius,
                    shadow = cardShadow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = pageHPad)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = null,
                            tint = title
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Message template",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title
                            )
                            Text(
                                text = if (expanded.value) "Tap text to copy • Tap title to collapse"
                                else "Tap title to expand • Tap copy to clipboard",
                                fontSize = 12.sp,
                                color = hint
                            )
                        }

                        IconButton(
                            onClick = {
                                copyToClipboard(ctx, "StepFlow message", baseMessage)
                                strongFeedback("Copied to clipboard")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy",
                                tint = title
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                            ) {
                                expanded.value = !expanded.value
                                strongFeedback(if (expanded.value) "Expanded" else "Collapsed")
                            }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (expanded.value) "Tap here to collapse"
                            else "Tap here to preview full template",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = title.copy(alpha = 0.65f)
                        )
                    }

                    // Inner box for template
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), // ✅ Adaptive
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val shownText = if (expanded.value) baseMessage else baseMessage.lines()
                            .take(10)
                            .joinToString("\n")
                            .trimEnd() + "\n\n…"

                        Text(
                            text = shownText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface, // ✅ Adaptive
                            lineHeight = 18.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true)
                                ) {
                                    copyToClipboard(ctx, "StepFlow message", baseMessage)
                                    strongFeedback("Copied to clipboard")
                                }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun SoftCard(
    bg: Color,
    stroke: Color,
    radius: androidx.compose.ui.unit.Dp,
    shadow: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(radius)

    Surface(
        modifier = modifier,
        color = bg,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = shadow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SectionTitle(text: String, hintColor: Color) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = hintColor,
        modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
    )
}

@Composable
private fun ContactRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val t = MaterialTheme.colorScheme.onSurface // ✅
    val hint = MaterialTheme.colorScheme.onSurfaceVariant // ✅

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = t)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = t
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = hint
            )
        }

        Text(
            text = "Open",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = t.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun DevRow(name: String) {
    val t = MaterialTheme.colorScheme.onSurface // ✅
    val hint = MaterialTheme.colorScheme.onSurfaceVariant // ✅

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Outlined.Person, contentDescription = null, tint = t)
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = t
            )
            Text(
                text = "Developer",
                fontSize = 13.sp,
                color = hint
            )
        }
    }
}

@Composable
private fun DividerLine(color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

private fun openEmail(context: Context, email: String, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun openDialer(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phone")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}