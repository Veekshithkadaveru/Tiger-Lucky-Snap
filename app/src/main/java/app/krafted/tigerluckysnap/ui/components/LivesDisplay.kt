package app.krafted.tigerluckysnap.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LivesDisplay(lives: Int, modifier: Modifier = Modifier) {
    // Phase C: 3 lantern icons (sym_round_lantern filled/greyed per life)
    Row(modifier = modifier) {
        repeat(3) { index ->
            Text(if (index < lives) "[L]" else "[ ]")
        }
    }
}
