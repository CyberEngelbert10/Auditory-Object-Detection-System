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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.auditoryobjectdetection.data.Language
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

    // State for detection results to draw bounding boxes
    var detectionResults by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var imageWidth by remember { mutableIntStateOf(1) }
    var imageHeight by remember { mutableIntStateOf(1) }

    // Track if Bemba audio is currently playing to prevent interruption
    var isAudioPlaying by remember { mutableStateOf(false) }
    var currentMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }



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
            // Clean up any playing MediaPlayer
            currentMediaPlayer?.release()
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

                override fun onResults(results: MutableList<Detection>?, inferenceTime: Long, imgHeight: Int, imgWidth: Int) {
                    // Update detection results for bounding box overlay
                    detectionResults = results?.toList() ?: emptyList()
                    imageWidth = imgWidth
                    imageHeight = imgHeight

                    if (!results.isNullOrEmpty()) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastSpokenTime > 3000 && !isAudioPlaying) {
                            val detectedLabels = results.map { it.categories.first().label }.distinct()
                            if (settings.language == Language.BEMBA) {
                                // Bemba support: play label audio then 'detected' audio
                                val firstLabel = detectedLabels.firstOrNull()?.lowercase(Locale.getDefault())
                                if (!firstLabel.isNullOrEmpty()) {
                                    val normalized = firstLabel.replace("-", "_").replace(" ", "_").replace(Regex("[^a-z0-9_]", RegexOption.IGNORE_CASE), "").lowercase(Locale.getDefault())
                                    val resName = "${normalized}_bemba"
                                    val audioResId = context.resources.getIdentifier(resName, "raw", context.packageName)
                                    if (audioResId != 0) {
                                        isAudioPlaying = true
                                        // Release any existing player
                                        currentMediaPlayer?.release()
                                        
                                        val mediaPlayer = MediaPlayer.create(context, audioResId)
                                        currentMediaPlayer = mediaPlayer
                                        mediaPlayer?.start()
                                        mediaPlayer?.setOnCompletionListener { mp ->
                                            mp.release()
                                            val detectedRes = context.resources.getIdentifier("detected_bemba", "raw", context.packageName)
                                            if (detectedRes != 0) {
                                                val mpDetected = MediaPlayer.create(context, detectedRes)
                                                currentMediaPlayer = mpDetected
                                                mpDetected?.start()
                                                mpDetected?.setOnCompletionListener { mp2 ->
                                                    mp2.release()
                                                    currentMediaPlayer = null
                                                    isAudioPlaying = false
                                                }
                                            } else {
                                                currentMediaPlayer = null
                                                isAudioPlaying = false
                                            }
                                        }
                                    } else {
                                        // If label audio missing, try to play the 'detected' audio in bemba only, otherwise fallback
                                        val detectedResOnly = context.resources.getIdentifier("detected_bemba", "raw", context.packageName)
                                        if (detectedResOnly != 0) {
                                            isAudioPlaying = true
                                            currentMediaPlayer?.release()
                                            
                                            val mpDetected = MediaPlayer.create(context, detectedResOnly)
                                            currentMediaPlayer = mpDetected
                                            mpDetected?.start()
                                            mpDetected?.setOnCompletionListener { mp ->
                                                mp.release()
                                                currentMediaPlayer = null
                                                isAudioPlaying = false
                                            }
                                        } else {
                                            val speechText = "Object detected: " + detectedLabels.joinToString()
                                            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
                                        }
                                    }
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

        // Detection overlay for bounding boxes, labels, and confidence scores
        DetectionOverlay(
            detections = detectionResults,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            modifier = Modifier.fillMaxSize()
        )

        FloatingActionButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }
}

/**
 * Composable that draws bounding boxes, labels, and confidence scores
 * over detected objects.
 */
@Composable
fun DetectionOverlay(
    detections: List<Detection>,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    val boxColor = Color.Green
    val textPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 48f
            isFakeBoldText = true
            setShadowLayer(4f, 2f, 2f, android.graphics.Color.BLACK)
        }
    }
    val backgroundPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(180, 0, 150, 0)
            style = Paint.Style.FILL
        }
    }

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calculate scale factors to map detection coordinates to canvas
        val scaleX = canvasWidth / imageWidth
        val scaleY = canvasHeight / imageHeight

        detections.forEach { detection ->
            val boundingBox = detection.boundingBox
            val category = detection.categories.firstOrNull()
            val label = category?.label ?: "Unknown"
            val confidence = category?.score ?: 0f
            val confidencePercent = (confidence * 100).toInt()

            // Scale bounding box coordinates
            val left = boundingBox.left * scaleX
            val top = boundingBox.top * scaleY
            val right = boundingBox.right * scaleX
            val bottom = boundingBox.bottom * scaleY

            // Draw bounding box
            drawRect(
                color = boxColor,
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 6f)
            )

            // Draw label with confidence score
            val labelText = "$label $confidencePercent%"
            val textWidth = textPaint.measureText(labelText)
            val textHeight = textPaint.textSize

            // Draw background rectangle for text
            drawContext.canvas.nativeCanvas.drawRect(
                left,
                top - textHeight - 8,
                left + textWidth + 16,
                top,
                backgroundPaint
            )

            // Draw text
            drawContext.canvas.nativeCanvas.drawText(
                labelText,
                left + 8,
                top - 10,
                textPaint
            )
        }
    }
}