package app.krafted.tigerluckysnap.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.viewmodel.GameViewModel

@Composable
fun GameScreen(
    mode: GameMode,
    difficulty: Difficulty,
    onGameOver: (Int) -> Unit,
    viewModel: GameViewModel = viewModel()
) {
    // Phase C: Previous/Current card, Tiger mascot, SNAP button layout
    LaunchedEffect(mode, difficulty) {
        viewModel.initGame(mode, difficulty)
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Game Screen — Phase C")
    }
}
