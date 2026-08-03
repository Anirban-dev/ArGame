package com.example.androidapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.androidapp.model.ArPlacedObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ArUiState(
    val placedObjects: List<ArPlacedObject> = emptyList(),
    val selectedObject: ArPlacedObject? = null,
    val isPlaneDetected: Boolean = true,
    val trackingStatusText: String = "Tracking Active (60 FPS)"
)

/**
 * ViewModel managing AR spatial scene state and placed 3D objects.
 */
class ArViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ArUiState())
    val uiState: StateFlow<ArUiState> = _uiState.asStateFlow()

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
                selectedObject = null
            )
        }
    }
}
