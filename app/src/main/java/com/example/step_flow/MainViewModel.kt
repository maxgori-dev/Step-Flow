package com.example.step_flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.step_flow.data.RunModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import android.net.Uri
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

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

    // ✅ НОВОЕ: Поток списка забегов из Firebase
    val runsFlow: StateFlow<List<RunModel>> = callbackFlow {
        // Подписка на коллекцию "runs", сортировка по дате (новые сверху)
        val listener = FirebaseFirestore.getInstance()
            .collection("runs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Закрываем поток при ошибке
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Преобразуем документы в объекты RunModel
                    val runs = snapshot.toObjects(RunModel::class.java)
                    trySend(runs) // Отправляем список в поток
                }
            }
        // Отписываемся, когда ViewModel очищается
        awaitClose { listener.remove() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
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
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun resetUploadState() {
        _uploadState.value = UploadState.Idle
    }

    // ✅ ГЛАВНАЯ ФУНКЦИЯ СОХРАНЕНИЯ
    fun saveRunToFirebase(
        distanceMeters: Float,
        durationSeconds: Long,
        calories: Int,
        avgSpeedKmh: Float,
        steps: Int,
        localScreenshotPath: String?
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                var imageUrl = ""

                // 1. Если есть скриншот, грузим его в Firebase Storage
                if (localScreenshotPath != null) {
                    val file = Uri.fromFile(File(localScreenshotPath))
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("runs_maps/${System.currentTimeMillis()}_${file.lastPathSegment}")

                    // Загрузка
                    storageRef.putFile(file).await()
                    // Получение ссылки
                    imageUrl = storageRef.downloadUrl.await().toString()
                }

                // 2. Создаем модель забега
                val runData = RunModel(
                    timestamp = System.currentTimeMillis(),
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    calories = calories,
                    avgSpeedKmh = avgSpeedKmh,
                    steps = steps,
                    mapImageUrl = imageUrl
                )

                // 3. Сохраняем в Firestore
                FirebaseFirestore.getInstance()
                    .collection("runs")
                    .add(runData)
                    .await()

                _uploadState.value = UploadState.Success

            } catch (e: Exception) {
                e.printStackTrace()
                _uploadState.value = UploadState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
