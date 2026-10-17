package com.example.androidapp.cv

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import java.nio.ByteBuffer

/**
 * Converts an ARCore CPU image (YUV_420_888) straight to an upright, downscaled
 * ARGB bitmap in ONE pass: colour conversion + clockwise rotation + resize.
 * This replaces the old convert -> createBitmap(rotate) -> createScaledBitmap chain.
 *
 * Call from a single background thread.
 */
object CameraFrameConverter {

    private var yBytes = ByteArray(0)
    private var uBytes = ByteArray(0)
    private var vBytes = ByteArray(0)
    private var pixels = IntArray(0)

    /**
     * @param rotationDegrees clockwise rotation to apply (0, 90, 180, 270)
     * @param maxDimension    longest side of the returned bitmap (never upscales)
     */
    @Synchronized
    fun toBitmap(
        image: Image,
        rotationDegrees: Int = 0,
        maxDimension: Int = Int.MAX_VALUE
    ): Bitmap? {
        if (image.format != ImageFormat.YUV_420_888) return null

        val srcW = image.width
        val srcH = image.height
        val rot = ((rotationDegrees % 360) + 360) % 360

        val swap = rot == 90 || rot == 270
        val rotW = if (swap) srcH else srcW
        val rotH = if (swap) srcW else srcH

        val scale = minOf(1f, maxDimension.toFloat() / maxOf(rotW, rotH))
        val outW = maxOf(1, (rotW * scale).toInt())
        val outH = maxOf(1, (rotH * scale).toInt())
        val stepX = rotW.toFloat() / outW
        val stepY = rotH.toFloat() / outH

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        yBytes = copyPlane(yPlane.buffer, yBytes)
        uBytes = copyPlane(uPlane.buffer, uBytes)
        vBytes = copyPlane(vPlane.buffer, vBytes)

        if (pixels.size != outW * outH) {
            pixels = IntArray(outW * outH)
        }

        val yRowStride = yPlane.rowStride
        val yPixelStride = yPlane.pixelStride
        val uRowStride = uPlane.rowStride
        val uPixelStride = uPlane.pixelStride
        val vRowStride = vPlane.rowStride
        val vPixelStride = vPlane.pixelStride

        var out = 0
        for (oy in 0 until outH) {
            val ry = ((oy + 0.5f) * stepY).toInt().coerceIn(0, rotH - 1)

            for (ox in 0 until outW) {
                val rx = ((ox + 0.5f) * stepX).toInt().coerceIn(0, rotW - 1)

                // Position in the original (sensor) image for this rotated pixel.
                val sx: Int
                val sy: Int
                when (rot) {
                    90 -> {
                        sx = ry
                        sy = srcH - 1 - rx
                    }
                    180 -> {
                        sx = srcW - 1 - rx
                        sy = srcH - 1 - ry
                    }
                    270 -> {
                        sx = srcW - 1 - ry
                        sy = rx
                    }
                    else -> {
                        sx = rx
                        sy = ry
                    }
                }

                val y = yBytes[sy * yRowStride + sx * yPixelStride].toInt() and 0xFF
                val u = (uBytes[(sy shr 1) * uRowStride + (sx shr 1) * uPixelStride]
                    .toInt() and 0xFF) - 128
                val v = (vBytes[(sy shr 1) * vRowStride + (sx shr 1) * vPixelStride]
                    .toInt() and 0xFF) - 128

                var r = y + ((1436 * v) shr 10)
                var g = y - ((352 * u + 731 * v) shr 10)
                var b = y + ((1814 * u) shr 10)

                if (r < 0) r = 0 else if (r > 255) r = 255
                if (g < 0) g = 0 else if (g > 255) g = 255
                if (b < 0) b = 0 else if (b > 255) b = 255

                pixels[out++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        val bitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, outW, 0, 0, outW, outH)
        return bitmap
    }

    private fun copyPlane(buffer: ByteBuffer, reuse: ByteArray): ByteArray {
        val source = buffer.duplicate()
        source.rewind()
        val size = source.remaining()
        val target = if (reuse.size == size) reuse else ByteArray(size)
        source.get(target)
        return target
    }
}