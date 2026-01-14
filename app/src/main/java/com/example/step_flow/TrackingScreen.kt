package com.example.step_flow

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.coroutines.resume
import com.google.android.gms.location.LocationServices

data class RunResult(
    val distanceMeters: Float,
    val durationSeconds: Long,
    val calories: Float,
    val avgSpeedKmh: Float,
    val steps: Int,
    val screenshotPath: String?
)

private val GlassCardShape = RoundedCornerShape(24.dp)
private val GlassBtnShape = RoundedCornerShape(16.dp)

private val PanelTop = Color(0xFF7E8991).copy(alpha = 0.56f)
private val PanelMid = Color(0xFF78838B).copy(alpha = 0.54f)
private val PanelBot = Color(0xFF656E77).copy(alpha = 0.56f)

private val ButtonTop = Color(0xFF9199A0).copy(alpha = 0.54f)
private val ButtonMid = Color(0xFF8A939A).copy(alpha = 0.52f)
private val ButtonBot = Color(0xFF7C858D).copy(alpha = 0.54f)

private val BorderOuter = Color.White.copy(alpha = 0.26f)
private val BorderInner = Color.White.copy(alpha = 0.10f)
private val HighlightTop = Color.White.copy(alpha = 0.22f)
private val HighlightEdge = Color.White.copy(alpha = 0.18f)
private val ShadeEdge = Color.Black.copy(alpha = 0.10f)

private val ShadowColor = Color.Black.copy(alpha = 0.28f)

private val TextMain = Color.White.copy(alpha = 0.92f)
private val TextSub = Color.White.copy(alpha = 0.60f)
private val ValueText = Color.White.copy(alpha = 0.88f)

@SuppressLint("MissingPermission")
@Composable
fun TrackingScreen(
    weightKg: Double,
    heightCm: Double,
    ageYears: Int,
    notificationsEnabled: Boolean, // ✅ ADDED
    isUploading: Boolean,
    onFinish: (RunResult) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

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
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }



    LaunchedEffect(Unit) {
        Intent(context, TrackingService::class.java).also { intent ->
            intent.putExtra("WEIGHT", weightKg)
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
            try {
                context.unbindService(connection)
            } catch (_: Exception) {
            }
        }
    }

    val duration by trackingService?.durationSeconds?.collectAsStateWithLifecycle()
        ?: remember { mutableLongStateOf(0L) }
    val distance by trackingService?.distanceMeters?.collectAsStateWithLifecycle()
        ?: remember { mutableFloatStateOf(0f) }
    val calories by trackingService?.calories?.collectAsStateWithLifecycle()
        ?: remember { mutableFloatStateOf(0f) }
    val steps by trackingService?.steps?.collectAsStateWithLifecycle()
        ?: remember { mutableIntStateOf(0) }
    val pathPoints by trackingService?.pathPoints?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(emptyList<LatLng>()) }
    val speedKmh by trackingService?.currentSpeedKmh?.collectAsStateWithLifecycle()
        ?: remember { mutableFloatStateOf(0f) }

    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var isSnapshotting by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 16f)
    }

    LaunchedEffect(Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            17f
                        )
                    )
                }
            }
        } catch (e: SecurityException) {}
    }

    LaunchedEffect(pathPoints) {
        pathPoints.lastOrNull()?.let {

            if (pathPoints.size == 1) {
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 17f))
            } else {
                cameraPositionState.animate(CameraUpdateFactory.newLatLng(it), 1000)
            }
        }
    }

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

            // ✅ SHOW NOTIFICATION AFTER FINISH (only if enabled in Settings)
            if (notificationsEnabled) {
                NotificationHelper.showRunFinished(context)
            }

            onFinish(
                RunResult(
                    distanceMeters = distance,
                    durationSeconds = duration,
                    calories = calories,
                    avgSpeedKmh = speedKmh,
                    steps = steps,
                    screenshotPath = path
                )
            )
        }
    }

    val mapUi = remember {
        MapUiSettings(
            myLocationButtonEnabled = true,
            zoomControlsEnabled = false,
            compassEnabled = false,
            mapToolbarEnabled = false,
            indoorLevelPickerEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            tiltGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    val mapProps = remember { MapProperties(isMyLocationEnabled = true) }

    val layoutDir = LocalLayoutDirection.current
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val panelSafeBottom = insets.calculateBottomPadding()
    val mapPadding = remember(insets, layoutDir) {
        PaddingValues(
            start = insets.calculateStartPadding(layoutDir) + 12.dp,
            top = insets.calculateTopPadding() + 12.dp,
            end = insets.calculateEndPadding(layoutDir) + 12.dp,
            bottom = panelSafeBottom + 16.dp + 220.dp
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProps,
            uiSettings = mapUi,
            contentPadding = mapPadding
        ) {
            MapEffect(Unit) { map -> googleMap = map }
            if (pathPoints.isNotEmpty()) {
                Polyline(points = pathPoints, color = Color(0xFF0066FF), width = 20f)
            }
        }

        GlassPanelExact(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)),
            shape = GlassCardShape
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                val w = maxWidth
                val timeMax = 56.sp
                val timeMin = 36.sp
                val timeSize = remember(w) {
                    val t = (w.value * 0.14f).sp
                    when {
                        t > timeMax -> timeMax
                        t < timeMin -> timeMin
                        else -> t
                    }
                }

                androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AutoSizeText(
                        text = formattedTime,
                        maxFontSize = timeSize,
                        minFontSize = 28.sp,
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = TextMain,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItemGlassExact(
                            value = String.format(Locale.US, "%.2f km", distance / 1000f),
                            label = "Distance"
                        )
                        StatItemGlassExact(
                            value = String.format(Locale.US, "%.1f", calories),
                            label = "Kcal"
                        )
                        StatItemGlassExact(
                            value = "$steps",
                            label = "Steps"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val isPaused by trackingService?.isPaused?.collectAsStateWithLifecycle()
                        ?: remember { mutableStateOf(false) }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GlassActionButtonExact(
                            text = if (isPaused) "RESUME" else "PAUSE",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isPaused) trackingService?.resumeService() else trackingService?.pauseService()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = GlassBtnShape,
                            enabled = true,
                            loading = false
                        )

                        GlassActionButtonExact(
                            text = "FINISH",
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                performStopAndSave()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = GlassBtnShape,
                            enabled = !isUploading && !isSnapshotting,
                            loading = isUploading || isSnapshotting
                        )
                    }
                }
            }
        }
    }
}

