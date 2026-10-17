package com.example.androidapp.model

import com.example.gameapi.ArSpatialPoint

/**
 * Data class representing a 3D AR object placed into the ARCore Spatial Scene.
 */
data class ArPlacedObject(
    val id: String = System.currentTimeMillis().toString(),
    val name: String = "3D AR Model",
    val modelUri: String = "models/chair.glb",
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val positionZ: Float = -1.2f,
    val rotationY: Float = 0f,
    val scale: Float = 1.0f,
    val isSelected: Boolean = false,
    val anchorId: String? = null
) {
    fun toSpatialPoint(): ArSpatialPoint = ArSpatialPoint(positionX, positionY, positionZ)
}
