package com.example.androidapp.ui.screens

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.Surface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.gameapi.GameAssetGateway
import com.example.gameapi.GameModelGateway
import com.example.gameapi.GameModelSession
import com.example.gameapi.ModelIds
import com.example.gameapi.ModelInput
import com.example.gameapi.ModelOutput
import com.google.ar.core.Config
import com.google.ar.core.Coordinates2d
import io.github.sceneview.ar.ARScene
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.isActive
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

// ---- Tuning knobs -----------------------------------------------------------

/** Finger must move faster than this (px/second) to cut fruit. Raise = harder. */
private const val MIN_SLICE_SPEED_PX_PER_SEC = 650f

/** Longest side of the image given to MediaPipe. Lower = faster, higher = sees farther. */
private const val MAX_TRACKING_DIMENSION = 480

/** How long the dot stays after the hand is lost. */
private const val HAND_LOST_GRACE_MS = 300L

/** Max time the dot is extrapolated ahead of the last detection. */
private const val MAX_PREDICTION_MS = 120f

/** How quickly the drawn dot chases the predicted position. Higher = snappier. */
private const val DOT_SMOOTHING_RATE = 35f

/** Safety cap on estimated finger speed (px/ms). */
private const val MAX_FINGER_SPEED_PX_PER_MS = 6f

// -----------------------------------------------------------------------------

private data class FlyingFruit(
    val id: Int,
    val emoji: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val bomb: Boolean,
    val size: Float,
    val previousX: Float = x,
    val previousY: Float = y
)

private data class JuiceEffect(
    val id: Int,
    val x: Float,
    val y: Float,
    val emoji: String,
    val age: Float = 0f
)

/** Latest detected fingertip, written by the camera pipeline, read every UI frame. */
private class FingerState {
    var hasTarget = false
    var targetX = 0f
    var targetY = 0f
    var velocityX = 0f // px per millisecond
    var velocityY = 0f
    var captureMs = 0L // time the camera frame was captured
    var lastSeenMs = 0L // time the detection arrived
    var speed = 0f // px per second along the drawn path
}

private fun backCameraSensorOrientation(context: Context): Int {
    return try {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        manager.cameraIdList.firstNotNullOfOrNull { id ->
            val c = manager.getCameraCharacteristics(id)
            if (c.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_BACK
            ) c.get(CameraCharacteristics.SENSOR_ORIENTATION) else null
        } ?: 90
    } catch (_: Exception) {
        90
    }
}

private fun displayRotationDegrees(rotation: Int): Int = when (rotation) {
    Surface.ROTATION_90 -> 90
    Surface.ROTATION_180 -> 180
    Surface.ROTATION_270 -> 270
    else -> 0
}

// Maps a point in the clockwise-rotated bitmap back to the original
// (unrotated) camera image, in normalized 0..1 coordinates.
private fun rotatedToImageNormalized(x: Float, y: Float, rotation: Int): Offset =
    when (rotation) {
        90 -> Offset(y, 1f - x)
        180 -> Offset(1f - x, 1f - y)
        270 -> Offset(1f - y, x)
        else -> Offset(x, y)
    }

@Composable
private fun DebugButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 11.sp)
    }
}

