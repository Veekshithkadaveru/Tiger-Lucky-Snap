package app.krafted.tigerluckysnap.viewmodel

import androidx.lifecycle.ViewModel
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.model.GameUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun initGame(mode: GameMode, difficulty: Difficulty) {
        _uiState.value = GameUiState(gameMode = mode, difficulty = difficulty)
        // Phase B: start auto-flip loop
    }

    fun onSnapTapped() {
        // Phase B: snap window logic — score or lose life
    }

    fun resetGame() {
        _uiState.value = GameUiState()
    }
}
