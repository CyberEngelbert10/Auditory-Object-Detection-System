package com.example.auditoryobjectdetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/**
 * Converts an [ImageProxy] in YUV_420_888 format to a [Bitmap].
 *
 * This utility function is necessary for converting the camera's output, which is often
 * in a YUV format, into a Bitmap that can be used for tasks like object detection or
 * displaying in the UI.
 *
 * The conversion process involves:
 * 1. Extracting the Y, U, and V planes from the [ImageProxy].
 * 2. Combining the planes into a single byte array in NV21 format.
 * 3. Creating a [YuvImage] from the NV21 data.
 * 4. Compressing the [YuvImage] into a JPEG.
 * 5. Decoding the JPEG byte array into a [Bitmap].
 *
 * Note: This function requires the `@ExperimentalGetImage` annotation because it uses
 * the `image` property of the [ImageProxy], which is an experimental API in CameraX.
 *
 * @return A [Bitmap] representation of the image.
 * @throws IllegalStateException if the underlying image is null.
 */
// Annotate directly with CameraX's ExperimentalGetImage annotation
@androidx.camera.core.ExperimentalGetImage
fun ImageProxy.toBitmapFromImageProxy(): Bitmap {
    val image = this.image ?: throw IllegalStateException("Image is null")

    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)           // V then U for NV21
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 90, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
