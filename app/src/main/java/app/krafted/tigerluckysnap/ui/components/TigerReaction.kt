package app.krafted.tigerluckysnap.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import app.krafted.tigerluckysnap.R
import app.krafted.tigerluckysnap.model.SelectionOutcome
import app.krafted.tigerluckysnap.model.TigerReactionState

@DrawableRes
private fun tigerDrawable(
    state: TigerReactionState,
    selectionOutcome: SelectionOutcome,
    selectionEventCount: Int
): Int {
    val rightDrawables = listOf(
        R.drawable.tiger_react_happy,
        R.drawable.tiger_react_excited,
        R.drawable.tiger_excited_new
    )
    val wrongDrawables = listOf(
        R.drawable.tiger_neutral
    )

    return when (selectionOutcome) {
        SelectionOutcome.RIGHT -> rightDrawables[selectionEventCount % rightDrawables.size]
        SelectionOutcome.WRONG -> wrongDrawables[selectionEventCount % wrongDrawables.size]
        SelectionOutcome.NONE -> when (state) {
            TigerReactionState.IDLE -> R.drawable.tiger_react_idle
            TigerReactionState.EXCITED -> R.drawable.tiger_react_excited
            TigerReactionState.HAPPY -> R.drawable.tiger_react_happy
            TigerReactionState.NEUTRAL -> R.drawable.tiger_neutral
        }
    }
}

private data class TigerVisualState(
    val reactionState: TigerReactionState,
    val selectionOutcome: SelectionOutcome,
    val selectionEventCount: Int
)

@DrawableRes
private fun TigerVisualState.drawableRes(): Int = tigerDrawable(
    state = reactionState,
    selectionOutcome = selectionOutcome,
    selectionEventCount = selectionEventCount
)

private fun TigerVisualState.animationScaleTarget(): Float = when (selectionOutcome) {
    SelectionOutcome.RIGHT -> 1.12f
    SelectionOutcome.WRONG -> 0.95f
    SelectionOutcome.NONE -> when (reactionState) {
        TigerReactionState.EXCITED, TigerReactionState.HAPPY -> 1.08f
        else -> 1f
    }
}

@Composable
fun TigerReaction(
    state: TigerReactionState,
    selectionOutcome: SelectionOutcome,
    selectionEventCount: Int,
    modifier: Modifier = Modifier
) {
    val visualState = TigerVisualState(
        reactionState = state,
        selectionOutcome = selectionOutcome,
        selectionEventCount = selectionEventCount
    )

    var isInitial by remember { mutableStateOf(true) }
    var targetRotation by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visualState) {
        if (isInitial) {
            isInitial = false
        } else {
            targetRotation += 360f
        }
    }

    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "tigerSpin"
    )

    val scale by animateFloatAsState(
        targetValue = visualState.animationScaleTarget(),
        animationSpec = spring(
            stiffness = Spring.StiffnessHigh,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "tigerBounce"
    )

    AnimatedContent(
        targetState = visualState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(220)
            )) togetherWith (fadeOut(animationSpec = tween(180)) + scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(180)
            ))
        },
        label = "tigerReactionSwap",
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = rotation
            }
    ) { current ->
        Image(
            painter = painterResource(current.drawableRes()),
            contentDescription = current.reactionState.name,
            contentScale = ContentScale.Crop
        )
    }
}
