
package com.example.androidapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidapp.loader.DynamicGameLoader
import com.example.androidapp.loader.DynamicGamePackage
import com.example.androidapp.mode1.FruitNinja2DGame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArScreen() {
    val context = LocalContext.current
    val gameLoader = remember { DynamicGameLoader(context) }

    var installedGames by remember {
        mutableStateOf(
            listOf(
                DynamicGamePackage(
                    packageId = "g1",
                    title = "AR Fruit Ninja Slicer",
                    developer = "Spatial Games",
                    version = "2.1.0",
                    fileSizeKb = 14500,
                    entryClass = "com.example.minigame.FruitNinjaModule"
                ),
                DynamicGamePackage(
                    packageId = "g2",
                    title = "Spatial Speed Runner",
                    developer = "Unity Dynamic",
                    version = "1.0.4",
                    fileSizeKb = 8900,
                    entryClass = "com.example.minigame.RunnerModule"
                ),
                DynamicGamePackage(
                    packageId = "g3",
                    title = "AR Room Editor & Splatting",
                    developer = "MediaPipe Core",
                    version = "3.0.0",
                    fileSizeKb = 22400,
                    entryClass = "com.example.minigame.RoomEditorModule"
                )
            )
        )
    }

    var activeGame by remember {
        mutableStateOf<DynamicGamePackage?>(null)
    }
    var searchQuery by remember { mutableStateOf("") }
    var showSdkGuide by remember { mutableStateOf(false) }
    var userTotalXp by remember { mutableIntStateOf(1450) }
    var userCoins by remember { mutableIntStateOf(320) }

    var logs by remember {
        mutableStateOf(
            listOf(
                "[DynamicGameLoader] Storage Access Framework Ready",
                "[Launcher] System Ready"
            )
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            val fileName = fileUri.lastPathSegment ?: "custom_game.zip"

            val newPkg = DynamicGamePackage(
                packageId = "user_${System.currentTimeMillis()}",
                title = fileName
                    .substringAfterLast("/")
                    .removeSuffix(".zip")
                    .removeSuffix(".apk")
                    .replace("_", " "),
                developer = "User Storage Import",
                version = "1.0.0",
                fileSizeKb = 6144,
                isLoaded = true
            )

            installedGames = listOf(newPkg) + installedGames

            logs = (logs + listOf(
                "[StorageAccessFramework] File selected: ${fileUri.path}",
                "[DynamicGameLoader] Registered '${newPkg.title}'"
            )).takeLast(10)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF090D16)
    ) {
        if (activeGame == null) {
            // Entire landing page scrolls on short screens.
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User profile
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF4F46E5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "CP",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    "User Auth: Verified",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Level 8 Spatial Architect",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = Color(0xFF1E1B4B),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "⭐ $userTotalXp XP",
                                    color = Color(0xFFC084FC),
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    ),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Surface(
                                color = Color(0xFF312E81),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "🪙 $userCoins",
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    ),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Storage and game-file section
                Surface(
                    color = Color(0xFF1E1B4B),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "DYNAMIC MODULAR ENGINE",
                                color = Color(0xFFA5B4FC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "Spatial AR Game Hub",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Select and load mini-game files from your phone storage.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4F46E5)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.FileOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Select File",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = { showSdkGuide = !showSdkGuide },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "SDK guide",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SDK", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Optional SDK instructions
                if (showSdkGuide) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "How to Create Dynamic Game Files",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                "1. Implement the mini-game contract.\n" +
                                        "2. Package the game in a supported format.\n" +
                                        "3. Select the file using the storage picker.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search installed mini-games...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF818CF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                // Installed games list
                Text(
                    "INSTALLED MINI-GAMES (${installedGames.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )

                val filteredGames = installedGames.filter {
                    it.title.contains(searchQuery.trim(), ignoreCase = true)
                }

                if (filteredGames.isEmpty()) {
                    Text(
                        "No games match your search.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredGames.forEach { game ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            game.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${game.developer} • v${game.version} " +
                                                    "(${game.fileSizeKb / 1024} MB)",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            activeGame = game
                                            logs = (
                                                    logs + listOf(
                                                        "[MiniGame] Launch: ${game.title}",
                                                        "[MiniGame] State: RUNNING"
                                                    )
                                                    ).takeLast(10)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF10B981)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "LAUNCH",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (activeGame?.packageId == "g1") {
            // Built-in Fruit Ninja game
            FruitNinja2DGame(
                onExit = { activeGame = null }
            )
        } else {
            // Placeholder runtime screen for other game entries
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF020617))
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.Center)
                        .border(
                            2.dp,
                            Color(0xFF10B981),
                            CircleShape
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { activeGame = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Back to Launcher",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        activeGame?.title ?: "Mini-game",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color(0xEE0F172A),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "SHARED DATA SINK STREAM",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        logs.takeLast(4).forEach { log ->
                            Text(
                                log,
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
