package app.krafted.tigerluckysnap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode

@Composable
fun HomeScreen(
    onStartGame: (GameMode, Difficulty) -> Unit,
    onViewLeaderboard: () -> Unit
) {
    // Phase C: Tiger mascot + mode/difficulty selector
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Tiger Lucky Snap")
        Button(onClick = { onStartGame(GameMode.SOLO, Difficulty.MEDIUM) }) { Text("Play Solo") }
        Button(onClick = { onStartGame(GameMode.VS_AI, Difficulty.MEDIUM) }) { Text("vs Tiger AI") }
        Button(onClick = onViewLeaderboard) { Text("Leaderboard") }
    }
}
