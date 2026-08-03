package com.example.androidapp.ar

import android.content.Context
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import kotlin.math.sqrt

/**
 * Spatial 3D Point in Augmented Reality space
 */
data class ArSpatialPoint(
    val x: Float,
    val y: Float,
    val z: Float
) {
    fun distanceTo(other: ArSpatialPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

/**
 * 3D AR Object Anchor in Kotlin
 */
data class ArPlacedObject(
    val id: String,
    val name: String,
    val modelUri: String,
    val position: ArSpatialPoint,
    val scale: Float = 1.0f,
    val rotationY: Float = 0.0f,
    val anchor: Anchor? = null
)

/**
 * Helper class for ARCore Session & Spatial Math in Kotlin
 */
class ArCoreManager(private val context: Context) {

    private var arSession: Session? = null

    fun initializeSession(): Boolean {
        return try {
            arSession = Session(context).apply {
                val config = Config(this).apply {
                    focusMode = Config.FocusMode.AUTO
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                    lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                }
                configure(config)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resume() {
        try {
            arSession?.resume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() {
        arSession?.pause()
    }

    /**
     * Calculates distance between two AR Spatial points in meters
     */
    fun calculateDistanceMeters(p1: ArSpatialPoint, p2: ArSpatialPoint): Float {
        return p1.distanceTo(p2)
    }

    /**
     * Converts meters to formatted string (e.g., "1.45 m" or "45 cm")
     */
    fun formatDistance(meters: Float): String {
        return if (meters < 1.0f) {
            "${(meters * 100).toInt()} cm"
        } else {
            String.format("%.2f m", meters)
        }
    }
}
