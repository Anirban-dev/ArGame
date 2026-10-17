package com.example.androidapp.games

import android.content.Context
import com.example.gameapi.IMiniGame
import com.example.gameapi.MiniGameEventListener
import com.example.gameapi.MiniGameState

class TargetShooterGame : IMiniGame {
    private var listener: MiniGameEventListener? = null
    private var score = 0
    private var state = MiniGameState.IDLE

    override fun getGameId(): String = "shooter"
    override fun getGameTitle(): String = "AR Target Shooter"

    override fun initialize(context: Context, listener: MiniGameEventListener) {
        this.listener = listener
        this.score = 0
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
        listener?.onGameEnded(getGameId(), score)
    }

    override fun processInputEvent(x: Float, y: Float, z: Float) {
        if (state != MiniGameState.RUNNING) return
        // Target hit logic calculation in 3D AR space
        val hitPoints = 100
        score += hitPoints
        listener?.onScoreUpdated(getGameId(), hitPoints)
        listener?.onProgressSaved(getGameId(), "targets_hit", score / 100)
    }

    override fun getScore(): Int = score
    override fun getState(): MiniGameState = state
}
