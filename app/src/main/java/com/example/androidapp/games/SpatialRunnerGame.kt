package com.example.androidapp.games

import android.content.Context
import com.example.androidapp.core.IMiniGame
import com.example.androidapp.core.MiniGameEventListener
import com.example.androidapp.core.MiniGameState

class SpatialRunnerGame : IMiniGame {
    private var listener: MiniGameEventListener? = null
    private var distanceTraveled = 0
    private var state = MiniGameState.IDLE

    override fun getGameId(): String = "runner"
    override fun getGameTitle(): String = "Spatial Speed Runner"

    override fun initialize(context: Context, listener: MiniGameEventListener) {
        this.listener = listener
        this.distanceTraveled = 0
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
        listener?.onGameEnded(getGameId(), distanceTraveled)
    }

    override fun processInputEvent(x: Float, y: Float, z: Float) {
        if (state != MiniGameState.RUNNING) return
        val bonus = 150
        distanceTraveled += bonus
        listener?.onScoreUpdated(getGameId(), bonus)
    }

    override fun getScore(): Int = distanceTraveled
    override fun getState(): MiniGameState = state
}
