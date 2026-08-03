import { AndroidProject } from "../types";

export const defaultAndroidProject: AndroidProject = {
  appName: "TaskPulse Pro",
  appDescription: "Modern Android Task & Productivity App built with Kotlin and Jetpack Compose Material 3.",
  packageName: "com.example.taskpulse",
  version: "1.0.0",
  targetSdk: 34,
  minSdk: 24,
  previewState: {
    title: "TaskPulse Workspaces",
    subtitle: "Focus on what matters today",
    theme: "light",
    primaryColor: "#4f46e5",
    screenType: "task",
    activeTab: "home",
    navigation: [
      { id: "home", label: "Tasks", icon: "CheckSquare", badgeCount: 4 },
      { id: "projects", label: "Projects", icon: "Folder" },
      { id: "analytics", label: "Metrics", icon: "BarChart3" },
      { id: "settings", label: "Settings", icon: "Settings" }
    ],
    cards: [
      {
        id: "1",
        title: "Design System Migration to Material 3",
        subtitle: "Update color palette, typography tokens, and surface specs",
        badge: "High Priority",
        value: "2h left",
        tag: "Design",
        completed: false,
        time: "10:30 AM",
        category: "UI/UX"
      },
      {
        id: "2",
        title: "Kotlin Coroutines & Flow Optimization",
        subtitle: "Refactor background repository sync using StateFlow & SupervisorJob",
        badge: "In Progress",
        value: "4h left",
        tag: "Backend",
        completed: false,
        time: "01:15 PM",
        category: "Architecture"
      },
      {
        id: "3",
        title: "Unit Test Jetpack ViewModel & State",
        subtitle: "Achieve 90% code coverage on Kotlin domain use-cases",
        badge: "Review",
        value: "Today",
        tag: "Testing",
        completed: true,
        time: "03:45 PM",
        category: "Quality"
      },
      {
        id: "4",
        title: "Prepare APK & App Bundle for Google Play",
        subtitle: "Generate signed release AAB bundle with ProGuard mapping",
        badge: "Pending",
        value: "Tomorrow",
        tag: "Release",
        completed: false,
        time: "05:00 PM",
        category: "DevOps"
      }
    ],
    actionButtonText: "New Kotlin Task"
  },
  files: [
    {
      id: "main-activity",
      name: "MainActivity.kt",
      path: "app/src/main/java/com/example/taskpulse/MainActivity.kt",
      language: "kotlin",
      isMain: true,
      content: `package com.example.taskpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.taskpulse.ui.navigation.TaskPulseAppNav
import com.example.taskpulse.ui.theme.TaskPulseTheme

/**
 * Main Entry Point for TaskPulse Android Application.
 * Built with Jetpack Compose, Material 3, and Kotlin Coroutines.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskPulseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskPulseAppNav()
                }
            }
        }
    }
}`
    },
    {
      id: "main-screen",
      name: "TaskScreen.kt",
      path: "app/src/main/java/com/example/taskpulse/ui/screens/TaskScreen.kt",
      language: "kotlin",
      content: `package com.example.taskpulse.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskpulse.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onAddTaskClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TaskPulse Workspaces",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Focus on what matters today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTaskClick,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("New Task") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                TaskSummaryHeader(
                    completedCount = uiState.tasks.count { it.isCompleted },
                    totalCount = uiState.tasks.size
                )
            }

            items(uiState.tasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task.id) }
                )
            }
        }
    }
}

@Composable
fun TaskSummaryHeader(completedCount: Int, totalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "$completedCount of $totalCount completed",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            LinearProgressIndicator(
                progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                modifier = Modifier
                    .width(100.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}`
    },
    {
      id: "view-model",
      name: "TaskViewModel.kt",
      path: "app/src/main/java/com/example/taskpulse/viewmodel/TaskViewModel.kt",
      language: "kotlin",
      content: `package com.example.taskpulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskModel(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: String,
    val dueDate: String
)

data class TaskUiState(
    val tasks: List<TaskModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TaskViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val initialTasks = listOf(
                TaskModel("1", "Design System Migration to Material 3", "Update color tokens", false, "High", "2h left"),
                TaskModel("2", "Kotlin Coroutines & Flow Optimization", "Refactor background repo", false, "Medium", "4h left"),
                TaskModel("3", "Unit Test Jetpack ViewModel", "Achieve 90% coverage", true, "Low", "Done")
            )
            _uiState.update { it.copy(tasks = initialTasks, isLoading = false) }
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        _uiState.update { currentState ->
            val updated = currentState.tasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
            currentState.copy(tasks = updated)
        }
    }

    fun addTask(title: String, description: String) {
        val newTask = TaskModel(
            id = System.currentTimeMillis().toString(),
            title = title,
            description = description,
            isCompleted = false,
            priority = "Normal",
            dueDate = "Today"
        )
        _uiState.update { currentState ->
            currentState.copy(tasks = listOf(newTask) + currentState.tasks)
        }
    }
}`
    },
    {
      id: "theme-kt",
      name: "Theme.kt",
      path: "app/src/main/java/com/example/taskpulse/ui/theme/Theme.kt",
      language: "kotlin",
      content: `package com.example.taskpulse.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    secondary = Color(0xFFC7D2FE),
    tertiary = Color(0xFFF472B6),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),
    secondary = Color(0xFF6366F1),
    tertiary = Color(0xFFEC4899),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81)
)

@Composable
fun TaskPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}`
    },
    {
      id: "build-gradle",
      name: "build.gradle.kts",
      path: "app/build.gradle.kts",
      language: "kotlin",
      content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.taskpulse"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskpulse"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}`
    },
    {
      id: "manifest-xml",
      name: "AndroidManifest.xml",
      path: "app/src/main/AndroidManifest.xml",
      language: "xml",
      content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="TaskPulse Pro"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TaskPulse"
        tools:targetApi="31">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="TaskPulse Pro"
            android:theme="@style/Theme.TaskPulse">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>`
    }
  ]
};
