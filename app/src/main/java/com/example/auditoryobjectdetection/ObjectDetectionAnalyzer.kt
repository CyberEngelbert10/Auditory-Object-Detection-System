package com.example.auditoryobjectdetection

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

/**
 * Analyzer that converts ImageProxy -> Bitmap -> TensorImage and runs TFLite ObjectDetector.
 *
 * Notes:
 * - The ImageProxy -> Bitmap conversion is implemented in the extension `toBitmapFromImageProxy()`.
 * - This declaration is annotated with CameraX's ExperimentalGetImage because it reads ImageProxy.image.
 */
@ExperimentalGetImage
class ObjectDetectionAnalyzer(
    private val objectDetector: ObjectDetector,
    private val onResults: (List<Detection>) -> Unit
) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        try {
            // 1) Convert ImageProxy -> Bitmap using your helper extension
            val bitmap = try {
                image.toBitmapFromImageProxy()
            } catch (e: Exception) {
                Log.e("ObjectDetectionAnalyzer", "Failed to convert ImageProxy to Bitmap: ${e.message}")
                null
            }

            if (bitmap == null) {
                // Ensure image is closed so camera continues streaming frames
                return
            }

            // 2) Convert Bitmap -> TensorImage (required by ObjectDetector API)
            val tensorImage = TensorImage.fromBitmap(bitmap)

            // 3) Run detection
            val rawResults = try {
                objectDetector.detect(tensorImage)
            } catch (e: Exception) {
                Log.e("ObjectDetectionAnalyzer", "Object detector failed: ${e.message}")
                emptyList<Detection>()
            }

            val results = rawResults.toList() // convert MutableList to List for the callback signature

            // 4) Logging for debugging
            if (results.isNotEmpty()) {
                val labels = results.map { det ->
                    det.categories.firstOrNull()?.label ?: "unknown"
                }
                Log.d("ObjectDetection", "Detected: ${labels.joinToString(", ")}")
            }

            // 5) Deliver results
            onResults(results)
        } catch (e: Exception) {
            Log.e("ObjectDetectionAnalyzer", "Unexpected error during analyze(): ${e.message}")
        } finally {
            // VERY IMPORTANT: close the image to receive the next frame
            try {
                image.close()
            } catch (ex: Exception) {
                Log.w("ObjectDetectionAnalyzer", "Failed to close imageProxy: ${ex.message}")
            }
        }
    }
}
