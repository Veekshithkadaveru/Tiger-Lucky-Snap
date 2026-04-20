package app.krafted.tigerluckysnap.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gold = Color(0xFFFFD700)
private val GoldDeep = Color(0xFFB8860B)
private val Cream = Color(0xFFFFF8E7)
private val DarkOverlay = Color(0xCC1A0A00)

@Composable
fun HomeScreen(
    onStartGame: (GameMode, Difficulty) -> Unit,
    onViewLeaderboard: () -> Unit
) {
    // Entry animations
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(-30f) }
    val avatarScale = remember { Animatable(0f) }
    val avatarAlpha = remember { Animatable(0f) }
    val buttonsAlpha = remember { Animatable(0f) }
    val buttonsOffset = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch { titleAlpha.animateTo(1f, tween(600, easing = EaseOutCubic)) }
        launch { titleOffset.animateTo(0f, tween(600, easing = EaseOutBack)) }
        delay(200)
        launch { avatarAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { avatarScale.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 260f)) }
        delay(350)
        launch { buttonsAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { buttonsOffset.animateTo(0f, tween(500, easing = EaseOutBack)) }
    }

    // Pulse animation for avatar ring
    val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatarPulseAlpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_temple_2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkOverlay)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Animated Title ──────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
            ) {
                Text(
                    text = "TIGER",
                    color = Gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 10.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xAAFFD700),
                            offset = Offset(0f, 4f),
                            blurRadius = 12f
                        )
                    )
                )
                Text(
                    text = "LUCKY SNAP",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0x66000000),
                            offset = Offset(0f, 4f),
                            blurRadius = 10f
                        )
                    )
                )

                Spacer(Modifier.height(6.dp))

                // Golden divider
                Canvas(
                    modifier = Modifier
                        .width(220.dp)
                        .height(2.dp)
                ) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFD700),
                                Color(0xFFFFEE88),
                                Color(0xFFFFD700),
                                Color.Transparent
                            )
                        ),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = size.height
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Circular Tiger Avatar with pulse ring ───────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        scaleX = avatarScale.value
                        scaleY = avatarScale.value
                        alpha = avatarAlpha.value
                    }
            ) {
                // Pulsing outer ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f * pulseScale
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = pulseAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
                // Radial glow
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x66FFD700), Color.Transparent)
                            ),
                            CircleShape
                        )
                )
                // Circular clipped image
                Image(
                    painter = painterResource(R.drawable.tiger_react_idle),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(190.dp)
                        .shadow(20.dp, CircleShape, spotColor = Color(0xFFFF9800))
                        .clip(CircleShape)
                        .border(3.dp, Gold, CircleShape)
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Buttons ─────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = buttonsAlpha.value
                        translationY = buttonsOffset.value
                    }
            ) {
                PlayButton(onClick = { onStartGame(GameMode.SOLO, Difficulty.MEDIUM) })
                LeaderboardButton(onClick = onViewLeaderboard)
            }
        }
    }
}

@Composable
private fun PlayButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(20.dp, RoundedCornerShape(24.dp), spotColor = Gold)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFFFFE680), Gold, GoldDeep))
            )
            .border(
                BorderStroke(
                    2.dp,
                    Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFB8860B)))
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "▶  PLAY",
            color = Color(0xFF1A0A00),
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            letterSpacing = 6.sp,
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

@Composable
private fun LeaderboardButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x33FFD700))
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(Color(0x88FFD700), Color(0xCCFFEE88), Color(0x88FFD700))
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🏆",
                fontSize = 20.sp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "LEADERBOARD",
                color = Cream,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 3.sp
            )
        }
    }
}
