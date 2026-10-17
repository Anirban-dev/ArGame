package com.example.androidapp.games

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidapp.mode2.RoomEditorEngine
import com.example.gameapi.GameAssetReference
import com.example.gameapi.GameMetadata
import com.example.gameapi.GamePlugin
import com.example.gameapi.GameRuntime
import com.example.gameapi.IMiniGame
import com.example.gameapi.MiniGameEventListener
import com.example.gameapi.MiniGameState

object RoomEditorPlugin : GamePlugin {
    override val metadata = GameMetadata(
        id = "room-editor",
        title = "AR Room Editor",
        developer = "ArGame",
        version = "1.0.0",
        description = "Place 3D objects and edit spatial scenes."
    )
    override val modelAssets = listOf(
        GameAssetReference(
            id = "room.duck",
            assetLocation = "models/Duck.glb",
            mimeType = "model/gltf-binary"
        )
    )

    override fun createGame(): IMiniGame = ArStudioGame()

    @Composable
    override fun Content(runtime: GameRuntime, game: IMiniGame, onExit: () -> Unit) {
        val duck = runtime.assets.resolveModel("room.duck")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(metadata.title, style = MaterialTheme.typography.headlineMedium)
            Text(metadata.description)
            Text("3D asset supplied by platform: ${duck.assetLocation}")
            Button(onClick = {
                game.processInputEvent(0f, 0f, 0f)
                onExit()
            }) {
                Text("Place object and back to games")
            }
        }
    }
}

private class ArStudioGame : IMiniGame {
    private var listener: MiniGameEventListener? = null
    private var placedObjectsCount = 0
    private var state = MiniGameState.IDLE
    private val engine = RoomEditorEngine()

    override fun getGameId(): String = RoomEditorPlugin.metadata.id
    override fun getGameTitle(): String = RoomEditorPlugin.metadata.title

    override fun initialize(context: Context, listener: MiniGameEventListener) {
        this.listener = listener
        this.placedObjectsCount = 0
        this.state = MiniGameState.IDLE
    }

    override fun onStartGame() {
        state = MiniGameState.RUNNING
    }

    override fun onPauseGame() {
        state = MiniGameState.PAUSED
    }

    override fun onEndGame() {
        state = MiniGameState.ENDED
        listener?.onGameEnded(getGameId(), placedObjectsCount * 50)
    }

    override fun processInputEvent(x: Float, y: Float, z: Float) {
        if (state != MiniGameState.RUNNING) return
        engine.runMobileSamCutout(x, y)
        placedObjectsCount++
        listener?.onScoreUpdated(getGameId(), 50)
        listener?.onProgressSaved(getGameId(), "anchor_position", "X:$x Y:$y Z:$z")
    }

    override fun getScore(): Int = placedObjectsCount * 50
    override fun getState(): MiniGameState = state
}
