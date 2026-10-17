package com.example.androidapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gameapi.ArSpatialPoint
import com.example.androidapp.model.ArPlacedObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt

enum class ArMode { OBJECT_PLACEMENT, SPATIAL_MEASURE }

data class ArUiState(
    val placedObjects: List<ArPlacedObject> = emptyList(),
    val selectedObject: ArPlacedObject? = null,
    val isPlaneDetected: Boolean = true,
    val trackingStatusText: String = "Tracking Active (60 FPS)",
    val trackingStatus: String = "ARCore Active Plane (60 FPS)",
    val appTitle: String = "Spatial Vision AR Studio",
    val activeMode: ArMode = ArMode.OBJECT_PLACEMENT,
    val selectedModelName: String = "Modern Chair",
    val selectedModelUri: String = "models/chair.gltf",
    val measurePoints: List<ArSpatialPoint> = emptyList(),
    val calculatedDistanceMeters: Float? = null
)

/**
 * ViewModel managing AR spatial scene state, placed 3D objects, modes, and spatial measurements.
 */
class ArViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ArUiState())
    val uiState: StateFlow<ArUiState> = _uiState.asStateFlow()

    fun setArMode(mode: ArMode) {
        _uiState.update { currentState ->
            currentState.copy(activeMode = mode)
        }
    }

    fun setSelectedModel(name: String, uri: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedModelName = name,
                selectedModelUri = uri
            )
        }
    }

    fun placeObjectAt(x: Float, y: Float, z: Float) {
        val currentName = _uiState.value.selectedModelName
        val currentUri = _uiState.value.selectedModelUri
        val newObj = ArPlacedObject(
            id = System.currentTimeMillis().toString(),
            name = currentName,
            modelUri = currentUri,
            positionX = x,
            positionY = y,
            positionZ = z
        )
        _uiState.update { currentState ->
            currentState.copy(
                placedObjects = currentState.placedObjects + newObj,
                selectedObject = newObj
            )
        }
    }

    fun addPlacedObject(objectName: String, x: Float, y: Float, z: Float) {
        val newObj = ArPlacedObject(
            id = System.currentTimeMillis().toString(),
            name = objectName,
            positionX = x,
            positionY = y,
            positionZ = z
        )
        _uiState.update { currentState ->
            currentState.copy(
                placedObjects = currentState.placedObjects + newObj,
                selectedObject = newObj
            )
        }
    }

    fun addMeasurePoint(x: Float, y: Float, z: Float) {
        val newPoint = ArSpatialPoint(x, y, z)
        _uiState.update { currentState ->
            val updatedPoints = currentState.measurePoints + newPoint
            val dist = if (updatedPoints.size >= 2) {
                val p1 = updatedPoints[updatedPoints.size - 2]
                val p2 = updatedPoints.last()
                val dx = p2.x - p1.x
                val dy = p2.y - p1.y
                val dz = p2.z - p1.z
                sqrt(dx * dx + dy * dy + dz * dz)
            } else null

            currentState.copy(
                measurePoints = updatedPoints,
                calculatedDistanceMeters = dist
            )
        }
    }

    fun removePlacedObject(id: String) {
        _uiState.update { currentState ->
            currentState.copy(
                placedObjects = currentState.placedObjects.filter { it.id != id },
                selectedObject = if (currentState.selectedObject?.id == id) null else currentState.selectedObject
            )
        }
    }

    fun selectObject(obj: ArPlacedObject?) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedObject = obj,
                placedObjects = currentState.placedObjects.map { item ->
                    item.copy(isSelected = item.id == obj?.id)
                }
            )
        }
    }

    fun clearAllObjects() {
        _uiState.update { currentState ->
            currentState.copy(
                placedObjects = emptyList(),
                selectedObject = null,
                measurePoints = emptyList(),
                calculatedDistanceMeters = null
            )
        }
    }
}
