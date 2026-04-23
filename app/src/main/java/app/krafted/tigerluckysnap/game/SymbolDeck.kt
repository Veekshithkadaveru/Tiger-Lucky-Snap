package app.krafted.tigerluckysnap.game

import app.krafted.tigerluckysnap.model.Symbol
import kotlin.random.Random

class SymbolDeck {
    private val weights = mapOf(
        Symbol.COIN_BAG     to 18,
        Symbol.ROUND_LANTERN to 18,
        Symbol.TALL_LANTERN to 16,
        Symbol.CROWN        to 16,
        Symbol.STRAWBERRY   to 14,
        Symbol.DIAMOND      to 12,
        Symbol.GRAPES       to 6
    )

    private val pool: List<Symbol> = weights.flatMap { (sym, w) -> List(w) { sym } }

    fun nextSymbol(): Symbol = pool[Random.nextInt(pool.size)]
}
