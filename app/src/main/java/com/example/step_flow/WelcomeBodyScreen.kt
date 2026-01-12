package com.example.step_flow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeBodyScreen(
    weightKg: Int,
    heightCm: Int,
    age: Int,
    onWeightChange: (Int) -> Unit,
    onHeightChange: (Int) -> Unit,
    onAgeChange: (Int) -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Profile setup", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            TextButton(onClick = onFinish) { Text("Skip") }
        }

        // FIX: в Text нет параметра alpha -> делаем через цвет
        Text(
            "2/3",
            modifier = Modifier.padding(top = 6.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(28.dp))

        MetricSliderRow(
            title = "Weight",
            valueText = "$weightKg kg",
            value = weightKg,
            range = 30..160,
            onValueChange = onWeightChange
        )

        Spacer(Modifier.height(18.dp))

        MetricSliderRow(
            title = "Height (cm)",
            valueText = "$heightCm cm",
            value = heightCm,
            range = 120..220,
            onValueChange = onHeightChange
        )

        Spacer(Modifier.height(18.dp))

        MetricSliderRow(
            title = "Age",
            valueText = "$age",
            value = age,
            range = 10..90,
            onValueChange = onAgeChange
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 24.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun MetricSliderRow(
    title: String,
    valueText: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 18.sp)
            Text(valueText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat()
        )

        Divider()
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=440")
@Composable
private fun WelcomeBodyScreenPreview() {
    WelcomeBodyScreen(
        weightKg = 68,
        heightCm = 172,
        age = 34,
        onWeightChange = {},
        onHeightChange = {},
        onAgeChange = {},
        onBack = {},
        onFinish = {}
    )
}
