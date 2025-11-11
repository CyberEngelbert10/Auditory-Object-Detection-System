package com.example.auditoryobjectdetection

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.VibratorManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auditoryobjectdetection.data.SettingsRepository
import com.example.auditoryobjectdetection.ui.settings.SettingsScreen
import com.example.auditoryobjectdetection.ui.settings.SettingsViewModel
import com.example.auditoryobjectdetection.ui.settings.SettingsViewModelFactory
import com.example.auditoryobjectdetection.ui.theme.AuditoryObjectDetectionTheme
import org.tensorflow.lite.task.vision.detector.Detection
import java.util.Locale
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuditoryObjectDetectionTheme {
                AppNavigation()
            }
        }
    }
}

/**
 * Annotated directly because some CameraX versions expect the declaration itself to be marked
 * (instead of using @OptIn). This function reads ImageProxy.image internally (via extension),
 * so it must be annotated.
 */
@ExperimentalGetImage
private fun analyzeImageProxy(
    imageProxy: androidx.camera.core.ImageProxy,
    objectDetectorHelper: ObjectDetectorHelper
) {
    try {
        val bitmap = imageProxy.toBitmapFromImageProxy() // your extension (see note below)
        objectDetectorHelper.detect(bitmap, imageProxy.imageInfo.rotationDegrees)
    } catch (e: Exception) {
        Log.e("CameraView", "Failed to convert/analyze image: ${e.message}")
    } finally {
        imageProxy.close()
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(settingsRepository)
    )

    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") {
            CameraPermissionScreen(
                settingsViewModel = settingsViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Direct annotation again so the compiler knows this composable uses the experimental API
 * indirectly (via analyzeImageProxy).
 */
@ExperimentalGetImage
@Composable
fun CameraPermissionScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasPermission = isGranted }
    )

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        CameraView(
            settingsViewModel = settingsViewModel,
            onNavigateToSettings = onNavigateToSettings
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Camera permission is required to use this app.")
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Request Permission")
                }
            }
        }
    }
}

@ExperimentalGetImage
@Composable
fun CameraView(
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    val settings by settingsViewModel.settings.collectAsState()
    var lastSpokenTime by remember { mutableLongStateOf(0L) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator

    // Map for Nyanja audio resources (add actual files to res/raw/nyanja/)
    val nyanjaAudioMap = remember {
        mapOf(
            "person" to R.raw.nyanja_person,
            "car" to R.raw.nyanja_car,
            "bicycle" to R.raw.nyanja_bicycle,
            "motorcycle" to R.raw.nyanja_motorcycle,
            "bus" to R.raw.nyanja_bus,
            "truck" to R.raw.nyanja_truck,
            "chair" to R.raw.nyanja_chair,
            "table" to R.raw.nyanja_table,
            "obstacle" to R.raw.nyanja_obstacle
        )
    }

    // This is the corrected DisposableEffect block to manage the TTS engine lifecycle
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        val listener = TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                // When initialization succeeds, update the composable's state
                tts = ttsInstance
            }
        }
        // Create the instance and assign it to a local variable
        ttsInstance = TextToSpeech(context, listener)

        onDispose {
            // Use the local variable for cleanup
            ttsInstance.stop()
            ttsInstance.shutdown()
            analysisExecutor.shutdown()
        }
    }

    // This effect correctly applies settings when tts is ready or settings change
    LaunchedEffect(tts, settings) {
        tts?.let {
            it.language = Locale.US
            it.setSpeechRate(settings.speechRate)
            it.setPitch(settings.speechPitch)
        }
    }

    val objectDetectorHelper = remember {
        ObjectDetectorHelper(
            context = context,
            objectDetectorListener = object : ObjectDetectorHelper.DetectorListener {
                override fun onError(error: String) {
                    Log.e("CameraScreen", "Detection Error: $error")
                }

                override fun onResults(results: MutableList<Detection>?, inferenceTime: Long, imageHeight: Int, imageWidth: Int) {
                    if (!results.isNullOrEmpty()) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastSpokenTime > 3000) {
                            val detectedLabels = results.map { it.categories.first().label }.distinct()
                            if (settings.language == "nyanja") {
                                // Play audio for the first detected object
                                val firstLabel = detectedLabels.firstOrNull()?.lowercase()
                                val audioResId = nyanjaAudioMap[firstLabel]
                                if (audioResId != null) {
                                    val mediaPlayer = MediaPlayer.create(context, audioResId)
                                    mediaPlayer?.start()
                                    mediaPlayer?.setOnCompletionListener { it.release() }
                                } else {
                                    // Fallback to TTS if no audio
                                    val speechText = "Object detected: " + detectedLabels.joinToString()
                                    tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            } else {
                                // English TTS
                                val speechText = "Object detected: " + detectedLabels.joinToString()
                                tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
                            }

                            if (settings.hapticsEnabled) {
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            lastSpokenTime = currentTime
                        }
                    }
                }
            }
        )
    }

    cameraController.setImageAnalysisAnalyzer(
        analysisExecutor
    ) { imageProxy ->
        analyzeImageProxy(imageProxy, objectDetectorHelper)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { PreviewView(it).apply { controller = cameraController } },
            modifier = Modifier.fillMaxSize()
        ) {
            cameraController.bindToLifecycle(lifecycleOwner)
        }
        FloatingActionButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }
}