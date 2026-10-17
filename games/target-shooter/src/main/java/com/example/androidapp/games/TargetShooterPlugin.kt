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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gameapi.GameMetadata
import com.example.gameapi.GamePlugin
import com.example.gameapi.GameRuntime
import com.example.gameapi.IMiniGame
import com.example.gameapi.MiniGameEventListener
import com.example.gameapi.MiniGameState

object TargetShooterPlugin : GamePlugin {
    override val metadata = GameMetadata(
        id = "shooter",
        title = "AR Target Shooter",
        developer = "ArGame",
        version = "1.0.0",
        description = "Aim, hit spatial targets, and build a high score."
    )

    override fun createGame(): IMiniGame = TargetShooterGame()

    @Composable
    override fun Content(runtime: GameRuntime, game: IMiniGame, onExit: () -> Unit) {
        var score by remember { mutableIntStateOf(0) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            Text(metadata.title, style = MaterialTheme.typography.headlineMedium)
            Text("Score: $score", style = MaterialTheme.typography.titleLarge)
            Text(metadata.description)
            Button(onClick = {
                game.processInputEvent(0f, 0f, 0f)
                score = game.getScore()
            }) { Text("Hit target (+100)") }
            Button(onClick = onExit) { Text("Back to games") }
        }
    }
}
