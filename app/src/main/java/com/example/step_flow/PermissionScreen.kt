package com.example.step_flow

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat

// Перечисление для управления состоянием экрана
enum class PermissionScreenState {
    REQUESTING_INITIAL,      // 1. Запрашиваем основные разрешения
    REQUESTING_BACKGROUND,   // 2. Запрашиваем фоновое разрешение
    DENIED_PERMANENTLY       // 3. Пользователь навсегда отклонил основные разрешения
}

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    // Управляем, какой UI показывать пользователю
    var screenState by remember { mutableStateOf(PermissionScreenState.REQUESTING_INITIAL) }

    // --- Лаунчер для ОСНОВНЫХ разрешений (геолокация + активность) ---
    val initialPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                screenState = PermissionScreenState.REQUESTING_BACKGROUND
            } else {
                onPermissionsGranted()
            }
        } else {
            if (
                activity != null &&
                !ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                screenState = PermissionScreenState.DENIED_PERMANENTLY
            } else {
                // остаёмся на REQUESTING_INITIAL
            }
        }
    }

    // --- Лаунчер для ФОНОВОГО разрешения (только для Android 10+) ---
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onPermissionsGranted()
    }

    when (screenState) {

        // --- Экран 1: Запрос основных разрешений ---
        PermissionScreenState.REQUESTING_INITIAL -> {
            PermissionUI(
                title = "Workout Permissions",
                infoText = "To track your route and steps, the app needs access to your location and physical activity data.",
                buttonText = "Grant Access",
                onButtonClick = { initialPermissionsLauncher.launch(getInitialPermissions()) },
                onBackClick = onPermissionDenied
            )

            // Авто-запрос при первом входе
            LaunchedEffect(Unit) {
                initialPermissionsLauncher.launch(getInitialPermissions())
            }
        }

        // --- Экран 2: Запрос фонового доступа ---
        PermissionScreenState.REQUESTING_BACKGROUND -> {
            PermissionUI(
                title = "Background Tracking",
                infoText = "To continue recording your route when the app is minimized or the screen is off, please allow location access \"all the time\".",
                buttonText = "Got it, Continue",
                onButtonClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                },
                onBackClick = {
                    onPermissionsGranted()
                }
            )
        }

        // --- Экран 3: Пользователь навсегда отклонил ---
        PermissionScreenState.DENIED_PERMANENTLY -> {
            PermissionUI(
                title = "Location Access Required",
                infoText = "You have permanently denied the permission. For the app to work, please grant access manually in the app settings.",
                buttonText = "Open Settings",
                onButtonClick = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onBackClick = onPermissionDenied
            )
        }
    }
}

private fun getInitialPermissions(): Array<String> {
    val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        perms.add(Manifest.permission.ACTIVITY_RECOGNITION)
    }
    return perms.toTypedArray()
}

@Composable
private fun PermissionUI(
    title: String,
    infoText: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = infoText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ✅ BaselineProfile: primary action button
        Button(
            onClick = onButtonClick,
            modifier = Modifier.semantics { contentDescription = "bp_permission_allow" }
        ) {
            Text(buttonText)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ✅ BaselineProfile: back/deny
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.semantics { contentDescription = "bp_permission_deny" }
        ) {
            Text("Back")
        }
    }
}

// Вспомогательная функция для безопасного поиска Activity
private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
