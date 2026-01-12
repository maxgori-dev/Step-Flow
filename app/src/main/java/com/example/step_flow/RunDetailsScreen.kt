package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.step_flow.data.RunModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunDetailsScreen(
    run: RunModel,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // 1. Форматирование даты
    val dateStr = remember(run.timestamp) {
        val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(run.timestamp))
    }

    // 2. Расчет ТЕМПА (мин/км)
    val paceText = remember(run.avgSpeedKmh) {
        if (run.avgSpeedKmh > 0.1) {
            val paceMinTotal = 60.0 / run.avgSpeedKmh
            val pMin = paceMinTotal.toInt()
            val pSec = ((paceMinTotal - pMin) * 60).toInt()
            String.format(Locale.US, "%d:%02d", pMin, pSec)
        } else {
            "-:--"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Run Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F9))
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // --- БЛОК С КАРТОЙ ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(250.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                if (run.mapImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = run.mapImageUrl,
                        contentDescription = "Run Map",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No map available", color = Color.Gray)
                    }
                }
            }

            // --- ДАТА ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = dateStr, color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- СТАТИСТИКА (Сетка карточек) ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Ряд 1: Дистанция и Калории
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailCard(
                        modifier = Modifier.weight(1f),
                        value = String.format(Locale.US, "%.2f", run.distanceMeters / 1000f),
                        unit = "km",
                        label = "Distance"
                    )
                    DetailCard(
                        modifier = Modifier.weight(1f),
                        value = "${run.calories}",
                        unit = "kcal",
                        label = "Calories"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ряд 2: Время и Скорость
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val h = run.durationSeconds / 3600
                    val m = (run.durationSeconds % 3600) / 60
                    val s = run.durationSeconds % 60
                    val timeStr = if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%02d:%02d", m, s)

                    DetailCard(
                        modifier = Modifier.weight(1f),
                        value = timeStr,
                        unit = "time",
                        label = "Duration"
                    )
                    DetailCard(
                        modifier = Modifier.weight(1f),
                        value = String.format(Locale.US, "%.1f", run.avgSpeedKmh),
                        unit = "km/h",
                        label = "Avg Speed"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ряд 3: Шаги и ТЕМП (Добавлено)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailCard(
                        modifier = Modifier.weight(1f),
                        value = "${run.steps}",
                        unit = "steps",
                        label = "Total Steps"
                    )
                    // ✅ Добавлена карточка Темпа
                    DetailCard(
                        modifier = Modifier.weight(1f),
                        value = paceText,
                        unit = "min/km",
                        label = "Avg Pace"
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DetailCard(modifier: Modifier = Modifier, value: String, unit: String, label: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
    }
}