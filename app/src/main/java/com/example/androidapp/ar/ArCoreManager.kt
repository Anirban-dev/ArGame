package com.example.androidapp.ar

import android.content.Context
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlin.math.sqrt

data class ArSpatialPoint(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: ArSpatialPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

class ArCoreManager(private val context: Context) {
    private var arSession: Session? = null

    fun initializeSession(): Boolean {
        return try {
            arSession = Session(context).apply {
                val config = Config(this).apply {
                    focusMode = Config.FocusMode.AUTO
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                }
                configure(config)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
