package app.krafted.tigerluckysnap.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import android.view.SoundEffectConstants
import app.krafted.tigerluckysnap.util.SoundManager
import app.krafted.tigerluckysnap.viewmodel.FeedbackEvent
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.model.Mission
import app.krafted.tigerluckysnap.model.TigerReactionState
import app.krafted.tigerluckysnap.ui.components.LivesDisplay
import app.krafted.tigerluckysnap.ui.components.PreviousCard
import app.krafted.tigerluckysnap.ui.components.SnapCard
import app.krafted.tigerluckysnap.ui.components.TigerReaction
import app.krafted.tigerluckysnap.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    mode: GameMode,
    difficulty: Difficulty,
    isMuted: Boolean,
    onGameOver: (Int) -> Unit,
    onBack: () -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val view = LocalView.current
    val soundManager = remember { SoundManager(context) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    var showExitDialog by remember { mutableStateOf(false) }
    var missionsExpanded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { soundManager.release() }
    }

    var shakeTrigger by remember { mutableStateOf(false) }

    val shakeAnim = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        viewModel.feedbackEvents.collectLatest { event ->
            when (event) {
                FeedbackEvent.CORRECT_MATCH -> {
                    if (!isMuted) {
                        soundManager.playCorrectSound()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 40), -1))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(longArrayOf(0, 30, 50, 40), -1)
                        }
                    }
                }
                FeedbackEvent.WRONG_MATCH -> {
                    if (!isMuted) {
                        soundManager.playWrongSound()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(150)
                        }
                    }
                    shakeTrigger = true
                }
            }
        }
    }

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${uiState.score}",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                style = TextStyle(
                                    shadow = Shadow(color = Color(0xFFFF5252), offset = Offset(0f, 2f), blurRadius = 8f)
                                )
                            )
                            if (uiState.comboMultiplier > 1) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFF6D00), Color(0xFFFFAB00))
                                            )
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "x${uiState.comboMultiplier}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ROUND",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${uiState.currentRound}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
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
                    
                    if (uiState.gameMode != GameMode.TIME_ATTACK) {
                        LivesDisplay(lives = uiState.lives, modifier = Modifier.scale(1.2f))
                    }
                }
            }

            if (uiState.missions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val completedCount = uiState.missions.count { it.isCompleted }
                val panelShape = RoundedCornerShape(if (missionsExpanded) 16.dp else 50.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, panelShape)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xCC1A0E00), Color(0xCC2A1800))),
                            panelShape
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0x44FFD700))),
                            panelShape
                        )
                        .clickable { missionsExpanded = !missionsExpanded }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MISSIONS",
                            color = Color(0xFFFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "$completedCount / ${uiState.missions.size} DONE",
                            color = Color(0xAAFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (missionsExpanded) "▲" else "▼",
                            color = Color(0xAAFFD700),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (missionsExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.missions.forEachIndexed { index, mission ->
                            if (index > 0) Spacer(modifier = Modifier.height(6.dp))
                            MissionRow(mission = mission)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    .offset { IntOffset(shakeAnim.value.toInt(), 0) }
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
                            val flipMs = when (difficulty) {
                                Difficulty.EASY -> 300
                                Difficulty.MEDIUM -> 200
                                Difficulty.HARD -> 100
                            }
                            SnapCard(
                                symbol = uiState.currentSymbol,
                                isFlipped = uiState.currentSymbol != null,
                                flipDurationMs = flipMs,
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

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val pressScale by animateFloatAsState(targetValue = if (isPressed) 0.9f else 1f, label = "pressScale")
            val finalButtonScale = snapButtonScale * pressScale

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .scale(snapButtonScale * 1.05f)
                        .alpha(snapGlowAlpha)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xCCFFD700),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(30.dp)
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(102.dp)
                        .scale(finalButtonScale)
                        .shadow(24.dp, RoundedCornerShape(30.dp), spotColor = Color(0xFFFF0000))
                        .clip(RoundedCornerShape(30.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (!isMuted) {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
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
                            RoundedCornerShape(30.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .border(2.dp, Color(0x33000000), RoundedCornerShape(24.dp))
                    )
                    Text(
                        text = "S N A P !",
                        fontSize = 39.sp,
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
                )
            }
        }

        var stampVisible by remember { mutableStateOf(false) }
        var stampLabel by remember { mutableStateOf("") }
        var stampMultLabel by remember { mutableStateOf("") }
        var stampColor by remember { mutableStateOf(Color(0xFFFFAB00)) }

        val stampScale by animateFloatAsState(
            targetValue = if (stampVisible) 1f else 2.5f,
            animationSpec = if (stampVisible) {
                spring(dampingRatio = 0.5f, stiffness = 1200f)
            } else {
                tween(100)
            },
            label = "stampScale"
        )
        val stampAlpha by animateFloatAsState(
            targetValue = if (stampVisible) 1f else 0f,
            animationSpec = if (stampVisible) tween(60) else tween(300),
            label = "stampAlpha"
        )

        LaunchedEffect(uiState.comboCount) {
            if (uiState.comboCount >= 3) {
                stampLabel = "${uiState.comboCount} STREAK"
                stampMultLabel = "x${uiState.comboMultiplier}"
                stampColor = when {
                    uiState.comboMultiplier >= 5 -> Color(0xFFFF1744)
                    uiState.comboMultiplier >= 3 -> Color(0xFFFF6D00)
                    else -> Color(0xFFFFAB00)
                }
                stampVisible = true
                delay(600)
                stampVisible = false
            }
        }

        if (stampAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(stampAlpha),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(stampScale)
                        .rotate(-3f)
                        .border(4.dp, stampColor, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                        .border(2.dp, stampColor, RoundedCornerShape(5.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stampLabel,
                            color = stampColor,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = stampMultLabel,
                            color = stampColor.copy(alpha = 0.8f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }

        var completedMissionIds by remember { mutableStateOf(setOf<String>()) }
        var missionStampVisible by remember { mutableStateOf(false) }
        var missionStampPoints by remember { mutableStateOf(0) }
        var missionStampDesc by remember { mutableStateOf("") }

        val missionStampScale by animateFloatAsState(
            targetValue = if (missionStampVisible) 1f else 2.5f,
            animationSpec = if (missionStampVisible) spring(dampingRatio = 0.5f, stiffness = 1200f) else tween(100),
            label = "missionStampScale"
        )
        val missionStampAlpha by animateFloatAsState(
            targetValue = if (missionStampVisible) 1f else 0f,
            animationSpec = if (missionStampVisible) tween(60) else tween(300),
            label = "missionStampAlpha"
        )

        LaunchedEffect(uiState.missions) {
            val currentCompletedIds = uiState.missions.filter { it.isCompleted }.map { it.id }.toSet()
            val newlyCompleted = currentCompletedIds - completedMissionIds
            if (newlyCompleted.isNotEmpty()) {
                val firstNewId = newlyCompleted.first()
                val mission = uiState.missions.find { it.id == firstNewId }
                missionStampPoints = mission?.rewardPoints ?: 0
                missionStampDesc = mission?.description ?: ""
                missionStampVisible = true
                delay(1800)
                missionStampVisible = false
            }
            completedMissionIds = currentCompletedIds
        }

        if (missionStampAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 160.dp)
                    .alpha(missionStampAlpha),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(missionStampScale)
                        .rotate(4f)
                        .shadow(24.dp, RoundedCornerShape(12.dp), spotColor = Color(0xFF4CAF50))
                        .background(
                            Brush.verticalGradient(listOf(Color(0xEE0D2D0D), Color(0xEE1A4020))),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            3.dp,
                            Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF4CAF50))),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 28.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MISSION COMPLETE",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                        if (missionStampDesc.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = missionStampDesc,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "+$missionStampPoints PTS",
                            color = Color(0xFFFFD700),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            style = TextStyle(
                                shadow = Shadow(
                                    color = Color(0xFF4CAF50),
                                    offset = Offset(0f, 4f),
                                    blurRadius = 12f
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionRow(mission: Mission) {
    val progress = (mission.currentValue.toFloat() / mission.targetValue).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "missionProgress"
    )
    val textColor = if (mission.isCompleted) Color(0xFF4CAF50) else Color(0xCCFFFFFF)
    val barBrush = if (mission.isCompleted)
        Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))
    else
        Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFF6D00)))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mission.description,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (mission.isCompleted) "✓  +${mission.rewardPoints}pts" else "${mission.currentValue}/${mission.targetValue}",
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(barBrush)
            )
        }
    }
}
