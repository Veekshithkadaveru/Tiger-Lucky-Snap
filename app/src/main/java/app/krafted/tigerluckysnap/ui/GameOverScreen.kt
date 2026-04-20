package app.krafted.tigerluckysnap.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.data.db.AppDatabase
import app.krafted.tigerluckysnap.data.db.ScoreEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gold       = Color(0xFFFFD700)
private val GoldDeep   = Color(0xFFB8860B)
private val GoldLight  = Color(0xFFFFEE88)
private val Cream      = Color(0xFFFFF8E7)
private val DarkBrown  = Color(0xFF1A0A00)
private val CardBg     = Color(0xCC1A0A00)

@Composable
fun GameOverScreen(
    score: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val quote = when {
        score >= 20 -> "Magnificent! You honor the temple!"
        score >= 10 -> "Well played, worthy challenger!"
        score >= 5  -> "Not bad... the tiger approves."
        else        -> "The tiger demands a rematch!"
    }
    val tigerDrawable = if (score >= 10) R.drawable.tiger_react_excited else R.drawable.tiger_react_happy

    // ── Bounce-in for tiger ──────────────────────────────────────────
    var bounceTarget by remember { mutableStateOf(0f) }
    val bounceScale by animateFloatAsState(
        targetValue  = bounceTarget,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tigerBounce"
    )

    // ── Staggered entry animations ──────────────────────────────────
    val cardAlpha  = remember { Animatable(0f) }
    val cardOffset = remember { Animatable(50f) }
    val btnAlpha   = remember { Animatable(0f) }
    val btnOffset  = remember { Animatable(30f) }

    // ── Pulse ring on avatar ────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ps"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f, targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(950, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pa"
    )

    // ── State ────────────────────────────────────────────────────────
    var playerName by remember { mutableStateOf("") }
    var scoreSaved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bounceTarget = 1f
        delay(200)
        launch { cardAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { cardOffset.animateTo(0f, tween(500, easing = EaseOutBack)) }
        delay(300)
        launch { btnAlpha.animateTo(1f, tween(450, easing = EaseOutCubic)) }
        launch { btnOffset.animateTo(0f, tween(450, easing = EaseOutBack)) }
    }

    fun saveScore() {
        if (!scoreSaved && playerName.isNotBlank()) {
            scope.launch {
                AppDatabase.getInstance(context).scoreDao().insert(
                    ScoreEntity(playerName = playerName.trim(), score = score, gameMode = "SOLO")
                )
                scoreSaved = true
            }
        }
    }
    fun saveAndPlay() { saveScore(); onPlayAgain() }
    fun saveAndHome() { saveScore(); onHome() }

    // ════════════════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {

        // Background
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.bg_temple_3),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color(0xDD1A0A00)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Circular tiger with pulse ring ────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(180.dp)
                    .scale(bounceScale)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Gold.copy(alpha = pulseAlpha),
                        radius = size.minDimension / 2f * pulseScale,
                        center = center,
                        style = Stroke(width = 5.dp.toPx())
                    )
                }
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0x55FFD700), Color.Transparent)),
                            CircleShape
                        )
                )
                Image(
                    painter = androidx.compose.ui.res.painterResource(tigerDrawable),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(148.dp)
                        .shadow(20.dp, CircleShape, spotColor = Color(0xFFFF9800))
                        .clip(CircleShape)
                        .border(3.dp, Gold, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Frosted score card ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = cardAlpha.value; translationY = cardOffset.value }
                    .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Gold)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xBB2A1200), Color(0xAA1A0800), Color(0xCC0D0500))
                        )
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.verticalGradient(listOf(Gold.copy(alpha = 0.6f), Gold.copy(alpha = 0.15f)))
                        ),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // GAME OVER title
                    Text(
                        text = "GAME OVER",
                        color = Gold,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 6.sp,
                        style = TextStyle(
                            shadow = Shadow(color = Color(0xAAFFD700), offset = Offset(0f, 4f), blurRadius = 14f)
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Golden divider
                    Canvas(modifier = Modifier.width(180.dp).height(2.dp)) {
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, Gold, GoldLight, Gold, Color.Transparent)
                            ),
                            start = Offset(0f, size.height / 2),
                            end   = Offset(size.width, size.height / 2),
                            strokeWidth = size.height
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Score display
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "$score",
                            color = Cream,
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Serif,
                            style = TextStyle(
                                shadow = Shadow(color = Color.Black, offset = Offset(2f, 4f), blurRadius = 8f)
                            ),
                            lineHeight = 80.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "pts",
                            color = Gold,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quote
                    Text(
                        text = quote,
                        color = Cream.copy(alpha = 0.80f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = Shadow(color = Color.Black, offset = Offset(1f, 2f), blurRadius = 4f))
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    // Name input
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it.take(20) },
                        placeholder = {
                            Text(
                                "Your name for the hall of fame",
                                color = Cream.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Serif
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { saveScore() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Gold,
                            unfocusedBorderColor = Gold.copy(alpha = 0.4f),
                            focusedTextColor     = Cream,
                            unfocusedTextColor   = Cream,
                            cursorColor          = Gold,
                            focusedContainerColor   = Color(0x22FFD700),
                            unfocusedContainerColor = Color(0x11FFD700)
                        ),
                        textStyle = TextStyle(
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Serif,
                            color      = Cream
                        ),
                        shape    = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Save button + confirmation
                    if (playerName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        if (!scoreSaved) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0x33FFD700), Color(0x22FFD700))
                                        )
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            Brush.horizontalGradient(
                                                listOf(Gold.copy(alpha = 0.7f), GoldLight.copy(alpha = 0.5f), Gold.copy(alpha = 0.7f))
                                            )
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { saveScore() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "💾  SAVE SCORE",
                                    color = Gold,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif,
                                    letterSpacing = 2.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "✓", color = Color(0xFF66FF88), fontSize = 18.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Score saved!",
                                    color = Color(0xFF66FF88),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Serif
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Action buttons ────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = btnAlpha.value; translationY = btnOffset.value }
            ) {
                // Play Again — gold gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .shadow(18.dp, RoundedCornerShape(20.dp), spotColor = Gold)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(GoldLight, Gold, GoldDeep)))
                        .border(
                            BorderStroke(2.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.4f), GoldDeep))),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { saveAndPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (playerName.isBlank()) "▶  PLAY AGAIN" else "💾  SAVE & PLAY AGAIN",
                        color = DarkBrown,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp,
                        style = TextStyle(
                            shadow = Shadow(color = Color(0x44FFFFFF), offset = Offset(0f, 2f), blurRadius = 4f)
                        )
                    )
                }

                // Home — ghost / glassy
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22FFFFFF))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(listOf(Gold.copy(alpha = 0.5f), GoldLight.copy(alpha = 0.7f), Gold.copy(alpha = 0.5f)))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { saveAndHome() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏠  HOME",
                        color = Cream,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 3.sp
                    )
                }
            }
        }
    }
}
