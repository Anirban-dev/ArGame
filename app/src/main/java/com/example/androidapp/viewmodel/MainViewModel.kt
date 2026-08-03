package com.example.androidapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val isCompleted: Boolean = false
)

data class MainUiState(
    val appName: String = "Kotlin Android App",
    val activeTab: String = "home",
    val isDark: Boolean = false,
    val items: List<AppItem> = emptyList()
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val initial = listOf(
                AppItem("1", "Kotlin Coroutines & StateFlow", "Manage unidirectional data flow", "Architecture", false),
                AppItem("2", "Jetpack Compose Material 3 UI", "Declarative UI components and themes", "UI/UX", true),
                AppItem("3", "Android Navigation & ViewModel", "Single-activity architecture with Compose", "Core", false),
                AppItem("4", "Gradle Dependency Injection", "Kotlin DSL configuration & version catalog", "Build", true)
            )
            _uiState.update { it.copy(items = initial) }
        }
    }

    fun setActiveTab(tab: String) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDark = !it.isDark) }
    }

    fun toggleItem(id: String) {
        _uiState.update { state ->
            val updated = state.items.map {
                if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
            }
            state.copy(items = updated)
        }
    }

    fun addNewItem(title: String, subtitle: String) {
        val newItem = AppItem(
            id = System.currentTimeMillis().toString(),
            title = title,
            subtitle = subtitle,
            tag = "Custom",
            isCompleted = false
        )
        _uiState.update { state ->
            state.copy(items = listOf(newItem) + state.items)
        }
    }
}
