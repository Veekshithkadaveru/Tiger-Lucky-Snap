package app.krafted.tigerluckysnap.model

enum class Symbol {
    COIN_BAG, ROUND_LANTERN, CROWN, STRAWBERRY, DIAMOND, GRAPES, STAR
}

enum class Difficulty { EASY, MEDIUM, HARD }

enum class GameMode { SOLO, VS_AI }

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
    val missedWindowCount: Int = 0
)
