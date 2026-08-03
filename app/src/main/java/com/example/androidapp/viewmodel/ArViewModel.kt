package com.example.arvision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arvision.ar.ArPlacedObject
import com.example.arvision.ar.ArSpatialPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class ArMode {
    OBJECT_PLACEMENT,
    SPATIAL_MEASURE,
    SURFACE_DETECTOR,
    ANNOTATION
}

data class ArUiState(
    val appTitle: String = "AR Vision Studio",
    val activeMode: ArMode = ArMode.OBJECT_PLACEMENT,
    val selectedModelName: String = "Modern Chair",
    val selectedModelUri: String = "models/chair.gltf",
    val trackingStatus: String = "Plane Detected • Tap Surface",
    val surfaceType: String = "HORIZONTAL_UPWARD",
    val lightIntensity: Float = 0.85f,
    val placedObjects: List<ArPlacedObject> = emptyList(),
    val measurePoints: List<ArSpatialPoint> = emptyList(),
    val calculatedDistanceMeters: Float? = null,
    val showPointCloud: Boolean = true,
    val showPlaneGrid: Boolean = true
)

class ArViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ArUiState())
    val uiState: StateFlow<ArUiState> = _uiState.asStateFlow()

    init {
        loadPresetObjects()
    }

    private fun loadPresetObjects() {
        val initialObjects = listOf(
            ArPlacedObject(
                id = "1",
                name = "Modern Lounge Chair",
                modelUri = "models/chair.gltf",
                position = ArSpatialPoint(0.2f, -0.4f, -1.2f),
                scale = 1.0f,
                rotationY = 45.0f
            ),
            ArPlacedObject(
                id = "2",
                name = "Cyber Portal Ring",
                modelUri = "models/portal.gltf",
                position = ArSpatialPoint(-0.5f, 0.1f, -1.8f),
                scale = 1.2f,
                rotationY = 90.0f
            )
        )
        _uiState.update { it.copy(placedObjects = initialObjects) }
    }

    fun setArMode(mode: ArMode) {
        _uiState.update { it.copy(activeMode = mode) }
    }

    fun setSelectedModel(name: String, uri: String) {
        _uiState.update { it.copy(selectedModelName = name, selectedModelUri = uri) }
    }

    fun placeObjectAt(x: Float, y: Float, z: Float) {
        val newObj = ArPlacedObject(
            id = System.currentTimeMillis().toString(),
            name = _uiState.value.selectedModelName,
            modelUri = _uiState.value.selectedModelUri,
            position = ArSpatialPoint(x, y, z),
            scale = 1.0f,
            rotationY = (0..360).random().toFloat()
        )
        _uiState.update { state ->
            state.copy(
                placedObjects = state.placedObjects + newObj,
                trackingStatus = "Placed ${newObj.name} in 3D Space"
            )
        }
    }

    fun addMeasurePoint(x: Float, y: Float, z: Float) {
        val newPoint = ArSpatialPoint(x, y, z)
        _uiState.update { state ->
            val updatedPoints = (state.measurePoints + newPoint).takeLast(2)
            var dist: Float? = null
            if (updatedPoints.size == 2) {
                val p1 = updatedPoints[0]
                val p2 = updatedPoints[1]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val dz = p1.z - p2.z
                dist = sqrt(dx * dx + dy * dy + dz * dz)
            }
            state.copy(
                measurePoints = updatedPoints,
                calculatedDistanceMeters = dist,
                trackingStatus = if (dist != null) "Measured: ${String.format("%.2f", dist)} meters" else "First Anchor Set • Tap 2nd Point"
            )
        }
    }

    fun clearAllObjects() {
        _uiState.update {
            it.copy(
                placedObjects = emptyList(),
                measurePoints = emptyList(),
                calculatedDistanceMeters = null,
                trackingStatus = "Scene Cleared • Tap Surface to Place"
            )
        }
    }
}
