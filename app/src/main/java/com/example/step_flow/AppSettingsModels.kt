package com.example.step_flow

enum class AppLanguage(val label: String) {
    System("System"),
    English("English"),
    Polish("Polski")
}

enum class Units(val label: String) {
    Metric("Metric (cm/kg)"),
    Imperial("Imperial (ft/lb)")
}

enum class AppTheme(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

data class SettingsState(
    val language: AppLanguage = AppLanguage.System,
    val units: Units = Units.Metric,
    val theme: AppTheme = AppTheme.System,
    val fontScale: Float = 1.0f,
    val notificationsEnabled: Boolean = true
)
