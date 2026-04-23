package app.krafted.tigerluckysnap.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.data.UserPreferences
import app.krafted.tigerluckysnap.model.Symbol
import app.krafted.tigerluckysnap.ui.components.LivesDisplay
import app.krafted.tigerluckysnap.ui.components.PreviousCard
import app.krafted.tigerluckysnap.ui.components.SnapCard
import kotlinx.coroutines.delay

private val Gold = Color(0xFFFFD700)
private val GoldLight = Color(0xFFFFF0A8)
private val BrandOrange = Color(0xFFFF8C00)
private val SnapRed = Color(0xFFD84315)
private val TextOffWhite = Color(0xFFFFF6E5)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(0) }

    val finishOnboarding = {
        UserPreferences.setOnboardingSeen(context)
        onFinished()
    }

    BackHandler { finishOnboarding() }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_temple_1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xEE0A0500), Color(0xF5050200))))
        )
        EmbersBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentPage + 1} / 3",
                    color = Gold.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp
                )
                AnimatedVisibility(visible = currentPage < 2, enter = fadeIn(), exit = fadeOut()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { finishOnboarding() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "SKIP",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }


            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    val forward = targetState > initialState
                    (slideInHorizontally(tween(350)) { if (forward) it else -it } +
                            fadeIn(tween(350))) togetherWith
                            (slideOutHorizontally(tween(350)) { if (forward) -it else it } +
                                    fadeOut(tween(350)))
                },
                modifier = Modifier.weight(1f),
                label = "pageContent"
            ) { page ->
                when (page) {
                    0 -> OnboardingPage1()
                    1 -> OnboardingPage2()
                    else -> OnboardingPage3()
                }
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isActive = index == currentPage
                    val dotWidth by animateDpAsState(
                        targetValue = if (isActive) 28.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "dotWidth$index"
                    )
                    Box(
                        modifier = Modifier
                            .width(dotWidth)
                            .height(8.dp)
                            .background(
                                if (isActive)
                                    Brush.horizontalGradient(listOf(Gold, BrandOrange))
                                else
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.White.copy(0.25f),
                                            Color.White.copy(0.25f)
                                        )
                                    ),
                                CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            OnboardingNavButton(
                isLastPage = currentPage == 2,
                onAction = { if (currentPage == 2) finishOnboarding() else currentPage++ }
            )

            Spacer(Modifier.height(44.dp))
        }
    }
}

@Composable
private fun OnboardingNavButton(isLastPage: Boolean, onAction: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val depthAnim by animateFloatAsState(
        targetValue = if (isPressed) 2f else 8f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "depth"
    )
    val yOffsetAnim by animateFloatAsState(
        targetValue = if (isPressed) 6f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "yOff"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer { translationY = yOffsetAnim.dp.toPx() }
            .clickable(interactionSource = interactionSource, indication = null) { onAction() }
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .offset(y = depthAnim.dp)
                .shadow(
                    if (isPressed) 4.dp else 20.dp,
                    RoundedCornerShape(36.dp),
                    spotColor = SnapRed
                )
                .background(SnapRed.copy(alpha = 0.8f), RoundedCornerShape(36.dp))
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    Brush.verticalGradient(listOf(GoldLight, Gold, BrandOrange)),
                    RoundedCornerShape(36.dp)
                )
                .border(
                    2.dp,
                    Brush.linearGradient(listOf(Color.White, Gold)),
                    RoundedCornerShape(36.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isLastPage) "▶  START PLAYING" else "NEXT  →",
                color = Color(0xFF2A1000),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 3.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color(0x55FFFFFF),
                        offset = Offset(0f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}

@Composable
private fun GoldDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, Gold.copy(alpha = 0.5f)))
                )
        )
        Text("  ✦  ", color = Gold.copy(alpha = 0.8f), fontSize = 10.sp)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(listOf(Gold.copy(alpha = 0.5f), Color.Transparent))
                )
        )
    }
}



@Composable
private fun OnboardingPage1() {
    val row1 = listOf(Symbol.COIN_BAG, Symbol.ROUND_LANTERN, Symbol.TALL_LANTERN, Symbol.CROWN)
    val row2 = listOf(Symbol.STRAWBERRY, Symbol.DIAMOND, Symbol.GRAPES)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(8.dp))
        Stroked3DTitle(text = "KNOW YOUR", fontSize = 24.sp, mainColor = Color.White)
        Spacer(Modifier.height(4.dp))
        Stroked3DTitle(text = "SYMBOLS", fontSize = 36.sp, mainColor = Gold)
        Spacer(Modifier.height(14.dp))
        GoldDivider()
        Spacer(Modifier.height(16.dp))

        Text(
            "These lucky symbols fly by fast.\nSnap when the same one appears twice in a row!",
            color = TextOffWhite.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(22.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            row1.forEach { SymbolTile(it) }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Top
        ) {
            row2.forEach { SymbolTile(it) }
        }
    }
}

@Composable
private fun SymbolTile(symbol: Symbol) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        SnapCard(symbol = symbol, isFlipped = true, modifier = Modifier.width(72.dp))
        Text(
            text = symbol.displayName,
            color = Gold.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}



