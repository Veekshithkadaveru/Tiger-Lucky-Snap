package app.krafted.tigerluckysnap.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

private val BrandGold = Color(0xFFFFD700)
private val BrandGoldLight = Color(0xFFFFF0A8)
private val BrandOrange = Color(0xFFFF8C00)
private val BrandRed = Color(0xFFD84315)
private val DarkBg = Color(0xFF0F0800)
private val TextOffWhite = Color(0xFFFFF6E5)

@Composable
fun HomeScreen(
    isMuted: Boolean,
    onMutedChange: (Boolean) -> Unit,
    onStartGame: (GameMode, Difficulty) -> Unit,
    onViewLeaderboard: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(GameMode.SOLO) }
    var selectedDifficulty by remember { mutableStateOf(Difficulty.MEDIUM) }

    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(-50f) }
    val avatarScale = remember { Animatable(0f) }
    val avatarAlpha = remember { Animatable(0f) }
    val buttonsAlpha = remember { Animatable(0f) }
    val buttonsOffset = remember { Animatable(60f) }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(800, easing = EaseOutCubic)) }
        launch { titleOffset.animateTo(0f, tween(800, easing = EaseOutBack)) }
        delay(200)
        launch { avatarAlpha.animateTo(1f, tween(700, easing = EaseOutCubic)) }
        launch { avatarScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 200f)) }
        delay(350)
        launch { buttonsAlpha.animateTo(1f, tween(700, easing = EaseOutCubic)) }
        launch { buttonsOffset.animateTo(0f, tween(700, easing = EaseOutBack)) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")


    val bgScale by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bgScale"
    )

    val floatY by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val raysRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "raysRot"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -800f,
        targetValue = 1500f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing = LinearEasing, delayMillis = 1500),
            RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_temple_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(bgScale)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xE60A0500), Color(0xF2050200))))
        )

        EmbersBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(30.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
            ) {
                Stroked3DTitle(text = "TIGER", fontSize = 34.sp, mainColor = Color.White)
                Spacer(Modifier.height(4.dp))
                Stroked3DTitle(text = "LUCKY SNAP", fontSize = 44.sp, mainColor = BrandGold)
            }

            Spacer(Modifier.height(40.dp))


            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer {
                        scaleX = avatarScale.value
                        scaleY = avatarScale.value
                        alpha = avatarAlpha.value
                        translationY = floatY
                    }
            ) {
                RadarRings()

                Canvas(modifier = Modifier.size(170.dp)) {
                    rotate(raysRotation) {
                        val sweepColors = listOf(
                            Color(0x88FFD700), Color.Transparent,
                            Color(0x88FF8C00), Color.Transparent,
                            Color(0x88FFD700), Color.Transparent,
                            Color(0x88FF8C00), Color.Transparent,
                            Color(0x88FFD700), Color.Transparent,
                            Color(0x88FF8C00), Color.Transparent,
                            Color(0x88FFD700)
                        )
                        val brush = Brush.sweepGradient(sweepColors)
                        drawCircle(brush)
                    }
                }

                Image(
                    painter = painterResource(R.drawable.tiger_react_idle),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(150.dp)
                        .shadow(30.dp, CircleShape, spotColor = BrandOrange)
                        .clip(CircleShape)
                        .border(
                            4.dp,
                            Brush.linearGradient(listOf(Color.White, BrandGold)),
                            CircleShape
                        )
                )
            }

            Spacer(Modifier.height(30.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = buttonsAlpha.value
                        translationY = buttonsOffset.value
                    }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Color.Black)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xEE1A0D00),
                                    Color(0xDD0F0800)
                                )
                            )
                        )
                        .border(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0x66FFD700),
                                    Color(0x22FFFFFF),
                                    Color(0x66FFD700)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeader("SELECT MODE")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ModeTile(
                                icon = "🎯",
                                label = "CLASSIC",
                                description = "3 lives",
                                selected = selectedMode == GameMode.SOLO,
                                modifier = Modifier.weight(1f)
                            ) { selectedMode = GameMode.SOLO }
                            ModeTile(
                                icon = "⏳",
                                label = "TIME\nATTACK",
                                description = "60s rush",
                                selected = selectedMode == GameMode.TIME_ATTACK,
                                modifier = Modifier.weight(1f)
                            ) { selectedMode = GameMode.TIME_ATTACK }
                            ModeTile(
                                icon = "💀",
                                label = "HARD\nCORE",
                                description = "1 life",
                                selected = selectedMode == GameMode.HARDCORE,
                                modifier = Modifier.weight(1f)
                            ) { selectedMode = GameMode.HARDCORE }
                        }

                        AnimatedVisibility(
                            visible = selectedMode != GameMode.HARDCORE,
                            enter = fadeIn(tween(250)) + expandVertically(
                                tween(
                                    300,
                                    easing = EaseOutBack
                                )
                            ),
                            exit = fadeOut(tween(200)) + shrinkVertically(tween(250))
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Spacer(Modifier.height(4.dp))
                                SectionHeader("DIFFICULTY")
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    DifficultyChip(
                                        label = "EASY",
                                        selected = selectedDifficulty == Difficulty.EASY,
                                        modifier = Modifier.weight(1f)
                                    ) { selectedDifficulty = Difficulty.EASY }
                                    DifficultyChip(
                                        label = "MEDIUM",
                                        selected = selectedDifficulty == Difficulty.MEDIUM,
                                        modifier = Modifier.weight(1f)
                                    ) { selectedDifficulty = Difficulty.MEDIUM }
                                    DifficultyChip(
                                        label = "HARD",
                                        selected = selectedDifficulty == Difficulty.HARD,
                                        modifier = Modifier.weight(1f)
                                    ) { selectedDifficulty = Difficulty.HARD }
                                }
                            }
                        }
                    }
                }

                PlayButton3D(
                    onClick = { onStartGame(selectedMode, selectedDifficulty) },
                    shimmerOffset = shimmerOffset
                )
            }
        }
        
        IconButton(
            onClick = onViewLeaderboard,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 16.dp)
                .background(Color(0x88000000), CircleShape)
        ) {
            Text(
                text = "🏆",
                fontSize = 24.sp
            )
        }
        
        IconButton(
            onClick = { onMutedChange(!isMuted) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
                .background(Color(0x88000000), CircleShape)
        ) {
            Text(
                text = if (isMuted) "🔇" else "🔊",
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun Stroked3DTitle(
    text: String,
    fontSize: TextUnit,
    mainColor: Color,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {

        for (i in 6 downTo 1) {
            Text(
                text = text,
                color = Color(0xFF381500),
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 6.sp,
                modifier = Modifier.offset(y = i.dp)
            )
        }

        val outlineColor = BrandGoldLight
        val offsets = listOf(Offset(-1f, -1f), Offset(1f, -1f), Offset(-1f, 1f), Offset(1f, 1f))
        offsets.forEach { offset ->
            Text(
                text = text,
                color = outlineColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 6.sp,
                modifier = Modifier.offset(x = offset.x.dp, y = offset.y.dp)
            )
        }

        Text(
            text = text,
            color = mainColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            letterSpacing = 6.sp,
            style = TextStyle(
                shadow = Shadow(color = BrandOrange, offset = Offset(0f, 4f), blurRadius = 16f)
            )
        )
    }
}

@Composable
fun RadarRings() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3000f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val maxRadius = size.minDimension / 2f
        for (i in 0 until 3) {
            val rawPhase = (time + i * 1000f) % 3000f / 3000f
            val alpha = (1f - rawPhase) * 0.7f
            drawCircle(
                color = BrandGold.copy(alpha = alpha),
                radius = maxRadius * rawPhase,
                style = Stroke(width = (5.dp.toPx() * (1f - rawPhase)).coerceAtLeast(1f))
            )
        }
    }
}

class Ember(
    val startX: Float,
    val speedY: Float,
    val speedX: Float,
    val size: Float,
    val phase: Float
)

@Composable
fun EmbersBackground() {
    val embers = remember {
        List(40) {
            Ember(
                startX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.6f + 0.2f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                size = Random.nextFloat() * 6f + 3f,
                phase = Random.nextFloat() * 100f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "embers")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100000f,
        animationSpec = infiniteRepeatable(tween(1000000, easing = LinearEasing)),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        embers.forEach { ember ->
            val yPos =
                size.height - (((time * ember.speedY * 500f) + ember.phase * 50f) % (size.height + 100f))
            val xPos = (ember.startX * size.width) + sin(time * ember.speedX + ember.phase) * 50f

            val alpha = (yPos / size.height).coerceIn(0f, 1f) * 0.7f

            drawCircle(
                color = BrandOrange.copy(alpha = alpha),
                radius = ember.size,
                center = Offset(xPos, yPos)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.9f),
                radius = ember.size * 0.4f,
                center = Offset(xPos, yPos)
            )
        }
    }
}

@Composable
fun PhysicalTile(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val depth = if (isPressed) 1.dp else if (selected) 2.dp else 6.dp
    val yOffset = if (isPressed) 5.dp else if (selected) 4.dp else 0.dp

    val depthAnim by animateFloatAsState(
        targetValue = depth.value,
        spring(dampingRatio = 0.6f, stiffness = 1000f),
        label = "depth"
    )
    val yOffsetAnim by animateFloatAsState(
        targetValue = yOffset.value,
        spring(dampingRatio = 0.6f, stiffness = 1000f),
        label = "yOffset"
    )

    val bgColor = if (selected) Brush.verticalGradient(listOf(BrandGoldLight, BrandGold))
    else Brush.verticalGradient(listOf(Color(0xFF3A2311), Color(0xFF1F1206)))

    val bottomColor = if (selected) BrandOrange else Color(0xFF0A0500)

    Box(
        modifier = modifier
            .graphicsLayer { translationY = yOffsetAnim.dp.toPx() }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = depthAnim.dp)
                .background(bottomColor, RoundedCornerShape(16.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    1.dp,
                    if (selected) Color.White else Color(0x33FFFFFF),
                    RoundedCornerShape(16.dp)
                )
                .background(bgColor, RoundedCornerShape(16.dp))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PlayButton3D(onClick: () -> Unit, shimmerOffset: Float) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val depth = if (isPressed) 2.dp else 10.dp
    val yOffset = if (isPressed) 8.dp else 0.dp

    val depthAnim by animateFloatAsState(
        targetValue = depth.value,
        spring(stiffness = Spring.StiffnessHigh),
        label = "depth"
    )
    val yOffsetAnim by animateFloatAsState(
        targetValue = yOffset.value,
        spring(stiffness = Spring.StiffnessHigh),
        label = "yOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(86.dp)
            .graphicsLayer { translationY = yOffsetAnim.dp.toPx() }
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .offset(y = depthAnim.dp)
                .shadow(
                    if (isPressed) 4.dp else 24.dp,
                    RoundedCornerShape(38.dp),
                    spotColor = BrandRed
                )
                .background(BrandRed, RoundedCornerShape(38.dp))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .background(
                    Brush.verticalGradient(listOf(BrandGoldLight, BrandGold, BrandOrange)),
                    RoundedCornerShape(38.dp)
                )
                .border(
                    2.dp,
                    Brush.linearGradient(listOf(Color.White, BrandGold)),
                    RoundedCornerShape(38.dp)
                )
                .clip(RoundedCornerShape(38.dp)),
            contentAlignment = Alignment.Center
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color(0xAAFFFFFF), Color.Transparent),
                    start = Offset(shimmerOffset - 150f, 0f),
                    end = Offset(shimmerOffset + 150f, size.height)
                )
                drawRect(shimmerBrush)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "▶",
                    color = Color(0xFF2A1000),
                    fontSize = 32.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x66FFFFFF),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = "PLAY NOW",
                    color = Color(0xFF2A1000),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x66FFFFFF),
                            offset = Offset(0f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            BrandGold.copy(alpha = 0.5f)
                        )
                    )
                )
        )
        Text(
            text = "  $label  ",
            color = BrandGoldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            letterSpacing = 2.sp,
            style = TextStyle(shadow = Shadow(color = BrandGold, blurRadius = 4f))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            BrandGold.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun ModeTile(
    icon: String,
    label: String,
    description: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PhysicalTile(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(110.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = icon,
                fontSize = 28.sp,
                modifier = Modifier.scale(if (selected) 1.2f else 1f),
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0xCC000000),
                        offset = Offset(0f, 4f),
                        blurRadius = 8f
                    )
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = if (selected) Color(0xFF2A1000) else BrandGoldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = if (selected) Color(0xDD2A1000) else TextOffWhite.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun DifficultyChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PhysicalTile(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(52.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color(0xFF2A1000) else TextOffWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp
        )
    }
}


