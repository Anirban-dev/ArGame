package com.example.androidapp.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidapp.viewmodel.ArMode
import com.example.androidapp.viewmodel.ArViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArScreen(
    viewModel: ArViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Simulated Live AR Camera & Surface Reticle
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

        // Top AR Overlay HUD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            ArHeaderBar(
                title = uiState.appTitle,
                trackingStatus = uiState.trackingStatus,
                onClearClick = { viewModel.clearAllObjects() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // AR Mode Selector Pills
            ArModeSelector(
                selectedMode = uiState.activeMode,
                onSelectMode = { viewModel.setArMode(it) }
            )
        }

        // Bottom Controls: 3D Catalog Carousel & Placement Action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            if (uiState.activeMode == ArMode.SPATIAL_MEASURE && uiState.calculatedDistanceMeters != null) {
                ArDistanceDisplayCard(meters = uiState.calculatedDistanceMeters!!)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.activeMode == ArMode.OBJECT_PLACEMENT) {
                ModelCatalogCarousel(
                    selectedModel = uiState.selectedModelName,
                    onSelectModel = { name, uri -> viewModel.setSelectedModel(name, uri) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Target Reticle Trigger Button
            Button(
                onClick = {
                    viewModel.placeObjectAt(0.0f, -0.3f, -1.0f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = "Place")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.activeMode == ArMode.SPATIAL_MEASURE) "Set Spatial Ruler Point" else "Place ${uiState.selectedModelName} in AR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ArCameraViewport(
    uiState: com.example.androidapp.viewmodel.ArUiState,
    onSurfaceTap: (Float, Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .clickable {
                onSurfaceTap(((-50..50).random() / 100f), ((-30..30).random() / 100f), -1.2f)
            }
    ) {
        // AR Center Target Reticle Grid
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.Center)
                .border(2.dp, Color(0xFF818CF8), CircleShape)
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFF6366F1), CircleShape)
            )
        }
    }
}

@Composable
fun ArHeaderBar(
    title: String,
    trackingStatus: String,
    onClearClick: () -> Unit
) {
    Surface(
        color = Color(0xCC0F172A),
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
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trackingStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.White)
            }
        }
    }
}

@Composable
fun ArModeSelector(
    selectedMode: ArMode,
    onSelectMode: (ArMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMode == ArMode.OBJECT_PLACEMENT,
            onClick = { onSelectMode(ArMode.OBJECT_PLACEMENT) },
            label = { Text("3D Models") },
            leadingIcon = { Icon(Icons.Default.ViewInAr, contentDescription = null) }
        )
        FilterChip(
            selected = selectedMode == ArMode.SPATIAL_MEASURE,
            onClick = { onSelectMode(ArMode.SPATIAL_MEASURE) },
            label = { Text("Ruler / Measure") },
            leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) }
        )
    }
}

@Composable
fun ArDistanceDisplayCard(meters: Float) {
    Surface(
        color = Color(0xEE1E293B),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Spatial Measurement", style = MaterialTheme.typography.labelMedium, color = Color(0xFF94A3B8))
                Text(
                    text = if (meters < 1.0f) "${(meters * 100).toInt()} cm" else String.format("%.2f m", meters),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
            }
            Icon(Icons.Default.Straighten, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun ModelCatalogCarousel(
    selectedModel: String,
    onSelectModel: (String, String) -> Unit
) {
    val models = listOf(
        Pair("Modern Chair", "models/chair.gltf"),
        Pair("Cyber Portal", "models/portal.gltf"),
        Pair("Spatial Beacon", "models/beacon.gltf"),
        Pair("3D Sculpture", "models/sculpture.gltf")
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 4.dp)
    ) {
        items(models) { (name, uri) ->
            val isSelected = selectedModel == name
            Surface(
                color = if (isSelected) Color(0xFF4F46E5) else Color(0xCC1E293B),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable { onSelectModel(name, uri) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
