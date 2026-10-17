package com.example.androidapp.models

import android.content.Context
import android.graphics.Bitmap
import com.example.androidapp.cv.MediaPipeHandTracker
import com.example.androidapp.cv.CameraFrameConverter
import com.example.gameapi.GameModelProvider
import com.example.gameapi.GameModelSession
import com.example.gameapi.ModelIds
import com.example.gameapi.ModelInput
import com.example.gameapi.ModelOutput

class MediaPipeHandModelProvider : GameModelProvider {
    override val modelIds = setOf(ModelIds.HAND_LANDMARKER)

    override fun createSession(context: Context, modelId: String): GameModelSession {
        require(modelId in modelIds) { "Unsupported MediaPipe model '$modelId'." }
        return MediaPipeHandModelSession(MediaPipeHandTracker(context))
    }
}

private class MediaPipeHandModelSession(
    private val tracker: MediaPipeHandTracker
) : GameModelSession {
    override val modelId: String = ModelIds.HAND_LANDMARKER
    override val runtimeInfo: String
        get() = tracker.activeDelegate

    override fun start() {
        tracker.startTracking()
    }

    override fun infer(input: ModelInput): ModelOutput {
        val bitmap = when (input) {
            is ModelInput.BitmapFrame -> input.bitmap
            is ModelInput.CameraFrame -> CameraFrameConverter.toBitmap(
                input.image,
                input.rotationDegrees,
                input.maxDimension
            ) ?: throw IllegalArgumentException("$modelId could not convert the camera frame.")
            else -> throw IllegalArgumentException(
                "$modelId accepts bitmap-frame or camera-frame inputs only."
            )
        }
        val recycleAfterInference = input is ModelInput.CameraFrame
        return ModelOutput.HandLandmarks(
            frame = try {
                tracker.detectHand(bitmap)
            } finally {
                if (recycleAfterInference) bitmap.recycle()
            },
            runtimeInfo = runtimeInfo
        )
    }

    override fun close() {
        tracker.stopTracking()
    }
}
