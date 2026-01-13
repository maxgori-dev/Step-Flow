package com.example.step_flow

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val LightColors = lightColorScheme(
    primary = Color(0xFF111111),       
    onPrimary = Color.White,           
    background = Color(0xFFF5F6F8),    
    onBackground = Color(0xFF111111),  
    surface = Color.White,             
    onSurface = Color(0xFF111111),     
    onSurfaceVariant = Color(0xFF6F747C), 
    secondaryContainer = Color(0xFFE9EAEE) 
)


private val DarkColors = darkColorScheme(
    primary = Color.White,             
    onPrimary = Color.Black,           
    background = Color(0xFF000000),    
    onBackground = Color.White,        
    surface = Color(0xFF1C1C1E),       
    onSurface = Color.White,           
    onSurfaceVariant = Color(0xFF8E8E93), 
    secondaryContainer = Color(0xFF2C2C2E) 
)


@Composable
fun StepFlowTheme(
    
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}