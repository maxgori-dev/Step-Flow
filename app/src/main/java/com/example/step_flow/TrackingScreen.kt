package com.example.step_flow

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val calories: Float, // Используем Float
    val avgSpeedKmh: Float,
    val steps: Int,
    val screenshotPath: String?
)

@SuppressLint("MissingPermission")
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

    // --- ПОДКЛЮЧЕНИЕ К СЕРВИСУ ---
    var trackingService by remember { mutableStateOf<TrackingService?>(null) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                trackingService = (service as TrackingService.LocalBinder).getService()
            }
            override fun onServiceDisconnected(name: ComponentName?) {
                trackingService = null
            }
        }
    }

    // Запуск и привязка
    LaunchedEffect(Unit) {
        Intent(context, TrackingService::class.java).also { intent ->
            intent.putExtra("WEIGHT", weightKg) // Передаем вес в сервис!
            intent.putExtra("HEIGHT", heightCm)
            intent.putExtra("AGE", ageYears)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { context.unbindService(connection) } catch (e: Exception) {}
        }
    }

    // --- ПОДПИСКА НА ДАННЫЕ СЕРВИСА ---
    // Теперь мы просто ЧИТАЕМ готовые данные, а не считаем их
    val duration by trackingService?.durationSeconds?.collectAsStateWithLifecycle() ?: remember { mutableLongStateOf(0L) }
    val distance by trackingService?.distanceMeters?.collectAsStateWithLifecycle() ?: remember { mutableFloatStateOf(0f) }

    // 👇 ВОТ ЗДЕСЬ МЫ БЕРЕМ КАЛОРИИ ИЗ СЕРВИСА (где вы поставили 0)
    val calories by trackingService?.calories?.collectAsStateWithLifecycle() ?: remember { mutableFloatStateOf(0f) }

    val steps by trackingService?.steps?.collectAsStateWithLifecycle() ?: remember { mutableIntStateOf(0) }
    val pathPoints by trackingService?.pathPoints?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyList()) }
    val speedKmh by trackingService?.currentSpeedKmh?.collectAsStateWithLifecycle() ?: remember { mutableFloatStateOf(0f) }

    // --- UI КАРТЫ ---
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isSnapshotting by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    LaunchedEffect(pathPoints) {
        pathPoints.lastOrNull()?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLng(it), 1000)
        }
    }

    // --- ФОРМАТИРОВАНИЕ ---
    val formattedTime = remember(duration) {
        val h = duration / 3600
        val m = (duration % 3600) / 60
        val s = duration % 60
        String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    fun performStopAndSave() {
        isSnapshotting = true
        scope.launch {
            val bitmap = googleMap?.awaitSnapshot()
            var path: String? = null
            if (bitmap != null) {
                val file = File(context.cacheDir, "run_map_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                path = file.absolutePath
            }

            trackingService?.stopService()

            onFinish(
                RunResult(
                    distanceMeters = distance,
                    durationSeconds = duration,
                    calories = calories, // Передаем то, что насчитал сервис
                    avgSpeedKmh = speedKmh,
                    steps = steps,
                    screenshotPath = path
                )
            )
        }
    }

    // --- UI ---
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

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(formattedTime, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(value = String.format(Locale.US, "%.2f km", distance / 1000f), label = "Distance")
                    // 👇 Отображаем калории из сервиса
                    StatItem(value = String.format(Locale.US, "%.1f", calories), label = "Kcal")
                    StatItem(value = "$steps", label = "Steps")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки (Пауза / Стоп)
                val isPaused by trackingService?.isPaused?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            if (isPaused) trackingService?.resumeService() else trackingService?.pauseService()
                        },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(if(isPaused) "RESUME" else "PAUSE", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { performStopAndSave() },
                        enabled = !isUploading && !isSnapshotting,
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isUploading || isSnapshotting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("FINISH", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

suspend fun GoogleMap.awaitSnapshot(): Bitmap? = suspendCancellableCoroutine { cont ->
    this.snapshot { bmp -> cont.resume(bmp) }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}