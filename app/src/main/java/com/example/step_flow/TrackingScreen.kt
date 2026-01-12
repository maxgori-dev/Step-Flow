package com.example.step_flow

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    // --- 1. ТАЙМЕР ---
    var secondsElapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(1000L)
            secondsElapsed++
        }
    }
    // Форматирование времени 00:00:00
    val formattedTime = remember(secondsElapsed) {
        val h = secondsElapsed / 3600
        val m = (secondsElapsed % 3600) / 60
        val s = secondsElapsed % 60
        String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
    }

    // --- 2. ШАГОМЕР ---
    var steps by remember { mutableIntStateOf(0) }
    // SensorManager для доступа к датчику шагов
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val stepSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) }

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            var initialSteps = -1f // Запоминаем кол-во шагов при старте

            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.values.isNotEmpty()) {
                        val currentSteps = it.values[0]
                        // Если это первое измерение, запоминаем его как "ноль"
                        if (initialSteps == -1f) {
                            initialSteps = currentSteps
                        }
                        // Текущие шаги за тренировку = Всего - Начальное
                        steps = (currentSteps - initialSteps).toInt().coerceAtLeast(0)
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Регистрируем слушателя, если датчик есть
        if (stepSensor != null) {
            sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // --- 3. ГЕОЛОКАЦИЯ И РАЗРЕШЕНИЯ ---
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var pathPoints by remember { mutableStateOf(listOf<LatLng>()) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    // Собираем список нужных разрешений
    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    // На Android 10+ (API 29) нужно разрешение на физическую активность
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
    }

    // Лаунчер для запроса списка разрешений
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // Можно проверить perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // Запускаем проверку при старте
    LaunchedEffect(Unit) {
        val allGranted = permissionsToRequest.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allGranted) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Проверка, есть ли разрешение прямо сейчас для логики трекинга
    val hasLocPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    DisposableEffect(hasLocPermission) {
        if (hasLocPermission) {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(1000)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(res: LocationResult) {
                    res.lastLocation?.let { loc ->
                        val newPoint = LatLng(loc.latitude, loc.longitude)
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

    // --- 4. UI ЭКРАНА ---
    Box(modifier = Modifier.fillMaxSize()) {
        // Карта
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocPermission),
            uiSettings = MapUiSettings(myLocationButtonEnabled = true, zoomControlsEnabled = false)
        ) {
            if (pathPoints.isNotEmpty()) {
                Polyline(points = pathPoints, color = Color(0xFF0066FF), width = 20f)
            }
        }

        // Нижняя панель с информацией
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Время
                Text(
                    text = formattedTime,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Шаги
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Steps: $steps", // Можно заменить на иконку 👣 + текст
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Кнопка Стоп
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("STOP RUN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}