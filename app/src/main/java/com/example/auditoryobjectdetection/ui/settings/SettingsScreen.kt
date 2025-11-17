package com.example.auditoryobjectdetection.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.example.auditoryobjectdetection.data.Language
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    // In a real app, you'd use an IconButton with an arrow icon
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Speech Rate Slider
            Text("Speech Rate", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.speechRate,
                onValueChange = { viewModel.onSpeechRateChange(it) },
                valueRange = 0.5f..2.0f,
                steps = 5
            )

            // Speech Pitch Slider
            Text("Speech Pitch", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = settings.speechPitch,
                onValueChange = { viewModel.onSpeechPitchChange(it) },
                valueRange = 0.5f..2.0f,
                steps = 5
            )

            // Haptic Feedback Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Haptic Feedback", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = settings.hapticsEnabled,
                    onCheckedChange = { viewModel.onHapticsToggle(it) }
                )
            }

            // Language Selection
            Text("Language", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("English")
                RadioButton(
                    selected = settings.language == Language.ENGLISH,
                    onClick = { viewModel.onLanguageChange(Language.ENGLISH) }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nyanja")
                RadioButton(
                    selected = settings.language == Language.NYANJA,
                    onClick = { viewModel.onLanguageChange(Language.NYANJA) }
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Bemba")
                RadioButton(
                    selected = settings.language == Language.BEMBA,
                    onClick = { viewModel.onLanguageChange(Language.BEMBA) }
                )
            }
        }
    }
}