import { AndroidProject } from "../types";

export const defaultAndroidProject: AndroidProject = {
  appName: "AR Vision Spatial Studio",
  appDescription: "Augmented Reality (ARCore & Jetpack Compose) Kotlin Android App for 3D Object Placement, Spatial Ruler, and Surface Mesh Detection.",
  packageName: "com.example.arvision",
  version: "1.0.0",
  targetSdk: 34,
  minSdk: 24,
  previewState: {
    title: "AR Spatial Viewfinder",
    subtitle: "Google ARCore & Jetpack Compose",
    theme: "dark",
    primaryColor: "#6366f1",
    screenType: "dashboard",
    activeTab: "ar_view",
    navigation: [
      { id: "ar_view", label: "AR Viewfinder", icon: "Box" },
      { id: "measure", label: "Spatial Ruler", icon: "Ruler" },
      { id: "catalog", label: "3D Catalog", icon: "Folder" },
      { id: "settings", label: "AR Settings", icon: "Settings" }
    ],
    cards: [
      {
        id: "1",
        title: "Modern Lounge Chair",
        subtitle: "Position: X: 0.20m, Y: -0.40m, Z: -1.20m",
        badge: "Placed",
        value: "45° Rot",
        tag: "Furniture 3D",
        completed: true,
        time: "AR Anchor #1",
        category: "3D Model"
      },
      {
        id: "2",
        title: "Cyber Portal Ring",
        subtitle: "Position: X: -0.50m, Y: 0.10m, Z: -1.80m",
        badge: "Active Anchor",
        value: "90° Rot",
        tag: "Sci-Fi FX",
        completed: true,
        time: "AR Anchor #2",
        category: "3D Effect"
      }
    ],
    actionButtonText: "Place 3D Anchor in AR"
  },
  files: [
    {
      id: "main-activity",
      name: "MainActivity.kt",
      path: "app/src/main/java/com/example/arvision/MainActivity.kt",
      language: "kotlin",
      isMain: true,
      content: `package com.example.arvision

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.arvision.ui.screens.ArScreen
import com.example.arvision.ui.theme.ArVisionTheme

/**
 * Android Main Activity for Augmented Reality (ARCore + Jetpack Compose).
 * Requests Camera permissions and mounts the AR Spatial Viewfinder.
 */
class MainActivity : ComponentActivity() {

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(
                    this,
                    "Camera permission is required for AR Spatial Tracking",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkCameraPermission()

        setContent {
            ArVisionTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArScreen()
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}`
    },
    {
      id: "ar-screen",
      name: "ArScreen.kt",
      path: "app/src/main/java/com/example/arvision/ui/screens/ArScreen.kt",
      language: "kotlin",
      content: `package com.example.arvision.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arvision.viewmodel.ArMode
import com.example.arvision.viewmodel.ArViewModel

@Composable
fun ArScreen(
    viewModel: ArViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // AR Viewfinder & Surface Mesh Layer
        ArCameraViewport(
            uiState = uiState,
            onSurfaceTap = { x, y, z ->
                if (uiState.activeMode == ArMode.SPATIAL_MEASURE) {
                    viewModel.addMeasurePoint(x, y, z)
                } else {
                    viewModel.placeObjectAt(x, y, z)
                }
            }
        )

        // Top AR HUD Info Overlay
        ArHeaderBar(
            title = uiState.appTitle,
            trackingStatus = uiState.trackingStatus,
            onClearClick = { viewModel.clearAllObjects() }
        )
    }
}`
    },
    {
      id: "ar-core-manager",
      name: "ArCoreManager.kt",
      path: "app/src/main/java/com/example/arvision/ar/ArCoreManager.kt",
      language: "kotlin",
      content: `package com.example.arvision.ar

import android.content.Context
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Session
import kotlin.math.sqrt

data class ArSpatialPoint(val x: Float, val y: Float, val z: Float) {
    fun distanceTo(other: ArSpatialPoint): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}

data class ArPlacedObject(
    val id: String,
    val name: String,
    val modelUri: String,
    val position: ArSpatialPoint,
    val scale: Float = 1.0f,
    val rotationY: Float = 0.0f
)

class ArCoreManager(private val context: Context) {
    private var arSession: Session? = null

    fun initializeSession(): Boolean {
        return try {
            arSession = Session(context).apply {
                val config = Config(this).apply {
                    focusMode = Config.FocusMode.AUTO
                    planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                }
                configure(config)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}`
    },
    {
      id: "ar-viewmodel",
      name: "ArViewModel.kt",
      path: "app/src/main/java/com/example/arvision/viewmodel/ArViewModel.kt",
      language: "kotlin",
      content: `package com.example.arvision.viewmodel

import androidx.lifecycle.ViewModel
import com.example.arvision.ar.ArPlacedObject
import com.example.arvision.ar.ArSpatialPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ArMode { OBJECT_PLACEMENT, SPATIAL_MEASURE, SURFACE_DETECTOR }

data class ArUiState(
    val appTitle: String = "AR Vision Studio",
    val activeMode: ArMode = ArMode.OBJECT_PLACEMENT,
    val selectedModelName: String = "Modern Chair",
    val trackingStatus: String = "Plane Detected • Tap Surface",
    val placedObjects: List<ArPlacedObject> = emptyList()
)

class ArViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ArUiState())
    val uiState: StateFlow<ArUiState> = _uiState.asStateFlow()

    fun placeObjectAt(x: Float, y: Float, z: Float) {
        val newObj = ArPlacedObject(
            id = System.currentTimeMillis().toString(),
            name = _uiState.value.selectedModelName,
            modelUri = "models/chair.gltf",
            position = ArSpatialPoint(x, y, z)
        )
        _uiState.update { it.copy(placedObjects = it.placedObjects + newObj) }
    }
}`
    },
    {
      id: "build-gradle",
      name: "build.gradle.kts",
      path: "app/build.gradle.kts",
      language: "kotlin",
      content: `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.arvision"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.arvision"
        minSdk = 24
        targetSdk = 34
    }
}

dependencies {
    implementation("com.google.ar:core:1.41.0")
    implementation("io.github.sceneview:arsceneview:0.10.0")
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.material3:material3")
}`
    },
    {
      id: "manifest-xml",
      name: "AndroidManifest.xml",
      path: "app/src/main/AndroidManifest.xml",
      language: "xml",
      content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera.ar" android:required="true" />
    <application android:label="AR Vision Kotlin">
        <meta-data android:name="com.google.ar.core" android:value="required" />
    </application>
</manifest>`
    }
  ]
};
