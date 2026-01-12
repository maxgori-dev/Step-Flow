package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.step_flow.data.RunModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    runs: List<RunModel>,
    onRunClick: (RunModel) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF4F6F9)
    ) { padding ->
        if (runs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No runs yet. Go for a run!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(runs) { run ->
                    RunHistoryItem(run = run, onClick = { onRunClick(run) })
                }
            }
        }
    }
}

@Composable
fun RunHistoryItem(run: RunModel, onClick: () -> Unit) {
    // 1. Форматирование даты
    val dateStr = remember(run.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(run.timestamp))
    }

    // 2. Дистанция
    val distKm = String.format(Locale.US, "%.2f km", run.distanceMeters / 1000f)

    // ✅ 3. Расчет ТЕМПА (Средняя скорость на 1 км)
    // Формула: 60 / км_ч = мин_на_км
    val paceText = remember(run.avgSpeedKmh) {
        if (run.avgSpeedKmh > 0.1) {
            val paceMinTotal = 60.0 / run.avgSpeedKmh
            val pMin = paceMinTotal.toInt()
            val pSec = ((paceMinTotal - pMin) * 60).toInt()
            String.format(Locale.US, "%d:%02d /km", pMin, pSec)
        } else {
            "-:-- /km"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка слева
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = Color(0xFF0066FF)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Текст по центру
            Column(modifier = Modifier.weight(1f)) {
                // Дата
                Text(text = dateStr, fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(4.dp))

                // Дистанция (Жирным)
                Text(text = distKm, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)

                // ✅ БЛОК С ТЕМПОМ (НОВОЕ)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Pace",
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp) // Размер иконки
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = paceText,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Калории справа
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${run.calories}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF9800)
                )
                Text(text = "kcal", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}