@Composable
private fun OnboardingPage2() {
    
    var demoState by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            demoState = 0; delay(2200)
            demoState = 1; delay(1600)
            demoState = 2; delay(700)
            demoState = 0; delay(400)
        }
    }

    val isMatch = demoState >= 1
    val isSnapped = demoState == 2

    val snapScale by animateFloatAsState(
        targetValue = when {
            isSnapped -> 0.87f
            isMatch -> 1.1f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "snapScale"
    )
    val glowAlpha by animateFloatAsState(
        targetValue = if (isMatch) 1f else 0f,
        animationSpec = tween(350),
        label = "glowAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(8.dp))
        Stroked3DTitle(text = "TAP SNAP TO", fontSize = 22.sp, mainColor = Color.White)
        Spacer(Modifier.height(4.dp))
        Stroked3DTitle(text = "MATCH!", fontSize = 38.sp, mainColor = Gold)
        Spacer(Modifier.height(12.dp))
        GoldDivider()
        Spacer(Modifier.height(16.dp))

        Text(
            "Watch the cards — when the current matches\nthe previous, hit SNAP before time runs out!",
            color = TextOffWhite.copy(alpha = 0.75f),
            fontSize = 13.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PreviousCard(symbol = Symbol.CROWN)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Previous",
                    color = TextOffWhite.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(Modifier.width(16.dp))

            
            Text(
                text = if (isMatch) "✓" else "≠",
                color = if (isMatch) Gold else Color.White.copy(alpha = 0.25f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = if (isMatch) Shadow(color = Gold, blurRadius = 14f) else null
                )
            )

            Spacer(Modifier.width(16.dp))

            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    SnapCard(
                        symbol = if (isMatch) Symbol.CROWN else Symbol.DIAMOND,
                        isFlipped = true,
                        modifier = Modifier.width(88.dp)
                    )
                    if (glowAlpha > 0.05f) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Gold.copy(alpha = glowAlpha * 0.18f),
                                    RoundedCornerShape(14.dp)
                                )
                                .border(
                                    (glowAlpha * 2.5f).dp,
                                    Gold.copy(alpha = glowAlpha * 0.85f),
                                    RoundedCornerShape(14.dp)
                                )
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Current",
                    color = if (isMatch) Gold else TextOffWhite.copy(alpha = 0.55f),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = if (isMatch) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .height(54.dp)
                .graphicsLayer { scaleX = snapScale; scaleY = snapScale }
                .shadow(
                    if (isMatch) 24.dp else 4.dp,
                    RoundedCornerShape(50),
                    spotColor = if (isMatch) SnapRed else Color.Transparent
                )
                .clip(RoundedCornerShape(50))
                .background(
                    if (isMatch)
                        Brush.horizontalGradient(
                            listOf(Color(0xFFAA1100), Color(0xFFDD2200), Color(0xFFFF4422))
                        )
                    else
                        Brush.horizontalGradient(listOf(Color(0xFF4A1515), Color(0xFF5A2020)))
                )
                .border(
                    2.dp,
                    if (isMatch)
                        Brush.horizontalGradient(listOf(Gold, Color.White, Gold))
                    else
                        Brush.horizontalGradient(
                            listOf(Color.White.copy(0.12f), Color.White.copy(0.12f))
                        ),
                    RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "S  N  A  P  !",
                color = if (isMatch) Color.White else Color.White.copy(alpha = 0.3f),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                letterSpacing = 2.sp,
                style = TextStyle(
                    shadow = if (isMatch) Shadow(color = Color(0xAAFFD700), blurRadius = 8f) else null
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        AnimatedVisibility(
            visible = isMatch,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            Text(
                "← Tap now!",
                color = Gold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                style = TextStyle(shadow = Shadow(color = Gold, blurRadius = 8f))
            )
        }
    }
}



@Composable
private fun OnboardingPage3() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.height(8.dp))
        Stroked3DTitle(text = "STAY", fontSize = 28.sp, mainColor = Color.White)
        Spacer(Modifier.height(4.dp))
        Stroked3DTitle(text = "SHARP", fontSize = 40.sp, mainColor = Gold)
        Spacer(Modifier.height(12.dp))
        GoldDivider()
        Spacer(Modifier.height(18.dp))

        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(110.dp)) {
            RadarRings()
            Image(
                painter = painterResource(R.drawable.tiger_react_idle),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, spotColor = BrandOrange)
                    .clip(CircleShape)
                    .border(
                        3.dp,
                        Brush.linearGradient(listOf(Color.White, Gold)),
                        CircleShape
                    )
            )
        }

        Spacer(Modifier.height(18.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LivesDisplay(lives = 3)
            Text(
                "= 3 lives to start",
                color = TextOffWhite.copy(alpha = 0.8f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif
            )
        }

        Spacer(Modifier.height(18.dp))

        PenaltyCard(icon = "🚫", title = "Wrong tap", subtitle = "Symbols don't match")
        Spacer(Modifier.height(10.dp))
        PenaltyCard(icon = "⏰", title = "Too slow", subtitle = "Miss the snap window")

        Spacer(Modifier.height(14.dp))

        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFCC2200).copy(alpha = 0.12f))
                .border(
                    1.dp,
                    Color(0xFFCC2200).copy(alpha = 0.35f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                LivesDisplay(lives = 0)
                Spacer(Modifier.width(12.dp))
                Text(
                    "= Game Over!",
                    color = Color(0xFFFF5533),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 1.sp,
                    style = TextStyle(shadow = Shadow(color = Color(0xAAFF2200), blurRadius = 8f))
                )
            }
        }
    }
}

@Composable
private fun PenaltyCard(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color(0xFFCC2200).copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(38.dp)
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFF4422), Color(0xFFAA1100))),
                    RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.width(12.dp))
        Text(icon, fontSize = 22.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Text(
                subtitle,
                color = TextOffWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Serif
            )
        }
        Text(
            "-1 life",
            color = Color(0xFFFF5533),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            style = TextStyle(shadow = Shadow(color = Color(0xAAFF2200), blurRadius = 6f))
        )
    }
}
