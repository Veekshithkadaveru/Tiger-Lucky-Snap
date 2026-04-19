package app.krafted.tigerluckysnap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun GameOverScreen(
    score: Int,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    // Phase C: Tiger quotes + score structure
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Game Over")
        Text("Score: $score")
        Button(onClick = onPlayAgain) { Text("Play Again") }
        Button(onClick = onHome) { Text("Home") }
    }
}
