package com.example.step_flow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.util.Locale

@SuppressLint("MissingPermission")
@Composable
fun TrackingScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // --- СОСТОЯНИЕ ДАННЫХ ---
    var secondsElapsed by remember { mutableLongStateOf(0L) }
    var totalDistanceMeters by remember { mutableFloatStateOf(0f) }
    var currentSpeedMps by remember { mutableFloatStateOf(0f) } // м/с
    var steps by remember { mutableIntStateOf(0) }

    // --- МАТЕМАТИКА ---

    // 1. Дистанция в км
    val distanceKm = totalDistanceMeters / 1000.0

    // 2. Текущая скорость (м/с -> км/ч)
    val speedKmh = currentSpeedMps * 3.6

    // 3. Средний темп (время на 1 км)
    val avgPaceText = remember(distanceKm, secondsElapsed) {
        if (distanceKm > 0.05) { // Считаем только если прошли > 50м
            val totalMinutes = secondsElapsed / 60.0
            val paceMinPerKm = totalMinutes / distanceKm

            val pMin = paceMinPerKm.toInt()
            val pSec = ((paceMinPerKm - pMin) * 60).toInt()
            String.format(Locale.US, "%d:%02d /km", pMin, pSec)
        } else {
            "-:-- /km"
        }
    }

    // 4. Таймер 00:00:00
    val formattedTime = remember(secondsElapsed) {
        val h = secondsElapsed / 3600
        val m = (secondsElapsed % 3600) / 60
        val s = secondsElapsed % 60
        String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    // --- ЗАПУСК ТАЙМЕРА ---
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
        if (stepSensor != null) sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // --- ГЕОЛОКАЦИЯ ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var pathPoints by remember { mutableStateOf(listOf<LatLng>()) }
    var lastLocationObj by remember { mutableStateOf<Location?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        hasPermission = it[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) perms.add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (!hasPermission) permissionLauncher.launch(perms.toTypedArray())
    }

    DisposableEffect(hasPermission) {
        if (hasPermission) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1000)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(res: LocationResult) {
                    res.lastLocation?.let { newLoc ->
                        val newPoint = LatLng(newLoc.latitude, newLoc.longitude)

                        // Считаем дистанцию
                        if (lastLocationObj != null) {
                            val dist = lastLocationObj!!.distanceTo(newLoc)
                            totalDistanceMeters += dist
                        }
                        lastLocationObj = newLoc

                        // Скорость
                        if (newLoc.hasSpeed()) {
                            currentSpeedMps = newLoc.speed
                        }

                        // Рисуем линию
                        pathPoints = pathPoints + newPoint
                        cameraPositionState.move(CameraUpdateFactory.newLatLng(newPoint))
                    }
                }
            }
            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
            onDispose { fusedLocationClient.removeLocationUpdates(callback) }
        } else {
            onDispose { }
        }
    }

    // --- ИНТЕРФЕЙС ---
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
        ) {
            if (pathPoints.isNotEmpty()) {
                Polyline(points = pathPoints, color = Color(0xFF0066FF), width = 20f)
            }
        }

        // КАРТОЧКА С ДАННЫМИ
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
                // Таймер
                Text(
                    text = formattedTime,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Статистика в один ряд
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // Равномерно распределяем
                ) {
                    StatItem(value = String.format("%.2f km", distanceKm), label = "Distance")
                    StatItem(value = String.format("%.1f km/h", speedKmh), label = "Speed")
                    StatItem(value = avgPaceText, label = "Avg Pace")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Шаги
                Text(
                    text = "Steps: $steps",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("STOP RUN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Компонент для одной цифры статистики
@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp, // Чуть крупнее
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