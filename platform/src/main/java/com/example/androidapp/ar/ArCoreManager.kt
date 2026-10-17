package com.example.androidapp.ar

import android.content.Context
import com.example.gameapi.ArSpatialPoint
import com.google.ar.core.Config
import com.google.ar.core.Session

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