@Composable
fun ARFruitNinjaGame(
    modelGateway: GameModelGateway,
    assetGateway: GameAssetGateway,
    onSlice: (Int) -> Unit,
    onExit: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val handModelSession = remember(modelGateway) {
        modelGateway.openSession(ModelIds.HAND_LANDMARKER)
    }
    val appleModel = remember(assetGateway) { assetGateway.resolveModel("fruit.apple") }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val mainHandler = remember {
        Handler(Looper.getMainLooper())
    }

    val isProcessingFrame = remember {
        AtomicBoolean(false)
    }

    val fingerState = remember { FingerState() }

    var lastCameraFrameTime by remember {
        mutableLongStateOf(0L)
    }

    // Position of the drawn dot (updated every UI frame, ~60 fps).
    var fingerPoint by remember {
        mutableStateOf<Offset?>(null)
    }

    var handDetected by remember {
        mutableStateOf(false)
    }

    // True while the finger is moving fast enough to cut fruit.
    var bladeActive by remember {
        mutableStateOf(false)
    }

    val view = LocalView.current
    val sensorOrientation = remember { backCameraSensorOrientation(context) }
    var viewSize by remember { mutableStateOf(IntSize.Zero) }

    // DEBUG tools. Set showDebug to false (or delete the debug UI) for release.
    var showDebug by remember { mutableStateOf(true) }
    var rotationOverride by remember { mutableIntStateOf(-1) } // -1 = automatic
    var calX by remember { mutableFloatStateOf(0f) }
    var calY by remember { mutableFloatStateOf(0f) }
    var debugText by remember { mutableStateOf("waiting for hand...") }
    var missCount by remember { mutableIntStateOf(0) }

    DisposableEffect(handModelSession, cameraExecutor) {
        cameraExecutor.execute {
            handModelSession.start()
        }

        onDispose {
            cameraExecutor.submit {
                handModelSession.close()
            }.get()
            cameraExecutor.shutdown()
        }
    }
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes {}
    val mainLightNode = rememberMainLightNode(engine) {
        intensity = 100_000.0f
    }

    val fruits = remember { mutableStateListOf<FlyingFruit>() }
    val effects = remember { mutableStateListOf<JuiceEffect>() }
    val trail = remember { mutableStateListOf<Offset>() }

    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var combo by remember { mutableIntStateOf(0) }
    var bestCombo by remember { mutableIntStateOf(0) }
    var secondsLeft by remember { mutableIntStateOf(60) }
    var gameOver by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(0) }
    var status by remember {
        mutableStateOf("Swipe fruit. Avoid bombs!")
    }

    LaunchedEffect(assetGateway, modelLoader) {
        childNodes.add(assetGateway.loadModelNode("fruit.apple", modelLoader))
    }

    // Fruit sizes in real pixels so the hitbox matches the drawn emoji.
    val density = LocalDensity.current
    val fruitPx = with(density) { 44.sp.toPx() }
    val bombPx = with(density) { 48.sp.toPx() }

    fun distanceToSegment(
        point: Offset,
        start: Offset,
        end: Offset
    ): Float {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val lengthSquared = dx * dx + dy * dy

        if (lengthSquared < 0.001f) {
            return (point - start).getDistance()
        }

        val t = (
                ((point.x - start.x) * dx +
                        (point.y - start.y) * dy) / lengthSquared
                ).coerceIn(0f, 1f)

        val closest = Offset(
            start.x + t * dx,
            start.y + t * dy
        )

        return (point - closest).getDistance()
    }

    // Accurate segment-to-segment distance, including crossings.
    fun segmentsIntersectWithinRadius(
        swipeStart: Offset,
        swipeEnd: Offset,
        fruitStart: Offset,
        fruitEnd: Offset,
        radius: Float
    ): Boolean {
        val sx = swipeEnd.x - swipeStart.x
        val sy = swipeEnd.y - swipeStart.y
        val fx = fruitEnd.x - fruitStart.x
        val fy = fruitEnd.y - fruitStart.y
        val denominator = sx * fy - sy * fx

        if (abs(denominator) > 0.0001f) {
            val dx = fruitStart.x - swipeStart.x
            val dy = fruitStart.y - swipeStart.y
            val t = (dx * fy - dy * fx) / denominator
            val u = (dx * sy - dy * sx) / denominator

            if (t in 0f..1f && u in 0f..1f) {
                return true
            }
        }

        val d1 = distanceToSegment(fruitStart, swipeStart, swipeEnd)
        val d2 = distanceToSegment(fruitEnd, swipeStart, swipeEnd)
        val d3 = distanceToSegment(swipeStart, fruitStart, fruitEnd)
        val d4 = distanceToSegment(swipeEnd, fruitStart, fruitEnd)

        return minOf(d1, d2, d3, d4) <= radius
    }

    fun sliceAlong(start: Offset, end: Offset) {
        if (gameOver) return

        val hits = fruits.filter { fruit ->
            segmentsIntersectWithinRadius(
                swipeStart = start,
                swipeEnd = end,
                fruitStart = Offset(fruit.previousX, fruit.previousY),
                fruitEnd = Offset(fruit.x, fruit.y),
                radius = fruit.size * 0.8f
            )
        }

        for (fruit in hits) {
            if (!fruits.removeAll { it.id == fruit.id }) continue

            if (fruit.bomb) {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

                lives = (lives - 1).coerceAtLeast(0)
                combo = 0
                status = "BOOM! Bomb hit!"

                effects.add(
                    JuiceEffect(
                        id = nextId++,
                        x = fruit.x,
                        y = fruit.y,
                        emoji = "💥"
                    )
                )

                if (lives == 0) {
                    gameOver = true
                    status = "GAME OVER!"
                }
            } else {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                combo++
                bestCombo = max(bestCombo, combo)

                val points = if (combo >= 3) 20 else 10
                score += points
                onSlice(points)
                status = if (combo >= 3) {
                    "COMBO x$combo! +$points"
                } else {
                    "NICE SLICE! +$points"
                }

                repeat(10) {
                    val angle = Random.nextFloat() * 6.28318f
                    val distance = Random.nextFloat() * 55f

                    effects.add(
                        JuiceEffect(
                            id = nextId++,
                            x = fruit.x + kotlin.math.cos(angle) * distance,
                            y = fruit.y + kotlin.math.sin(angle) * distance,
                            emoji = listOf("💧", "✨", "🟠", "💦").random()
                        )
                    )
                }

                effects.add(
                    JuiceEffect(
                        id = nextId++,
                        x = fruit.x,
                        y = fruit.y - 35f,
                        emoji = "+$points"
                    )
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { viewSize = it }
    ) {
        val widthPx = viewSize.width.toFloat()
        val heightPx = viewSize.height.toFloat()

        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            childNodes = childNodes,
            mainLightNode = mainLightNode,
            planeRenderer = false,

            onSessionUpdated = { _, frame ->
                val now = SystemClock.uptimeMillis()

                if (
                    now - lastCameraFrameTime >= 16L &&
                    isProcessingFrame.compareAndSet(false, true)
                ) {
                    lastCameraFrameTime = now
                    var cameraImage: Image? = null

                    try {
                        // Frame is only valid inside this callback, so capture the
                        // image->view mapping now. It's affine, so 3 points define it:
                        // (0,0), (1,0), (0,1) -> dst = [x0,y0, x1,y1, x2,y2]
                        val src = floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f)
                        val dst = FloatArray(6)
                        frame.transformCoordinates2d(
                            Coordinates2d.IMAGE_NORMALIZED, src,
                            Coordinates2d.VIEW, dst
                        )

                        val displayDeg = displayRotationDegrees(
                            view.display?.rotation ?: Surface.ROTATION_0
                        )
                        val autoRotation = (sensorOrientation - displayDeg + 360) % 360
                        val rotation =
                            if (rotationOverride >= 0) rotationOverride else autoRotation

                        val image: Image = frame.acquireCameraImage()
                        cameraImage = image
                        val imgW = image.width
                        val imgH = image.height

                        cameraExecutor.execute {
                            try {
                                val modelOutput = handModelSession.infer(
                                    ModelInput.CameraFrame(
                                        image = image,
                                        rotationDegrees = rotation,
                                        maxDimension = MAX_TRACKING_DIMENSION
                                    )
                                ) as? ModelOutput.HandLandmarks
                                    ?: error("Hand model returned an unsupported result.")
                                    val pose = modelOutput.frame
                                    val latencyMs = SystemClock.uptimeMillis() - now

                                    mainHandler.post {
                                        handDetected = pose.isHandDetected

                                        if (pose.isHandDetected) {
                                            val nowMs = SystemClock.uptimeMillis()

                                            // Landmark is normalized in the rotated bitmap.
                                            // Undo the rotation, then let ARCore map the
                                            // camera image to view pixels.
                                            val uv = rotatedToImageNormalized(
                                                pose.indexTipScreenX,
                                                pose.indexTipScreenY,
                                                rotation
                                            )

                                            val screenX = dst[0] +
                                                    uv.x * (dst[2] - dst[0]) +
                                                    uv.y * (dst[4] - dst[0]) + calX
                                            val screenY = dst[1] +
                                                    uv.x * (dst[3] - dst[1]) +
                                                    uv.y * (dst[5] - dst[1]) + calY

                                            // Estimate finger velocity from the last detection.
                                            val dtMs = (now - fingerState.captureMs)
                                                .coerceAtLeast(1L)
                                            if (fingerState.hasTarget && dtMs < 250L) {
                                                val ivx = ((screenX - fingerState.targetX) / dtMs)
                                                    .coerceIn(
                                                        -MAX_FINGER_SPEED_PX_PER_MS,
                                                        MAX_FINGER_SPEED_PX_PER_MS
                                                    )
                                                val ivy = ((screenY - fingerState.targetY) / dtMs)
                                                    .coerceIn(
                                                        -MAX_FINGER_SPEED_PX_PER_MS,
                                                        MAX_FINGER_SPEED_PX_PER_MS
                                                    )
                                                fingerState.velocityX =
                                                    fingerState.velocityX * 0.5f + ivx * 0.5f
                                                fingerState.velocityY =
                                                    fingerState.velocityY * 0.5f + ivy * 0.5f
                                            } else {
                                                fingerState.velocityX = 0f
                                                fingerState.velocityY = 0f
                                            }

                                            fingerState.targetX = screenX
                                            fingerState.targetY = screenY
                                            fingerState.captureMs = now
                                            fingerState.lastSeenMs = nowMs
                                            fingerState.hasTarget = true

                                            val rawX = "%.2f".format(pose.indexTipScreenX)
                                            val rawY = "%.2f".format(pose.indexTipScreenY)
                                            debugText =
                                                "rot=$rotation auto=$autoRotation " +
                                                        "sensor=$sensorOrientation disp=$displayDeg\n" +
                                                        "cam=${imgW}x$imgH " +
                                                        "view=${widthPx.toInt()}x${heightPx.toInt()}\n" +
                                                        "raw=$rawX,$rawY -> " +
                                                        "${screenX.toInt()},${screenY.toInt()}\n" +
                                                        "lag=${latencyMs}ms misses=$missCount " +
                                                        "ai=${modelOutput.runtimeInfo}\n" +
                                                        "asset=${appleModel.assetLocation}\n" +
                                                        "cal=${calX.toInt()},${calY.toInt()} " +
                                                        "dst=" + dst.joinToString(",") {
                                                    it.toInt().toString()
                                                }
                                        } else {
                                            missCount++
                                            debugText =
                                                "NO HAND  lag=${latencyMs}ms " +
                                                        "misses=$missCount " +
                                                        "ai=${handModelSession.runtimeInfo}\n" +
                                                        "asset=${appleModel.assetLocation}\n" +
                                                        "cam=${imgW}x$imgH rot=$rotation"
                                        }
                                    }
                            } catch (e: Exception) {
                                android.util.Log.e("FingerTracking", "Frame failed", e)
                                mainHandler.post {
                                    debugText =
                                        "ERROR: ${e.javaClass.simpleName}: ${e.message}"
                                }
                            } finally {
                                image.close()
                                isProcessingFrame.set(false)
                            }
                        }
                    } catch (_: Exception) {
                        cameraImage?.close()
                        isProcessingFrame.set(false)
                    }
                }
            },

            sessionConfiguration = { _, config ->
                // Autofocus keeps a hand held close to the camera sharp.
                config.focusMode = Config.FocusMode.AUTO
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                // No 3D models or planes are used, so skip that per-frame work.
                config.lightEstimationMode = Config.LightEstimationMode.DISABLED
                config.planeFindingMode = Config.PlaneFindingMode.DISABLED
                config.depthMode = Config.DepthMode.DISABLED
            }
        )

        // Finger loop (~60 fps): predicts where the finger is NOW, moves the dot
        // smoothly toward it, slices along the path, and fades the trail.
        LaunchedEffect(Unit) {
            var lastNanos = 0L

            while (isActive) {
                withFrameNanos { frameNanos ->
                    if (lastNanos == 0L) lastNanos = frameNanos
                    val dt = ((frameNanos - lastNanos) / 1_000_000_000f)
                        .coerceIn(0.001f, 0.05f)
                    lastNanos = frameNanos

                    val nowMs = SystemClock.uptimeMillis()
                    var addedTrailPoint = false

                    if (!fingerState.hasTarget ||
                        nowMs - fingerState.lastSeenMs > HAND_LOST_GRACE_MS
                    ) {
                        fingerState.hasTarget = false
                        fingerState.speed = 0f
                        if (fingerPoint != null) fingerPoint = null
                        if (bladeActive) bladeActive = false
                    } else {
                        // Dead-reckon from the capture time to "now" (+1 frame).
                        val age = ((nowMs - fingerState.captureMs).toFloat() + 16f)
                            .coerceIn(0f, MAX_PREDICTION_MS)
                        val targetX = fingerState.targetX + fingerState.velocityX * age
                        val targetY = fingerState.targetY + fingerState.velocityY * age

                        val previous = fingerPoint
                        val next = if (previous == null) {
                            Offset(targetX, targetY)
                        } else {
                            val a = 1f - exp(-dt * DOT_SMOOTHING_RATE)
                            Offset(
                                previous.x + (targetX - previous.x) * a,
                                previous.y + (targetY - previous.y) * a
                            )
                        }

                        if (previous != null) {
                            val distance = (next - previous).getDistance()
                            val instantSpeed = distance / dt
                            fingerState.speed =
                                fingerState.speed * 0.6f + instantSpeed * 0.4f

                            val fast = fingerState.speed >= MIN_SLICE_SPEED_PX_PER_SEC
                            if (bladeActive != fast) bladeActive = fast

                            // Only a real swipe cuts fruit; slow drifting does nothing.
                            if (fast && distance > 0.5f) {
                                sliceAlong(previous, next)
                                trail.add(next)
                                addedTrailPoint = true
                            }
                        }

                        fingerPoint = next
                    }

                    while (trail.size > 20) {
                        trail.removeAt(0)
                    }
                    if (!addedTrailPoint && trail.isNotEmpty()) {
                        trail.removeAt(0)
                    }
                }
            }
        }

        // Game loop: spawning, movement, timer, and particles.
        LaunchedEffect(gameOver, widthPx, heightPx) {
            if (gameOver || widthPx <= 1f || heightPx <= 1f) {
                return@LaunchedEffect
            }

            var lastFrame = 0L
            var spawnTimer = 0f
            var secondTimer = 0f

            while (isActive && !gameOver) {
                withFrameNanos { frameTime ->
                    if (lastFrame == 0L) lastFrame = frameTime

                    val dt = (
                            (frameTime - lastFrame) / 1_000_000_000f
                            ).coerceIn(0f, 0.04f)

                    lastFrame = frameTime
                    spawnTimer += dt
                    secondTimer += dt

                    if (spawnTimer >= 0.75f) {
                        spawnTimer = 0f

                        repeat(Random.nextInt(2, 4)) {
                            val bomb = Random.nextFloat() < 0.15f

                            fruits.add(
                                FlyingFruit(
                                    id = nextId++,
                                    emoji = if (bomb) "💣" else listOf(
                                        "🍎", "🍊", "🍉",
                                        "🍋", "🍑", "🍐"
                                    ).random(),
                                    x = widthPx *
                                            (0.12f + Random.nextFloat() * 0.76f),
                                    y = heightPx + 45f,
                                    vx = Random.nextFloat() * 280f - 140f,
                                    vy = -(1350f + Random.nextFloat() * 200f),
                                    bomb = bomb,
                                    size = if (bomb) bombPx else fruitPx
                                )
                            )
                        }
                    }

                    if (secondTimer >= 1f) {
                        secondTimer -= 1f
                        secondsLeft = max(0, secondsLeft - 1)

                        if (secondsLeft == 0) {
                            gameOver = true
                            status = "TIME UP!"
                        }
                    }

                    for (i in fruits.indices.reversed()) {
                        val fruit = fruits[i]
                        val moved = fruit.copy(
                            x = fruit.x + fruit.vx * dt,
                            y = fruit.y + fruit.vy * dt,
                            vy = fruit.vy + 650f * dt,
                            previousX = fruit.x,
                            previousY = fruit.y
                        )

                        if (
                            moved.y > heightPx + 100f ||
                            moved.x < -100f ||
                            moved.x > widthPx + 100f
                        ) {
                            fruits.removeAt(i)
                            if (!fruit.bomb) combo = 0
                        } else {
                            fruits[i] = moved
                        }
                    }

                    for (i in effects.indices.reversed()) {
                        val moved = effects[i].copy(
                            age = effects[i].age + dt
                        )

                        if (moved.age >= 0.75f) {
                            effects.removeAt(i)
                        } else {
                            effects[i] = moved
                        }
                    }
                }
            }
        }

        // Glowing trail follows the player's finger.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
        ) {
            for (i in 1 until trail.size) {
                val alpha = i.toFloat() / trail.size.coerceAtLeast(1)

                drawLine(
                    color = Color.Cyan.copy(alpha = alpha),
                    start = trail[i - 1],
                    end = trail[i],
                    strokeWidth = 5f + 8f * alpha,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = trail[i - 1],
                    end = trail[i],
                    strokeWidth = 2f,
                    cap = StrokeCap.Round
                )
            }

            // Dot colour: white = slicing fast, green = hand tracked, red = hand lost.
            fingerPoint?.let { point ->
                val dotColor = when {
                    bladeActive -> Color.White
                    handDetected -> Color.Green
                    else -> Color.Red
                }
                drawCircle(
                    color = dotColor.copy(alpha = 0.35f),
                    radius = if (bladeActive) 34f else 26f,
                    center = point
                )
                drawCircle(
                    color = dotColor,
                    radius = if (bladeActive) 20f else 16f,
                    center = point
                )
            }
        }

        fruits.forEach { fruit ->
            Text(
                text = fruit.emoji,
                fontSize = if (fruit.bomb) 48.sp else 44.sp,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (fruit.x - fruit.size / 2f).roundToInt(),
                            (fruit.y - fruit.size / 2f).roundToInt()
                        )
                    }
                    .zIndex(3f)
            )
        }

        effects.forEach { effect ->
            Text(
                text = effect.emoji,
                color = if (effect.emoji.startsWith("+")) {
                    Color.Yellow
                } else {
                    Color.White
                },
                fontSize = if (effect.emoji.startsWith("+")) 24.sp else 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (effect.x - 20f).roundToInt(),
                            (effect.y - effect.age * 100f).roundToInt()
                        )
                    }
                    .zIndex(4f)
            )
        }

        // Touch slicing (fallback when the hand isn't tracked).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(5f)
                .pointerInput(gameOver) {
                    var previous: Offset? = null

                    detectDragGestures(
                        onDragStart = { start ->
                            previous = start
                            trail.clear()
                            trail.add(start)
                        },
                        onDrag = { change, _ ->
                            val current = change.position
                            val old = previous

                            if (old != null) {
                                val dx = current.x - old.x
                                val dy = current.y - old.y
                                val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                                val steps = (distance / 8f)
                                    .toInt()
                                    .coerceIn(1, 40)

                                // Smooth visual trail.
                                for (i in 1..steps) {
                                    val t = i.toFloat() / steps
                                    trail.add(
                                        Offset(
                                            old.x + dx * t,
                                            old.y + dy * t
                                        )
                                    )
                                }

                                // Collision test over the full swipe.
                                sliceAlong(old, current)
                            } else {
                                trail.add(current)
                            }

                            while (trail.size > 20) {
                                trail.removeAt(0)
                            }

                            previous = current
                            change.consume()
                        },
                        onDragEnd = {
                            previous = null
                        },
                        onDragCancel = {
                            previous = null
                            trail.clear()
                        }
                    )
                }
        )

        // Heads-up display.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .zIndex(6f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF252B45)
                    )
                ) {
                    Text("BACK", color = Color.White)
                }

                Column(
                    modifier = Modifier
                        .background(
                            Color(0xDD101526),
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SCORE", color = Color.LightGray, fontSize = 12.sp)
                    Text(
                        "$score",
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("TIME  $secondsLeft", color = Color.Yellow)
                    Text("❤️ $lives", color = Color.White)
                    Text("COMBO x$combo", color = Color.Cyan)
                    Text(
                        "BEST x$bestCombo",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // DEBUG tools: remove (or set showDebug = false) for release.
                if (showDebug) {
                    Text(
                        text = debugText,
                        color = Color.Green,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(Color(0xAA000000), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DebugButton("ROT: " + if (rotationOverride < 0) "AUTO" else "$rotationOverride") {
                            rotationOverride = when (rotationOverride) {
                                -1 -> 0
                                0 -> 90
                                90 -> 180
                                180 -> 270
                                else -> -1
                            }
                        }
                        DebugButton("DBG OFF") { showDebug = false }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Nudge the dot onto your fingertip; cal= in the debug box shows the offset.
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DebugButton("UP") { calY -= 10f }
                        DebugButton("DOWN") { calY += 10f }
                        DebugButton("LEFT") { calX -= 10f }
                        DebugButton("RIGHT") { calX += 10f }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                } else {
                    DebugButton("DBG") { showDebug = true }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = status,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(
                            Color(0xDD101526),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (gameOver) {
                    Text(
                        "GAME OVER",
                        color = Color.Yellow,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("FINAL SCORE: $score", color = Color.White)

                    Button(
                        onClick = {
                            fruits.clear()
                            effects.clear()
                            trail.clear()
                            score = 0
                            lives = 3
                            combo = 0
                            bestCombo = 0
                            secondsLeft = 60
                            nextId = 0
                            gameOver = false
                            status = "Swipe fruit. Avoid bombs!"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C4DFF)
                        )
                    ) {
                        Text("PLAY AGAIN")
                    }
                } else {
                    Text(
                        if (fingerPoint == null) {
                            "🖐 SHOW YOUR HAND TO THE CAMERA"
                        } else {
                            "SWIPE FAST THROUGH FRUIT  •  AVOID BOMBS"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                Color(0xAA101526),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    )
                }
            }
        }
    }
}