package com.example.step_flow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
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
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.resume

// ✅ 1. Класс для возврата результата (данные + путь к скриншоту)
data class RunResult(
    val distanceMeters: Float,
    val durationSeconds: Long,
    val calories: Int,
    val avgSpeedKmh: Float,
    val steps: Int,
    val screenshotPath: String?
)

@SuppressLint("MissingPermission")
@Composable
fun TrackingScreen(
    // ✅ 2. Новые параметры для расчетов и сохранения
    weightKg: Double,
    heightCm: Double,
    ageYears: Int,
    isUploading: Boolean, // Показываем спиннер, если идет отправка в Firebase
    onFinish: (RunResult) -> Unit, // Коллбэк успеха
    onBack: () -> Unit // Коллбэк отмены/выхода
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ЗАЩИТА РАЗРЕШЕНИЙ ---
    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasActivityRecognitionPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else true

    if (!hasLocationPermission || !hasActivityRecognitionPermission) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // --- СОСТОЯНИЕ ---
    var secondsElapsed by remember { mutableLongStateOf(0L) }
    var totalDistanceMeters by remember { mutableFloatStateOf(0f) }
    var currentSpeedMps by remember { mutableFloatStateOf(0f) }
    var steps by remember { mutableIntStateOf(0) }

    // ✅ 3. Состояния для скриншота
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isSnapshotting by remember { mutableStateOf(false) }

    // ✅ 4. Продвинутый расчет калорий
    val caloriesBurned = remember(totalDistanceMeters, secondsElapsed) {
        if (secondsElapsed == 0L) return@remember 0

        val durationMin = secondsElapsed / 60.0
        val distanceKm = totalDistanceMeters / 1000.0
        // Средняя скорость (км/ч)
        val speedKmh = if (durationMin > 0) distanceKm / (durationMin / 60.0) else 0.0

        // BMR (Миффлин-Сан Жеор для мужчин, упрощенная)
        // 10*вес + 6.25*рост - 5*возраст + 5
        val bmrDay = (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) + 5

        // MET (Интенсивность)
        val met = when {
            speedKmh < 0.5 -> 1.0 // Стоим
            speedKmh < 6.0 -> 4.0 // Ходьба
            speedKmh < 9.0 -> 8.0 // Легкий бег
            else -> 11.5          // Бег
        }

        // Формула: (MET * 3.5 * вес / 200) * время_мин
        val burned = (met * 3.5 * weightKg / 200.0) * durationMin
        burned.toInt()
    }

    // --- Метрики для UI ---
    val distanceKm = totalDistanceMeters / 1000.0
    val speedKmh = currentSpeedMps * 3.6
    val avgPaceText = remember(distanceKm, secondsElapsed) {
        if (distanceKm > 0.05) {
            val totalMinutes = secondsElapsed / 60.0
            val paceMinPerKm = totalMinutes / distanceKm
            val pMin = paceMinPerKm.toInt()
            val pSec = ((paceMinPerKm - pMin) * 60).toInt()
            String.format(Locale.US, "%d:%02d /km", pMin, pSec)
        } else { "-:-- /km" }
    }
    val formattedTime = remember(secondsElapsed) {
        val h = secondsElapsed / 3600
        val m = (secondsElapsed % 3600) / 60
        val s = secondsElapsed % 60
        String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    // --- ТАЙМЕР ---
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000L)
            secondsElapsed++
        }
    }

    // --- ШАГОМЕР ---
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            var initialSteps = -1f
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.values.isNotEmpty()) {
                        val current = it.values[0]
                        if (initialSteps == -1f) initialSteps = current
                        steps = (current - initialSteps).toInt().coerceAtLeast(0)
                    }
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        stepSensor?.let { sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // --- ГЕОЛОКАЦИЯ ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var pathPoints by remember { mutableStateOf(listOf<LatLng>()) }
    var lastLocationObj by remember { mutableStateOf<Location?>(null) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    DisposableEffect(Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1000).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.lastLocation?.let { newLoc ->
                    val newPoint = LatLng(newLoc.latitude, newLoc.longitude)
                    if (lastLocationObj != null) {
                        totalDistanceMeters += lastLocationObj!!.distanceTo(newLoc)
                    }
                    lastLocationObj = newLoc
                    if (newLoc.hasSpeed()) currentSpeedMps = newLoc.speed
                    pathPoints = pathPoints + newPoint
                    cameraPositionState.move(CameraUpdateFactory.newLatLng(newPoint))
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        onDispose { fusedLocationClient.removeLocationUpdates(callback) }
    }

    // ✅ 5. Логика "Стоп и Сохранить"
    fun performStopAndSave() {
        isSnapshotting = true
        scope.launch {
            // А. Делаем скриншот
            val bitmap = try {
                googleMap?.awaitSnapshot()
            } catch (e: Exception) { null }

            // Б. Сохраняем во временный файл
            var path: String? = null
            if (bitmap != null) {
                val file = File(context.cacheDir, "run_map_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
                }
                path = file.absolutePath
            }

            // В. Передаем результат наверх
            onFinish(
                RunResult(
                    distanceMeters = totalDistanceMeters,
                    durationSeconds = secondsElapsed,
                    calories = caloriesBurned,
                    avgSpeedKmh = if (secondsElapsed > 0) (totalDistanceMeters/1000f)/(secondsElapsed/3600f) else 0f,
                    steps = steps,
                    screenshotPath = path
                )
            )
            isSnapshotting = false
        }
    }

    // --- ИНТЕРФЕЙС ---
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
        ) {
            // ✅ Запоминаем объект карты для скриншота
            MapEffect(Unit) { map -> googleMap = map }

            if (pathPoints.isNotEmpty()) {
                Polyline(points = pathPoints, color = Color(0xFF0066FF), width = 20f)
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(value = String.format("%.2f km", distanceKm), label = "Distance")
                    StatItem(value = "$caloriesBurned", label = "Kcal")
                    StatItem(value = "$steps", label = "Steps")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // ✅ Обновленная кнопка
                Button(
                    onClick = { performStopAndSave() },
                    // Блокируем, если уже идет сохранение или скриншот
                    enabled = !isUploading && !isSnapshotting,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isUploading || isSnapshotting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if(isSnapshotting) "Saving Map..." else "Uploading...")
                    } else {
                        Text("STOP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ✅ Extension-функция для скриншота карты
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