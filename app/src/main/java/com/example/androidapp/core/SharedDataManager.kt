package com.example.androidapp.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SharedPlayerStats(
    val totalXp: Int = 1450,
    val totalCoins: Int = 320,
    val shooterHighScore: Int = 850,
    val runnerHighScore: Int = 1200,
    val puzzleItemsPlaced: Int = 14,
    val syncStatus: String = "Synced to Cloud Storage"
)

/**
 * Shared Data / Score Manager (Persistent Core).
 * Sinks and aggregates player scores, progress, and achievements from dynamic mini-games.
 */
class SharedDataManager {
    private val _stats = MutableStateFlow(SharedPlayerStats())
    val stats: StateFlow<SharedPlayerStats> = _stats.asStateFlow()

    fun updateScore(gameId: String, points: Int) {
        _stats.update { current ->
            val newXp = current.totalXp + points
            when (gameId) {
                "shooter" -> current.copy(
                    totalXp = newXp,
                    shooterHighScore = maxOf(current.shooterHighScore, points),
                    syncStatus = "Score Sunk -> Shooter ($points pts)"
                )
                "runner" -> current.copy(
                    totalXp = newXp,
                    runnerHighScore = maxOf(current.runnerHighScore, points),
                    syncStatus = "Score Sunk -> Runner ($points pts)"
                )
                "puzzle" -> current.copy(
                    totalXp = newXp,
                    puzzleItemsPlaced = current.puzzleItemsPlaced + 1,
                    syncStatus = "Progress Sunk -> Studio Object Placed"
                )
                else -> current.copy(totalXp = newXp)
            }
        }
    }
}
