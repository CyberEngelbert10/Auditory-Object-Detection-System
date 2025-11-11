package com.example.auditoryobjectdetection.data

data class AppSettings(
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val hapticsEnabled: Boolean = true,
    val language: String = "english" // "english" or "nyanja"
)