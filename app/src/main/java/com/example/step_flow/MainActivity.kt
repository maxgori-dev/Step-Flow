package com.example.step_flow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import com.example.step_flow.data.RunModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    private fun hasRequiredPermissions(): Boolean {
        val hasLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Для Android 10+ нужно еще разрешение на физическую активность
        val hasActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return hasLocation && hasActivity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ui by vm.uiState.collectAsStateWithLifecycle()

            val uploadState by vm.uploadState.collectAsStateWithLifecycle()
            // ПОДПИСЫВАЕМСЯ НА СПИСОК ЗАБЕГОВ
            val runsList by vm.runsFlow.collectAsStateWithLifecycle()
            // ПЕРЕМЕННАЯ ДЛЯ ХРАНЕНИЯ ВЫБРАННОГО ЗАБЕГА
            var selectedRun by remember { mutableStateOf<RunModel?>(null) }

            if (!ui.loaded) {
                MaterialTheme { Surface(modifier = Modifier.fillMaxSize()) {} }
                return@setContent
            }

            val savedName = ui.name
            val savedAvatarUri = ui.avatarUri

            var nameDraft by rememberSaveable { mutableStateOf("") }

            var heightCm by rememberSaveable { mutableStateOf("") }
            var weightKg by rememberSaveable { mutableStateOf("") }
            var ageYears by rememberSaveable { mutableStateOf("") }

            LaunchedEffect(ui.heightCm, ui.weightKg, ui.ageYears) {
                if (heightCm.isBlank()) heightCm = ui.heightCm
                if (weightKg.isBlank()) weightKg = ui.weightKg
                if (ageYears.isBlank()) ageYears = ui.ageYears
            }

            var language by rememberSaveable { mutableStateOf(AppLanguage.System) }
            var units by rememberSaveable { mutableStateOf(Units.Metric) }
            var theme by rememberSaveable { mutableStateOf(AppTheme.System) }
            var fontScale by rememberSaveable { mutableFloatStateOf(1.0f) }
            var notificationsEnabled by rememberSaveable { mutableStateOf(true) }

            var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

            val backStack = rememberSaveable(
                saver = listSaver(
                    save = { it.toList() },
                    restore = { it.toMutableStateList() }
                )
            ) { mutableStateListOf<Int>() }

            var step by rememberSaveable { mutableIntStateOf(0) }
            var forwardAnim by rememberSaveable { mutableStateOf(true) }

            var bootstrapped by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(ui.loaded, ui.name, ui.setupDone) {
                if (!bootstrapped) {
                    val first = when {
                        ui.shouldShowWelcome -> 0
                        ui.shouldShowProfileSetup -> 1
                        else -> 2
                    }
                    backStack.clear()
                    backStack.add(first)
                    step = first
                    forwardAnim = true

                    if (nameDraft.isBlank() && ui.name.isNotBlank()) {
                        nameDraft = ui.name
                    }

                    bootstrapped = true
                }
            }

            fun navigate(to: Int) {
                if (backStack.lastOrNull() == to) return
                forwardAnim = true
                backStack.add(to)
                step = to
            }

            fun navigateRoot(to: Int) {
                forwardAnim = true
                backStack.clear()
                backStack.add(to)
                step = to
            }

            fun goBack() {
                if (backStack.size > 1) {
                    forwardAnim = false
                    backStack.removeAt(backStack.lastIndex)
                    step = backStack.last()
                } else {
                    finish()
                }
            }

            LaunchedEffect(uploadState) {
                if (uploadState is UploadState.Success) {
                    vm.resetUploadState()
                    // Используем флаг для навигации или сбрасываем состояние внутри обработчика
                    // Но так как navigateRoot локальная функция, мы сделаем это через onFinish
                }
            }

            BackHandler(enabled = true) { goBack() }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnimatedStepHost(step = step, forward = forwardAnim) { target ->
                        when (target) {

                            // 0 — Welcome (name)
                            0 -> {
                                WelcomeNameScreen(
                                    name = nameDraft,
                                    onNameChange = { nameDraft = it },
                                    onContinue = {
                                        val finalName = nameDraft.trim()
                                        if (finalName.isNotBlank()) {
                                            vm.saveName(finalName)
                                            navigate(1)
                                        }
                                    }
                                )
                            }

                            // 1 — Profile setup
                            1 -> {
                                ProfileSetupScreen(
                                    heightCm = heightCm,
                                    weightKg = weightKg,
                                    ageYears = ageYears,
                                    onHeightChange = { heightCm = it },
                                    onWeightChange = { weightKg = it },
                                    onAgeChange = { ageYears = it },
                                    onContinue = {
                                        vm.saveProfile(heightCm, weightKg, ageYears)
                                        navigateRoot(2)
                                    }
                                )
                            }

                            // 2 — Home
                            2 -> {
                                HomeScreenNow(
                                    onRunClick = {
                                        if (hasRequiredPermissions()) {
                                        // Если права УЖЕ есть -> сразу запускаем тренировку (шаг 11)
                                        // Используем navigateRoot, чтобы нельзя было вернуться назад к меню кнопкой "Назад"
                                        navigateRoot(11)
                                    } else {
                                        // Если прав НЕТ -> идем на экран запроса (шаг 10)
                                        navigate(10)
                                    } },
                                    onTileCalendar = { navigate(3) },
                                    onTileHistory = { navigate(12) },
                                    onTileAchievements = { /* TODO */ },
                                    onTopProfile = { navigate(4) },
                                    onTopSettings = { navigate(6) },
                                    onBottomTabChange = { tab ->
                                        when (tab) {
                                            HomeTab.Calendar -> navigate(3)
                                            HomeTab.Home -> navigate(2)
                                            HomeTab.Profile -> navigate(4)
                                        }
                                    }
                                )
                            }

                            // 3 — Activity Calendar
                            3 -> {
                                ActivityCalendarScreen(
                                    initialSelectedDate = selectedDate,
                                    onPickDay = { date -> selectedDate = date },
                                    onBack = { goBack() },
                                    onPickMonth = { /* TODO */ }
                                )
                            }

                            // 4 — Profile (✅ avatar сохранение)
                            4 -> {
                                ProfileScreen(
                                    name = savedName,
                                    avatarUriString = savedAvatarUri,
                                    onAvatarChange = { uri: String -> vm.saveAvatarUri(uri) },
                                    onNameChange = { vm.saveName(it) },
                                    onBack = { goBack() },
                                    onPersonalDetails = { navigate(5) },
                                    onSettings = { navigate(6) },
                                    onTips = { navigate(9) },
                                    onFaq = { navigate(7) },
                                    onContact = { navigate(8) }
                                )
                            }

                            // 5 — Personal Details
                            5 -> {
                                PersonalDetailsScreen(
                                    name = savedName,
                                    heightCm = heightCm,
                                    weightKg = weightKg,
                                    ageYears = ageYears,
                                    onNameChange = { vm.saveName(it) },
                                    onHeightChange = { heightCm = it },
                                    onWeightChange = { weightKg = it },
                                    onAgeChange = { ageYears = it },
                                    onBack = { goBack() },
                                    onSave = {
                                        vm.saveProfile(heightCm, weightKg, ageYears)
                                        goBack()
                                    }
                                )
                            }

                            // 6 — Settings
                            6 -> {
                                SettingsScreen(
                                    language = language,
                                    units = units,
                                    theme = theme,
                                    fontScale = fontScale,
                                    notificationsEnabled = notificationsEnabled,
                                    onLanguageChange = { language = it },
                                    onUnitsChange = { units = it },
                                    onThemeChange = { theme = it },
                                    onFontScaleChange = { fontScale = it },
                                    onNotificationsChange = { notificationsEnabled = it },
                                    onBack = { goBack() },
                                    onSave = { goBack() }
                                )
                            }

                            7 -> FaqScreen(onBack = { goBack() })
                            8 -> ContactUsScreen(onBack = { goBack() })
                            9 -> TipsAndTricksScreen(onBack = { goBack() })
                            10 -> {
                                PermissionScreen(
                                    onPermissionsGranted = {
                                        // Разрешения получены -> переходим на экран тренировки (шаг 11)
                                        // navigateRoot, чтобы нельзя было вернуться назад к разрешениям
                                        navigateRoot(11)
                                    },
                                    onPermissionDenied = {
                                        // Отказано -> возвращаемся на предыдущий экран (Home)
                                        goBack()
                                    }
                                )
                            }
                            // ИСПРАВЛЕНО: Шаг 11 - теперь это экран тренировки
                            11 -> {
                                val w = ui.weightKg.toDoubleOrNull() ?: 64.0
                                val h = ui.heightCm.toDoubleOrNull() ?: 172.0
                                val a = ui.ageYears.toIntOrNull() ?: 34
                                TrackingScreen(
                                    weightKg = w,
                                    heightCm = h,
                                    ageYears = a,
                                    isUploading = uploadState is UploadState.Loading,
                                    onFinish = { result ->
                                        // 2. ЗАПУСКАЕМ СОХРАНЕНИЕ В FIREBASE
                                        vm.saveRunToFirebase(
                                            distanceMeters = result.distanceMeters,
                                            durationSeconds = result.durationSeconds,
                                            calories = result.calories,
                                            avgSpeedKmh = result.avgSpeedKmh,
                                            steps = result.steps,
                                            localScreenshotPath = result.screenshotPath
                                        )
                                        // Сразу уходим на главный экран, загрузка продолжится в фоне (ViewModelScope)
                                        // Либо можно остаться и ждать UploadState.Success
                                        navigateRoot(2)
                                    },
                                    onBack = { goBack() }
                                )
                            }

                            12 -> {
                                HistoryScreen(
                                    runs = runsList,
                                    onRunClick = { run ->
                                        selectedRun = run // Запоминаем забег
                                        navigate(13)      // Идем в детали
                                    },
                                    onBack = { goBack() }
                                )
                            }

                            13 -> {
                                if (selectedRun != null) {
                                    RunDetailsScreen(
                                        run = selectedRun!!,
                                        onBack = { goBack() }
                                    )
                                } else {
                                    // Если вдруг null, вернемся назад
                                    LaunchedEffect(Unit) { goBack() }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun AnimatedStepHost(
        step: Int,
        forward: Boolean,
        content: @Composable (Int) -> Unit
    ) {
        val iosEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
        val duration = 680

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val inSlide = slideInHorizontally(
                    animationSpec = tween(durationMillis = duration, easing = iosEase)
                ) { full -> if (forward) full / 6 else -full / 10 }

                val outSlide = slideOutHorizontally(
                    animationSpec = tween(durationMillis = duration, easing = iosEase)
                ) { full -> if (forward) -full / 14 else full / 7 }

                val inFade = fadeIn(
                    animationSpec = tween(durationMillis = duration, easing = iosEase),
                    initialAlpha = 0.86f
                )

                val outFade = fadeOut(
                    animationSpec = tween(durationMillis = duration, easing = iosEase),
                    targetAlpha = 0.98f
                )

                (inSlide + inFade)
                    .togetherWith(outSlide + outFade)
                    .using(SizeTransform(clip = false))
            },
            label = "UltraSmoothIOSPushPop"
        ) { target ->
            content(target)
        }
    }
}
