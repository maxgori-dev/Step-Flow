package com.example.step_flow

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.IBinder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.resume

data class RunResult(
    val distanceMeters: Float,
    val durationSeconds: Long,
    val calories: Int,
    val avgSpeedKmh: Float,
    val steps: Int,
    val screenshotPath: String?
)

@Composable
fun TrackingScreen(
    weightKg: Double,
    heightCm: Double,
    ageYears: Int,
    isUploading: Boolean,
    onFinish: (RunResult) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var trackingService by remember { mutableStateOf<TrackingService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val localBinder = binder as TrackingService.LocalBinder
                trackingService = localBinder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
                trackingService = null
            }
        }
    }

    LaunchedEffect(Unit) {
        val intent = Intent(context, TrackingService::class.java).apply {
            putExtra("WEIGHT", weightKg)
            putExtra("HEIGHT", heightCm)
            putExtra("AGE", ageYears)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(connection)
                isBound = false
            }
        }
    }

    // --- Чтение данных ---
    val duration by trackingService?.durationSeconds?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0L) }
    val distance by trackingService?.distanceMeters?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0f) }
    val calories by trackingService?.calories?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0) }
    val steps by trackingService?.steps?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0) }
    val pathPoints by trackingService?.pathPoints?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val speedKmh by trackingService?.currentSpeedKmh?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(0f) }

    // ✅ Слушаем состояние паузы
    val isPaused by trackingService?.isPaused?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isSnapshotting by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    LaunchedEffect(pathPoints) {
        pathPoints.lastOrNull()?.let { last ->
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(last))
        }
    }

    fun stopAndSave() {
        if (trackingService == null) return
        isSnapshotting = true

        scope.launch {
            val bitmap = try {
                googleMap?.awaitSnapshot()
            } catch (e: Exception) { null }

            var path: String? = null
            if (bitmap != null) {
                val file = File(context.cacheDir, "map_snap_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                path = file.absolutePath
            }

            val result = RunResult(
                distanceMeters = distance,
                durationSeconds = duration,
                calories = calories,
                avgSpeedKmh = speedKmh,
                steps = steps,
                screenshotPath = path
            )
            trackingService?.stopService()
            onFinish(result)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
        ) {
            MapEffect(Unit) { map -> googleMap = map }
            if (pathPoints.isNotEmpty()) {
                Polyline(points = pathPoints, color = Color(0xFF0066FF), width = 20f)
            }
        }

        // Карточка статистики
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Таймер
                val h = duration / 3600
                val m = (duration % 3600) / 60
                val s = duration % 60
                Text(
                    text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s),
                    fontSize = 50.sp, fontWeight = FontWeight.Bold,
                    color = if (isPaused) Color.Gray else Color.Black // Серый цвет времени на паузе
                )
                if (isPaused) {
                    Text("PAUSED", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                } else {
                    Spacer(modifier = Modifier.height(14.dp)) // Чтобы высота не прыгала
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(value = String.format("%.2f km", distance / 1000f), label = "Distance")
                    StatItem(value = "$calories", label = "Kcal")
                    StatItem(value = "$steps", label = "Steps")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ КНОПКИ УПРАВЛЕНИЯ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Кнопка Пауза/Старт
                    Button(
                        onClick = {
                            if (isPaused) trackingService?.resumeService()
                            else trackingService?.pauseService()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) Color(0xFF4CAF50) else Color(0xFFFFC107) // Зеленый для старта, Желтый для паузы
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPaused) "RESUME" else "PAUSE",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Кнопка Стоп
                    Button(
                        onClick = { stopAndSave() },
                        enabled = !isUploading && !isSnapshotting,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isUploading || isSnapshotting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FINISH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Extension для скриншота карты
suspend fun GoogleMap.awaitSnapshot(): Bitmap? = suspendCancellableCoroutine { cont ->
    this.snapshot { bmp -> cont.resume(bmp) }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}