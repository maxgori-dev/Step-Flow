package com.example.step_flow

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ContactUsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val bg = Color.White
    val title = Color(0xFF111111)
    val hint = Color(0xFF6F747C)
    val cardBg = Color(0xFFF3F5F8)
    val stroke = Color(0xFFE6E9EF)

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

    Scaffold(containerColor = bg) { inner ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(bg)
                .padding(inner)
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
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = title
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Contact us",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = title
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "We usually reply within 24–48 hours.",
                fontSize = 13.sp,
                color = hint,
                modifier = Modifier.padding(start = 6.dp, bottom = 12.dp)
            )

            // Contact card
            SoftCard(
                bg = cardBg,
                stroke = stroke,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionTitle("CONTACT", hint)

                ContactRow(
                    icon = Icons.Outlined.Email,
                    title = "Email",
                    value = email,
                    onClick = { openEmail(ctx, email, "StepFlow — Support", baseMessage) }
                )

                DividerLine(stroke)

                ContactRow(
                    icon = Icons.Outlined.Phone,
                    title = "Phone",
                    value = phone,
                    onClick = { openDialer(ctx, phone) }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Developers card
            SoftCard(
                bg = cardBg,
                stroke = stroke,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionTitle("DEVELOPERS", hint)

                DevRow(name = "Yehor Myroshnychenko")
                DividerLine(stroke)
                DevRow(name = "Ivan Pashyn")
            }

            Spacer(Modifier.height(16.dp))

            // Message template
            SoftCard(
                bg = cardBg,
                stroke = stroke,
                modifier = Modifier.fillMaxWidth()
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
                            text = "Tap to copy and paste into email",
                            fontSize = 12.sp,
                            color = hint
                        )
                    }

                    IconButton(
                        onClick = { copyToClipboard(ctx, "StepFlow message", baseMessage) }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = title
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = baseMessage,
                        fontSize = 12.sp,
                        color = Color(0xFF2A2D33),
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .clickable {
                                copyToClipboard(ctx, "StepFlow message", baseMessage)
                            }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}


@Composable
private fun SoftCard(
    bg: Color,
    stroke: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = modifier,
        color = bg,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 6.dp
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
    val t = Color(0xFF111111)
    val hint = Color(0xFF6F747C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            color = Color(0xFF111111).copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun DevRow(name: String) {
    val t = Color(0xFF111111)
    val hint = Color(0xFF6F747C)

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
