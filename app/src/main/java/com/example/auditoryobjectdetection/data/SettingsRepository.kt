package com.example.auditoryobjectdetection.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val speechRate = prefs.getFloat("speech_rate", 1.0f)
        val speechPitch = prefs.getFloat("speech_pitch", 1.0f)
        val hapticsEnabled = prefs.getBoolean("haptics_enabled", true)
        return AppSettings(speechRate, speechPitch, hapticsEnabled)
    }

    fun saveSettings(appSettings: AppSettings) {
        prefs.edit()
            .putFloat("speech_rate", appSettings.speechRate)
            .putFloat("speech_pitch", appSettings.speechPitch)
            .putBoolean("haptics_enabled", appSettings.hapticsEnabled)
            .apply()
        _settings.value = appSettings
    }
}