package app.krafted.tigerluckysnap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.krafted.tigerluckysnap.model.Symbol

@Composable
fun PreviousCard(symbol: Symbol?, modifier: Modifier = Modifier) {
    // Phase C: small dimmed reference card showing the last symbol
    SnapCard(symbol = symbol, isFlipped = symbol != null, modifier = modifier)
}
