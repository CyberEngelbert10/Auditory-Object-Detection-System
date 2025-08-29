package com.example.auditoryobjectdetection

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ObjectDetectorHelper(
    var threshold: Float = 0.5f,
    var numThreads: Int = 2,
    var maxResults: Int = 3,
    val context: Context,
    val objectDetectorListener: DetectorListener?
) {
    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    private fun setupObjectDetector() {
        val optionsBuilder =
            ObjectDetector.ObjectDetectorOptions.builder()
                .setScoreThreshold(threshold)
                .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        val modelName = "model.tflite"

        try {
            objectDetector =
                ObjectDetector.createFromFileAndOptions(context, modelName, optionsBuilder.build())
        } catch (e: Exception) {
            objectDetectorListener?.onError("Object detector failed to initialize. See error logs.")
            Log.e("ObjectDetectorHelper", "TFLite failed to load model: ${e.message}")
            objectDetector = null
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (objectDetector == null) {
            // If the detector is still null, don't proceed.
            return
        }

        var inferenceTime = SystemClock.uptimeMillis()

        val imageProcessor = ImageProcessor.Builder()
            // Coerce the value to prevent crashes from unexpected rotation values
            .add(Rot90Op((-imageRotation / 90).coerceIn(-3, 3)))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val results = objectDetector?.detect(tensorImage)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        objectDetectorListener?.onResults(
            results?.toMutableList(),
            inferenceTime,
            tensorImage.height,
            tensorImage.width
        )
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: MutableList<Detection>?,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int
        )
    }
}