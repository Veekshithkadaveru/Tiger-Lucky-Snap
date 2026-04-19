package app.krafted.tigerluckysnap.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.TigerReactionState

@DrawableRes
private fun TigerReactionState.drawableRes(): Int = when (this) {
    TigerReactionState.IDLE -> R.drawable.tiger_react_idle
    TigerReactionState.HAPPY -> R.drawable.tiger_react_happy
    TigerReactionState.EXCITED -> R.drawable.tiger_react_excited
    TigerReactionState.SURPRISED -> R.drawable.tiger_react_surprised
    TigerReactionState.SAD -> R.drawable.tiger_react_surprised
}

@Composable
fun TigerReaction(
    state: TigerReactionState,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (state == TigerReactionState.EXCITED || state == TigerReactionState.HAPPY) 1.08f else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "tigerBounce"
    )

    Image(
        painter = painterResource(state.drawableRes()),
        contentDescription = state.name,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}
