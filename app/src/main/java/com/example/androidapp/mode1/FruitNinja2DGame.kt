
package com.example.androidapp.mode1

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

private data class Fruit(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color,
    val bomb: Boolean = false
)

private data class SliceHalf(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val color: Color,
    val side: Int,
    val age: Float = 0f
)

private data class Spark(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val age: Float = 0f
)

@Composable
fun FruitNinja2DGame(onExit: () -> Unit) {
    var fruits by remember { mutableStateOf(listOf<Fruit>()) }
    var halves by remember { mutableStateOf(listOf<SliceHalf>()) }
    var sparks by remember { mutableStateOf(listOf<Spark>()) }
    var trail by remember { mutableStateOf(listOf<Offset>()) }

    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(10) }
    var gameOver by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(0) }
    var spawnTimer by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var bombFlash by remember { mutableFloatStateOf(0f) }

    val fruitColors = listOf(
        Color(0xFFEF4444),
        Color(0xFFFFB020),
        Color(0xFF22C55E),
        Color(0xFFFACC15),
        Color(0xFF9333EA)
    )

    fun restartGame() {
        fruits = emptyList()
        halves = emptyList()
        sparks = emptyList()
        trail = emptyList()
        score = 0
        lives = 10
        gameOver = false
        spawnTimer = 0f
        bombFlash = 0f
    }

    // Game loop: movement, gravity, spawning and effects.
    LaunchedEffect(gameOver) {
        var lastTime = System.nanoTime()

        while (!gameOver) {
            delay(16)

            val now = System.nanoTime()
            val dt = ((now - lastTime) / 1_000_000_000f)
                .coerceIn(0f, 0.04f)
            lastTime = now

            val w = canvasSize.width
            val h = canvasSize.height

            if (w > 0f && h > 0f) {
                spawnTimer += dt

                if (spawnTimer >= 1.2f) {
                    spawnTimer -= 1.2f

                    val count = if (Random.nextFloat() < 0.3f) 2 else 1
                    val radius = min(w, h) * 0.047f

                    val newFruits = (0 until count).map {
                        val isBomb = Random.nextFloat() < 0.16f

                        Fruit(
                            id = nextId++,
                            x = w * Random.nextFloat().coerceIn(0.15f, 0.85f),
                            y = h + radius,
                            vx = (Random.nextFloat() - 0.5f) * w * 0.65f,
                            vy = -h * (0.82f + Random.nextFloat() * 0.35f),
                            radius = radius,
                            color = if (isBomb) Color.DarkGray
                            else fruitColors.random(),
                            bomb = isBomb
                        )
                    }

                    fruits = fruits + newFruits
                }

                val updatedFruits = mutableListOf<Fruit>()
                var missed = 0

                fruits.forEach { fruit ->
                    val nx = fruit.x + fruit.vx * dt
                    val ny = fruit.y + fruit.vy * dt
                    val nvy = fruit.vy + h * 1.45f * dt

                    if (ny > h + fruit.radius * 2) {
                        if (!fruit.bomb) missed++
                    } else {
                        updatedFruits.add(
                            fruit.copy(x = nx, y = ny, vy = nvy)
                        )
                    }
                }

                fruits = updatedFruits

                if (missed > 0) {
                    lives = max(0, lives - missed)
                    if (lives == 0) gameOver = true
                }

                halves = halves.mapNotNull { p ->
                    val age = p.age + dt
                    if (age > 0.65f) null
                    else p.copy(
                        x = p.x + p.vx * dt,
                        y = p.y + p.vy * dt,
                        vy = p.vy + h * 1.1f * dt,
                        age = age
                    )
                }

                sparks = sparks.mapNotNull { p ->
                    val age = p.age + dt
                    if (age > 0.35f) null
                    else p.copy(
                        x = p.x + p.vx * dt,
                        y = p.y + p.vy * dt,
                        vy = p.vy + h * 0.8f * dt,
                        age = age
                    )
                }

                trail = trail.takeLast(10)
                bombFlash = max(0f, bombFlash - dt * 2.5f)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF172554), Color(0xFF050816))
                )
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gameOver) {
                    detectDragGestures(
                        onDragStart = { start ->
                            trail = listOf(start)
                        },
                        onDragEnd = {
                            trail = emptyList()
                        },
                        onDragCancel = {
                            trail = emptyList()
                        }
                    ) { change, _ ->
                        val current = change.position
                        val previous = trail.lastOrNull()

                        change.consume()
                        trail = (trail + current).takeLast(12)

                        if (previous != null && !gameOver) {
                            val dx = current.x - previous.x
                            val dy = current.y - previous.y
                            val lengthSquared = dx * dx + dy * dy

                            var points = 0
                            var hitBomb = false
                            val newHalves = mutableListOf<SliceHalf>()
                            val newSparks = mutableListOf<Spark>()

                            fruits.forEach { fruit ->
                                val t = if (lengthSquared == 0f) 0f
                                else (
                                        ((fruit.x - previous.x) * dx +
                                                (fruit.y - previous.y) * dy) /
                                                lengthSquared
                                        ).coerceIn(0f, 1f)

                                val nearX = previous.x + t * dx
                                val nearY = previous.y + t * dy
                                val distX = fruit.x - nearX
                                val distY = fruit.y - nearY

                                if (sqrt(distX * distX + distY * distY) <
                                    fruit.radius * 1.25f
                                ) {
                                    if (fruit.bomb) {
                                        hitBomb = true
                                    } else {
                                        points += 10

                                        // Two halves fly away from each other.
                                        for (side in listOf(-1, 1)) {
                                            newHalves.add(
                                                SliceHalf(
                                                    id = nextId++,
                                                    x = fruit.x,
                                                    y = fruit.y,
                                                    vx = fruit.vx + side * 170f,
                                                    vy = fruit.vy - 90f,
                                                    radius = fruit.radius,
                                                    color = fruit.color,
                                                    side = side
                                                )
                                            )
                                        }

                                        repeat(8) {
                                            newSparks.add(
                                                Spark(
                                                    x = fruit.x,
                                                    y = fruit.y,
                                                    vx = (Random.nextFloat() - 0.5f) * 650f,
                                                    vy = (Random.nextFloat() - 0.5f) * 650f
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Remove sliced fruits and bombs.
                            val hitIds = fruits.filter { fruit ->
                                val t = if (lengthSquared == 0f) 0f
                                else (
                                        ((fruit.x - previous.x) * dx +
                                                (fruit.y - previous.y) * dy) /
                                                lengthSquared
                                        ).coerceIn(0f, 1f)

                                val nx = previous.x + t * dx
                                val ny = previous.y + t * dy

                                sqrt(
                                    (fruit.x - nx) * (fruit.x - nx) +
                                            (fruit.y - ny) * (fruit.y - ny)
                                ) < fruit.radius * 1.25f
                            }.map { it.id }.toSet()

                            fruits = fruits.filterNot { it.id in hitIds }
                            halves = halves + newHalves
                            sparks = sparks + newSparks
                            score += points

                            if (hitBomb) {
                                lives = max(0, lives - 1)
                                bombFlash = 1f
                                if (lives == 0) gameOver = true
                            }
                        }
                    }
                }
        ) {
            canvasSize = size

            // Ground line.
            drawLine(
                Color(0xFF334155),
                Offset(0f, size.height * 0.92f),
                Offset(size.width, size.height * 0.92f),
                strokeWidth = 2f
            )

            // Bomb warning flash.
            if (bombFlash > 0f) {
                drawRect(
                    Color.Red.copy(alpha = bombFlash * 0.25f)
                )
            }

            // Whole fruit and bombs.
            fruits.forEach { fruit ->
                val center = Offset(fruit.x, fruit.y)

                if (fruit.bomb) {
                    drawCircle(Color(0xFF111827), fruit.radius, center)
                    drawCircle(
                        Color.Red,
                        fruit.radius,
                        center,
                        style = Stroke(width = 4f)
                    )
                    drawLine(
                        Color.Yellow,
                        Offset(center.x, center.y - fruit.radius),
                        Offset(center.x + fruit.radius * 0.3f,
                            center.y - fruit.radius * 1.4f),
                        strokeWidth = 5f
                    )
                } else {
                    drawCircle(fruit.color, fruit.radius, center)
                    drawCircle(
                        Color.White.copy(alpha = 0.8f),
                        fruit.radius * 0.15f,
                        Offset(
                            center.x - fruit.radius * 0.3f,
                            center.y - fruit.radius * 0.3f
                        )
                    )
                    drawCircle(
                        Color.White.copy(alpha = 0.7f),
                        fruit.radius,
                        center,
                        style = Stroke(width = 3f)
                    )
                }
            }

            // Animated sliced halves.
            halves.forEach { half ->
                val alpha = (1f - half.age / 0.65f).coerceIn(0f, 1f)
                val r = half.radius
                val left = half.x - r
                val top = half.y - r

                drawArc(
                    color = half.color.copy(alpha = alpha),
                    startAngle = if (half.side < 0) 90f else -90f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(left, top),
                    size = Size(r * 2, r * 2)
                )

                // Pale cut surface.
                drawLine(
                    Color.White.copy(alpha = alpha),
                    Offset(half.x, half.y - r * 0.7f),
                    Offset(half.x, half.y + r * 0.7f),
                    strokeWidth = 3f
                )
            }

            // Slice sparks.
            sparks.forEach { spark ->
                val alpha = (1f - spark.age / 0.35f).coerceIn(0f, 1f)
                drawCircle(
                    Color(0xFFFFF3B0).copy(alpha = alpha),
                    radius = 4f,
                    center = Offset(spark.x, spark.y)
                )
            }

            // Glowing swipe trail.
            if (trail.size >= 2) {
                val path = Path().apply {
                    moveTo(trail.first().x, trail.first().y)
                    trail.drop(1).forEach { lineTo(it.x, it.y) }
                }

                drawPath(
                    path = path,
                    color = Color(0xFF38BDF8).copy(alpha = 0.3f),
                    style = Stroke(width = 16f)
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 5f)
                )
            }
        }

        // Score and lives.
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "FRUIT NINJA",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        "SWIPE TO SLICE • AVOID BOMBS",
                        color = Color(0xFF93C5FD),
                        fontSize = 10.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "SCORE  $score",
                        color = Color(0xFF86EFAC),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "LIVES  ${"♥ ".repeat(lives)}",
                        color = Color(0xFFF87171),
                        fontSize = 15.sp
                    )
                }
            }
        }

        if (gameOver) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        Color(0xF018223B),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "GAME OVER",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Text("Final score: $score", color = Color(0xFF86EFAC))
                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = { restartGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF16A34A)
                    )
                ) {
                    Text("PLAY AGAIN")
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = onExit) {
                    Text("BACK TO LAUNCHER")
                }
            }
        } else {
            Button(
                onClick = onExit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                Text("BACK TO LAUNCHER")
            }
        }
    }
}
