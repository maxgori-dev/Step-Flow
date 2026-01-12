package com.example.step_flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val loaded: Boolean,
    val name: String,
    val heightCm: String,
    val weightKg: String,
    val ageYears: String,
    val setupDone: Boolean,
    val avatarUri: String
) {
    val hasName: Boolean get() = name.isNotBlank()
    val hasProfile: Boolean get() =
        heightCm.isNotBlank() && weightKg.isNotBlank() && ageYears.isNotBlank()

    // onboarding rules
    val shouldShowWelcome: Boolean get() = !hasName
    val shouldShowProfileSetup: Boolean get() = hasName && !setupDone
}

private data class PrefsSnapshot(
    val name: String,
    val heightCm: String,
    val weightKg: String,
    val ageYears: String,
    val setupDone: Boolean,
    val avatarUri: String
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prefs: UserPrefsRepository
) : ViewModel() {

    private val loadedFlag = MutableStateFlow(false)

    // ✅ собираем prefs в 2 шага, чтобы НЕ использовать combine на 6+ flows
    private val prefsFlow: StateFlow<PrefsSnapshot> =
        combine(
            prefs.nameFlow,
            prefs.heightFlow,
            prefs.weightFlow,
            prefs.ageFlow,
            prefs.setupDoneFlow
        ) { name, h, w, a, done ->
            PrefsSnapshot(
                name = name,
                heightCm = h,
                weightKg = w,
                ageYears = a,
                setupDone = done,
                avatarUri = "" // set on next combine
            )
        }.combine(prefs.avatarUriFlow) { snap, avatar ->
            snap.copy(avatarUri = avatar)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PrefsSnapshot(
                name = "",
                heightCm = "",
                weightKg = "",
                ageYears = "",
                setupDone = false,
                avatarUri = ""
            )
        )

    val uiState: StateFlow<MainUiState> =
        combine(loadedFlag, prefsFlow) { loaded, snap ->
            MainUiState(
                loaded = loaded,
                name = snap.name,
                heightCm = snap.heightCm,
                weightKg = snap.weightKg,
                ageYears = snap.ageYears,
                setupDone = snap.setupDone,
                avatarUri = snap.avatarUri
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MainUiState(
                loaded = false,
                name = "",
                heightCm = "",
                weightKg = "",
                ageYears = "",
                setupDone = false,
                avatarUri = ""
            )
        )

    init {
        viewModelScope.launch {
            prefs.awaitFirstLoad()
            loadedFlag.value = true
        }
    }

    fun saveName(value: String) {
        viewModelScope.launch { prefs.setName(value) }
    }

    fun saveProfile(heightCm: String, weightKg: String, ageYears: String) {
        viewModelScope.launch { prefs.setProfile(heightCm, weightKg, ageYears) }
    }

    fun saveAvatarUri(uri: String) {
        viewModelScope.launch { prefs.setAvatarUri(uri) }
    }

    fun resetOnboarding() {
        viewModelScope.launch { prefs.resetOnboarding() }
    }
}
