package com.example.androidapp.mode1

import com.example.gameapi.ArSpatialPoint

enum class FruitType { WATERMELON, BANANA, APPLE, BOMB }

data class ArFruitTarget(
    val id: String,
    val type: FruitType,
    val position: ArSpatialPoint,
    val velocityY: Float,
    val radius: Float = 0.15f,
    val isSliced: Boolean = false
)

data class JuiceDecal(val x: Float, val y: Float, val color: String)

/**
 * MODE 1: 3D AR FRUIT NINJA (Spatial Arcade Experience)
 * - ARCore spatial fruit spawner
 * - Continuous 3D Raycasting from camera through MediaPipe index finger landmark (#8)
 * - Filament 3D Physics Collider & 2D Juice Splatter Decal generator
 */
class FruitNinjaEngine {
    private var score = 0
    private var comboCounter = 0
    private val activeFruits = mutableListOf<ArFruitTarget>()
    private val juiceDecals = mutableListOf<JuiceDecal>()

    fun processFingerRayIntersection(rayX: Float, rayY: Float, rayZ: Float): Boolean {
        var hitAny = false
        activeFruits.forEachIndexed { idx, fruit ->
            if (!fruit.isSliced) {
                val dx = fruit.position.x - rayX
                val dy = fruit.position.y - rayY
                val dz = fruit.position.z - rayZ
                val distance = kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
                if (distance < fruit.radius + 0.1f) {
                    activeFruits[idx] = fruit.copy(isSliced = true)
                    score += 100
                    comboCounter++
                    hitAny = true
                    juiceDecals.add(JuiceDecal(rayX, rayY, "#ef4444"))
                }
            }
        }
        return hitAny
    }

    fun getScore(): Int = score
    fun getCombo(): Int = comboCounter
}
