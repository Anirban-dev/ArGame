package com.example.androidapp.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Core Controller / Scene Manager (Persistent Launcher).
 * Handles loading, unloading, input routing, and standardized communication with active mini-games.
 */
class GameLauncherManager(
    private val context: Context,
    private val sharedDataManager: SharedDataManager,
    private val profileManager: ProfileManager
) : MiniGameEventListener {

    private val _activeGame = MutableStateFlow<IMiniGame?>(null)
    val activeGame: StateFlow<IMiniGame?> = _activeGame.asStateFlow()

    private val _eventLogs = MutableStateFlow<List<String>>(emptyList())
    val eventLogs: StateFlow<List<String>> = _eventLogs.asStateFlow()

    fun loadMiniGame(game: IMiniGame) {
        // Unload current active game if running
        _activeGame.value?.onEndGame()

        logEvent("[CoreController] Unloading previous module...")
        logEvent("[CoreController] Dynamic Loading -> ${game.getGameTitle()} (${game.getGameId()})")

        game.initialize(context, this)
        game.onStartGame()
        _activeGame.value = game

        logEvent("[IMiniGame] Sunk & Initialized: ${game.getGameId()} -> State: RUNNING")
    }

    fun dispatchInputToGame(x: Float, y: Float, z: Float) {
        _activeGame.value?.processInputEvent(x, y, z)
    }

    override fun onScoreUpdated(gameId: String, newScore: Int) {
        logEvent("[IMiniGame -> SharedData] Event: onScoreUpdated($gameId, +$newScore pts)")
        sharedDataManager.updateScore(gameId, newScore)
    }

    override fun onProgressSaved(gameId: String, key: String, value: Any) {
        logEvent("[IMiniGame -> SharedData] Event: onProgressSaved($gameId, $key=$value)")
    }

    override fun onGameEnded(gameId: String, finalScore: Int) {
        logEvent("[IMiniGame -> CoreController] Event: onGameEnded($gameId, score=$finalScore)")
        sharedDataManager.updateScore(gameId, finalScore)
    }

    private fun logEvent(msg: String) {
        _eventLogs.value = (_eventLogs.value + msg).takeLast(20)
    }
}
