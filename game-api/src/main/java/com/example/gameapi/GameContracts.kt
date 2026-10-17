package com.example.gameapi

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import androidx.compose.runtime.Composable
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode

data class GameMetadata(
    val id: String,
    val title: String,
    val developer: String,
    val version: String,
    val description: String,
    val fileSizeKb: Long = 0
)

data class ArSpatialPoint(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: ArSpatialPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }
}

interface GamePlugin {
    val metadata: GameMetadata
    val modelAssets: List<GameAssetReference>
        get() = emptyList()

    fun createGame(): IMiniGame

    @Composable
    fun Content(
        runtime: GameRuntime,
        game: IMiniGame,
        onExit: () -> Unit
    )
}

data class GameRuntime(
    val models: GameModelGateway,
    val assets: GameAssetGateway
)

interface MiniGameEventListener {
    fun onScoreUpdated(gameId: String, newScore: Int)
    fun onProgressSaved(gameId: String, key: String, value: Any)
    fun onGameEnded(gameId: String, finalScore: Int)
}

enum class MiniGameState { IDLE, RUNNING, PAUSED, ENDED }

interface IMiniGame {
    fun getGameId(): String
    fun getGameTitle(): String
    fun initialize(context: Context, listener: MiniGameEventListener)
    fun onStartGame()
    fun onPauseGame()
    fun onEndGame()
    fun processInputEvent(x: Float, y: Float, z: Float)
    fun getScore(): Int
    fun getState(): MiniGameState
}

object ModelIds {
    const val HAND_LANDMARKER = "mediapipe.hand-landmarker"
}

sealed interface ModelInput {
    data class BitmapFrame(val bitmap: Bitmap) : ModelInput
    data class CameraFrame(
        val image: Image,
        val rotationDegrees: Int = 0,
        val maxDimension: Int = Int.MAX_VALUE
    ) : ModelInput
    data class Text(val value: String) : ModelInput
    data class Tensor(val shape: List<Int>, val values: FloatArray) : ModelInput
}

sealed interface ModelOutput {
    data class HandLandmarks(
        val frame: HandPoseFrame,
        val runtimeInfo: String
    ) : ModelOutput

    data class Mask(val width: Int, val height: Int, val values: ByteArray) : ModelOutput
    data class Tensor(val shape: List<Int>, val values: FloatArray) : ModelOutput
    data class Text(val value: String) : ModelOutput
}

data class HandLandmark3D(
    val id: Int,
    val name: String,
    val x: Float,
    val y: Float,
    val z: Float
)

data class HandPoseFrame(
    val isHandDetected: Boolean = false,
    val landmarks: List<HandLandmark3D> = emptyList(),
    val indexTipScreenX: Float = 0.5f,
    val indexTipScreenY: Float = 0.5f,
    val thumbTipScreenX: Float = 0.5f,
    val thumbTipScreenY: Float = 0.5f,
    val pinchDistance: Float = 1.0f,
    val isPinching: Boolean = false
)

interface GameModelGateway {
    val availableModelIds: Set<String>
    fun openSession(modelId: String): GameModelSession
}

data class GameAssetReference(
    val id: String,
    val assetLocation: String,
    val mimeType: String
)

interface GameAssetGateway {
    val availableAssetIds: Set<String>
    fun resolveModel(assetId: String): GameAssetReference
    suspend fun loadModelNode(assetId: String, modelLoader: ModelLoader): ModelNode
}

interface GameModelSession : AutoCloseable {
    val modelId: String
    val runtimeInfo: String

    fun start()
    fun infer(input: ModelInput): ModelOutput
    override fun close()
}

interface GameModelProvider {
    val modelIds: Set<String>
    fun createSession(context: Context, modelId: String): GameModelSession
}
