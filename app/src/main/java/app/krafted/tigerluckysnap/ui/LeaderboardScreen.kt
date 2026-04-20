package app.krafted.tigerluckysnap.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.data.db.AppDatabase
import app.krafted.tigerluckysnap.data.db.ScoreEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gold = Color(0xFFFFD700)
private val GoldDeep = Color(0xFFB8860B)
private val Cream = Color(0xFFFFF8E7)
private val DarkOverlay = Color(0xCC1A0A00)

private val rankMedals = listOf("🥇", "🥈", "🥉")
private val rankColors = listOf(
    Color(0xFFFFD700),
    Color(0xFFCCCCCC),
    Color(0xFFCD7F32),
)
private val rankGlows = listOf(
    Color(0x55FFD700),
    Color(0x33CCCCCC),
    Color(0x33CD7F32),
)

@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val scores by db.scoreDao().getAllTopScores().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    // Entry animations
    val headerAlpha = remember { Animatable(0f) }
    val headerOffset = remember { Animatable(-24f) }
    val listAlpha = remember { Animatable(0f) }
    val listOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        // Purge any legacy blank-name rows from earlier buggy saves
        scope.launch { db.scoreDao().deleteUnnamedScores() }
        launch { headerAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { headerOffset.animateTo(0f, tween(500, easing = EaseOutBack)) }
        delay(250)
        launch { listAlpha.animateTo(1f, tween(500, easing = EaseOutCubic)) }
        launch { listOffset.animateTo(0f, tween(500, easing = EaseOutBack)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.bg_temple_4),
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
                .padding(horizontal = 20.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ───────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = headerAlpha.value
                    translationY = headerOffset.value
                }
            ) {
                // Tiger avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x55FFD700), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )
                    Image(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.tiger_react_idle),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(8.dp, CircleShape, spotColor = Color(0xFFFF9800))
                            .clip(CircleShape)
                            .border(2.dp, Gold, CircleShape)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "LEADERBOARD",
                    color = Gold,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 5.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xAAFFD700),
                            offset = Offset(0f, 3f),
                            blurRadius = 10f
                        )
                    )
                )

                Spacer(Modifier.height(6.dp))

                // Golden divider
                Canvas(
                    modifier = Modifier
                        .width(200.dp)
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

                Text(
                    text = "Top Snappers",
                    color = Cream.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp
                )
            }

            Spacer(Modifier.height(18.dp))

            // ── Score List ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        alpha = listAlpha.value
                        translationY = listOffset.value
                    }
            ) {
                if (scores.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🐯",
                                fontSize = 48.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "No scores yet.\nPlay to make history!",
                                color = Cream.copy(alpha = 0.65f),
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Serif
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(scores) { index, entry ->
                            ScoreRow(rank = index + 1, entry = entry)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Back Button ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22FFFFFF))
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0x88FFD700), Color(0xCCFFEE88), Color(0x88FFD700))
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "← BACK",
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 4.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(rank: Int, entry: ScoreEntity) {
    val rankColor = rankColors.getOrElse(rank - 1) { Cream.copy(alpha = 0.75f) }
    val rankGlow = rankGlows.getOrElse(rank - 1) { Color(0x11FFFFFF) }
    val medal = rankMedals.getOrNull(rank - 1)
    val isTopThree = rank <= 3
    val cardHeight = if (isTopThree) 72.dp else 60.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .shadow(
                elevation = if (isTopThree) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isTopThree) rankColor else Color.Transparent
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = if (isTopThree)
                        listOf(rankGlow, Color(0x22000000), Color(0x11000000))
                    else
                        listOf(Color(0x18FFFFFF), Color(0x0DFFFFFF))
                )
            )
            .border(
                width = if (isTopThree) 1.5.dp else 0.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(rankColor.copy(alpha = if (isTopThree) 0.6f else 0.2f), Color.Transparent)
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (isTopThree) 40.dp else 32.dp)
                    .background(
                        color = rankColor.copy(alpha = if (isTopThree) 0.18f else 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = if (isTopThree) 1.5.dp else 1.dp,
                        color = rankColor.copy(alpha = if (isTopThree) 0.7f else 0.3f),
                        shape = CircleShape
                    )
            ) {
                if (medal != null) {
                    Text(
                        text = medal,
                        fontSize = if (isTopThree) 20.sp else 14.sp
                    )
                } else {
                    Text(
                        text = "#$rank",
                        color = rankColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Player info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.playerName,
                    color = Cream,
                    fontSize = if (isTopThree) 16.sp else 14.sp,
                    fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.SemiBold,
                    fontFamily = FontFamily.Serif,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = entry.gameMode,
                    color = rankColor.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.Serif
                )
            }

            // Score chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isTopThree) rankColor.copy(alpha = 0.15f)
                        else Color(0x15FFFFFF)
                    )
                    .border(
                        1.dp,
                        rankColor.copy(alpha = if (isTopThree) 0.5f else 0.2f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entry.score}",
                    color = if (isTopThree) rankColor else Cream,
                    fontSize = if (isTopThree) 18.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif
                )
            }
        }
    }
}
