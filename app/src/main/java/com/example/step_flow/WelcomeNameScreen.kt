package com.example.step_flow

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val MAX_NAME_LEN = 18

// ✅ Разрешены ТОЛЬКО буквы (кириллица + латиница) и пробел
private val NotLetterOrSpace = Regex("[^\\p{L} ]")

@Composable
fun WelcomeNameScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .imePadding()
    ) {

        NameBadgeInput(
            name = name,
            onNameChange = onNameChange,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    scaleX = 1.25f
                    scaleY = 1.25f
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome!",
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Enter your name",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onContinue,
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun NameBadgeInput(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.aspectRatio(1.35f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.hello_my_name_is),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .height(80.dp)
                .offset(y = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = name,
                onValueChange = { raw ->
                    val filtered = raw
                        .replace(NotLetterOrSpace, "") // ❌ цифры и символы удаляются
                        .replace(Regex("\\s+"), " ")
                        .trimStart()
                        .take(MAX_NAME_LEN)

                    onNameChange(filtered)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,              // ⌨️ буквенная
                    capitalization = KeyboardCapitalization.Words,
                    autoCorrect = false
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (name.isBlank()) {
                        Text(
                            text = "Your name",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 26.sp,
                            color = Color.Black.copy(alpha = 0.25f)
                        )
                    }
                    inner()
                }
            )
        }
    }
}
