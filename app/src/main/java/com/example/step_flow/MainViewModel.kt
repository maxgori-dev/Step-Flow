package com.example.step_flow

import androidx.lifecycle.ViewModel
import android.content.Context
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
import java.util.UUID
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.asStateFlow
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
    private val prefs: UserPrefsRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) : ViewModel() {

    private val currentUserId: String by lazy {
        val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        var id = sharedPrefs.getString("user_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString() 
            sharedPrefs.edit().putString("user_id", id).apply()
        }
        id!!
    }

    private val loadedFlag = MutableStateFlow(false)

    
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
                avatarUri = "" 
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

    
    val runsFlow: StateFlow<List<RunModel>> = callbackFlow {
        
        val listener = FirebaseFirestore.getInstance()
            .collection("runs")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) 
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    
                    val runs = snapshot.toObjects(RunModel::class.java)
                    trySend(runs) 
                }
            }
        
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

    
    private val _goals = MutableStateFlow(Triple(6000, 45, 500))
    val goals = _goals.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.awaitFirstLoad()
            loadedFlag.value = true
            loadGoals() 
        }
    }

    private fun loadGoals() {
        val sp = context.getSharedPreferences("user_goals", Context.MODE_PRIVATE)
        val s = sp.getInt("goal_steps", 6000)
        val m = sp.getInt("goal_minutes", 45)
        val k = sp.getInt("goal_kcal", 500)
        _goals.value = Triple(s, m, k)
    }

    fun saveGoals(steps: Int, minutes: Int, kcal: Int) {
        val sp = context.getSharedPreferences("user_goals", Context.MODE_PRIVATE)
        sp.edit()
            .putInt("goal_steps", steps)
            .putInt("goal_minutes", minutes)
            .putInt("goal_kcal", kcal)
            .apply()
        _goals.value = Triple(steps, minutes, kcal)
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

    
    fun saveRunToFirebase(
        distanceMeters: Float,
        durationSeconds: Long,
        calories: Float,
        avgSpeedKmh: Float,
        steps: Int,
        localScreenshotPath: String?
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                var imageUrl = ""

                
                if (localScreenshotPath != null) {
                    val file = Uri.fromFile(File(localScreenshotPath))
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("runs_maps/${System.currentTimeMillis()}_${file.lastPathSegment}")

                    
                    storageRef.putFile(file).await()
                    
                    imageUrl = storageRef.downloadUrl.await().toString()
                }

                
                val runData = RunModel(
                    userId = currentUserId,
                    timestamp = System.currentTimeMillis(),
                    distanceMeters = distanceMeters,
                    durationSeconds = durationSeconds,
                    calories = calories,
                    avgSpeedKmh = avgSpeedKmh,
                    steps = steps,
                    mapImageUrl = imageUrl
                )

                
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
