package com.example.auditoryobjectdetection.data

data class AppSettings(
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val hapticsEnabled: Boolean = true
    // We can add language selection here later
)