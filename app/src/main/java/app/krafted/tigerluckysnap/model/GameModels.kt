package app.krafted.tigerluckysnap.model

enum class Symbol(val displayName: String) {
    COIN_BAG("Coin Bag"),
    ROUND_LANTERN("Lantern"),
    CROWN("Crown"),
    STRAWBERRY("Strawberry"),
    DIAMOND("Diamond"),
    GRAPES("Grapes"),
    STAR("Star"),
}

enum class Difficulty { EASY, MEDIUM, HARD }

enum class GameMode { SOLO, VS_AI, TIME_ATTACK, HARDCORE }

enum class TigerReactionState { IDLE, HAPPY, EXCITED, NEUTRAL }
enum class SelectionOutcome { NONE, RIGHT, WRONG }

data class GameUiState(
    val lives: Int = 3,
    val previousSymbol: Symbol? = null,
    val currentSymbol: Symbol? = null,
    val cardsFlipped: Int = 0,
    val isMatchActive: Boolean = false,
    val tigerReaction: TigerReactionState = TigerReactionState.IDLE,
    val selectionOutcome: SelectionOutcome = SelectionOutcome.NONE,
    val selectionEventCount: Int = 0,
    val score: Int = 0,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val gameMode: GameMode = GameMode.SOLO,
    val isGameOver: Boolean = false,
    val missedWindowCount: Int = 0,
    val timeRemainingSeconds: Int = 0,
    val currentRound: Int = 1,
    val matchesFound: Int = 0,
    val comboCount: Int = 0,
    val comboMultiplier: Int = 1,
    val maxCombo: Int = 0,
    val missions: List<Mission> = emptyList()
)

data class Mission(
    val id: String,
    val description: String,
    val targetValue: Int,
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val rewardPoints: Int
)
