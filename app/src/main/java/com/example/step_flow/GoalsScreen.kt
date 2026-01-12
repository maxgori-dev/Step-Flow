package com.example.step_flow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    currentSteps: Int,
    currentMinutes: Int,
    currentKcal: Int,
    onBack: () -> Unit,
    onSave: (Int, Int, Int) -> Unit
) {
    var steps by remember { mutableFloatStateOf(currentSteps.toFloat()) }
    var minutes by remember { mutableFloatStateOf(currentMinutes.toFloat()) }
    var kcal by remember { mutableFloatStateOf(currentKcal.toFloat()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Goals", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F6F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Карточка настроек
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(20.dp)) {

                    // 1. Steps
                    GoalSlider(
                        title = "Steps Goal",
                        value = steps,
                        range = 1000f..20000f,
                        step = 100f,
                        unit = "steps",
                        onValueChange = { steps = it }
                    )

                    Divider(Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                    // 2. Minutes
                    GoalSlider(
                        title = "Active Minutes",
                        value = minutes,
                        range = 10f..180f,
                        step = 5f,
                        unit = "min",
                        onValueChange = { minutes = it }
                    )

                    Divider(Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                    // 3. Calories
                    GoalSlider(
                        title = "Calories (Active)",
                        value = kcal,
                        range = 100f..2000f,
                        step = 50f,
                        unit = "kcal",
                        onValueChange = { kcal = it }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    onSave(steps.roundToInt(), minutes.roundToInt(), kcal.roundToInt())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111))
            ) {
                Text("Save Goals", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun GoalSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    unit: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(title, fontWeight = FontWeight.Medium, color = Color.Gray, fontSize = 14.sp)
            Text(
                "${value.toInt()} $unit",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF111111)
            )
        }
        Slider(
            value = value,
            onValueChange = {
                // Привязка к шагу (например, по 100 шагов)
                val rounded = (it / step).roundToInt() * step
                onValueChange(rounded)
            },
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF111111),
                activeTrackColor = Color(0xFF111111),
                inactiveTrackColor = Color(0xFFE0E0E0)
            )
        )
    }
}