package com.example.auditoryobjectdetection.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.auditoryobjectdetection.data.AppSettings
import com.example.auditoryobjectdetection.data.Language
import com.example.auditoryobjectdetection.data.SettingsRepository

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings = repository.settings

    fun onSpeechRateChange(rate: Float) {
        val newSettings = settings.value.copy(speechRate = rate)
        repository.saveSettings(newSettings)
    }

    fun onSpeechPitchChange(pitch: Float) {
        val newSettings = settings.value.copy(speechPitch = pitch)
        repository.saveSettings(newSettings)
    }

    fun onHapticsToggle(enabled: Boolean) {
        val newSettings = settings.value.copy(hapticsEnabled = enabled)
        repository.saveSettings(newSettings)
    }

    fun onLanguageChange(language: Language) {
        val newSettings = settings.value.copy(language = language)
        repository.saveSettings(newSettings)
    }
}

// Factory to create the ViewModel with its dependency
class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}