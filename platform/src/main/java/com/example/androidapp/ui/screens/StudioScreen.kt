package com.example.androidapp.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.MalformedURLException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONException

private data class StudioSourceFile(
    val name: String,
    val path: String,
    val content: String
)

private val bundledSourcePaths = listOf(
    "platform/MainActivity.kt",
    "game-api/com/example/gameapi/GameContracts.kt",
    "platform/core/GameLauncherManager.kt",
    "platform/ui/screens/ArScreen.kt",
    "platform/models/PlatformModelGateway.kt",
    "platform/models/MediaPipeHandModelProvider.kt",
    "games/fruit-ninja/games/FruitNinjaPlugin.kt",
    "games/fruit-ninja/ui/screens/ARFruitNinjaGame.kt",
    "games/spatial-runner/games/SpatialRunnerPlugin.kt",
    "games/target-shooter/games/TargetShooterPlugin.kt",
    "games/room-editor/games/ArStudioGame.kt"
)

private val examplePrompts = listOf(
    "Add a score and timer overlay to the AR game",
    "Create a Jetpack Compose profile screen",
    "Design a settings screen for the AR game"
)

@Composable
fun StudioScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var endpoint by rememberSaveable {
        mutableStateOf("http://10.0.2.2:3000/api/generate-kotlin")
    }
    var prompt by rememberSaveable { mutableStateOf("") }
    var sourceFiles by remember { mutableStateOf<List<StudioSourceFile>>(emptyList()) }
    var generatedFiles by remember { mutableStateOf<List<StudioSourceFile>>(emptyList()) }
    var selectedPath by rememberSaveable { mutableStateOf("") }
    var isLoadingSources by remember { mutableStateOf(true) }
    var isGenerating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(context) {
        try {
            sourceFiles = withContext(Dispatchers.IO) {
                bundledSourcePaths.map { relativePath ->
                    val assetPath = "studio_sources/$relativePath"
                    val contents = context.assets.open(assetPath)
                        .bufferedReader()
                        .use { it.readText() }
                    StudioSourceFile(
                        name = relativePath.substringAfterLast('/'),
                        path = "ArGame-Project/$relativePath",
                        content = contents
                    )
                }
            }
            if (selectedPath.isBlank()) selectedPath = sourceFiles.firstOrNull()?.path.orEmpty()
        } catch (error: IOException) {
            errorMessage = "Could not load the bundled Android sources: ${error.message}"
        } finally {
            isLoadingSources = false
        }
    }

    val allFiles = generatedFiles + sourceFiles
    val selectedFile = allFiles.firstOrNull { it.path == selectedPath }
        ?: generatedFiles.firstOrNull()
        ?: sourceFiles.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StudioHeader()
            ProjectInfoCard()

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "AI Kotlin generation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Connect to the existing generation API. The server must have GEMINI_API_KEY configured.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = endpoint,
                        onValueChange = { endpoint = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Generation API URL") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Describe the Android feature") },
                        minLines = 3,
                        maxLines = 5
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        examplePrompts.forEach { example ->
                            FilterChip(
                                selected = false,
                                onClick = { prompt = example },
                                label = { Text(example, maxLines = 1) }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            errorMessage = null
                            statusMessage = null
                            isGenerating = true
                            scope.launch {
                                try {
                                    generatedFiles = requestGeneratedFiles(
                                        endpoint = endpoint,
                                        prompt = prompt
                                    )
                                    selectedPath = generatedFiles.firstOrNull()?.path.orEmpty()
                                    statusMessage =
                                        "Generated ${generatedFiles.size} Kotlin project file(s). Review and copy them below."
                                } catch (error: IOException) {
                                    errorMessage = error.message ?: "Unable to reach the generation server."
                                } catch (error: IllegalArgumentException) {
                                    errorMessage = error.message ?: "The generation server returned invalid data."
                                } finally {
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = !isGenerating && prompt.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(18.dp)
                                    .width(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Generating Kotlin…")
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Generate source")
                        }
                    }
                    Text(
                        "Generated source is shown here for review; it does not modify this installed app or build an APK.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            errorMessage?.let { message ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null)
                        Text(message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            statusMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            StudioSourceViewer(
                files = allFiles,
                selectedFile = selectedFile,
                isLoading = isLoadingSources,
                onSelect = { selectedPath = it },
                onCopy = { file ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(file.name, file.content)
                    )
                    Toast.makeText(context, "${file.name} copied", Toast.LENGTH_SHORT).show()
                }
            )

            BuildStatusCard()
        }
    }
}

@Composable
private fun StudioHeader() {
    Column(
        modifier = Modifier.padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Android Studio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Native project tools for Spatial Vision AR Studio",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectInfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Smartphone, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Native Android project", fontWeight = FontWeight.Bold)
            }
            Text("com.example.androidapp", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Kotlin • Jetpack Compose • ARCore • min SDK 24 • target SDK 34",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StudioSourceViewer(
    files: List<StudioSourceFile>,
    selectedFile: StudioSourceFile?,
    isLoading: Boolean,
    onSelect: (String) -> Unit,
    onCopy: (StudioSourceFile) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Project source", fontWeight = FontWeight.Bold)
                }
                selectedFile?.let { file ->
                    Button(
                        onClick = { onCopy(file) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Copy")
                    }
                }
            }
            if (files.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    files.forEach { file ->
                        FilterChip(
                            selected = file.path == selectedFile?.path,
                            onClick = { onSelect(file.path) },
                            label = { Text(file.name, maxLines = 1) }
                        )
                    }
                }
            }
            when {
                isLoading -> CircularProgressIndicator()
                selectedFile == null -> Text(
                    "No source file is available.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {
                    Text(
                        selectedFile.path,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    selectedFile.content,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildStatusCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Build with Gradle", fontWeight = FontWeight.Bold)
            }
            Text(
                "Build the installable debug APK from the project root with:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "gradlew.bat :app:assembleDebug",
                    modifier = Modifier.padding(10.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
            Text(
                "The APK is produced by the local Gradle build, not by AI source generation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private suspend fun requestGeneratedFiles(
    endpoint: String,
    prompt: String
): List<StudioSourceFile> = withContext(Dispatchers.IO) {
    require(prompt.isNotBlank()) { "Enter a feature description first." }
    val parsedEndpoint = try {
        URL(endpoint.trim())
    } catch (error: MalformedURLException) {
        throw IllegalArgumentException("Enter a valid API URL.", error)
    }
    require(parsedEndpoint.protocol == "http" || parsedEndpoint.protocol == "https") {
        "The API URL must use HTTP or HTTPS."
    }

    val connection = parsedEndpoint.openConnection() as? HttpURLConnection
        ?: throw IOException("The API URL must use HTTP or HTTPS.")
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 15_000
        connection.readTimeout = 90_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.use { output ->
            output.write(
                JSONObject().put("prompt", prompt.trim()).toString().toByteArray(Charsets.UTF_8)
            )
        }

        val statusCode = connection.responseCode
        val responseStream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        } ?: throw IOException("Generation server returned HTTP $statusCode with no response body.")
        val responseText = responseStream.bufferedReader().use { it.readText() }
        val response = try {
            JSONObject(responseText)
        } catch (error: JSONException) {
            throw IOException("Generation server returned invalid JSON (HTTP $statusCode).", error)
        }
        if (statusCode !in 200..299) {
            throw IOException(
                response.optString("error").ifBlank {
                    "Generation server returned HTTP $statusCode."
                }
            )
        }
        if (!response.optBoolean("success")) {
            throw IOException(
                response.optString("error").ifBlank {
                    "The generation server did not return a successful result."
                }
            )
        }

        val data = response.optJSONObject("data")
            ?: throw IOException("Generation server response is missing project data.")
        val files = data.optJSONArray("files")
            ?: throw IOException("Generation server response is missing generated files.")
        if (files.length() == 0) throw IOException("The generation server returned no source files.")

        buildList {
            for (index in 0 until files.length()) {
                val file = files.optJSONObject(index)
                    ?: throw IOException("Generated file $index is malformed.")
                val path = file.optString("path").takeIf { it.isNotBlank() }
                    ?: throw IOException("Generated file $index has no path.")
                val content = file.optString("content").takeIf { it.isNotBlank() }
                    ?: throw IOException("Generated file $path has no source content.")
                add(
                    StudioSourceFile(
                        name = path.substringAfterLast('/'),
                        path = path,
                        content = content
                    )
                )
            }
        }
    } finally {
        connection.disconnect()
    }
}
