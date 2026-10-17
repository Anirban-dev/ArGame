package com.example.androidapp.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.example.androidapp.core.GameLauncherManager
import com.example.androidapp.core.ProfileManager
import com.example.androidapp.core.SharedDataManager
import com.example.androidapp.models.MediaPipeHandModelProvider
import com.example.androidapp.models.PlatformAssetGateway
import com.example.androidapp.models.PlatformModelGateway
import com.example.gameapi.GamePlugin
import com.example.gameapi.GameRuntime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding

private enum class AppTab(val label: String) {
    GAMES("Games"),
    MEASURE("Measure"),
    STUDIO("Studio")
}

@Composable
fun NativeAppScreen(games: List<GamePlugin>) {
    val context = LocalContext.current
    val runtime = remember(context, games) {
        GameRuntime(
            models = PlatformModelGateway(context, listOf(MediaPipeHandModelProvider())),
            assets = PlatformAssetGateway(
                context,
                games.flatMap(GamePlugin::modelAssets)
            )
        )
    }
    val launcherManager = remember(context) {
        GameLauncherManager(
            context,
            SharedDataManager(),
            ProfileManager()
        )
    }
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.GAMES) }
    var isGameRunning by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (!isGameRunning) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == AppTab.GAMES,
                        onClick = { selectedTab = AppTab.GAMES },
                        icon = {
                            Icon(Icons.Default.SportsEsports, contentDescription = null)
                        },
                        label = { Text(AppTab.GAMES.label) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.MEASURE,
                        onClick = { selectedTab = AppTab.MEASURE },
                        icon = {
                            Icon(Icons.Default.Straighten, contentDescription = null)
                        },
                        label = { Text(AppTab.MEASURE.label) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == AppTab.STUDIO,
                        onClick = { selectedTab = AppTab.STUDIO },
                        icon = {
                            Icon(Icons.Default.Code, contentDescription = null)
                        },
                        label = { Text(AppTab.STUDIO.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            AppTab.GAMES -> ArScreen(
                gamePlugins = games,
                runtime = runtime,
                gameLauncherManager = launcherManager,
                modifier = Modifier.padding(paddingValues),
                onGameVisibilityChanged = { isGameRunning = it }
            )
            AppTab.MEASURE -> SpatialMeasureScreen(
                modifier = Modifier.padding(paddingValues)
            )
            AppTab.STUDIO -> StudioScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}
