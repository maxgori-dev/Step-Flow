package com.example.step_flow

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private object SettingsKeys {
    val LANGUAGE = intPreferencesKey("language")
    val UNITS = intPreferencesKey("units")
    val THEME = intPreferencesKey("theme")
    val FONT_SCALE = floatPreferencesKey("font_scale")
    val NOTIFICATIONS = booleanPreferencesKey("notifications")
}

class SettingsRepository(private val context: Context) {

    val flow: Flow<SettingsState> = context.settingsDataStore.data.map { prefs ->
        val langOrdinal = prefs[SettingsKeys.LANGUAGE] ?: AppLanguage.System.ordinal
        val unitsOrdinal = prefs[SettingsKeys.UNITS] ?: Units.Metric.ordinal
        val themeOrdinal = prefs[SettingsKeys.THEME] ?: AppTheme.System.ordinal
        val font = prefs[SettingsKeys.FONT_SCALE] ?: 1.0f
        val notif = prefs[SettingsKeys.NOTIFICATIONS] ?: true

        SettingsState(
            language = AppLanguage.entries.getOrElse(langOrdinal) { AppLanguage.System },
            units = Units.entries.getOrElse(unitsOrdinal) { Units.Metric },
            theme = AppTheme.entries.getOrElse(themeOrdinal) { AppTheme.System },
            fontScale = font.coerceIn(0.85f, 1.25f),
            notificationsEnabled = notif
        )
    }

    suspend fun setLanguage(v: AppLanguage) {
        context.settingsDataStore.edit { it[SettingsKeys.LANGUAGE] = v.ordinal }
    }

    suspend fun setUnits(v: Units) {
        context.settingsDataStore.edit { it[SettingsKeys.UNITS] = v.ordinal }
    }

    suspend fun setTheme(v: AppTheme) {
        context.settingsDataStore.edit { it[SettingsKeys.THEME] = v.ordinal }
    }

    suspend fun setFontScale(v: Float) {
        context.settingsDataStore.edit { it[SettingsKeys.FONT_SCALE] = v.coerceIn(0.85f, 1.25f) }
    }

    suspend fun setNotifications(v: Boolean) {
        context.settingsDataStore.edit { it[SettingsKeys.NOTIFICATIONS] = v }
    }
}
