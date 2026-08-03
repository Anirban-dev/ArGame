package com.example.androidapp.games

import android.content.Context
import com.example.androidapp.core.IMiniGame
import com.example.androidapp.core.MiniGameEventListener
import com.example.androidapp.core.MiniGameState

class ArStudioGame : IMiniGame {
    private var listener: MiniGameEventListener? = null
    private var placedObjectsCount = 0
    private var state = MiniGameState.IDLE

    override fun getGameId(): String = "puzzle"
    override fun getGameTitle(): String = "AR Studio & Spatial Ruler"

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
        placedObjectsCount++
        listener?.onScoreUpdated(getGameId(), 50)
        listener?.onProgressSaved(getGameId(), "anchor_position", "X:$x Y:$y Z:$z")
    }

    override fun getScore(): Int = placedObjectsCount * 50
    override fun getState(): MiniGameState = state
}
