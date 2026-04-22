package app.krafted.tigerluckysnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.tigerluckysnap.game.SymbolDeck
import app.krafted.tigerluckysnap.game.TigerAI
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.model.GameUiState
import app.krafted.tigerluckysnap.model.SelectionOutcome
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

    private val deck = SymbolDeck()
    private var tigerAI: TigerAI? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var flipJob: Job? = null
    private var snapWindowJob: Job? = null
    private var aiSnapJob: Job? = null
    private var countdownJob: Job? = null
    private var snapWindowDeferred: CompletableDeferred<Unit>? = null

    @Volatile
    private var snapWindowActive = false

    @Volatile
    private var currentMatchResolved = false

    fun initGame(mode: GameMode, difficulty: Difficulty) {
        cancelAllJobs()
        snapWindowDeferred?.complete(Unit)
        snapWindowDeferred = null
        snapWindowActive = false
        currentMatchResolved = false
        tigerAI = if (mode == GameMode.VS_AI) TigerAI(difficulty) else null
        _uiState.value = when (mode) {
            GameMode.HARDCORE -> GameUiState(gameMode = mode, difficulty = difficulty, lives = 1)
            GameMode.TIME_ATTACK -> GameUiState(
                gameMode = mode,
                difficulty = difficulty,
                timeRemainingSeconds = 60
            )

            else -> GameUiState(gameMode = mode, difficulty = difficulty)
        }
        startFlipLoop()
        if (mode == GameMode.TIME_ATTACK) startCountdown()
    }

    private fun startCountdown() {
        countdownJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0 && !_uiState.value.isGameOver) {
                delay(1000L)
                val remaining = _uiState.value.timeRemainingSeconds - 1
                if (remaining <= 0) {
                    flipJob?.cancel()
                    snapWindowJob?.cancel()
                    aiSnapJob?.cancel()
                    _uiState.update { it.copy(timeRemainingSeconds = 0, isGameOver = true) }
                    return@launch
                }
                _uiState.update { it.copy(timeRemainingSeconds = remaining) }
            }
        }
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

        if (isMatch) currentMatchResolved = false

        _uiState.update {
            it.copy(
                previousSymbol = it.currentSymbol,
                currentSymbol = next,
                cardsFlipped = it.cardsFlipped + 1,
                isMatchActive = isMatch,
                tigerReaction = if (isMatch) TigerReactionState.EXCITED else TigerReactionState.IDLE,
                selectionOutcome = SelectionOutcome.NONE
            )
        }
        return isMatch
    }

    private fun startSnapWindow(deferred: CompletableDeferred<Unit>) {
        snapWindowActive = true
        val windowMs = getSnapWindowDuration(_uiState.value.cardsFlipped)

        tigerAI?.let { ai ->
            aiSnapJob = viewModelScope.launch {
                delay(ai.reactionMs)
                if (snapWindowActive) {
                    handleAiSnap(deferred)
                }
            }
        }

        snapWindowJob = viewModelScope.launch {
            delay(windowMs)
            if (snapWindowActive) {
                handleMissedSnap(deferred)
            }
        }
    }

    private fun getSnapWindowDuration(cardsFlipped: Int): Long = when {
        cardsFlipped < 10 -> 1500L
        cardsFlipped < 20 -> 1200L
        cardsFlipped < 30 -> 1000L
        cardsFlipped < 40 -> 800L
        cardsFlipped < 50 -> 650L
        else -> 500L
    }

    fun onSnapTapped() {
        val state = _uiState.value
        if (state.isGameOver) return

        val visuallyMatches =
            state.currentSymbol != null && state.currentSymbol == state.previousSymbol

        if (visuallyMatches && !currentMatchResolved) {
            currentMatchResolved = true
            if (snapWindowActive) {
                snapWindowActive = false
                snapWindowJob?.cancel()
                aiSnapJob?.cancel()
                snapWindowDeferred?.complete(Unit)
            }
            val points = calculateScore(state.cardsFlipped)
            _uiState.update {
                it.copy(
                    isMatchActive = false,
                    score = it.score + points,
                    tigerReaction = TigerReactionState.HAPPY,
                    selectionOutcome = SelectionOutcome.RIGHT,
                    selectionEventCount = it.selectionEventCount + 1
                )
            }
            return
        }

        if (visuallyMatches) return

        loseLife(TigerReactionState.NEUTRAL, SelectionOutcome.WRONG)
    }

    private fun handleAiSnap(deferred: CompletableDeferred<Unit>) {
        if (!snapWindowActive) return
        snapWindowActive = false
        snapWindowJob?.cancel()
        _uiState.update {
            it.copy(
                tigerReaction = TigerReactionState.EXCITED,
                selectionOutcome = SelectionOutcome.NONE
            )
        }
        deferred.complete(Unit)
    }

    private fun handleMissedSnap(deferred: CompletableDeferred<Unit>) {
        if (!snapWindowActive) return
        snapWindowActive = false
        currentMatchResolved = true
        aiSnapJob?.cancel()
        _uiState.update { it.copy(missedWindowCount = it.missedWindowCount + 1) }
        loseLife(TigerReactionState.NEUTRAL)
        deferred.complete(Unit)
    }

    private fun loseLife(
        reaction: TigerReactionState,
        selectionOutcome: SelectionOutcome = SelectionOutcome.NONE
    ) {
        val newLives = _uiState.value.lives - 1
        _uiState.update {
            it.copy(
                lives = newLives,
                isMatchActive = false,
                tigerReaction = reaction,
                selectionOutcome = selectionOutcome,
                selectionEventCount = if (selectionOutcome == SelectionOutcome.NONE) {
                    it.selectionEventCount
                } else {
                    it.selectionEventCount + 1
                },
                isGameOver = newLives <= 0
            )
        }
        if (newLives <= 0) {
            flipJob?.cancel()
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

    private fun cancelAllJobs() {
        flipJob?.cancel()
        snapWindowJob?.cancel()
        aiSnapJob?.cancel()
        countdownJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllJobs()
    }
}
