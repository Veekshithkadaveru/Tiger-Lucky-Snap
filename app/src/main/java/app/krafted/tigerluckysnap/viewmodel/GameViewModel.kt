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

    private val db = AppDatabase.getInstance(application)
    private val deck = SymbolDeck()
    private var tigerAI: TigerAI? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var flipJob: Job? = null
    private var snapWindowJob: Job? = null
    private var aiSnapJob: Job? = null
    private var snapWindowDeferred: CompletableDeferred<Unit>? = null

    // Single source of truth for whether a match is currently tappable.
    // Set to true when the match window opens, flipped to false by whichever
    // resolver (tap / AI / timeout) fires first. Everyone else no-ops.
    private var snapWindowActive = false

    // Whether the current visible match has already been settled — either
    // the user scored it, or the timeout fired and cost a life. AI snapping
    // does NOT flip this: the user always wins ties, so a tap on visually
    // matching cards still scores even after the AI's coroutine fired.
    private var currentMatchResolved = false

    fun initGame(mode: GameMode, difficulty: Difficulty) {
        cancelAllJobs()
        snapWindowDeferred?.complete(Unit)
        snapWindowDeferred = null
        snapWindowActive = false
        currentMatchResolved = false
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

        tigerAI?.let { ai ->
            aiSnapJob = viewModelScope.launch {
                delay(ai.reactionMs)
                if (snapWindowActive) {
                    handleAiSnap(deferred)
                }
            }
        }

        snapWindowJob = viewModelScope.launch {
            delay(2000L)
            if (snapWindowActive) {
                handleMissedSnap(deferred)
            }
        }
    }

    fun onSnapTapped() {
        val state = _uiState.value
        if (state.isGameOver) return

        val visuallyMatches = state.currentSymbol != null && state.currentSymbol == state.previousSymbol

        if (visuallyMatches && !currentMatchResolved) {
            // User always wins ties: even if the AI's snap coroutine already
            // fired and closed the window, the tap still scores as long as the
            // match hasn't been awarded or timed out yet.
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

        // Cards still visually match but the match is already resolved
        // (awarded or missed). Don't re-score and don't penalize.
        if (visuallyMatches) return

        // Genuine false snap.
        loseLife(TigerReactionState.NEUTRAL, SelectionOutcome.WRONG)
    }

    private fun handleAiSnap(deferred: CompletableDeferred<Unit>) {
        if (!snapWindowActive) return
        // AI's snap closes the window (cancelling the timeout so the user can't
        // lose a life on this match) but does NOT resolve the match — the user
        // may still tap on the still-visible matching cards to score.
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
        snapWindowActive = false
        currentMatchResolved = false
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
