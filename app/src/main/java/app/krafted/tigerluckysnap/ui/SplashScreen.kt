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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.data.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val iconScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(40f) }
    val underlineWidth = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        launch { iconAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { iconScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 300f)) }

        delay(300)

        launch { titleAlpha.animateTo(1f, tween(600, easing = EaseOutCubic)) }
        launch { titleOffset.animateTo(0f, tween(700, easing = EaseOutBack)) }

        delay(400)

        launch { underlineWidth.animateTo(1f, tween(500, easing = EaseOutCubic)) }

        delay(200)

        launch { subtitleAlpha.animateTo(1f, tween(400, easing = EaseOutCubic)) }

        delay(1500)
        if (UserPreferences.hasSeenOnboarding(context)) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.bg_temple_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.50f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(Modifier.weight(0.3f))

            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                        alpha = iconAlpha.value
                    }
            ) {
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f * pulseScale
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = pulseAlpha),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6.dp.toPx())
                    )
                }

                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f - 4.dp.toPx()
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFFFEE88),
                                Color(0xFFFFD700),
                                Color.Transparent
                            ),
                            center = center
                        ),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x88FFD700), Color.Transparent)
                            ),
                            CircleShape
                        )
                )

                
                Image(
                    painter = painterResource(R.drawable.tiger_react_idle),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(168.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color(0xFFFF9800))
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFFFFD700), CircleShape)
                )
            }

            Spacer(Modifier.height(28.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
            ) {
                Text(
                    text = "TIGER",
                    color = Color(0xFFFFD700),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 8.sp
                )
                Text(
                    text = "LUCKY SNAP",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 6.sp
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .width(260.dp)
                        .height(3.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineWidth = size.width * underlineWidth.value
                        val startX = (size.width - lineWidth) / 2f
                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFFFD700),
                                    Color(0xFFFFEE88),
                                    Color(0xFFFFD700),
                                    Color.Transparent
                                ),
                                startX = startX,
                                endX = startX + lineWidth
                            ),
                            start = Offset(startX, size.height / 2),
                            end = Offset(startX + lineWidth, size.height / 2),
                            strokeWidth = size.height
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Match the symbols. Snap to win!",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                letterSpacing = 1.sp,
                modifier = Modifier.graphicsLayer { alpha = subtitleAlpha.value }
            )

            Spacer(Modifier.weight(0.7f))
        }
    }
}
