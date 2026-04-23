package app.krafted.tigerluckysnap.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.Symbol

@DrawableRes
fun getSymbolDrawable(symbol: Symbol): Int = when (symbol) {
    Symbol.COIN_BAG -> R.drawable.sym_coin_bag
    Symbol.ROUND_LANTERN -> R.drawable.sym_round_lantern
    Symbol.TALL_LANTERN -> R.drawable.sym_crown
    Symbol.CROWN -> R.drawable.sym_strawberry
    Symbol.STRAWBERRY -> R.drawable.sym_diamond
    Symbol.DIAMOND -> R.drawable.sym_grapes
    Symbol.GRAPES -> R.drawable.sym_star
}

@Composable
fun SnapCard(
    symbol: Symbol?,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    flipDurationMs: Int = 300
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = flipDurationMs),
        label = "cardFlip"
    )

    val isFaceShowing = rotation > 90f
    val cardShape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .shadow(10.dp, cardShape)
            .clip(cardShape)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isFaceShowing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF7B0000))
                    .border(BorderStroke(4.dp, Color(0xFFFFD700)), cardShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.card_back_tiger),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .border(BorderStroke(1.dp, Color(0x33FFD700)), RoundedCornerShape(8.dp))
                )
            }
        } else {
            // Face up: solid cream/white card with gold border and elevation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFDF5), // top near-white
                                Color(0xFFF5E9C8)  // cream bottom
                            )
                        )
                    )
                    .border(
                        BorderStroke(2.dp, Color(0xFFDAA520)),
                        cardShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Thin inner accent ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                        .border(
                            BorderStroke(1.dp, Color(0x33A67C00)),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (symbol != null) {
                        Image(
                            painter = painterResource(getSymbolDrawable(symbol)),
                            contentDescription = symbol.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}
