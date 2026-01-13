package com.example.step_flow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.step_flow.data.RunModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()

    private fun hasRequiredPermissions(): Boolean {
        val hasLocation =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        val hasActivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) ==
                    PackageManager.PERMISSION_GRANTED
        } else true

        return hasLocation && hasActivity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val ui by vm.uiState.collectAsStateWithLifecycle()
            val uploadState by vm.uploadState.collectAsStateWithLifecycle()
            val goals by vm.goals.collectAsStateWithLifecycle()
            val runsList by vm.runsFlow.collectAsStateWithLifecycle()

            var selectedRun by remember { mutableStateOf<RunModel?>(null) }

            if (!ui.loaded) {
                MaterialTheme { Surface {} }
                return@setContent
            }

            
            
            
            val settingsRepo = remember { SettingsRepository(applicationContext) }
            val settings by settingsRepo.flow.collectAsStateWithLifecycle(initialValue = SettingsState())
            val scope = rememberCoroutineScope()

            
            val baseDensity = LocalDensity.current
            val scaledDensity = remember(settings.fontScale, baseDensity) {
                Density(
                    density = baseDensity.density,
                    fontScale = baseDensity.fontScale * settings.fontScale.coerceIn(0.85f, 1.25f)
                )
            }

            
            val dark = when (settings.theme) {
                AppTheme.System -> isSystemInDarkTheme()
                AppTheme.Light -> false
                AppTheme.Dark -> true
            }

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                StepFlowTheme(darkTheme = dark) {
                    Surface (
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background) {



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
                            if (uploadState is UploadState.Success) vm.resetUploadState()
                        }

                        BackHandler(enabled = true) { goBack() }

                        AnimatedStepHost(step = step, forward = forwardAnim) { target ->
                            when (target) {
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

                                2 -> {
                                    HomeScreenNow(
                                        onRunClick = {
                                            if (hasRequiredPermissions()) {
                                                navigateRoot(11)
                                            } else {
                                                navigate(10)
                                            }
                                        },
                                        onTileCalendar = { navigate(3) },
                                        onTileHistory = { navigate(12) },
                                        onTileAchievements = { navigate(14) },
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

                                3 -> {
                                    ActivityCalendarScreen(
                                        runs = runsList,
                                        goalSteps = goals.first,
                                        goalMinutes = goals.second,
                                        goalKcal = goals.third,
                                        initialSelectedDate = selectedDate,
                                        onPickDay = { date -> selectedDate = date },
                                        onBack = { goBack() }
                                    )
                                }

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

                                6 -> {
                                    SettingsScreen(
                                        language = settings.language,
                                        units = settings.units,
                                        theme = settings.theme,
                                        fontScale = settings.fontScale,
                                        notificationsEnabled = settings.notificationsEnabled,

                                        
                                        onLanguageChange = { v ->
                                            scope.launch {
                                                settingsRepo.setLanguage(v)
                                                applyAppLanguage(v)
                                            }
                                        },

                                        onUnitsChange = { v -> scope.launch { settingsRepo.setUnits(v) } },
                                        onThemeChange = { v -> scope.launch { settingsRepo.setTheme(v) } },
                                        onFontScaleChange = { v -> scope.launch { settingsRepo.setFontScale(v) } },
                                        onNotificationsChange = { v -> scope.launch { settingsRepo.setNotifications(v) } },

                                        onGoalsClick = { navigate(15) },
                                        onBack = { goBack() },
                                        onSave = { goBack() }
                                    )
                                }

                                7 -> FaqScreen(onBack = { goBack() })
                                8 -> ContactUsScreen(onBack = { goBack() })
                                9 -> TipsAndTricksScreen(onBack = { goBack() })

                                10 -> {
                                    PermissionScreen(
                                        onPermissionsGranted = { navigateRoot(11) },
                                        onPermissionDenied = { goBack() }
                                    )
                                }

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
                                            vm.saveRunToFirebase(
                                                distanceMeters = result.distanceMeters,
                                                durationSeconds = result.durationSeconds,
                                                calories = result.calories.toFloat(),
                                                avgSpeedKmh = result.avgSpeedKmh,
                                                steps = result.steps,
                                                localScreenshotPath = result.screenshotPath
                                            )
                                            navigateRoot(2)
                                        },
                                        onBack = { goBack() }
                                    )
                                }

                                12 -> {
                                    HistoryScreen(
                                        runs = runsList,
                                        onRunClick = { run ->
                                            selectedRun = run
                                            navigate(13)
                                        },
                                        onBack = { goBack() }
                                    )
                                }

                                13 -> {
                                    val run = selectedRun
                                    if (run != null) {
                                        RunDetailsScreen(run = run, onBack = { goBack() })
                                    } else {
                                        LaunchedEffect(Unit) { goBack() }
                                    }
                                }

                                14 -> {
                                    AchievementsScreen(
                                        runs = runsList,
                                        name = savedName,
                                        avatarUriString = savedAvatarUri,
                                        onBack = { goBack() }
                                    )
                                }

                                15 -> {
                                    GoalsScreen(
                                        currentSteps = goals.first,
                                        currentMinutes = goals.second,
                                        currentKcal = goals.third,
                                        onBack = { goBack() },
                                        onSave = { s, m, k ->
                                            vm.saveGoals(s, m, k)
                                            goBack()
                                        }
                                    )
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
