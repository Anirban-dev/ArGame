
package com.example.androidapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.ar.core.Config
import io.github.sceneview.ar.ARScene
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlinx.coroutines.isActive
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

private data class FlyingFruit(
    val id: Int,
    val emoji: String,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val bomb: Boolean,
    val size: Float = 58f,
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

@Composable
fun ARFruitNinjaGame(onExit: () -> Unit) {
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

    val density = LocalDensity.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current

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
                radius = fruit.size * 1.30f
            )
        }

        for (fruit in hits) {
            if (!fruits.removeAll { it.id == fruit.id }) continue

            if (fruit.bomb) {
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
                combo++
                bestCombo = max(bestCombo, combo)

                val points = if (combo >= 3) 20 else 10
                score += points
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

    Box(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(density) {
            androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp.dp.toPx()
        }
        val heightPx = with(density) {
            androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp.toPx()
        }

        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            childNodes = childNodes,
            mainLightNode = mainLightNode,
            planeRenderer = true,
            sessionConfiguration = { _, config ->
                config.lightEstimationMode =
                    Config.LightEstimationMode.ENVIRONMENTAL_HDR
            }
        )

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
                                    size = if (bomb) 62f else 58f
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

                    if (trail.isNotEmpty()) {
                        trail.removeAt(0)
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

        // Finger tracking and slicing.
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
                        "SWIPE FRUIT  •  AVOID BOMBS",
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
