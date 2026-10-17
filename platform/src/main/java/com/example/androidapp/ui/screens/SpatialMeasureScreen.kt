package com.example.androidapp.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlin.math.sqrt

private data class MeasurementPoint(
    val x: Float,
    val y: Float,
    val z: Float
)

@Composable
fun SpatialMeasureScreen(modifier: Modifier = Modifier) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes {}
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 100_000.0f
    }
    val points = remember { mutableStateListOf<MeasurementPoint>() }
    var latestFrame by remember { mutableStateOf<Frame?>(null) }
    var status by remember { mutableStateOf("Move your phone slowly to find a surface.") }

    fun clearPoints() {
        points.clear()
        status = "Measurement cleared. Tap a detected surface to place point A."
    }

    DisposableEffect(Unit) {
        onDispose {
            latestFrame = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            childNodes = childNodes,
            mainLightNode = mainLightNode,
            planeRenderer = true,
            onSessionUpdated = { _, frame ->
                latestFrame = frame
                if (frame.camera.trackingState == TrackingState.TRACKING &&
                    status.startsWith("Move your phone")
                ) {
                    status = "Surface tracking. Tap once for A, then again for B."
                }
            },
            sessionConfiguration = { _, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.focusMode = Config.FocusMode.AUTO
            },
            onTouchEvent = { event, _ ->
                if (event.action != MotionEvent.ACTION_UP) {
                    false
                } else {
                    val frame = latestFrame
                    val hit = frame?.hitTest(event.x, event.y)
                        ?.firstOrNull { result ->
                            val plane = result.trackable as? Plane
                            plane != null &&
                                plane.trackingState == TrackingState.TRACKING &&
                                plane.isPoseInPolygon(result.hitPose)
                        }

                    if (hit == null) {
                        status = "No tracked surface at that point. Try tapping a visible plane."
                    } else {
                        val pose = hit.hitPose
                        if (points.size == 2) points.clear()
                        points.add(MeasurementPoint(pose.tx(), pose.ty(), pose.tz()))
                        status = if (points.size == 1) {
                            "Point A placed. Tap the second point on the surface."
                        } else {
                            "Distance measured between the two surface points."
                        }
                    }
                    true
                }
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xE610172A)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Spatial measuring tool",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )

                    if (points.isNotEmpty()) {
                        Text(
                            "Point A: ${points[0].formatted()}",
                            color = Color(0xFFA5B4FC),
                            fontSize = 12.sp
                        )
                    }
                    if (points.size == 2) {
                        val distance = distanceMeters(points[0], points[1])
                        Text(
                            "Point B: ${points[1].formatted()}",
                            color = Color(0xFFA5B4FC),
                            fontSize = 12.sp
                        )
                        Text(
                            "Distance: ${"%.1f".format(distance * 100)} cm",
                            color = Color(0xFF34D399),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Button(
                        onClick = ::clearPoints,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F46E5)
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 14.dp,
                            vertical = 8.dp
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Text("Reset points", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            color = Color(0xD910172A),
            shape = RoundedCornerShape(50)
        ) {
            RowLabel()
        }
    }
}

@Composable
private fun RowLabel() {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.MyLocation,
            contentDescription = null,
            tint = Color(0xFF818CF8)
        )
        Text(
            "ARCore plane tracking",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun MeasurementPoint.formatted(): String =
    "(${String.format("%.2f", x)}, ${String.format("%.2f", y)}, ${String.format("%.2f", z)}) m"

private fun distanceMeters(first: MeasurementPoint, second: MeasurementPoint): Float {
    val dx = second.x - first.x
    val dy = second.y - first.y
    val dz = second.z - first.z
    return sqrt(dx * dx + dy * dy + dz * dz)
}
