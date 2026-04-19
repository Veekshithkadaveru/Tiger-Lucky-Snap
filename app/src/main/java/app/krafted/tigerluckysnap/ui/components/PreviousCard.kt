package app.krafted.tigerluckysnap.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.model.Symbol

@Composable
fun PreviousCard(
    symbol: Symbol?,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        SnapCard(
            symbol = symbol,
            isFlipped = symbol != null,
            modifier = Modifier
                .width(72.dp)
                .alpha(if (symbol != null) 0.75f else 0.3f)
        )
    }
}
