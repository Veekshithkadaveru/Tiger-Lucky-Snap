package app.krafted.tigerluckysnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.tigerluckysnap.data.db.AppDatabase
import app.krafted.tigerluckysnap.data.db.ScoreEntity
import app.krafted.tigerluckysnap.game.SymbolDeck
import app.krafted.tigerluckysnap.game.TigerAI
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.model.GameUiState
import app.krafted.tigerluckysnap.model.TigerReactionState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val deck = SymbolDeck()
    private var tigerAI: TigerAI? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var flipJob: Job? = null
    private var snapWindowJob: Job? = null
    private var aiSnapJob: Job? = null
    private var snapWindowDeferred: CompletableDeferred<Unit>? = null

    fun initGame(mode: GameMode, difficulty: Difficulty) {
        cancelAllJobs()
        snapWindowDeferred?.complete(Unit)
        snapWindowDeferred = null
        tigerAI = if (mode == GameMode.VS_AI) TigerAI(difficulty) else null
        _uiState.value = GameUiState(gameMode = mode, difficulty = difficulty)
        startFlipLoop()
    }

    private fun startFlipLoop() {
        flipJob = viewModelScope.launch {
            while (!_uiState.value.isGameOver) {
                delay(getFlipInterval(_uiState.value.cardsFlipped))
                if (_uiState.value.isGameOver) break

                val matched = flipNextCard()
                if (matched) {
                    val deferred = CompletableDeferred<Unit>()
                    snapWindowDeferred = deferred
                    startSnapWindow(deferred)
                    deferred.await()
                    snapWindowDeferred = null
                }
            }
        }
    }

    private fun flipNextCard(): Boolean {
        val state = _uiState.value
        val next = deck.nextSymbol()
        val isMatch = state.currentSymbol != null && state.currentSymbol == next

        _uiState.update {
            it.copy(
                previousSymbol = it.currentSymbol,
                currentSymbol = next,
                cardsFlipped = it.cardsFlipped + 1,
                isMatchActive = isMatch,
                tigerReaction = if (isMatch) TigerReactionState.EXCITED else TigerReactionState.IDLE
            )
        }
        return isMatch
    }

    private fun startSnapWindow(deferred: CompletableDeferred<Unit>) {
        tigerAI?.let { ai ->
            aiSnapJob = viewModelScope.launch {
                delay(ai.reactionMs)
                if (_uiState.value.isMatchActive) {
                    handleAiSnap(deferred)
                }
            }
        }

        snapWindowJob = viewModelScope.launch {
            delay(500L)
            if (_uiState.value.isMatchActive) {
                handleMissedSnap(deferred)
            }
        }
    }

    fun onSnapTapped() {
        val state = _uiState.value
        if (state.isGameOver) return

        if (state.isMatchActive) {
            snapWindowJob?.cancel()
            aiSnapJob?.cancel()
            val points = calculateScore(state.cardsFlipped)
            _uiState.update {
                it.copy(
                    isMatchActive = false,
                    score = it.score + points,
                    tigerReaction = TigerReactionState.HAPPY
                )
            }
            snapWindowDeferred?.complete(Unit)
        } else {
            loseLife(TigerReactionState.SURPRISED)
        }
    }

    private fun handleAiSnap(deferred: CompletableDeferred<Unit>) {
        snapWindowJob?.cancel()
        _uiState.update {
            it.copy(
                isMatchActive = false,
                tigerReaction = TigerReactionState.EXCITED
            )
        }
        deferred.complete(Unit)
    }

    private fun handleMissedSnap(deferred: CompletableDeferred<Unit>) {
        aiSnapJob?.cancel()
        loseLife(TigerReactionState.SAD)
        deferred.complete(Unit)
    }

    private fun loseLife(reaction: TigerReactionState) {
        val newLives = _uiState.value.lives - 1
        _uiState.update {
            it.copy(
                lives = newLives,
                isMatchActive = false,
                tigerReaction = reaction,
                isGameOver = newLives <= 0
            )
        }
        if (newLives <= 0) {
            flipJob?.cancel()
            val finalScore = _uiState.value.score
            val mode = _uiState.value.gameMode
            viewModelScope.launch {
                db.scoreDao().insert(ScoreEntity(score = finalScore, gameMode = mode.name))
            }
        }
    }

    private fun getFlipInterval(cardsFlipped: Int): Long = when {
        cardsFlipped < 10 -> 2000L
        cardsFlipped < 20 -> 1600L
        cardsFlipped < 30 -> 1200L
        cardsFlipped < 40 -> 900L
        cardsFlipped < 50 -> 700L
        else -> 500L
    }

    private fun calculateScore(cardsFlipped: Int): Int {
        val speedBonus = when {
            cardsFlipped >= 50 -> 50
            cardsFlipped >= 30 -> 30
            cardsFlipped >= 20 -> 20
            else -> 0
        }
        return 100 + speedBonus
    }

    fun resetGame() {
        cancelAllJobs()
        snapWindowDeferred?.complete(Unit)
        snapWindowDeferred = null
        _uiState.value = GameUiState()
    }

    private fun cancelAllJobs() {
        flipJob?.cancel()
        snapWindowJob?.cancel()
        aiSnapJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }
}
