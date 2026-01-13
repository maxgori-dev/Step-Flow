package com.example.step_flow

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val KEY_NAME = stringPreferencesKey("user_name")
        val KEY_HEIGHT = stringPreferencesKey("height_cm")
        val KEY_WEIGHT = stringPreferencesKey("weight_kg")
        val KEY_AGE = stringPreferencesKey("age_years")

        
        val KEY_SETUP_DONE = booleanPreferencesKey("setup_done")

        
        val KEY_AVATAR_URI = stringPreferencesKey("avatar_uri")
    }

    
    val nameFlow: Flow<String> = dataStore.data.map { it[KEY_NAME] ?: "" }
    val heightFlow: Flow<String> = dataStore.data.map { it[KEY_HEIGHT] ?: "" }
    val weightFlow: Flow<String> = dataStore.data.map { it[KEY_WEIGHT] ?: "" }
    val ageFlow: Flow<String> = dataStore.data.map { it[KEY_AGE] ?: "" }

    val setupDoneFlow: Flow<Boolean> = dataStore.data.map { it[KEY_SETUP_DONE] ?: false }

    
    val avatarUriFlow: Flow<String> = dataStore.data.map { it[KEY_AVATAR_URI] ?: "" }

    
    suspend fun awaitFirstLoad() {
        dataStore.data.first() 
    }

    
    suspend fun setName(value: String) {
        dataStore.edit { prefs ->
            prefs[KEY_NAME] = value.trim()
        }
    }

    
    suspend fun setProfile(heightCm: String, weightKg: String, ageYears: String) {
        dataStore.edit { prefs ->
            prefs[KEY_HEIGHT] = heightCm.trim()
            prefs[KEY_WEIGHT] = weightKg.trim()
            prefs[KEY_AGE] = ageYears.trim()
            prefs[KEY_SETUP_DONE] = true
        }
    }

    suspend fun setAvatarUri(uri: String) {
        dataStore.edit { prefs ->
            val clean = uri.trim()
            if (clean.isBlank()) {
                prefs.remove(KEY_AVATAR_URI)
            } else {
                prefs[KEY_AVATAR_URI] = clean
            }
        }
    }

    suspend fun resetOnboarding() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_NAME)
            prefs.remove(KEY_HEIGHT)
            prefs.remove(KEY_WEIGHT)
            prefs.remove(KEY_AGE)
            prefs.remove(KEY_AVATAR_URI)
            prefs[KEY_SETUP_DONE] = false
        }
    }
}
