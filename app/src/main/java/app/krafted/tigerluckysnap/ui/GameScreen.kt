package app.krafted.tigerluckysnap.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.model.TigerReactionState
import app.krafted.tigerluckysnap.ui.components.LivesDisplay
import app.krafted.tigerluckysnap.ui.components.PreviousCard
import app.krafted.tigerluckysnap.ui.components.SnapCard
import app.krafted.tigerluckysnap.ui.components.TigerReaction
import app.krafted.tigerluckysnap.viewmodel.GameViewModel

@Composable
fun GameScreen(
    mode: GameMode,
    difficulty: Difficulty,
    onGameOver: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = !uiState.isGameOver) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Quit game?") },
            text  = { Text("Your current score will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onBack()
                }) { Text("QUIT") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("KEEP PLAYING") }
            }
        )
    }

    LaunchedEffect(mode, difficulty) {
        viewModel.initGame(mode, difficulty)
    }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) {
            onGameOver(uiState.score)
        }
    }

    var tooSlowVisible by remember { mutableStateOf(false) }
    val tooSlowAlpha by animateFloatAsState(
        targetValue = if (tooSlowVisible) 1f else 0f,
        animationSpec = if (tooSlowVisible) tween(80) else tween(600),
        label = "tooSlowFade"
    )
    LaunchedEffect(uiState.missedWindowCount) {
        if (uiState.missedWindowCount > 0) {
            tooSlowVisible = true
            delay(350)
            tooSlowVisible = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgPulse"
    )

    val snapPulseRaw by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snapPulse"
    )
    val snapButtonScale = if (uiState.isMatchActive) snapPulseRaw else 1f
    val snapGlowAlpha = if (uiState.isMatchActive) 1f else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_temple_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().scale(1.05f).blur(16.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x883E0000)
                        ),
                        radius = 1500f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(50))
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))), RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xEE3E2723), Color(0xEE1B0000))
                        ),
                        RoundedCornerShape(50)
                    )
            ) {
                Box(
                    modifier = Modifier.matchParentSize().padding(4.dp).border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(46.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "SCORE",
                            color = Color(0xFFFFD700),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "${uiState.score}",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(
                                shadow = Shadow(color = Color(0xFFFF5252), offset = Offset(0f, 2f), blurRadius = 8f)
                            )
                        )
                    }
                    
                    if (uiState.gameMode == GameMode.TIME_ATTACK) {
                        val timerColor = if (uiState.timeRemainingSeconds <= 10) Color(0xFFFF4444) else Color.White
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TIME",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${uiState.timeRemainingSeconds}s",
                                color = timerColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "CARDS",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${uiState.cardsFlipped}",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    LivesDisplay(lives = uiState.lives, modifier = Modifier.scale(1.2f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val tigerScale = if (uiState.tigerReaction == TigerReactionState.EXCITED) scalePulse else 1f
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .scale(tigerScale)
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x99FFD700),
                                    Color.Transparent
                                )
                            )
                        )
                )
                TigerReaction(
                    state = uiState.tigerReaction,
                    selectionOutcome = uiState.selectionOutcome,
                    selectionEventCount = uiState.selectionEventCount,
                    modifier = Modifier
                        .size(178.dp)
                        .shadow(16.dp, CircleShape, spotColor = Color(0xFFFF9800))
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 8.dp)
                    .shadow(24.dp, RoundedCornerShape(32.dp))
                    .border(2.dp, Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B))), RoundedCornerShape(32.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1B5E20),
                                Color(0xFF003300)
                            )
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp)
                        .border(1.dp, Color(0x44FFD700), RoundedCornerShape(26.dp))
                )
                
                Row(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            "PREVIOUS",
                            color = Color(0xAAFFFFFF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Box(contentAlignment = Alignment.Center) {
                            PreviousCard(
                                symbol = uiState.previousSymbol,
                                modifier = Modifier
                                    .width(90.dp)
                                    .rotate(-8f)
                                    .shadow(16.dp, RoundedCornerShape(8.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .rotate(-8f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x44000000))
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                        Text(
                            "CURRENT",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Box(contentAlignment = Alignment.Center) {
                            if (uiState.isMatchActive) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .scale(1.15f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xAA4CAF50))
                                        .border(4.dp, Color(0xFF81C784), RoundedCornerShape(12.dp))
                                        .shadow(32.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF4CAF50))
                                )
                            }
                            SnapCard(
                                symbol = uiState.currentSymbol,
                                isFlipped = uiState.currentSymbol != null,
                                modifier = Modifier
                                    .width(160.dp)
                                    .rotate(2f)
                                    .shadow(24.dp, RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .scale(snapButtonScale * 1.05f)
                        .alpha(snapGlowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xCCFFD700),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(36.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .scale(snapButtonScale)
                        .shadow(24.dp, RoundedCornerShape(36.dp), spotColor = Color(0xFFFF0000))
                        .clip(RoundedCornerShape(36.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onSnapTapped()
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF8A80),
                                    Color(0xFFD50000),
                                    Color(0xFF880E4F)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                4.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFFFD700),
                                        Color(0x44000000)
                                    )
                                )
                            ),
                            RoundedCornerShape(36.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .border(2.dp, Color(0x33000000), RoundedCornerShape(28.dp))
                    )
                    Text(
                        text = "S N A P !",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFF9C4),
                        letterSpacing = 6.sp,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color(0xAA000000),
                                offset = Offset(0f, 8f),
                                blurRadius = 12f
                            )
                        )
                    )
                }
            }
        }

        if (tooSlowAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(tooSlowAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TOO SLOW!",
                    color = Color(0xFFFF4444),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(0f, 6f),
                            blurRadius = 18f
                        )
                    )
                )¬
            }
        }
    }
}
