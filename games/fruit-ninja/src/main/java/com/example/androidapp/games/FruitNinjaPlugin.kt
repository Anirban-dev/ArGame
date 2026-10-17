package com.example.androidapp.games

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.androidapp.mode1.FruitNinjaEngine
import com.example.androidapp.ui.screens.ARFruitNinjaGame
import com.example.gameapi.GameMetadata
import com.example.gameapi.GameAssetReference
import com.example.gameapi.GamePlugin
import com.example.gameapi.GameRuntime
import com.example.gameapi.IMiniGame
import com.example.gameapi.MiniGameEventListener
import com.example.gameapi.MiniGameState

object FruitNinjaPlugin : GamePlugin {
    override val metadata = GameMetadata(
        id = "fruit-ninja",
        title = "AR Fruit Ninja Slicer",
        developer = "ArGame",
        version = "1.0.0",
        description = "Slice fruit using hand tracking or touch."
    )
    override val modelAssets = listOf(
        GameAssetReference(
            id = "fruit.apple",
            assetLocation = "models/apple.glb",
            mimeType = "model/gltf-binary"
        ),
        GameAssetReference(
            id = "fruit.apple-half-left",
            assetLocation = "models/apple_half_left.glb",
            mimeType = "model/gltf-binary"
        ),
        GameAssetReference(
            id = "fruit.apple-half-right",
            assetLocation = "models/apple_half_right.glb",
            mimeType = "model/gltf-binary"
        )
    )

    override fun createGame(): IMiniGame = FruitNinjaModuleGame()

    @Composable
    override fun Content(runtime: GameRuntime, game: IMiniGame, onExit: () -> Unit) {
        ARFruitNinjaGame(
            runtime.models,
            runtime.assets,
            onSlice = { points ->
                (game as? FruitNinjaModuleGame)?.recordSlice(points)
            },
            onExit = onExit
        )
    }
}

private class FruitNinjaModuleGame : IMiniGame {
    private var listener: MiniGameEventListener? = null
    private var state = MiniGameState.IDLE
    private var score = 0
    private val engine = FruitNinjaEngine()

    override fun getGameId(): String = FruitNinjaPlugin.metadata.id
    override fun getGameTitle(): String = FruitNinjaPlugin.metadata.title

    override fun initialize(context: Context, listener: MiniGameEventListener) {
        this.listener = listener
        state = MiniGameState.IDLE
        score = 0
    }

    override fun onStartGame() {
        state = MiniGameState.RUNNING
    }

    override fun onPauseGame() {
        state = MiniGameState.PAUSED
    }

    override fun onEndGame() {
        state = MiniGameState.ENDED
        listener?.onGameEnded(getGameId(), getScore())
    }

    override fun processInputEvent(x: Float, y: Float, z: Float) {
        if (state != MiniGameState.RUNNING) return
        if (engine.processFingerRayIntersection(x, y, z)) {
            listener?.onScoreUpdated(getGameId(), engine.getScore())
        }
    }

    fun recordSlice(points: Int) {
        if (state != MiniGameState.RUNNING || points <= 0) return
        score += points
        listener?.onScoreUpdated(getGameId(), points)
    }

    override fun getScore(): Int = maxOf(score, engine.getScore())
    override fun getState(): MiniGameState = state
}
