package com.example.step_flow

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // ===== Profile =====
            var name by rememberSaveable { mutableStateOf("") }

            // Personal details
            var heightCm by rememberSaveable { mutableStateOf("") }
            var weightKg by rememberSaveable { mutableStateOf("") }
            var ageYears by rememberSaveable { mutableStateOf("") }

            // ===== Settings =====
            var language by rememberSaveable { mutableStateOf(AppLanguage.System) }
            var units by rememberSaveable { mutableStateOf(Units.Metric) }
            var theme by rememberSaveable { mutableStateOf(AppTheme.System) }
            var fontScale by rememberSaveable { mutableFloatStateOf(1.0f) }
            var notificationsEnabled by rememberSaveable { mutableStateOf(true) }

            // Calendar state
            var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

            /**
             * ✅ Back stack для step-навигации
             * 0 welcome, 1 setup, 2 home, 3 calendar, 4 profile, 5 personal, 6 settings, 7 faq, 8 contact, 9 tips
             */
            val backStack = rememberSaveable(
                saver = listSaver(
                    save = { it.toList() },
                    restore = { it.toMutableStateList() }
                )
            ) { mutableStateListOf(0) }

            var step by rememberSaveable { mutableIntStateOf(backStack.last()) }

            // ✅ направление анимации (вперёд / назад)
            var forwardAnim by rememberSaveable { mutableStateOf(true) }

            fun navigate(to: Int) {
                if (backStack.lastOrNull() == to) return
                forwardAnim = true
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

            // ✅ Системная кнопка "Назад" -> на предыдущий экран
            BackHandler(enabled = true) { goBack() }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    // ✅ Ultra-smooth iOS-like transitions (без рывков)
                    AnimatedStepHost(
                        step = step,
                        forward = forwardAnim
                    ) { target ->

                        when (target) {

                            // 0 — Welcome (name)
                            0 -> {
                                WelcomeNameScreen(
                                    name = name,
                                    onNameChange = { name = it },
                                    onContinue = { navigate(1) }
                                )
                            }

                            // 1 — Profile setup
                            1 -> {
                                ProfileSetupScreen(
                                    onContinue = { navigate(2) }
                                )
                            }

                            // 2 — Home
                            2 -> {
                                HomeScreenNow(
                                    onRunClick = {
                                        navigate(10)
                                    },
                                    onTileCalendar = { navigate(3) },
                                    onTileHistory = {
                                        // TODO
                                    },
                                    onTileAchievements = {
                                        // TODO
                                    },
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
                                    onPickMonth = {
                                        // TODO
                                    }
                                )
                            }

                            // 4 — Profile
                            4 -> {
                                ProfileScreen(
                                    name = name,
                                    onNameChange = { name = it },
                                    onBack = { goBack() },

                                    onPersonalDetails = { navigate(5) },
                                    onSettings = { navigate(6) },

                                    // ✅ FIX: реально открываем Tips
                                    onTips = { navigate(9) },

                                    onFaq = { navigate(7) },
                                    onContact = { navigate(8) }
                                )
                            }

                            // 5 — Personal Details
                            5 -> {
                                PersonalDetailsScreen(
                                    name = name,
                                    heightCm = heightCm,
                                    weightKg = weightKg,
                                    ageYears = ageYears,

                                    onNameChange = { name = it },
                                    onHeightChange = { heightCm = it },
                                    onWeightChange = { weightKg = it },
                                    onAgeChange = { ageYears = it },

                                    onBack = { goBack() },
                                    onSave = { goBack() }
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

                            // 7 — FAQ
                            7 -> {
                                FaqScreen(
                                    onBack = { goBack() }
                                )
                            }

                            // 8 — Contact Us
                            8 -> {
                                ContactUsScreen(
                                    onBack = { goBack() }
                                )
                            }

                            // 9 — Tips & Tricks
                            9 -> {
                                TipsAndTricksScreen(
                                    onBack = { goBack() }
                                )
                            }

                            // 10 — Run Tracking
                            10 -> {
                                TrackingScreen(
                                    onBack = {
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
@androidx.compose.runtime.Composable
private fun AnimatedStepHost(
    step: Int,
    forward: Boolean,
    content: @androidx.compose.runtime.Composable (Int) -> Unit
) {
    val iosEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

    val duration = 680

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            val inSlide = slideInHorizontally(
                animationSpec = tween(durationMillis = duration, easing = iosEase)
            ) { full ->
                if (forward) full / 6 else -full / 10
            }

            val outSlide = slideOutHorizontally(
                animationSpec = tween(durationMillis = duration, easing = iosEase)
            ) { full ->
                if (forward) -full / 14 else full / 7
            }

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
