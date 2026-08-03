package com.example.androidapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidapp.loader.DynamicGameLoader
import com.example.androidapp.loader.DynamicGamePackage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArScreen() {
    val context = LocalContext.current
    val gameLoader = remember { DynamicGameLoader(context) }
    
    // Dynamic Installed Games List State
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

    var activeGame by remember { mutableStateOf<DynamicGamePackage?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSdkGuide by remember { mutableStateOf(false) }
    var userTotalXp by remember { mutableStateOf(1450) }
    var userCoins by remember { mutableStateOf(320) }
    var logs by remember {
        mutableStateOf(
            listOf(
                "[DynamicGameLoader] Storage Access Framework Ready",
                "[Launcher] System Ready: Select game file (.apk, .zip, .bundle, .glb)"
            )
        )
    }

    // Storage Access Framework File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            val fileName = fileUri.lastPathSegment ?: "custom_game.zip"
            val newPkg = DynamicGamePackage(
                packageId = "user_${System.currentTimeMillis()}",
                title = fileName.replace(".zip", "").replace(".apk", "").replace("_", " ").capitalize(),
                developer = "User Storage Import",
                version = "1.0.0",
                fileSizeKb = 6144,
                isLoaded = true
            )
            installedGames = listOf(newPkg) + installedGames
            logs = logs.takeLast(10) + listOf(
                "[StorageAccessFramework] File selected: ${fileUri.path}",
                "[DynamicGameLoader] Extracting package '${newPkg.title}'...",
                "[IMiniGame] Successfully registered dynamic game!"
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF090D16)
    ) {
        if (activeGame == null) {
            // VIEW 1: MODULAR LANDING PAGE HUB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header: User Profile & Auth
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF4F46E5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("CP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("User Auth: Verified", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Level 8 Spatial Architect", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color(0xFF1E1B4B), shape = RoundedCornerShape(8.dp)) {
                                Text("⭐ ${userTotalXp} XP", color = Color(0xFFC084FC), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Surface(color = Color(0xFF312E81), shape = RoundedCornerShape(8.dp)) {
                                Text("🪙 ${userCoins}", color = Color(0xFFFBBF24), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Banner: Add Game File
                Surface(
                    color = Color(0xFF1E1B4B),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DYNAMIC MODULAR ENGINE", color = Color(0xFFA5B4FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Spatial AR Game Hub", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Select and load dynamic mini-game files (.apk, .zip, .bundle, .glb) directly from local phone storage.", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("+ Select Storage File", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showSdkGuide = !showSdkGuide },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SDK Spec", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search & Catalog Header
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search installed mini-games...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (showSdkGuide) {
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("📖 How to Create Dynamic Game Files:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. Implement IMiniGame contract in Kotlin/Unity\n2. Package into .apk, .zip, .bundle, or .glb\n3. Select file from phone storage via SAF picker", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text("INSTALLED MINI-GAMES (${installedGames.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(8.dp))

                // Game List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(installedGames.filter { it.title.contains(searchQuery, ignoreCase = true) }) { game ->
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(game.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${game.developer} • v${game.version} (${game.fileSizeKb / 1024} MB)", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        activeGame = game
                                        logs = logs + listOf("[IMiniGame] Instantiate -> Class: ${game.entryClass}", "[IMiniGame] State: RUNNING")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("LAUNCH", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // VIEW 2: ACTIVE DYNAMIC MINI-GAME RUNTIME VIEWPORT
            Box(modifier = Modifier.fillMaxSize()) {
                // Simulated Camera Feed & Spatial Scene
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF020617))
                        .clickable {
                            userTotalXp += 100
                            logs = logs.takeLast(10) + listOf("[IMiniGame -> SharedData] Event: Target Raycast Hit (+100 XP)")
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.Center)
                            .border(2.dp, Color(0xFF10B981), CircleShape)
                    )
                }

                // Top Navigation Bar inside Game
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back to Launcher", color = Color.White, fontSize = 12.sp)
                    }

                    Surface(color = Color(0xFF0F172A), shape = RoundedCornerShape(12.dp)) {
                        Text("🎮 ${activeGame?.title}", color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom Log Overlay
                Surface(
                    color = Color(0xEE0F172A),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📡 SHARED DATA SINK STREAM", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        logs.takeLast(4).forEach { log ->
                            Text(log, color = Color(0xFF94A3B8), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
