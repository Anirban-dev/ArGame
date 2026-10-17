package com.example.androidapp.cv

import android.content.Context
import android.os.SystemClock
import com.example.gameapi.HandLandmark3D
import com.example.gameapi.HandPoseFrame
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * One Euro filter: heavy smoothing when the finger is slow (removes jitter),
 * light smoothing when it moves fast (keeps the dot responsive).
 * Coordinates are normalized 0..1, so beta is large.
 * The game also predicts motion, so this filter is kept fairly light.
 */
private class OneEuroFilter(
    private val minCutoff: Float = 2.5f,
    private val beta: Float = 10.0f,
    private val dCutoff: Float = 1.0f
) {
    private var hasPrevious = false
    private var xPrev = 0f
    private var dxPrev = 0f
    private var tPrevMs = 0L

    fun reset() {
        hasPrevious = false
    }

    private fun alpha(cutoff: Float, dt: Float): Float {
        val tau = 1f / (2f * PI.toFloat() * cutoff)
        return 1f / (1f + tau / dt)
    }

    fun filter(x: Float, tMs: Long): Float {
        if (!hasPrevious) {
            hasPrevious = true
            xPrev = x
            dxPrev = 0f
            tPrevMs = tMs
            return x
        }

        val dt = ((tMs - tPrevMs) / 1000f).coerceAtLeast(0.001f)
        tPrevMs = tMs

        val dx = (x - xPrev) / dt
        val dxHat = dxPrev + alpha(dCutoff, dt) * (dx - dxPrev)
        dxPrev = dxHat

        val cutoff = minCutoff + beta * abs(dxHat)
        val xHat = xPrev + alpha(cutoff, dt) * (x - xPrev)
        xPrev = xHat
        return xHat
    }
}

class MediaPipeHandTracker(private val context: Context) {

    private var handLandmarker: HandLandmarker? = null
    private var lastTimestampMs = 0L

    private val filterX = OneEuroFilter()
    private val filterY = OneEuroFilter()

    /** "GPU" or "CPU", shown in the game's debug box. */
    @Volatile
    var activeDelegate: String = "none"
        private set

    // Call and use from the same background thread (the GPU delegate needs this).
    fun startTracking() {
        if (handLandmarker != null) return

        handLandmarker = try {
            createLandmarker(Delegate.GPU).also { activeDelegate = "GPU" }
        } catch (_: Exception) {
            createLandmarker(Delegate.CPU).also { activeDelegate = "CPU" }
        }
    }

    private fun createLandmarker(delegate: Delegate): HandLandmarker {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("models/hand_landmarker.task")
            .setDelegate(delegate)
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.VIDEO)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.3f)
            .setMinHandPresenceConfidence(0.3f)
            .setMinTrackingConfidence(0.3f)
            .build()

        return HandLandmarker.createFromOptions(context, options)
    }

    fun detectHand(bitmap: android.graphics.Bitmap): HandPoseFrame {
        val landmarker = handLandmarker ?: return HandPoseFrame()

        // VIDEO mode needs strictly increasing timestamps.
        var timestampMs = SystemClock.uptimeMillis()
        if (timestampMs <= lastTimestampMs) {
            timestampMs = lastTimestampMs + 1
        }
        lastTimestampMs = timestampMs

        val mpImage = BitmapImageBuilder(bitmap).build()
        val result = landmarker.detectForVideo(mpImage, timestampMs)

        if (result.landmarks().isEmpty()) {
            filterX.reset()
            filterY.reset()
            return HandPoseFrame()
        }

        val points = result.landmarks()[0]

        val landmarks = points.mapIndexed { index, point ->
            HandLandmark3D(
                id = index,
                name = when (index) {
                    4 -> "Thumb Tip"
                    8 -> "Index Finger Tip"
                    else -> "Landmark $index"
                },
                x = point.x(),
                y = point.y(),
                z = point.z()
            )
        }

        val thumb = points[4]
        val index = points[8]

        val dx = index.x() - thumb.x()
        val dy = index.y() - thumb.y()
        val distance = sqrt(dx * dx + dy * dy)

        return HandPoseFrame(
            isHandDetected = true,
            landmarks = landmarks,
            indexTipScreenX = filterX.filter(index.x(), timestampMs),
            indexTipScreenY = filterY.filter(index.y(), timestampMs),
            thumbTipScreenX = thumb.x(),
            thumbTipScreenY = thumb.y(),
            pinchDistance = distance,
            isPinching = distance < 0.08f
        )
    }

    fun stopTracking() {
        handLandmarker?.close()
        handLandmarker = null
        filterX.reset()
        filterY.reset()
    }

    fun computePinchState(
        indexX: Float,
        indexY: Float,
        thumbX: Float,
        thumbY: Float
    ): Boolean {
        val dx = indexX - thumbX
        val dy = indexY - thumbY
        return sqrt(dx * dx + dy * dy) < 0.08f
    }
}