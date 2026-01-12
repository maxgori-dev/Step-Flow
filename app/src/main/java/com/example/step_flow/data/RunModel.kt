package com.example.step_flow.data

data class RunModel(
    val id: String = "",
    val timestamp: Long = 0,
    val distanceMeters: Float = 0f,
    val durationSeconds: Long = 0,
    val calories: Int = 0,
    val avgSpeedKmh: Float = 0f,
    val steps: Int = 0,
    val mapImageUrl: String = "" // Ссылка на картинку
)
