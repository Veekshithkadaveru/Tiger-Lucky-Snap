package app.krafted.tigerluckysnap.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.Symbol

@DrawableRes
private fun Symbol.drawableRes(): Int = when (this) {
    Symbol.COIN_BAG -> R.drawable.sym_coin_bag
    Symbol.ROUND_LANTERN -> R.drawable.sym_round_lantern
    Symbol.CROWN -> R.drawable.sym_crown
    Symbol.STRAWBERRY -> R.drawable.sym_strawberry
    Symbol.DIAMOND -> R.drawable.sym_diamond
    Symbol.GRAPES -> R.drawable.sym_grapes
    Symbol.STAR -> R.drawable.sym_star
}

@Composable
fun SnapCard(
    symbol: Symbol?,
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "cardFlip"
    )

    val isFaceShowing = rotation > 90f

    Box(
        modifier = modifier
            .aspectRatio(0.7f)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        contentAlignment = Alignment.Center
    ) {
        if (!isFaceShowing) {
            Image(
                painter = painterResource(R.drawable.card_back_tiger),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFF8E7))
                    .graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center
            ) {
                if (symbol != null) {
                    Image(
                        painter = painterResource(symbol.drawableRes()),
                        contentDescription = symbol.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}
