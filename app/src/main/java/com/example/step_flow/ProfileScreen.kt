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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    name: String,
    avatarUriString: String,                 // from DataStore
    onAvatarChange: (String) -> Unit,        // save to DataStore
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

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) {
            }
            onAvatarChange(uri.toString())
        }
    }

    val avatarBitmap: ImageBitmap? by rememberAvatarBitmap(avatarUriString)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6F8))
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val minDim = min(maxWidth, maxHeight)
        val uiScale = (minDim / 390.dp).coerceIn(0.82f, 1.20f)

        val padH = 16.dp * uiScale
        val topGap = 8.dp * uiScale
        val sectionGap = 22.dp * uiScale

        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = padH)
                .verticalScroll(scroll)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Spacer(Modifier.height(topGap))

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp * uiScale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(44.dp * uiScale)) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111111),
                        modifier = Modifier.size(22.dp * uiScale)
                    )
                }
                Spacer(Modifier.size(6.dp * uiScale))
                Text(
                    text = "Profile",
                    fontSize = (18.sp * uiScale),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111111)
                )
            }

            Spacer(Modifier.height(8.dp * uiScale))

            ProfileHero(
                scale = uiScale,
                avatarBitmap = avatarBitmap,
                onAvatarClick = { pickAvatarLauncher.launch(arrayOf("image/*")) }
            )

            Spacer(Modifier.height((48.dp * uiScale).coerceIn(28.dp, 58.dp)))

            Text(
                text = displayName,
                fontSize = (22.sp * uiScale),
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showNameDialog = true }
                    .padding(vertical = 6.dp * uiScale)
            )

            Spacer(Modifier.height((28.dp * uiScale).coerceIn(16.dp, 34.dp)))

            SettingsSection(
                scale = uiScale,
                title = "PERSONALIZE",
                items = listOf(
                    SettingItem("Personal Details", Icons.Outlined.Person, onPersonalDetails),
                    SettingItem("Settings", Icons.Outlined.Settings, onSettings)
                )
            )

            Spacer(Modifier.height(sectionGap))

            SettingsSection(
                scale = uiScale,
                title = "NEED HELP?",
                items = listOf(
                    SettingItem("Tips and Tricks", Icons.Outlined.Info, onTips),
                    SettingItem("Frequently Asked Questions", Icons.Outlined.HelpOutline, onFaq),
                    SettingItem("Contact Us", Icons.Outlined.Email, onContact)
                )
            )

            Spacer(Modifier.height(14.dp * uiScale))
        }
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
// Hero section (adaptive)
// -----------------------------
@Composable
private fun ProfileHero(
    scale: Float,
    avatarBitmap: ImageBitmap?,
    onAvatarClick: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp * scale)

    // держим пропорции, чтобы на разных экранах не “плющило”
    val heroHeight = (180.dp * scale).coerceIn(150.dp, 220.dp)
    val bannerHeight = (150.dp * scale).coerceIn(120.dp, 190.dp)

    val frameSize = (92.dp * scale).coerceIn(78.dp, 110.dp)
    val avatarSize = (84.dp * scale).coerceIn(70.dp, 102.dp)
    val avatarOffsetY = (44.dp * scale).coerceIn(28.dp, 56.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
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
                .size(frameSize)
                .offset(y = avatarOffsetY)
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
                        .size(avatarSize)
                        .clip(CircleShape)
                )
            } else {
                AvatarPlaceholder(modifier = Modifier.size(avatarSize))
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
// Settings UI (adaptive)
// -----------------------------
@Composable
private fun SettingsSection(
    scale: Float,
    title: String,
    items: List<SettingItem>
) {
    Column {
        Text(
            text = title,
            fontSize = (12.sp * scale),
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8E9097),
            modifier = Modifier.padding(start = 8.dp * scale, bottom = 8.dp * scale)
        )

        Surface(
            shape = RoundedCornerShape(20.dp * scale),
            color = Color.White,
            shadowElevation = 6.dp * scale
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingsRow(scale = scale, item = item)
                    if (index != items.lastIndex) DividerLight(scale = scale)
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(scale: Float, item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color(0xFF111111),
            modifier = Modifier.size(22.dp * scale)
        )

        Spacer(Modifier.width(14.dp * scale))

        Text(
            text = item.title,
            fontSize = (16.sp * scale),
            color = Color(0xFF111111),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Outlined.ArrowForwardIos,
            contentDescription = null,
            tint = Color(0xFFB0B2B8),
            modifier = Modifier.size(18.dp * scale)
        )
    }
}

@Composable
private fun DividerLight(scale: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((1.dp * scale).coerceAtLeast(1.dp))
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
