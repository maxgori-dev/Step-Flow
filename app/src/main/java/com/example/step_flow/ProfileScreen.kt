package com.example.step_flow

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    name: String,
    avatarUriString: String,                 // ✅ приходит из DataStore (ui.avatarUri)
    onAvatarChange: (String) -> Unit,        // ✅ сохраняем в DataStore (vm.saveAvatarUri)
    onNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onPersonalDetails: () -> Unit = {},
    onSettings: () -> Unit = {},
    onTips: () -> Unit = {},
    onFaq: () -> Unit = {},
    onContact: () -> Unit = {}
) {
    val context = LocalContext.current
    val displayName = name.trim().ifBlank { "Maxgori" }

    var showNameDialog by rememberSaveable { mutableStateOf(false) }

    // ✅ Надёжный выбор изображения с persistable permission
    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            // persist read permission (если провайдер поддерживает)
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) {
                // норм — не все провайдеры дают persistable, но строку uri сохраняем
            }
            onAvatarChange(uri.toString())
        }
    }

    val avatarBitmap: ImageBitmap? by rememberAvatarBitmap(avatarUriString)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Top bar (Back)
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
                text = "Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )
        }

        Spacer(Modifier.height(8.dp))

        ProfileHero(
            avatarBitmap = avatarBitmap,
            onAvatarClick = {
                // ✅ image/*
                pickAvatarLauncher.launch(arrayOf("image/*"))
            }
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = displayName,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111111),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { showNameDialog = true }
                .padding(vertical = 6.dp)
        )

        Spacer(Modifier.height(28.dp))

        SettingsSection(
            title = "PERSONALIZE",
            items = listOf(
                SettingItem("Personal Details", Icons.Outlined.Person, onPersonalDetails),
                SettingItem("Settings", Icons.Outlined.Settings, onSettings)
            )
        )

        Spacer(Modifier.height(22.dp))

        SettingsSection(
            title = "NEED HELP?",
            items = listOf(
                SettingItem("Tips and Tricks", Icons.Outlined.Info, onTips),
                SettingItem("Frequently Asked Questions", Icons.Outlined.HelpOutline, onFaq),
                SettingItem("Contact Us", Icons.Outlined.Email, onContact)
            )
        )
    }

    if (showNameDialog) {
        var draft by remember { mutableStateOf(displayName) }

        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit name") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newName = draft.trim().ifBlank { "Maxgori" }
                        onNameChange(newName)
                        showNameDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -----------------------------
// Hero section
// -----------------------------
@Composable
private fun ProfileHero(
    avatarBitmap: ImageBitmap?,
    onAvatarClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(shape)
        ) {
            AnimatedWaveGradient(
                modifier = Modifier.fillMaxSize(),
                colors = listOf(
                    Color(0xFF19C5B7),
                    Color(0xFF0A1A1F),
                    Color(0xFFE53935)
                ),
                durationMillis = 8500
            )
        }

        Box(
            modifier = Modifier
                .size(92.dp)
                .offset(y = 44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (avatarBitmap != null) {
                Image(
                    painter = BitmapPainter(avatarBitmap),
                    contentDescription = null,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                )
            } else {
                AvatarPlaceholder(modifier = Modifier.size(84.dp))
            }
        }
    }
}

@Composable
private fun AnimatedWaveGradient(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    durationMillis: Int
) {
    val transition = rememberInfiniteTransition(label = "waveGradient")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing)
        ),
        label = "t"
    )

    Box(
        modifier = modifier.drawBehind {
            val w = size.width
            val h = size.height
            val base = (2f * PI.toFloat()) * t

            val cx = w * 0.5f + sin(base) * w * 0.35f
            val cy = h * 0.5f + cos(base * 0.9f) * h * 0.35f

            val start = Offset(cx - w * 0.70f, cy - h * 0.70f)
            val end = Offset(cx + w * 0.70f, cy + h * 0.70f)

            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = start,
                    end = end
                )
            )

            val cx2 = w * 0.5f + sin(base * 1.25f + 1.2f) * w * 0.25f
            val cy2 = h * 0.35f + cos(base * 1.10f + 0.4f) * h * 0.35f

            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(cx2, cy2),
                    radius = min(w, h) * 1.05f
                )
            )
        }
    )
}

@Composable
private fun AvatarPlaceholder(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .clip(CircleShape)
            .background(Color(0xFFE9EAEF))
    ) {
        val w = size.width
        val h = size.height
        drawCircle(
            color = Color(0xFFB9BCC4),
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.42f)
        )
        drawCircle(
            color = Color(0xFFB9BCC4),
            radius = w * 0.28f,
            center = Offset(w * 0.5f, h * 0.82f)
        )
    }
}

// -----------------------------
// Settings UI
// -----------------------------
@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8E9097),
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsRow(item)
                    if (index != items.lastIndex) DividerLight()
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color(0xFF111111)
        )

        Spacer(Modifier.width(14.dp))

        Text(
            text = item.title,
            fontSize = 16.sp,
            color = Color(0xFF111111),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFB0B2B8)
        )
    }
}

@Composable
private fun DividerLight() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFFE9EAEE))
    )
}

private data class SettingItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

// -----------------------------
// Load avatar bitmap (no Coil)
// -----------------------------
@Composable
private fun rememberAvatarBitmap(uriString: String): State<ImageBitmap?> {
    val context = LocalContext.current

    return produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        if (uriString.isBlank()) {
            value = null
            return@produceState
        }

        try {
            val uri = Uri.parse(uriString)
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }
            value = bitmap.asImageBitmap()
        } catch (_: Throwable) {
            value = null
        }
    }
}
