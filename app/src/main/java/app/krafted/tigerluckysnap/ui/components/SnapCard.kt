package app.krafted.tigerluckysnap.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.krafted.tigerluckysnap.model.Symbol

@Composable
fun SnapCard(symbol: Symbol?, isFlipped: Boolean, modifier: Modifier = Modifier) {
    // Phase C: scaleX flip animation + symbol image
    Card(modifier = modifier.size(120.dp)) {
        Box(contentAlignment = Alignment.Center) {}
    }
}
