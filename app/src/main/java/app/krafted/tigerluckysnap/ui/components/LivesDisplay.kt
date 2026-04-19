package app.krafted.tigerluckysnap.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.R

@Composable
fun LivesDisplay(
    lives: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isActive = index < lives
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.75f,
                animationSpec = spring(
                    stiffness = Spring.StiffnessMedium,
                    dampingRatio = Spring.DampingRatioMediumBouncy
                ),
                label = "lifeScale$index"
            )
            Image(
                painter = painterResource(R.drawable.sym_round_lantern),
                contentDescription = if (isActive) "Life ${index + 1}" else "Lost life",
                modifier = Modifier
                    .size(36.dp)
                    .alpha(if (isActive) 1f else 0.25f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}
