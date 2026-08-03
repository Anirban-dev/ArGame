package com.example.androidapp.core

import android.content.Context

interface MiniGameEventListener {
    fun onScoreUpdated(gameId: String, newScore: Int)
    fun onProgressSaved(gameId: String, key: String, value: Any)
    fun onGameEnded(gameId: String, finalScore: Int)
}

enum class MiniGameState { IDLE, RUNNING, PAUSED, ENDED }

/**
 * Standardized IMiniGame interface implemented by all dynamic mini-game modules.
 * Decouples game logic from the persistent main launcher container.
 */
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