suspend fun GoogleMap.awaitSnapshot(): Bitmap? =
    suspendCancellableCoroutine { cont -> this.snapshot { bmp -> cont.resume(bmp) } }

@Composable
private fun GlassPanelExact(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val cornerPx = with(density) { 24.dp.toPx() }
    val cr = CornerRadius(cornerPx, cornerPx)

    Box(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            )
            .clip(shape)
            .background(Brush.verticalGradient(listOf(PanelTop, PanelMid, PanelBot)))
            .border(1.dp, BorderOuter, shape)
            .drawBehind {
                val w = size.width
                val h = size.height

                drawRoundRect(
                    color = BorderInner,
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = cr,
                    size = Size(w, h)
                )

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0f to HighlightTop,
                        0.50f to Color.Transparent
                    ),
                    size = Size(w, h),
                    cornerRadius = cr
                )

                drawLine(
                    color = HighlightEdge,
                    start = Offset(w * 0.08f, h * 0.18f),
                    end = Offset(w * 0.92f, h * 0.18f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = ShadeEdge,
                    start = Offset(w * 0.10f, h * 0.86f),
                    end = Offset(w * 0.90f, h * 0.86f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
    ) {
        content()
    }
}

@Composable
private fun GlassActionButtonExact(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val alpha = if (enabled) 1f else 0.55f

    val density = LocalDensity.current
    val cornerPx = with(density) { 16.dp.toPx() }
    val cr = CornerRadius(cornerPx, cornerPx)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            )
            .clip(shape),
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ButtonTop, ButtonMid, ButtonBot)))
                .border(1.dp, BorderOuter.copy(alpha = BorderOuter.alpha * alpha), shape)
                .drawBehind {
                    val w = size.width
                    val h = size.height

                    drawRoundRect(
                        color = BorderInner.copy(alpha = BorderInner.alpha * alpha),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius = cr,
                        size = Size(w, h)
                    )

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            0f to HighlightTop.copy(alpha = HighlightTop.alpha * alpha),
                            0.60f to Color.Transparent
                        ),
                        cornerRadius = cr,
                        size = Size(w, h)
                    )

                    drawLine(
                        color = HighlightEdge.copy(alpha = HighlightEdge.alpha * alpha),
                        start = Offset(w * 0.10f, h * 0.28f),
                        end = Offset(w * 0.90f, h * 0.28f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = ShadeEdge.copy(alpha = ShadeEdge.alpha * alpha),
                        start = Offset(w * 0.12f, h * 0.82f),
                        end = Offset(w * 0.88f, h * 0.82f),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier.fillMaxSize(),
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TextMain
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = text,
                        fontWeight = FontWeight.Bold,
                        color = TextMain.copy(alpha = TextMain.alpha * alpha),
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItemGlassExact(value: String, label: String) {
    androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AutoSizeText(
            text = value,
            maxFontSize = 22.sp,
            minFontSize = 14.sp,
            style = TextStyle(fontWeight = FontWeight.Bold, color = ValueText),
            maxLines = 1
        )
        Text(text = label, fontSize = 12.sp, color = TextSub, maxLines = 1)
    }
}

@Composable
private fun AutoSizeText(
    text: String,
    maxFontSize: androidx.compose.ui.unit.TextUnit,
    minFontSize: androidx.compose.ui.unit.TextUnit,
    style: TextStyle,
    maxLines: Int,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val fixedWidth = constraints.minWidth == constraints.maxWidth
        val targetW = if (fixedWidth) constraints.maxWidth.coerceAtLeast(0) else constraints.maxWidth.coerceAtLeast(0)

        fun fits(fontSize: androidx.compose.ui.unit.TextUnit): Boolean {
            val p = subcompose("m:${fontSize.value}") {
                Text(
                    text = text,
                    style = style.copy(fontSize = fontSize),
                    maxLines = maxLines,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    textAlign = style.textAlign ?: TextAlign.Start
                )
            }[0].measure(Constraints(maxWidth = Constraints.Infinity, maxHeight = constraints.maxHeight))
            return if (fixedWidth) p.width <= targetW else true
        }

        var lo = minFontSize.value
        var hi = maxFontSize.value
        var best = lo
        var i = 0
        while (i < 18) {
            val mid = (lo + hi) / 2f
            if (fits(mid.sp)) {
                best = mid
                lo = mid
            } else {
                hi = mid
            }
            i++
        }

        val placeable = subcompose("final") {
            Text(
                text = text,
                style = style.copy(fontSize = best.sp),
                maxLines = maxLines,
                softWrap = false,
                overflow = TextOverflow.Clip,
                textAlign = style.textAlign ?: TextAlign.Start
            )
        }[0].measure(constraints.copy(minWidth = 0))

        val layoutW = if (fixedWidth) targetW else placeable.width
        val x = if (fixedWidth) ((layoutW - placeable.width) / 2).coerceAtLeast(0) else 0

        layout(layoutW, placeable.height) {
            placeable.place(x, 0)
        }
    }
}