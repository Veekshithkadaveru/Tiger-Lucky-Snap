package app.krafted.tigerluckysnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.tigerluckysnap.game.SymbolDeck
import app.krafted.tigerluckysnap.game.TigerAI
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.model.GameUiState
import app.krafted.tigerluckysnap.model.Mission
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
        val initialMissions = generateMissions()
        _uiState.value = when (mode) {
            GameMode.HARDCORE -> GameUiState(gameMode = mode, difficulty = difficulty, lives = 1, missions = initialMissions)
            GameMode.TIME_ATTACK -> GameUiState(
                gameMode = mode,
                difficulty = difficulty,
                timeRemainingSeconds = 60,
                missions = initialMissions
            )

            else -> GameUiState(gameMode = mode, difficulty = difficulty, missions = initialMissions)
        }
        startFlipLoop()
        if (mode == GameMode.TIME_ATTACK) startCountdown()
    }

    private fun generateMissions(): List<Mission> = listOf(
        Mission("snaps", "Make 10 correct SNAPs", 10, rewardPoints = 500),
        Mission("round", "Reach Round 5", 5, rewardPoints = 1000),
        Mission("combo", "Get a 3x Combo", 3, rewardPoints = 800)
    )

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
                delay(getFlipInterval(_uiState.value.currentRound))
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
        val windowMs = getSnapWindowDuration(_uiState.value.currentRound)

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

    private fun getSnapWindowDuration(round: Int): Long {
        val diff = _uiState.value.difficulty
        val base = when (diff) {
            Difficulty.EASY -> 1500L
            Difficulty.MEDIUM -> 1000L
            Difficulty.HARD -> 500L
        }
        val normalDrop = (round - 1) * 100L
        val spikeDrop = (round / 5) * 150L
        val floor = when (diff) {
            Difficulty.EASY -> 400L
            Difficulty.MEDIUM -> 280L
            Difficulty.HARD -> 150L
        }
        return maxOf(floor, base - normalDrop - spikeDrop)
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
            val newMatchesFound = state.matchesFound + 1
            val newRound = (newMatchesFound / 3) + 1
            val newCombo = state.comboCount + 1
            val multiplier = comboMultiplier(newCombo)
            val points = calculateScore(newRound) * multiplier
            _uiState.update {
                val updatedState = it.copy(
                    isMatchActive = false,
                    score = it.score + points,
                    tigerReaction = TigerReactionState.HAPPY,
                    selectionOutcome = SelectionOutcome.RIGHT,
                    selectionEventCount = it.selectionEventCount + 1,
                    matchesFound = newMatchesFound,
                    currentRound = newRound,
                    comboCount = newCombo,
                    comboMultiplier = multiplier,
                    maxCombo = maxOf(it.maxCombo, newCombo)
                )
                updateMissions(updatedState)
            }
            return
        }

        if (visuallyMatches) return

        loseLife(TigerReactionState.NEUTRAL, SelectionOutcome.WRONG)
    }

    private fun handleAiSnap(deferred: CompletableDeferred<Unit>) {
        if (!snapWindowActive) return
        snapWindowActive = false
        currentMatchResolved = true
        snapWindowJob?.cancel()
        _uiState.update {
            it.copy(
                tigerReaction = TigerReactionState.EXCITED,
                selectionOutcome = SelectionOutcome.NONE,
                comboCount = 0,
                comboMultiplier = 1
            )
        }
        loseLife(TigerReactionState.EXCITED)
        deferred.complete(Unit)
    }

    private fun handleMissedSnap(deferred: CompletableDeferred<Unit>) {
        if (!snapWindowActive) return
        snapWindowActive = false
        currentMatchResolved = true
        aiSnapJob?.cancel()
        _uiState.update {
            it.copy(
                missedWindowCount = it.missedWindowCount + 1,
                comboCount = 0,
                comboMultiplier = 1
            )
        }
        loseLife(TigerReactionState.NEUTRAL)
        deferred.complete(Unit)
    }

    private fun loseLife(
        reaction: TigerReactionState,
        selectionOutcome: SelectionOutcome = SelectionOutcome.NONE
    ) {
        if (_uiState.value.gameMode == GameMode.TIME_ATTACK) {
            _uiState.update {
                it.copy(
                    isMatchActive = false,
                    tigerReaction = reaction,
                    selectionOutcome = selectionOutcome,
                    selectionEventCount = if (selectionOutcome == SelectionOutcome.NONE)
                        it.selectionEventCount else it.selectionEventCount + 1,
                    comboCount = 0,
                    comboMultiplier = 1
                )
            }
            return
        }
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
                isGameOver = newLives <= 0,
                comboCount = 0,
                comboMultiplier = 1
            )
        }
        if (newLives <= 0) {
            flipJob?.cancel()
        }
    }

    private fun getFlipInterval(round: Int): Long {
        val diff = _uiState.value.difficulty
        val base = when (diff) {
            Difficulty.EASY -> 2000L
            Difficulty.MEDIUM -> 1400L
            Difficulty.HARD -> 800L
        }
        val normalDrop = (round - 1) * 150L
        val spikeDrop = (round / 5) * 200L
        val floor = when (diff) {
            Difficulty.EASY -> 500L
            Difficulty.MEDIUM -> 350L
            Difficulty.HARD -> 200L
        }
        return maxOf(floor, base - normalDrop - spikeDrop)
    }

    private fun calculateScore(round: Int): Int {
        val speedBonus = (round - 1) * 10
        return 100 + speedBonus
    }

    private fun comboMultiplier(combo: Int): Int = when {
        combo >= 10 -> 5
        combo >= 5 -> 3
        combo >= 2 -> 2
        else -> 1
    }

    private fun updateMissions(state: GameUiState): GameUiState {
        var addedScore = 0
        val updatedMissions = state.missions.map { mission ->
            if (mission.isCompleted) return@map mission

            val currentValue = when (mission.id) {
                "snaps" -> state.matchesFound
                "round" -> state.currentRound
                "combo" -> state.maxCombo
                else -> mission.currentValue
            }
            
            val isCompleted = currentValue >= mission.targetValue
            if (isCompleted && !mission.isCompleted) {
                addedScore += mission.rewardPoints
            }
            
            mission.copy(currentValue = minOf(currentValue, mission.targetValue), isCompleted = isCompleted)
        }
        
        return state.copy(
            missions = updatedMissions,
            score = state.score + addedScore
        )
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
