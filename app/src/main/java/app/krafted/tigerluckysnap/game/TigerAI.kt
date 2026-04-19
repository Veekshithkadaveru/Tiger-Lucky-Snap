package app.krafted.tigerluckysnap.game

import app.krafted.tigerluckysnap.model.Difficulty

class TigerAI(private val difficulty: Difficulty) {
    val reactionMs: Long get() = when (difficulty) {
        Difficulty.EASY   -> 420L
        Difficulty.MEDIUM -> 250L
        Difficulty.HARD   -> 120L
    }
}
