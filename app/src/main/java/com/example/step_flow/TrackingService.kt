package com.example.step_flow

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TrackingService : Service() {

    private val binder = LocalBinder()
    inner class LocalBinder : Binder() {
        fun getService(): TrackingService = this@TrackingService
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // --- StateFlow ---
    private val _durationSeconds = MutableStateFlow(0L)
    val durationSeconds = _durationSeconds.asStateFlow()

    private val _distanceMeters = MutableStateFlow(0f)
    val distanceMeters = _distanceMeters.asStateFlow()

    private val _calories = MutableStateFlow(0f)
    val calories = _calories.asStateFlow()

    private val _steps = MutableStateFlow(0)
    val steps = _steps.asStateFlow()

    private val _pathPoints = MutableStateFlow<List<LatLng>>(emptyList())
    val pathPoints = _pathPoints.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(0f)
    val currentSpeedKmh = _currentSpeedKmh.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var notificationManager: NotificationManager

    // Данные пользователя
    private var weightKg: Double = 70.0
    private var heightCm: Double = 175.0
    private var ageYears: Int = 25

    private var isTracking = false
    private var lastLocation: Location? = null
    private var initialStepCount = -1

    private var preciseCalories: Double = 0.0

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            weightKg = intent.getDoubleExtra("WEIGHT", 70.0)
            heightCm = intent.getDoubleExtra("HEIGHT", 175.0)
            ageYears = intent.getIntExtra("AGE", 25)

            if (!isTracking) {
                startTracking()
            }
        }
        return START_STICKY
    }

    private fun startTracking() {
        isTracking = true
        _isPaused.value = false

        startForegroundService()

        serviceScope.launch {
            while (isTracking) {
                if (!_isPaused.value) {
                    delay(1000L) // Ждем 1 секунду
                    _durationSeconds.value += 1

                    // Обновляем калории
                    updateCaloriesForOneSecond()

                    updateNotification()
                } else {
                    delay(500L)
                }
            }
        }
        registerSensors()
    }

    private fun updateCaloriesForOneSecond() {
        val speed = _currentSpeedKmh.value

        // 🛡️ 1. ЗАЩИТА ОТ ДРЕЙФА GPS
        // Если скорость меньше 2.0 км/ч, считаем, что мы стоим или топчемся на месте.
        // Калории не начисляются.
        if (speed < 2.5f) {
            return
        }

        // 🛡️ 2. ДИНАМИЧЕСКИЙ РАСЧЕТ MET (зависит от точной скорости)
        // Мы не используем жесткие рамки, а умножаем скорость на коэффициент.
        val met = if (speed <= 7.0) {
            // Ходьба: Энергозатраты растут медленнее
            // Пример: 5 км/ч * 0.7 = 3.5 MET (стандарт для ходьбы)
            speed * 0.7f
        } else {
            // Бег: Энергозатраты равны или чуть выше скорости
            // Пример: 10 км/ч * 1.0 = 10 MET (стандарт для бега)
            speed * 1.0f
        }

        // 🛡️ 3. СТАНДАРТНАЯ ФОРМУЛА КАЛОРИЙ
        // Формула: (MET * 3.5 * Вес) / 200 = Ккал в МИНУТУ
        val kcalPerMin = (met * 3.5 * weightKg) / 200.0

        // Делим на 60, чтобы получить порцию за 1 СЕКУНДУ
        val kcalPerSec = kcalPerMin / 60.0

        preciseCalories += kcalPerSec
        _calories.value = preciseCalories.toFloat()
    }

    @SuppressLint("MissingPermission")
    private fun registerSensors() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1000)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())

        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensors() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(stepListener)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (_isPaused.value) return

            result.lastLocation?.let { location ->
                // 🛡️ Фильтр плохих точек GPS (>20м)
                if (location.accuracy > 20) return@let

                if (lastLocation != null) {
                    val dist = lastLocation!!.distanceTo(location)
                    _distanceMeters.value += dist
                }
                lastLocation = location

                val speedMps = if (location.hasSpeed()) location.speed else 0f
                _currentSpeedKmh.value = speedMps * 3.6f

                val newPoint = LatLng(location.latitude, location.longitude)
                _pathPoints.value = _pathPoints.value + newPoint
            }
        }
    }

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (_isPaused.value) return
            event?.let {
                val current = it.values[0].toInt()
                if (initialStepCount == -1) initialStepCount = current
                _steps.value = (current - initialStepCount).coerceAtLeast(0)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun pauseService() {
        _isPaused.value = true
        unregisterSensors()
        updateNotification()
    }

    fun resumeService() {
        _isPaused.value = false
        registerSensors()
        updateNotification()
    }

    fun stopService() {
        isTracking = false
        unregisterSensors()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundService() {
        val notification = createNotification("Workout started...")
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(this, 1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }
    }

    private fun updateNotification() {
        val stateText = if (_isPaused.value) "PAUSED" else formatTime(_durationSeconds.value)
        val distStr = String.format(Locale.US, "%.2f km", _distanceMeters.value / 1000f)
        val calStr = String.format(Locale.US, "%.1f kcal", _calories.value)

        val notification = createNotification("$stateText • $distStr • $calStr")
        notificationManager.notify(1, notification)
    }

    private fun createNotification(content: String) = NotificationCompat.Builder(this, "tracking_channel")
        .setContentTitle("Step-Flow Run")
        .setContentText(content)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Замените на вашу иконку!
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("tracking_channel", "Workout Tracking", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        unregisterSensors()
    }

    override fun onBind(intent: Intent): IBinder = binder

    private fun formatTime(sec: Long): String {
        val h = sec / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }
}